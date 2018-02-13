package server;import com.fasterxml.jackson.databind.ObjectMapper;import exceptions.FaRAlreadyConnectedClient;import exceptions.FaRWrongDataBase;import javafx.collections.ListChangeListener;import javafx.util.Pair;import model.*;import reactor.core.publisher.Flux;import settings.Base;import settings.Settings;import settings.User;import settings.UserPrivileges;import transport.Data;import transport.ListChangeAdapter;import transport.UserInformation;import java.io.DataInputStream;import java.io.DataOutputStream;import java.io.IOException;import java.io.OutputStream;import java.net.ServerSocket;import java.net.Socket;import java.util.*;import java.util.concurrent.ConcurrentHashMap;import java.util.concurrent.ExecutorService;import java.util.concurrent.Executors;import java.util.concurrent.locks.ReentrantReadWriteLock;import java.util.stream.Collectors;public class Server{    public static Settings settings;    static{//        todo:Для Тестирования//        settings = SettingsManager.loadSettings();        settings = new Settings( "" , "" , 1000 * 60 * 30L , new ArrayList<Base>(){{            add( new Base( "test" , true , new ArrayList<User>(){{                add( new User( "testUser" , "pass" , UserPrivileges.ReadWrite ) );            }} ) );        }} );    }    //    contains all login of connected read clients    static ReentrantReadWriteLock    loginsLock      = new ReentrantReadWriteLock( true );    static List<String>              connectedLogins = new ArrayList<>();    static ReentrantReadWriteLock    adminsLock      = new ReentrantReadWriteLock( true );    //    contains all admins with their socket who is already connected    static Map<String, OutputStream> admins          = new ConcurrentHashMap<>();    @SuppressWarnings( "InfiniteLoopStatement" )    public static void main( String[] args ) throws IOException{        int port = Integer.parseInt( args[ 0 ] );        try( ServerSocket requestServerSocket = new ServerSocket( port ) ){//            Request threads            ExecutorService requestService = Executors.newCachedThreadPool();            while( true ){                requestService.execute( new RequestParser( requestServerSocket.accept() ) );            }        }    }    static class RequestParser implements Runnable{        private final Socket           socket;        private       DataInputStream  inputStream;        private       DataOutputStream outputStream;        private ObjectMapper mapper = new ObjectMapper();        RequestParser( Socket socket ) throws IOException{            this.socket = socket;            try{                this.outputStream = new DataOutputStream( socket.getOutputStream() );                this.inputStream = new DataInputStream( socket.getInputStream() );            }catch( IOException e ){                e.printStackTrace();                socket.close();            }        }        @Override        public void run(){            try{                UserInformation userInformation =                        mapper.readerFor( UserInformation.class ).readValue( inputStream.readUTF() );                if( userInformation.getDataBase() == null ){                    outputStream.writeUTF( mapper.writeValueAsString(                            databasesListRequest( userInformation.getLogin() , userInformation.getPassword() ) ) );                    closeConnection();                    return;                }//                finding any database with specified user                Optional<Pair<Base, User>> optionalBase = settings.getBase()                                                                  .parallelStream()                                                                  .filter( base -> base.getName()                                                                                       .equals(                                                                                               userInformation.getDataBase() ) )                                                                  .map( base -> new Pair<>( base , base.getUsers()                                                                                                       .parallelStream()                                                                                                       .filter(                                                                                                               user -> userPredicate(                                                                                                                       user ,                                                                                                                       userInformation                                                                                                                               .getLogin() ,                                                                                                                       userInformation                                                                                                                               .getPassword() ) )                                                                                                       .findFirst() ) )                                                                  .filter( pair -> pair.getValue().isPresent() )                                                                  .map( pair -> new Pair<>( pair.getKey() ,                                                                                            pair.getValue().get() ) )                                                                  .findFirst();                if( optionalBase.isPresent() ){                    switch( optionalBase.get().getValue().getPrivilege() ){                        case ReadWrite:                            adminsLock.readLock().lock();//                            if this client is already connected to server                            boolean alreadyConnected = admins.keySet().contains( userInformation.getLogin() );                            adminsLock.readLock().unlock();                            if( alreadyConnected ){                                Data response = new Data();                                response.setException( new FaRAlreadyConnectedClient(                                        String.format( "Client %s is already connected. Connection refused." ,                                                       userInformation.getLogin() ) ) );                                outputStream.writeUTF( mapper.writeValueAsString( response ) );                                closeConnection();                                return;                            }else{                                adminsLock.writeLock().lock();                                admins.put( userInformation.getLogin() , outputStream );                                adminsLock.writeLock().unlock();                                DataModelWithLock dataModel =                                        DataModelInstanceSaver.getInstance( userInformation.getDataBase() )                                                              .orElseThrow( IllegalStateException::new );                                ListChangeListener<? extends FlightOrRoute> sendChange = this::sendChange;                                dataModel.getModel().addRoutesListener( ( ListChangeListener<Route> ) sendChange );                                dataModel.getModel().addFlightsListener( ( ListChangeListener<Flight> ) sendChange );                                Flux.<String> create( emitter -> {                                    try{                                        String req;                                        while( !( req = inputStream.readUTF() ).equals( "*" ) ){                                            emitter.next( req );                                        }                                        outputStream.writeUTF( req );                                        emitter.complete();                                    }catch( IOException e ){                                        emitter.error( e );                                    }                                } ).<UserInformation> map( string -> {                                    try{                                        return mapper.readerFor( UserInformation.class ).readValue( string );                                    }catch( IOException e ){                                        e.printStackTrace();                                        return null;                                    }                                } ).map( UserInformation::getChanges ).doOnNext( listChanges -> {                                    adminsLock.writeLock().lock();                                    listChanges.forEach( change -> {                                        try{                                            change.apply( dataModel.getModel() );                                        }catch( IOException e ){                                            e.printStackTrace();                                        }                                    } );                                    adminsLock.writeLock().unlock();                                } ).doFinally( signalType -> {                                    dataModel.getModel()                                             .removeRoutesListener( ( ListChangeListener<Route> ) sendChange );                                    dataModel.getModel()                                             .removeFlightsListener( ( ListChangeListener<Flight> ) sendChange );                                    admins.remove( userInformation.getLogin() );                                    try{                                        closeConnection();                                    }catch( IOException e ){                                        e.printStackTrace();                                    }                                } ).blockLast();                            }                            break;                        case Read:                            break;                    }                }else{                    outputStream.writeUTF( mapper.writeValueAsString(                            wrongDatabaseSelect( userInformation.getDataBase() , userInformation.getLogin() ) ) );                    closeConnection();                }            }catch( IOException e ){                e.printStackTrace();            }        }        private void closeConnection() throws IOException{            outputStream.close();            inputStream.close();            socket.close();        }        private void sendChange( ListChangeListener.Change<? extends FlightOrRoute> change ){            Data data = new Data();            try{                data.setChanges( Collections.singletonList( new ListChangeAdapter( change ) ) );                admins.entrySet()                      .stream()                      .map( entry -> ( DataOutputStream ) entry.getValue() )                      .forEach( outputStream -> {                          try{                              outputStream.writeUTF( mapper.writeValueAsString( data ) );                          }catch( IOException e ){                              e.printStackTrace();                          }                      } );            }catch( IOException e ){                e.printStackTrace();            }        }        private boolean userPredicate( User user , String login , String password ){            return user.getLogin().equals( login ) && user.getPassword().equals( password );        }        private Data databasesListRequest( String login , String pass ){            Data response = new Data();//            collecting databases with privileges of specified clients            response.setBases( settings.getBase()                                       .parallelStream()                                       .map( base -> new Pair<>( base , base.getUsers()                                                                            .parallelStream()                                                                            .filter( user -> userPredicate( user ,                                                                                                            login ,                                                                                                            pass ) )                                                                            .findFirst() ) )                                       .filter( pair -> pair.getValue().isPresent() )                                       .map( pair -> new Pair<>( pair.getKey().getName() ,                                                                 pair.getValue().get().getPrivilege().name() ) )                                       .collect( Collectors.toMap( Pair::getKey , Pair::getValue ) ) );            return response;        }        private Data wrongDatabaseSelect( String dataBaseName , String login ){            Data data = new Data();            data.setException( new FaRWrongDataBase(                    String.format( "Server doesn't contain database %s for user %s" , dataBaseName , login ) ) );            return data;        }    }}
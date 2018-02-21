package sample;

import com.fasterxml.jackson.databind.ObjectMapper;
import exceptions.FlightAndRouteException;
import model.DataModelInstanceSaver;
import model.Flight;
import model.Route;
import reactor.core.publisher.Flux;
import transport.Data;
import transport.ListChangeAdapter;
import transport.UserInformation;

import java.io.*;
import java.net.Socket;
import java.util.Optional;
import java.util.concurrent.Exchanger;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

/**
 Support class for controllers
 */
public class Controller{

    String  host;
    Integer port;
    String  login;
    String  password;
    String  base;

    Predicate<Route>        routePredicate  = route -> true;
    Predicate<Flight>       flightPredicate = flight -> true;
    AtomicReference<Socket> adminConnection = new AtomicReference<>( null );

    private ObjectMapper mapper = new ObjectMapper();
    Boolean changed = false;
    File savingFile;

    private Controller(){}

    public static synchronized Controller getInstance(){
        return Controller.InstanceHolder.instance;
    }

    Exchanger<FlightAndRouteException> updateExchanger        = new Exchanger<>();
    AtomicReference<ListChangeAdapter> lastUsersUpdateRequest = new AtomicReference<>( null );

    void startUpdateThread( String privilege ){
//        signals from server
        if( privilege.equalsIgnoreCase( "ReadWrite" ) ){
            Executors.newSingleThreadExecutor().submit( () -> {
                while( true ){
                    Optional.ofNullable( Controller.getInstance().adminConnection.get() ).ifPresent( socket -> {
                        try{
                            DataInputStream inputStream = new DataInputStream( socket.getInputStream() );
                            Flux.<String> create( emitter -> {
                                //noinspection Duplicates
                                try{
                                    String req;
                                    while( !( req = inputStream.readUTF() ).equals( "*" ) ){
                                        emitter.next( req );
                                    }
                                    emitter.complete();
                                }catch( EOFException e ){
                                    emitter.complete();
                                }catch( IOException e ){
                                    emitter.error( e );
                                }
                            } ).<Data> map( request -> {
                                try{
                                    return mapper.readerFor( Data.class ).readValue( request );
                                }catch( Exception e ){
                                    throw new IllegalStateException( "Exception while reading info from server" , e );
                                }
                            } ).doOnNext( response -> response.withoutExceptionOrWith( data -> {
                                data.getChanges().forEach( change -> {
//                            if GUI waiting this response
                                    if( lastUsersUpdateRequest.get() != null &&
                                        lastUsersUpdateRequest.get().equalsEntities( change ) ){
                                        try{
//                                                          if null, request was accepted
                                            updateExchanger.exchange( null );
                                        }catch( InterruptedException e ){
                                            throw new IllegalStateException( "Someone closed requests thread" , e );
                                        }
                                    }else{
                                        change.apply( DataModelInstanceSaver.getInstance() );
                                    }
                                } );
                            } , error -> {
                                try{
//                        exchange error to request window
                                    updateExchanger.exchange( error );
                                }catch( InterruptedException e ){
                                    e.printStackTrace();
                                }
                            } ) ).doFinally( signalType -> {
                                try{
//                        todo : статус связи
                                    Controller.getInstance().adminConnection.getAndSet( null ).close();
                                }catch( IOException e ){
                                    throw new IllegalStateException( "Connection closed" , e );
                                }
                            } ).blockLast();
                        }catch( IOException e ){
                            throw new IllegalStateException( "Error with sockets: " + e.getMessage() , e );
                        }
                    } );
                    Thread.sleep( 5 * 1000 );
                    try{
                        Socket newConnection =
                                new Socket( Controller.getInstance().host , Controller.getInstance().port );
                        Controller.getInstance().adminConnection.set( newConnection );
                        DataOutputStream outputStream  = new DataOutputStream( newConnection.getOutputStream() );
                        DataInputStream  newDataStream = new DataInputStream( newConnection.getInputStream() );
                        UserInformation request = new UserInformation( Controller.getInstance().login ,
                                                                       Controller.getInstance().password ,
                                                                       Controller.getInstance().base );
                        outputStream.writeUTF( mapper.writeValueAsString( request ) );
                        Data response = mapper.readerFor( Data.class ).readValue( newDataStream.readUTF() );
                        response.withoutExceptionOrWith( data -> {
                            DataModelInstanceSaver.getInstance()
                                                  .getRouteObservableList()
                                                  .setAll( response.getRoutes() );
                            DataModelInstanceSaver.getInstance()
                                                  .getFlightObservableList()
                                                  .setAll( response.getFlights() );
                        } , ClientMain::showWarningByError );
                    }catch( IOException e ){
//                        todo : статус связи
                        Controller.getInstance().adminConnection.set( null );
                    }
                }
            } );
        }else{
            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate( () -> {
                try( Socket socket = new Socket( host , port ) ;
                     DataOutputStream outputStream = new DataOutputStream( socket.getOutputStream() ) ;
                     DataInputStream inputStream = new DataInputStream( socket.getInputStream() ) ){
//                    todo : Статус связи
                    UserInformation request = new UserInformation( Controller.getInstance().login ,
                                                                   Controller.getInstance().password ,
                                                                   Controller.getInstance().base );
                    outputStream.writeUTF( mapper.writeValueAsString( request ) );
                    mapper.readerFor( Data.class ).<Data> readValue( inputStream.readUTF() ).withoutExceptionOrWith(
                            data -> {
                                if( data.getChanges() != null ){
                                    data.getChanges()
                                        .forEach( change -> change.apply( DataModelInstanceSaver.getInstance() ) );
                                }else{
                                    DataModelInstanceSaver.getInstance()
                                                          .getRouteObservableList()
                                                          .setAll( data.getRoutes() );
                                    DataModelInstanceSaver.getInstance()
                                                          .getFlightObservableList()
                                                          .setAll( data.getFlights() );
                                }
                            } ,
                            ClientMain::showWarningByError );
                }catch( IOException e ){
//                    todo : Статус связи
                    e.printStackTrace();
                }
            } , 5 , 5 , TimeUnit.MINUTES );
        }
    }

    private static class InstanceHolder{
        private static final Controller instance = new Controller();

    }
}


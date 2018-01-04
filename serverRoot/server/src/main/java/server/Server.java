package server;import exceptions.FlightAndRouteException;import model.DataModel;import model.DataModelInstanceSaver;import settings.Base;import settings.Settings;import settings.SettingsManager;import java.io.IOException;import java.net.ServerSocket;import java.net.Socket;import java.nio.file.Path;import java.nio.file.Paths;import java.util.Map;import java.util.concurrent.ConcurrentHashMap;import java.util.concurrent.ExecutorService;import java.util.concurrent.Executors;import java.util.concurrent.TimeUnit;public class Server{    class CacheKey<T>{        T    key;        Long timestamp;        CacheKey( T key , Long timestamp ){            this.key = key;            this.timestamp = timestamp;        }        @Override        public int hashCode(){            int hash = 7;            hash = 43 * hash + ( this.key != null ? this.key.hashCode() : 0 );            return hash;        }        @Override        public boolean equals( Object obj ){            if( obj == null ){                return false;            }            if( !( obj instanceof CacheKey ) ){                return false;            }            CacheKey cacheKey = ( CacheKey ) obj;            return this.key.equals( cacheKey.key );        }        @Override        public String toString(){            return String.format( "key[%s]" , key.toString() );        }    }    private Map<CacheKey<Path>, DataModel> bases;    private Settings                       settings;    {        settings = SettingsManager.loadSettings();        bases = new ConcurrentHashMap<>( ( int ) settings.getBase().stream().filter( Base::isRunning ).count() , 0.75f ,                                         32 );        settings.getBase().stream().filter( Base::isRunning ).forEach( base -> {            Path path = Paths.get( base.getPath() );            try{                DataModel dataModel = DataModelInstanceSaver.getInstance();                dataModel.importFromFile( path.toFile() );                bases.put( new CacheKey<>( path , System.currentTimeMillis() ) , dataModel );            }catch( FlightAndRouteException e ){                System.out.println( "Some troubles with load database " + path );                SettingsManager.startStopBase( base.getPath() , false );                e.printStackTrace();            }catch( IOException e ){                e.printStackTrace();            }        } );        Executors.newSingleThreadScheduledExecutor( r -> {            Thread th = new Thread( r );            th.setDaemon( true );            return th;        } ).scheduleAtFixedRate( () -> {            bases.entrySet().removeIf(                    cacheKeyDataModelEntry -> System.currentTimeMillis() - cacheKeyDataModelEntry.getKey().timestamp >                                              settings.getCacheTimeout() );        } , settings.getCacheTimeout() , settings.getCacheTimeout() , TimeUnit.MILLISECONDS );    }    @SuppressWarnings( "InfiniteLoopStatement" )    public static void main( String[] args ) throws IOException{        int port         = Integer.parseInt( args[ 0 ] );        int stoppingPort = Integer.parseInt( args[ 1 ] );        try( ServerSocket requestServerSocket = new ServerSocket( port ) ;             ServerSocket stoppingServerSocket = new ServerSocket( stoppingPort ) ){            new Thread( () -> {                while( true ){                    try{                        Socket stopSignal = stoppingServerSocket.accept();//                      todo : Останова сервера                    }catch( IOException e ){                        e.printStackTrace();                    }                }            } ).start();            ExecutorService requestService = Executors.newCachedThreadPool();            while( true ){                requestService.execute( new RequestParser( requestServerSocket.accept() ) );            }        }    }    static class RequestParser implements Runnable{        private final Socket socket;        RequestParser( Socket socket ){            this.socket = socket;        }        @Override        public void run(){            try( socket ){            }catch( IOException e ){                e.printStackTrace();            }        }    }}
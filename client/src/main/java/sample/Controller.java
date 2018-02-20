package sample;

import com.fasterxml.jackson.databind.ObjectMapper;
import exceptions.FlightAndRouteException;
import model.DataModelInstanceSaver;
import model.Flight;
import model.Route;
import reactor.core.publisher.Flux;
import transport.Data;
import transport.ListChangeAdapter;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    AtomicReference<Socket> connection      = new AtomicReference<>( null );

    private ObjectMapper mapper = new ObjectMapper();
    Boolean changed = false;
    File savingFile;

    private Controller(){}

    public static synchronized Controller getInstance(){
        return Controller.InstanceHolder.instance;
    }

    Exchanger<FlightAndRouteException> updateExchanger;
    AtomicReference<ListChangeAdapter> lastUsersUpdateRequest = new AtomicReference<>( null );
    private ExecutorService updatesStreamFromServer = Executors.newSingleThreadExecutor();

    {
//        todo : Если на чтение
//        signals from server
        updatesStreamFromServer.submit( () -> {
            while( true ){
                DataInputStream inputStream =
                        new DataInputStream( Controller.getInstance().connection.get().getInputStream() );
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
                        updateExchanger.exchange( error );
                    }catch( InterruptedException e ){
                        e.printStackTrace();
                    }
                } ) ).doFinally( signalType -> {
//                       todo : Закрытие текущих потоков
                } ).blockLast();
//                todo : Немного подождать и сделать новый connect
            }
        } );
    }

    private static class InstanceHolder{
        private static final Controller instance = new Controller();
    }
}


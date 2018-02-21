package model;


import com.fasterxml.jackson.databind.ObjectMapper;
import exceptions.FlightAndRouteException;
import javafx.collections.ListChangeListener;
import server.Server;
import settings.Base;
import settings.SettingsManager;
import transport.Data;
import transport.ListChangeAdapter;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static settings.SettingsManager.basesCacheFiles;
import static settings.SettingsManager.basesFolder;

public abstract class DataModelInstanceSaver{
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Map<CacheKey<String>, DataModelWithLockAndListener> bases;

    public static final Map<String, OutputStream> connectedAdmins = new ConcurrentHashMap<>();
    public static final ReentrantReadWriteLock    adminsLock      = new ReentrantReadWriteLock( true );

    public static void sendChange( ListChangeAdapter adapter ){
        Data data = new Data();
        data.setChanges( Collections.singletonList( adapter ) );
        adminsLock.readLock().lock();
//            Send changes to all admins
        connectedAdmins.entrySet()
                       .stream()
                       .map( Map.Entry::getValue )
                       .map( DataOutputStream.class::cast )
                       .forEach( outputStream -> {
                           try{
                               outputStream.writeUTF( mapper.writeValueAsString( data ) );
                           }catch( IOException e ){
                               e.printStackTrace();
                           }
                       } );
        adminsLock.readLock().unlock();
    }

    static{
        bases =
                new ConcurrentHashMap<>( ( int ) Server.settings.getBase().stream().filter( Base::isRunning ).count() ,
                                         0.75f ,
                                         32 );
        Executors.newSingleThreadScheduledExecutor( r -> {
            Thread thread = new Thread( r );
            thread.setDaemon( true );
            return thread;
        } )
                 .scheduleAtFixedRate( () -> bases.entrySet()
                                                  .stream()
                                                  .peek( DataModelInstanceSaver::saveChanges )
                                                  .filter( dataModel -> ( System.currentTimeMillis() -
                                                                          dataModel.getKey().timestamp ) >
                                                                        Server.settings.getCacheTimeout() )
                                                  .forEach( DataModelInstanceSaver::clearCache ) ,
                                       Server.settings.getCacheTimeout() ,
                                       Server.settings.getCacheTimeout() ,
                                       TimeUnit.MILLISECONDS );
    }

    private static void clearCache( Map.Entry<CacheKey<String>, DataModelWithLockAndListener> dataModel ){
        saveChanges( dataModel );
        dataModel.getValue().lock.writeLock().lock();
        bases.remove( dataModel.getKey() , dataModel.getValue() );
        dataModel.getValue().model.getRouteObservableList().removeListener( dataModel.getValue().routeListener );
        dataModel.getValue().model.getFlightObservableList().removeListener( dataModel.getValue().flightListener );
        dataModel.getValue().lock.writeLock().unlock();
    }

    /**
     Sometimes we need to save all changes of database for our safety. If database's very popular, it spends all time
     in cache table

     @param dataModel for saving
     */
    private static void saveChanges( Map.Entry<CacheKey<String>, DataModelWithLockAndListener> dataModel ){
        try( OutputStream outputStream = Files.newOutputStream( Paths.get( basesFolder +
                                                                           dataModel.getKey().key +
                                                                           ".far" ) ) ){
            dataModel.getValue().lock.writeLock().lock();
            dataModel.getValue().model.saveTo( outputStream );
            dataModel.getValue().lock.writeLock().unlock();
        }catch( IOException e ){
            System.err.println( "Database " + dataModel.getKey().key + " has problems" );
            SettingsManager.startStopBase( dataModel.getKey().key , false );
            bases.remove( dataModel.getKey() , dataModel.getValue() );
            dataModel.getValue().lock.writeLock().unlock();
        }
    }


    /**
     @param baseName name of database without extension in the end ( .far )

     @return Optional<DataModelWithLockAndListener> if this name contains in the configs of server and Optional.empty() in other
     case
     */
    public static synchronized Optional<DataModelWithLockAndListener> getInstance( String baseName ){
//        find database in cache table
        Optional<Map.Entry<CacheKey<String>, DataModelWithLockAndListener>>
                optionalEntity =
                bases.entrySet().stream().filter( entity -> entity.getKey().key.equals( baseName ) ).findAny();
        if( optionalEntity.isPresent() ){
            optionalEntity.map( Map.Entry::getKey ).orElseThrow( IllegalStateException::new ).resetTimeStamp();
            return optionalEntity.map( Map.Entry::getValue );
        }else{
            Optional<Base>
                    optionalBase =
                    Server.settings.getBase()
                                   .parallelStream()
                                   .filter( base -> base.getName().equals( baseName ) )
                                   .filter( Base::isRunning )
                                   .findAny();
//            find settings of database
            if( optionalBase.isPresent() ){
                try{
                    DataModel dataModel = new DataModel();
                    dataModel.importFrom( Files.newInputStream( Paths.get( basesFolder + baseName + ".far" ) ) );
//                    Save all changes to file of this base
                    ListChangeListener<Route>
                            routeListener =
                            change -> cacheReaderClientsChanges( baseName , ListChangeAdapter.routeChange( change ) );
                    ListChangeListener<Flight>
                            flightListener =
                            change -> cacheReaderClientsChanges( baseName , ListChangeAdapter.flightChange( change ) );
                    dataModel.addRoutesListener( routeListener );
                    dataModel.addFlightsListener( flightListener );
                    DataModelWithLockAndListener
                            modelWithLockAndListener =
                            new DataModelWithLockAndListener( dataModel ,
                                                              new ReentrantReadWriteLock( true ) ,
                                                              routeListener ,
                                                              flightListener );
                    bases.put( new CacheKey<>( baseName ) , modelWithLockAndListener );
                    return Optional.of( modelWithLockAndListener );
                }catch( FlightAndRouteException e ){
                    System.err.println( "Some troubles with load database " + baseName );
                    SettingsManager.startStopBase( baseName , false );
                    e.printStackTrace();
                }catch( IOException e ){
                    e.printStackTrace();
                }
                return Optional.empty();
            }else{
                return Optional.empty();
            }
        }
    }

    private static void cacheReaderClientsChanges( String baseName , ListChangeAdapter listChangeAdapter ){
        try{
            cacheChanges( baseName , listChangeAdapter );
        }catch( IOException e ){
            e.printStackTrace();
        }
    }

    /**
     Serialize all changes of database in all files, belongs to this database that exists in basesCacheFiles folder

     @param baseName full name of specified database without file extension
     @param change   object, that observable database produce on each change on inner data
     */
    private static void cacheChanges( String baseName , ListChangeAdapter change ) throws IOException{
        Files.list( Paths.get( basesCacheFiles ) )
             .filter( path -> path.toString().matches( "^" + basesCacheFiles + baseName + "_.+$" ) )
             .forEach( path -> {
                 try{
                     Files.write( path ,
                                  change.getUpdate().getBytes( Charset.forName( "UTF-8" ) ) ,
                                  StandardOpenOption.APPEND ,
                                  StandardOpenOption.CREATE );
                 }catch( IOException e ){
                     e.printStackTrace();
                 }
             } );
    }
}

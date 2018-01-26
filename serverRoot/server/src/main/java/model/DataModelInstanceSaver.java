package model;

import exceptions.FlightAndRouteException;
import server.Server;
import settings.Base;
import settings.SettingsManager;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class DataModelInstanceSaver{
    private static Map<CacheKey<Path>, DataModelWithLock> bases;
    public static AtomicBoolean stopped = new AtomicBoolean( false );
    private static String basesCacheFiles;

    static{
        try{
            basesCacheFiles = new File(
                    SettingsManager.class.getProtectionDomain().getCodeSource().getLocation().toURI() ).getParent() +
                              "/serverfiles/clientsUpdates/";
        }catch( URISyntaxException e ){
            e.printStackTrace();
        }
        bases = new ConcurrentHashMap<>( ( int ) Server.settings.getBase().stream().filter( Base::isRunning ).count() ,
                                         0.75f , 32 );
        ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor( Thread::new );
        cleaner.scheduleAtFixedRate( () -> {
            if( !stopped.get() ){
                bases.entrySet()
                     .stream()
                     .peek( dataModel -> {
                         try{
                             dataModel.getValue().lock.writeLock().lock();
                             dataModel.getValue().model.saveToFile( dataModel.getKey().key.toFile() );
                             dataModel.getValue().lock.writeLock().unlock();
                         }catch( IOException e ){
                             System.err.println( "Database " + dataModel.getKey().key.toString() + " has problems" );
                             SettingsManager.startStopBase( dataModel.getKey().key.toString() , false );
                             bases.remove( dataModel.getKey() , dataModel.getValue() );
                             dataModel.getValue().lock.writeLock().unlock();
                         }
                     } )
                     .filter( dataModel -> System.currentTimeMillis() - dataModel.getKey().timestamp >
                                           Server.settings.getCacheTimeout() )
                     .forEach( dataModel -> {
                         dataModel.getValue().lock.writeLock().lock();
                         bases.remove( dataModel.getKey() , dataModel.getValue() );
                         dataModel.getValue().lock.writeLock().unlock();
                     } );
            }else{
                cleaner.shutdownNow();
            }
        } , Server.settings.getCacheTimeout() , Server.settings.getCacheTimeout() , TimeUnit.MILLISECONDS );
    }

    public static synchronized Optional<DataModelWithLock> getInstance( Path path ){
        Optional<Map.Entry<CacheKey<Path>, DataModelWithLock>> optionalEntity =
                bases.entrySet().stream().filter( entity -> entity.getKey().key.equals( path ) ).findAny();
        if( optionalEntity.isPresent() ){
            optionalEntity.map( Map.Entry::getKey ).get().resetTimeStamp();
            return optionalEntity.map( Map.Entry::getValue );
        }else{
            Optional<Base> optionalBase = Server.settings.getBase()
                                                         .parallelStream()
                                                         .filter( base -> Paths.get( base.getPath() ).equals( path ) )
                                                         .filter( Base::isRunning )
                                                         .findAny();
            if( optionalBase.isPresent() ){
                try{
                    DataModel dataModel = new DataModel();
                    dataModel.importFromFile( path.toFile() );
//                    Save all changes to file of this base
                    dataModel.addRoutesListener( c -> {
                        try{
                            Files.find( Paths.get( basesCacheFiles ) , 1 ,
                                        ( path1 , basicFileAttributes ) -> path1.getFileName()
                                                                                .startsWith( path.getFileName() ) )
                                 .forEach( path1 -> {
                                     Files.write(  )
                                 } );
                        }catch( IOException e ){
                            e.printStackTrace();
                        }
                    } ); dataModel.addFlightsListener( c -> {

                    } );
                    DataModelWithLock modelWithLock =
                            new DataModelWithLock( dataModel , new ReentrantReadWriteLock( true ) );
                    bases.put( new CacheKey<>( path ) , modelWithLock );
                    return Optional.of( modelWithLock );
                }catch( FlightAndRouteException e ){
                    System.err.println( "Some troubles with load database " + path );
                    SettingsManager.startStopBase( path.toString() , false );
                    e.printStackTrace();
                }catch( IOException e ){
                    e.printStackTrace();
                } return Optional.empty();
            }else{
                return Optional.empty();
            }
        }
    }
}

package model;


import exceptions.FlightAndRouteException;
import server.Server;
import settings.Base;
import settings.SettingsManager;
import transport.ListChangeAdapter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class DataModelInstanceSaver{
    private static Map<CacheKey<String>, DataModelWithLock> bases;
    static         String                                   basesCacheFiles;
    static         String                                   basesFolder;

    static{
        String serverFilesPath =
                Paths.get( SettingsManager.class.getProtectionDomain().getCodeSource().getLocation().getPath() ,
                           "UTF-8" ).getParent().getParent().getParent().toString() + "/serverfiles/";
        basesCacheFiles = serverFilesPath + "clientUpdates/";
        basesFolder = serverFilesPath + "bases/";
        bases = new ConcurrentHashMap<>( ( int ) Server.settings.getBase().stream().filter( Base::isRunning ).count() ,
                                         0.75f , 32 );
        Executors.newSingleThreadScheduledExecutor( r -> {
            Thread thread = new Thread( r );
            thread.setDaemon( true );
            return thread;
        } ).scheduleAtFixedRate( () -> bases.entrySet()
                                            .stream()
                                            .peek( dataModel -> {
                                                try{
                                                    dataModel.getValue().lock.writeLock().lock();
                                                    dataModel.getValue().model.saveTo( Files.newOutputStream( Paths.get(
                                                            basesFolder + dataModel.getKey().key + ".far" ) ) );
                                                    dataModel.getValue().lock.writeLock().unlock();
                                                }catch( IOException e ){
                                                    System.err.println(
                                                            "Database " + dataModel.getKey().key + " has problems" );
                                                    SettingsManager.startStopBase( dataModel.getKey().key , false );
                                                    bases.remove( dataModel.getKey() , dataModel.getValue() );
                                                    dataModel.getValue().lock.writeLock().unlock();
                                                }
                                            } )
                                            .filter( dataModel ->
                                                             System.currentTimeMillis() - dataModel.getKey().timestamp >
                                                             Server.settings.getCacheTimeout() )
                                            .forEach( dataModel -> {
                                                dataModel.getValue().lock.writeLock().lock();
                                                bases.remove( dataModel.getKey() , dataModel.getValue() );
                                                dataModel.getValue().lock.writeLock().unlock();
                                            } ) , Server.settings.getCacheTimeout() ,
                                 Server.settings.getCacheTimeout() , TimeUnit.MILLISECONDS );
    }


    /**
     @param baseName name of database without extension in the end ( .far )

     @return Optional<DataModelWithLock> if this name contains in the configs of server and Optional.empty() in other
     case
     */
    public static synchronized Optional<DataModelWithLock> getInstance( String baseName ){
        Optional<Map.Entry<CacheKey<String>, DataModelWithLock>> optionalEntity =
                bases.entrySet().stream().filter( entity -> entity.getKey().key.equals( baseName ) ).findAny();
        if( optionalEntity.isPresent() ){
            optionalEntity.map( Map.Entry::getKey ).get().resetTimeStamp();
            return optionalEntity.map( Map.Entry::getValue );
        }else{
            Optional<Base> optionalBase = Server.settings.getBase()
                                                         .parallelStream()
                                                         .filter( base -> base.getName().equals( baseName ) )
                                                         .filter( Base::isRunning )
                                                         .findAny();
            if( optionalBase.isPresent() ){
                try{
                    DataModel dataModel = new DataModel();
                    dataModel.importFrom( Files.newInputStream( Paths.get( basesFolder + baseName + ".far" ) ) );
//                    Save all changes to file of this base
                    dataModel.addRoutesListener( change -> {
                        try{
                            cacheChanges( baseName , ListChangeAdapter.routeChange( change ) );
                        }catch( IOException e ){
                            e.printStackTrace();
                        }
                    } );
                    dataModel.addFlightsListener( change -> {
                        try{
                            cacheChanges( baseName , ListChangeAdapter.flightChange( change ) );
                        }catch( IOException e ){
                            e.printStackTrace();
                        }
                    } );
                    DataModelWithLock modelWithLock =
                            new DataModelWithLock( dataModel , new ReentrantReadWriteLock( true ) );
                    bases.put( new CacheKey<>( baseName ) , modelWithLock );
                    return Optional.of( modelWithLock );
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

    /**
     Serialize all changes of database in all files, belongs to this database that exists in basesCacheFiles folder

     @param baseName full name of specified database without file extension
     @param change   object, that observable database produce on each change on inner data
     */
    private static void cacheChanges( String baseName , ListChangeAdapter change ) throws IOException{
        Files.list( Paths.get( basesCacheFiles ) )
             .filter( path2 -> path2.toString().matches( basesCacheFiles + baseName + "_.+" ) )
             .forEach( path1 -> {
                 try{
                     Files.write( path1 , change.getUpdate().getBytes( Charset.forName( "UTF-8" ) ) ,
                                  StandardOpenOption.APPEND , StandardOpenOption.CREATE );
                 }catch( IOException e ){
                     e.printStackTrace();
                 }
             } );
    }
}

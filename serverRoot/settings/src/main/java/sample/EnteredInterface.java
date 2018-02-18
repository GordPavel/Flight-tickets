package sample;

import asg.cliche.*;
import settings.SettingsManager;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings( "WeakerAccess" )
public class EnteredInterface implements ShellDependent{

    private final Integer stoppingPort = 5556;
    private Shell   shell;
    private Integer port;

    @Override
    public void cliSetShell( Shell shell ){
        this.shell = shell;
    }

    @Command( description = "Lists all databases.", abbrev = "-l", name = "--list" )
    public void listAllDatabases(){
        SettingsManager.settings.listBases().forEach( System.out::println );
    }

    @Command( description = "Change the admin's name.", abbrev = "-cN", name = "--changeName" )
    public void changeName(
            @Param( description = "New admin's name", name = "New name" )
                    String name ){
        SettingsManager.setAdminName( name );
        System.out.println( "To accept all changes please restart server. ( command <restart> )" );
    }

    @Command( description = "Change the admin's password.", abbrev = "-cP", name = "--changePassword" )
    public void changePassword(
            @Param( description = "New admin's password", name = "New pass" )
                    String pass ){
        SettingsManager.setAdminPassword( pass );
        System.out.println( "To accept all changes please restart server. ( command <restart> )" );
    }

    @Command( description = "Change the live time of each database executor in cache.", abbrev = "-cT", name = "--changeTimeout" )
    public void changeTimeout(
            @Param( description = "New cache time", name = "New time" )
                    Long timeout ){
        SettingsManager.setCacheTimeout( timeout );
        System.out.println( "To accept all changes please restart server. ( command <restart> )" );
    }

    @Command( description = "Add path of new database", name = "--newDatabase", abbrev = "-nD" )
    public void addNewDatabase(
            @Param( description = "Name of file *.far in folder /serverfiles/bases.", name = "name" )
                    String name ){
        SettingsManager.addNewBase( name );
        System.out.println( "To accept all changes please restart server. ( command <restart> )" );
    }

    @Command( description = "Delete the path of database from settings file.", abbrev = "-dD", name = "--deleteDatabase" )
    public void deleteDatabase(
            @Param( description = "Name of file *.far in folder /serverfiles/base.", name = "path" )
                    String name ){
        SettingsManager.deleteBase( name );
        System.out.println( "To accept all changes please restart server. ( command <restart> )" );
    }

    @Command( description = "Open submenu to configure specified database. Don't forget to restart server after all " +
                            "changes.", abbrev = "-mD", name = "--manageDatabase" )
    public void manageDatabase(
            @Param( description = "Name of file *.far in folder /serverfiles/base.", name = "path" )
                    String name ) throws IOException{
        ShellFactory.createSubshell( name ,
                                     shell ,
                                     name ,
                                     new DatabaseInterface( SettingsManager.getBase( name )
                                                                           .orElseThrow( () -> new IllegalArgumentException(
                                                                                   "Server doesn't have database " +
                                                                                   name ) ) ) ).commandLoop();
    }

    @Command( description = "Write path to file where write all server logs", abbrev = "-sLF", name = "--setLogFile" )
    public void setLogFile(
            @Param( description = "path to file with server logs", name = "path" )
                    String path ){
        SettingsManager.setLogFile( path );
        System.out.println( "To accept all changes please restart server. ( command <restart> )" );
    }

    @Command( description = "Use it when you don't want to use file for logging no longer", abbrev = "-cLF", name = "--clearLogFile" )
    public void clearLogFile(
            @Param( description = "path to file with server logs", name = "path" )
                    String path ){
        SettingsManager.setLogFile( null );
        System.out.println( "To accept all changes please restart server. ( command <restart> )" );
    }

    @Command( description = "Path to file where write all server logs", abbrev = "-lF", name = "--lofFile" )
    public void logFile(){
        System.out.println( SettingsManager.logFile() );
    }

    @Command( description = "Start server" )
    public void start(
            @Param( description = "listening port for server", name = "port" )
                    Integer port ) throws IOException{
        if( port == 5556 ){
            System.out.println( "Port 5556 is reserved for stopping server" );
            return;
        }
        this.port = port;
        startServer( port );
    }

    private void startServer( Integer port ) throws IOException{
        ProcessBuilder processBuilder = new ProcessBuilder( "java" ,
                                                            "-jar" ,
                                                            "flight-system-server-1.1.0.jar" ,
                                                            port.toString() ,
                                                            this.stoppingPort.toString() );
        Optional.ofNullable( SettingsManager.logFile() ).map( Paths::get ).ifPresent( path -> {
            if( !Files.exists( path ) ){
                try{
                    Files.createFile( path );
                }catch( IOException e ){
                    e.printStackTrace();
                }
            }
            processBuilder.redirectOutput( path.toFile() );
            processBuilder.redirectError( path.toFile() );
        } );
        processBuilder.directory( Paths.get( SettingsManager.rootFolderPath ).toFile() );
        processBuilder.start();
        System.out.println( "Server has been started" );
    }

    @Command( description = "Stop server when it can" )
    public void stop(){
        stopServer();
    }

    private void stopServer(){
        try( Socket stoppingSocket = new Socket( "localhost" , this.stoppingPort ) ;
             DataInputStream inputStream = new DataInputStream( stoppingSocket.getInputStream() ) ){
            System.out.println( inputStream.readUTF() );
        }catch( IOException e ){
            e.printStackTrace();
        }
    }

    @Command( description = "Immediately stop server" )
    public void kill(){
        try( BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( new ProcessBuilder( "ps" ,
                                                                                                            "-few" ).start()
                                                                                                                    .getInputStream() ) ) ){
            String pid = bufferedReader.lines()
                                       .filter( process -> process.contains( "/usr/bin/java" ) )
                                       .filter( process -> process.contains( "flight-system-server" ) )
                                       .map( process -> {
                                           Matcher matcher =
                                                   Pattern.compile( "^\\s*(\\d+)\\s+(?<PID>\\d+)" ).matcher( process );
                                           //noinspection ResultOfMethodCallIgnored
                                           matcher.find();
                                           return matcher.group( "PID" );
                                       } )
                                       .findFirst()
                                       .orElseThrow( IllegalStateException::new );
            new ProcessBuilder( "kill" , pid ).start();
            System.out.println( "Server process has been killed" );
        }catch( IOException e ){
            System.out.println( "Something went wrong while killing the service" );
        }
    }

    @Command( description = "Immediately restart server" )
    public void restart() throws IOException{
        stopServer();
        startServer( Optional.ofNullable( this.port )
                             .orElseThrow( () -> new IllegalArgumentException( "Server's port isn't configured" ) ) );
    }

}

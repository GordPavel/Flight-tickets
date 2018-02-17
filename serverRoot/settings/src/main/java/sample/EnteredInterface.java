package sample;

import asg.cliche.*;
import settings.SettingsManager;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings( "WeakerAccess" )
public class EnteredInterface implements ShellDependent{

    private Shell   shell;
    private Integer port;
    private Integer stoppingPort = 5556;

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
        ShellFactory.createSubshell( name , shell , name , new DatabaseInterface( SettingsManager.getBase( name )
                                                                                                 .orElseThrow(
                                                                                                         () -> new IllegalArgumentException(
                                                                                                                 "Server doesn't have database " +
                                                                                                                 name ) ) ) )
                    .commandLoop();
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
        ProcessBuilder processBuilder =
                new ProcessBuilder( "java" , "-jar" , "flight-system-server-1.1.0.jar" , port.toString() ,
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

    static class Process{
        Integer UID;
        Integer PID;
        Integer PPID;
        Integer C;
        String  STIME;
        String  TTY;
        String  TIME;
        Path    path;

        static Optional<Process> instace( String processString ){
            Matcher matcher = pattern.matcher( processString );
            if( matcher.find() ){
                return Optional.of(
                        new Process( matcher.group( "UID" ) , matcher.group( "PID" ) , matcher.group( "PPID" ) ,
                                     matcher.group( "C" ) , matcher.group( "STIME" ) , matcher.group( "TTY" ) ,
                                     matcher.group( "TIME" ) , matcher.group( "path" ) ) );
            }else{
                return Optional.empty();
            }
        }

        public Process( String UID , String PID , String PPID , String C , String STIME , String TTY , String TIME ,
                        String path ){
            this.UID = Integer.parseInt( UID );
            this.PID = Integer.parseInt( PID );
            this.PPID = Integer.parseInt( PPID );
            this.C = Integer.parseInt( C );
            this.STIME = STIME;
            this.TTY = TTY;
            this.TIME = TIME;
            this.path = Paths.get( path );
        }

        @Override
        public String toString(){
            return String.format( "%d %d %d %d %s %s %s %s" , UID , PID , PPID , C , STIME , TTY , TIME ,
                                  path.toString() );
        }

        public Integer getPID(){
            return PID;
        }

        static final Pattern pattern = Pattern.compile(
                "^\\s*(?<UID>\\d+)\\s+(?<PID>\\d+)\\s+(?<PPID>\\d+)\\s+(?<C>\\d+)\\s+(?<STIME>\\d+:\\d+[AP]M)\\s+" +
                "(?<TTY>.+)\\s+(?<TIME>\\d+:\\d+.\\d+)\\s+(?<path>.+)$" );
    }

    @Command( description = "Immediately stop server" )
    public void kill(){
        try( BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader( new ProcessBuilder( "ps" , "-few" ).start().getInputStream() ) ) ){
            Integer pid = bufferedReader.lines()
                                        .map( Process::instace )
                                        .filter( Optional::isPresent )
                                        .map( Optional::get )
                                        .filter( process -> process.path.toString().contains( "server.Server" ) )
                                        .map( Process::getPID )
                                        .findFirst()
                                        .orElseThrow( IllegalStateException::new );
            new ProcessBuilder( "kill" , pid.toString() ).start();
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

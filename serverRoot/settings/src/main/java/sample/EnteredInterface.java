package sample;

import asg.cliche.*;
import settings.SettingsManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SuppressWarnings( "WeakerAccess" )
public class EnteredInterface implements ShellDependent{

    private Shell   shell;
    private Process serverProcess;
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

    @Command( description = "Start server" )
    public void start(
            @Param( description = "listening port for server", name = "port" )
                    Integer port ) throws IOException{
        this.port = port;
        ProcessBuilder processBuilder =
                new ProcessBuilder( "java" , "-jar" , "flight-system-server-1.1.0.jar" , this.port.toString() );
        Path path = Paths.get( SettingsManager.logFile() );
        if( !Files.exists( path ) ){
            Files.createFile( path );
        }
        processBuilder.redirectOutput( path.toFile() );
        processBuilder.directory( Paths.get( SettingsManager.rootFolderPath ).toFile() );
        serverProcess = processBuilder.start();
    }

    @Command( description = "Stop server when it can" )
    public void stop(){
        serverProcess.destroy();
    }

    @Command( description = "Immediately stop server" )
    public void kill(){
        serverProcess.destroyForcibly();
    }

    @Command( description = "Immediately restart server" )
    public void restart() throws IOException{
        ProcessBuilder processBuilder =
                new ProcessBuilder( "java" , "-jar" , "flight-system-server-1.1.0.jar" , this.port.toString() );
        Path path = Paths.get( "/Users/pavelgordeev/Desktop/errors.txt" );
        if( !Files.exists( path ) ){
            Files.createFile( path );
        }
        processBuilder.redirectOutput( path.toFile() );
        processBuilder.directory( Paths.get( SettingsManager.rootFolderPath ).toFile() );
        serverProcess = processBuilder.start();
    }
}

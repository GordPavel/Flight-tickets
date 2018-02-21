package settings;

import settings.serverexceptions.CopyBase;
import settings.serverexceptions.CopyUser;
import settings.serverexceptions.IllegalBasePath;
import settings.serverexceptions.StartStopBaseException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Predicate;

public class SettingsManager{
    public static final String rootFolderPath;
    public static final String basesCacheFiles;
    public static final String basesFolder;
    private final static String
            defaultSettingsFileString =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<settings/>";
    private static final                             String      settingsFilePath;
    public static                                    Settings    settings;
    @SuppressWarnings( "CanBeFinal" ) private static JAXBContext jaxbContext;

    static{
        rootFolderPath = "/Volumes/Users/pavelgordeev/IdeaProjects/Flight-tickets/serverRoot";
//                Paths.get( SettingsManager.class.getProtectionDomain().getCodeSource().getLocation().getPath() ,
//                           "UTF-8" ).getParent().getParent().getParent().toString() + "/target";
        settingsFilePath = rootFolderPath + "/serverfiles/settings.xml";
        basesCacheFiles = rootFolderPath + "/serverfiles/clientUpdates/";
        basesFolder = rootFolderPath + "/serverfiles/bases/";
        try{
            jaxbContext = JAXBContext.newInstance( Settings.class , Base.class , User.class );
        }catch( JAXBException e ){
            e.printStackTrace();
            System.exit( 1 );
        }
        settings = loadSettings();
    }

    public static Settings loadSettings(){
        if( !Files.exists( Paths.get( settingsFilePath ) ) ){
            Path filePath = Paths.get( settingsFilePath );
            try{
                Files.write( filePath ,
                             defaultSettingsFileString.getBytes( StandardCharsets.UTF_8 ) ,
                             StandardOpenOption.CREATE_NEW );
            }catch( IOException e ){
                e.printStackTrace();
            }
        }
        try( InputStream inputStream = new FileInputStream( settingsFilePath ) ){
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            settings = ( Settings ) jaxbUnmarshaller.unmarshal( inputStream );
        }catch( JAXBException e ){
            System.out.println( "Your settings.xml file is damaged. Do you want to delete it and restart program? [y/n]" +
                                "All your settings'll be lost." );
            Scanner scanner = new Scanner( System.in );
            String  answer;
            while( !( answer = scanner.next() ).matches( "[yn]" ) ){
                System.out.println( "Type y - yes, delete file; or n - no, not delete." );
            }
            if( answer.equals( "y" ) ){
                try{
                    Files.delete( Paths.get( settingsFilePath ) );
                }catch( IOException e1 ){
                    e1.printStackTrace();
                }
            }
            System.exit( 0 );
        }catch( IOException e ){
            e.printStackTrace();
            System.exit( 1 );
        }
        return settings;
    }

    private static void saveSettings(){
        try( OutputStream outputStream = new FileOutputStream( settingsFilePath ) ){
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT , true );
            jaxbMarshaller.marshal( settings , outputStream );
        }catch( JAXBException | IOException e ){
            e.printStackTrace();
        }
    }

    public static void setAdminName( String name ){
        settings.setAdminName( name );
        saveSettings();
    }

    public static void setAdminPassword( String password ){
        settings.setRootPassword( password );
        saveSettings();
    }

    public static void setCacheTimeout( Long timeout ){
        if( timeout <= 0 ){
            throw new IllegalArgumentException( "This value can be only positive" );
        }
        settings.setCacheTimeout( timeout );
        saveSettings();
    }

    public static Optional<Base> getBase( String path ){
        return settings.getBase().stream().filter( base -> base.getName().equals( path ) ).findAny();
    }

    public static void addNewBase( String name ){
        if( settings.getBase().parallelStream().map( Base::getName ).noneMatch( Predicate.isEqual( name ) ) ){
            Base base = new Base( name );
            base.setRunning( false );
            settings.getBase().add( base );
            saveSettings();
        }else{
            throw new CopyBase( "Server already contains this base " + name );
        }
    }

    public static void setLogFile( String path ){
        settings.setLogFile( path );
        saveSettings();
    }

    public static String logFile(){
        return settings.getLogFile();
    }

    public static void deleteBase( String name ){
        settings.getBase().removeIf( Predicate.isEqual( name ) );
        saveSettings();
    }

    public static void startStopBase( String name , Boolean start ){
        Optional<Base>
                optionalBase =
                settings.getBase().stream().filter( base -> base.getName().equals( name ) ).findFirst();
        if( optionalBase.isPresent() ){
            Base editingBase = optionalBase.get();
            if( editingBase.isRunning() && start ){
                throw new StartStopBaseException( "Base " + name + " is already running. Stop it at first." );
            }else if( !editingBase.isRunning() && !start ){
                throw new StartStopBaseException( "Base " + name + " is already stopped. Start it at first." );
            }else{
                editingBase.setRunning( start );
                saveSettings();
            }
        }else{
            throw new IllegalBasePath( "Server doesn't contain this base " + name );
        }
    }

    public static void addNewClient( String baseName ,
                                     String clientName ,
                                     String clientPassword ,
                                     UserPrivileges privileges ){
        Optional<Base>
                optionalBase =
                settings.getBase().stream().filter( base -> base.getName().equals( baseName ) ).findFirst();
        if( optionalBase.isPresent() ){
            Base editingBase = optionalBase.get();
            if( editingBase.getUsers()
                           .parallelStream()
                           .map( User::getLogin )
                           .noneMatch( Predicate.isEqual( clientName ) ) ){
                editingBase.getUsers().add( new User( clientName , clientPassword , privileges ) );
                saveSettings();
            }else{
                throw new CopyUser( "Base " + baseName + " already has this client " + clientName );
            }
        }else{
            throw new IllegalBasePath( "Server doesn't contain this base " + baseName );
        }
    }

    public static void deleteClient( String baseName , String clientName ){
        Optional<Base>
                optionalBase =
                settings.getBase().stream().filter( base -> base.getName().equals( baseName ) ).findFirst();
        if( optionalBase.isPresent() ){
            Base editingBase = optionalBase.get();
            editingBase.getUsers().removeIf( user -> userNamesEqual( clientName , user ) );
            saveSettings();
        }else{
            throw new IllegalBasePath( "Server doesn't contain this base " + baseName );
        }
    }

    public static void changeClientName( String baseName , String oldName , String newName ){
        Optional<Base>
                optionalBase =
                settings.getBase().stream().filter( base -> base.getName().equals( baseName ) ).findFirst();
        if( optionalBase.isPresent() ){
            Base editingBase = optionalBase.get();
            Optional<User>
                    optionalUser =
                    editingBase.getUsers().stream().filter( user -> userNamesEqual( oldName , user ) ).findFirst();
            if( optionalUser.isPresent() ){
                optionalUser.get().setLogin( newName );
                saveSettings();
            }else{
                throw new CopyUser( "Base " + baseName + " doesn't have this client " + oldName );
            }
        }else{
            throw new IllegalBasePath( "Server doesn't contain this base " + baseName );
        }
    }

    public static void changeClientPassword( String baseName , String clientName , String newPassword ){
        Optional<Base>
                optionalBase =
                settings.getBase().stream().filter( base -> base.getName().equals( baseName ) ).findFirst();
        if( optionalBase.isPresent() ){
            Base editingBase = optionalBase.get();
            Optional<User>
                    optionalUser =
                    editingBase.getUsers().stream().filter( user -> userNamesEqual( clientName , user ) ).findFirst();
            if( optionalUser.isPresent() ){
                optionalUser.get().setPassword( newPassword );
                saveSettings();
            }else{
                throw new CopyUser( "Base " + baseName + " doesn't have this client " + clientName );
            }
        }else{
            throw new IllegalBasePath( "Server doesn't contain this base " + baseName );
        }
    }

    public static void changeClientPrivilege( String baseName , String clientName ){
        Optional<Base>
                optionalBase =
                settings.getBase().stream().filter( base -> baseNamesEqual( baseName , base ) ).findFirst();
        if( optionalBase.isPresent() ){
            Base editingBase = optionalBase.get();
            Optional<User>
                    optionalUser =
                    editingBase.getUsers().stream().filter( user -> userNamesEqual( clientName , user ) ).findFirst();
            if( optionalUser.isPresent() ){
                User user = optionalUser.get();
                user.setPrivilege( user.getPrivilege() == UserPrivileges.Read ?
                                   UserPrivileges.ReadWrite :
                                   UserPrivileges.Read );
                saveSettings();
            }else{
                throw new CopyUser( "Base " + baseName + " doesn't have this client " + clientName );
            }
        }else{
            throw new IllegalBasePath( "Server doesn't contain this base " + baseName );
        }
    }

    private static boolean userNamesEqual( String clientName , User user ){
        return user.getLogin().equals( clientName );
    }

    private static boolean baseNamesEqual( String baseName , Base base ){
        return base.getName().equals( baseName );
    }

}





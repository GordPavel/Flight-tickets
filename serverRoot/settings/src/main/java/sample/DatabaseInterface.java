package sample;

import asg.cliche.Command;
import asg.cliche.Param;
import asg.cliche.Shell;
import asg.cliche.ShellDependent;
import settings.Base;
import settings.SettingsManager;
import settings.UserPrivileges;

public class DatabaseInterface implements ShellDependent{
    private       Shell shell;
    private final Base  base;

    DatabaseInterface( Base base ){
        this.base = base;
    }

    @Override
    public void cliSetShell( Shell shell ){
        this.shell = shell;
    }

    @Command( description = "Lists all clients.", abbrev = "-l", name = "--list" )
    public void listAllClients(){
        base.listAllUsers().forEach( System.out::println );
    }

    @Command( description = "Say server to share this database.", name = "--start" )
    public void startDatabase(){
        if( base.isRunning() ){
            System.out.println( "Database is already running." );
        }else{
            SettingsManager.startStopBase( base.getName() , true );
            System.out.println(
                    "To accept all changes please restart server. ( exit from this submenu <exit> and then command <restart> )" );
        }
    }

    @Command( description = "Say server to stop sharing this database.", name = "--stop" )
    public void stopDatabase(){
        if( !base.isRunning() ){
            System.out.println( "Database is already stopped." );
        }else{
            SettingsManager.startStopBase( base.getName() , false );
            System.out.println(
                    "To accept all changes please restart server. ( exit from this submenu <exit> and then command <restart> )" );
        }
    }

    @Command( description = "Add new client to allow him connect to this database.", name = "--newClient", abbrev = "-nC" )
    public void addNewClient(
            @Param( description = "Name of user", name = "name" )
                    String name ,
            @Param( description = "Password of user", name = "pass" )
                    String password ,
            @Param( description = "Privilege of user. r - read privilege , rw - read & write privilege", name = "privilege" )
                    String privilege ){
        if( !privilege.equals( "rw" ) && !privilege.equals( "r" ) ){
            throw new IllegalArgumentException( "User privilege must be just r (read) or rw (read & write)." );
        }
        SettingsManager.addNewClient( base.getName() , name , password ,
                                      privilege.equals( "r" ) ? UserPrivileges.Read : UserPrivileges.ReadWrite );
        System.out.println(
                "To accept all changes please restart server. ( exit from this submenu <exit> and then command <restart> )" );
    }

    @Command( description = "Delete client from allow list to this database.", abbrev = "-dC", name = "--deleteClient" )
    public void deleteClient(
            @Param( description = "Name of user", name = "name" )
                    String name ){
        SettingsManager.deleteClient( base.getName() , name );
        System.out.println(
                "To accept all changes please restart server. ( exit from this submenu <exit> and then command <restart> )" );
    }

    @Command( description = "Change name of specified client", abbrev = "-cN", name = "--changeClientName" )
    public void changeClientName(
            @Param( description = "Old name of user", name = "old name" )
                    String oldName ,
            @Param( description = "New name of user", name = "new name" )
                    String newName ){
        SettingsManager.changeClientName( base.getName() , oldName , newName );
        System.out.println(
                "To accept all changes please restart server. ( exit from this submenu <exit> and then command <restart> )" );
    }

    @Command( description = "Change password of specified client", abbrev = "-cP", name = "--changePass" )
    public void changeClientPassword(
            @Param( description = "Name of user", name = "name" )
                    String userName ,
            @Param( description = "Password of user", name = "pass" )
                    String newPassword ){
        SettingsManager.changeClientPassword( base.getName() , userName , newPassword );
        System.out.println(
                "To accept all changes please restart server. ( exit from this submenu <exit> and then command <restart> )" );
    }

    @Command( description = "Change privilege of specified client ( if he already has read privilege, after " +
                            "this command he'll have read & write privilege. And vice versa)", abbrev = "-cPr", name = "--changePrivilege" )
    public void changeClientPrivilege(
            @Param( description = "Name of user", name = "name" )
                    String userName ){
        SettingsManager.changeClientPrivilege( base.getName() , userName );
        System.out.println(
                "To accept all changes please restart server. ( exit from this submenu <exit> and then command <restart> )" );
    }
}

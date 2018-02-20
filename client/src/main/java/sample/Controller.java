package sample;

import model.Flight;
import model.Route;

import java.io.File;
import java.net.Socket;
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
    Socket  connection;
    AtomicReference<Predicate<Route>>  routePredicate  = new AtomicReference<>( route -> true );
    AtomicReference<Predicate<Flight>> flightPredicate = new AtomicReference<>( flight -> true );

    static Boolean changed = false;
    static File savingFile;

    private Controller(){}

    public static synchronized Controller getInstance(){
        return Controller.InstanceHolder.instance;
    }


    private static class InstanceHolder{
        private static final Controller instance = new Controller();
    }
}


package sample;

import model.Flight;
import model.Route;

import java.io.File;
import java.net.Socket;
import java.util.function.Predicate;

/**
 Support class for controllers
 */
public class Controller{

    String            host;
    Integer           port;
    String            login;
    String            password;
    String            base;
    Predicate<Route>  routePredicate;
    Predicate<Flight> flightPredicate;
    Socket            connection;

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


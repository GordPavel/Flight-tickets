package sample;

import javafx.collections.ObservableList;
import model.Flight;
import model.Route;

/**
 Support class for controllers
 */
class Controller{

    private Controller(){}

    private static class InstanceHolder{
        private static final Controller instance = new Controller();
    }

    static synchronized Controller getInstance(){
        return Controller.InstanceHolder.instance;
    }

    private static boolean flightSearchActive = false;
    private ObservableList<Flight> mergeFlights;
    private ObservableList<Route>  mergeRoutes;
    private FaRThread              thread;

    void setThread( FaRThread thread ){
        this.thread = thread;
    }

    void startThread(){
        thread.start();
    }

    void stopThread(){
        thread.setStop();
    }

    void setMergeFlights( ObservableList<Flight> mergeFlights ){
        this.mergeFlights = mergeFlights;
    }

    ObservableList<Flight> getMergeFlights(){
        return mergeFlights;
    }

    void setMergeRoutes( ObservableList<Route> mergeRoutes ){
        this.mergeRoutes = mergeRoutes;
    }

    ObservableList<Route> getMergeRoutes(){
        return mergeRoutes;
    }

    void setFlightSearchActive( boolean flightSearchActive ){
        Controller.flightSearchActive = flightSearchActive;
    }

    boolean isFlightSearchActive(){
        return flightSearchActive;
    }
}


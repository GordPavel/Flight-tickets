package sample;

import javafx.collections.ObservableList;
import model.DataModelInstanceSaver;
import model.Flight;
import model.Route;

/**
 Support class for controllers
 */
public class Controller{

    private Controller(){}

    private static class InstanceHolder{
        private static final Controller instance = new Controller();
    }

    public static synchronized Controller getInstance(){
        return Controller.InstanceHolder.instance;
    }

    private static boolean flightSearchActive = false;
    private ObservableList<Route>  routes;
    private ObservableList<Flight> flights;
    private ObservableList<Flight> mergeFlights;
    private ObservableList<Route>  mergeRoutes;
    private FaRThread              thread;

    public void setThread( FaRThread thread ){
        this.thread = thread;
    }

    public void startThread(){
        thread.start();
    }

    public void stopThread(){
        thread.setStop();
    }

    public ObservableList<Route> getRoutes(){
        return routes;
    }

    public ObservableList<Flight> getFlights(){
        return flights;
    }

    public void setRoutes( ObservableList<Route> routes ){
        this.routes = routes;
    }

    public void setFlights( ObservableList<Flight> flights ){
        this.flights = flights;
    }

    public void setMergeFlights( ObservableList<Flight> mergeFlights ){
        this.mergeFlights = mergeFlights;
    }

    public ObservableList<Flight> getMergeFlights(){
        return mergeFlights;
    }

    public void setMergeRoutes( ObservableList<Route> mergeRoutes ){
        this.mergeRoutes = mergeRoutes;
    }

    public ObservableList<Route> getMergeRoutes(){
        return mergeRoutes;
    }

    public void updateRoutes(){
        this.routes = DataModelInstanceSaver.getInstance().getRouteObservableList();
    }

    public void updateFlights(){
        this.flights = DataModelInstanceSaver.getInstance().getFLightObservableList();
    }

    public void setFlightSearchActive( boolean flightSearchActive ){
        Controller.flightSearchActive = flightSearchActive;
    }

    public boolean isFlightSearchActive(){
        return flightSearchActive;
    }
}


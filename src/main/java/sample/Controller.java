package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.DataModel;
import model.Flight;
import model.Route;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 Support class for controllers
 */
public class Controller{

    Controller(){}

    private static class InstanceHolder{
        private static final Controller instance = new Controller();
    }

    public static synchronized Controller getInstance(){
        return Controller.InstanceHolder.instance;
    }

    static         DataModel model              = DataModel.getInstance();
    private static boolean   flightSearchActive = false;
    private       ObservableList<Route>  routes;
    private       ObservableList<Flight> flights;
    public static Route                  routeForEdit;
    public static Flight                 flightForEdit;
    private       ObservableList<Flight> mergeFlights;
    private       ObservableList<Route>  mergeRoutes;
    private       FaRThread                 thread;

    public void setThread(FaRThread thread) {
        this.thread = thread;
    }

    public void startThread(){
        thread.start();
    }

    public void stopThread() {thread.setStop();}


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

    public void setMergeRoutes( ObservableList<Route> mergeRoutes ){this.mergeRoutes = mergeRoutes;}

    public ObservableList<Route> getMergeRoutes(){
        return mergeRoutes;
    }

    public void updateRoutes(){

        this.routes = FXCollections.observableArrayList(
                ( Collection<? extends Route> ) model.listRoutesWithPredicate( route -> true )
                                                     .collect( Collectors.toCollection( ArrayList::new ) ) );
    }

    public void updateFlights(){

        this.flights = FXCollections.observableArrayList(
                ( Collection<? extends Flight> ) model.listFlightsWithPredicate( flight -> true )
                                                      .collect( Collectors.toCollection( ArrayList::new ) ) );
    }

    public Route getRouteForEdit(){

        return routeForEdit;
    }

    public void setRouteForEdit( Route routeForEdit ){

        Controller.routeForEdit = routeForEdit;
    }

    public Flight getFlightForEdit(){

        return flightForEdit;
    }

    public void setFlightForEdit( Flight flightForEdit ){

        Controller.flightForEdit = flightForEdit;
    }

    public void setFlightSearchActive( boolean flightSearchActive ){
        Controller.flightSearchActive = flightSearchActive;
    }

    public boolean isFlightSearchActive(){
        return flightSearchActive;
    }
}


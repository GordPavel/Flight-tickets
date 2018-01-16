package transport;

import model.Flight;
import model.Route;

/**
 * Class for sending data range
 */

import java.util.ArrayList;
import java.util.function.Predicate;

public class Data {

    private ArrayList<Flight> flights;
    private ArrayList<Route>  routes;
    public  Predicate<String> predicate;

    public Data(ArrayList<Flight> flights, ArrayList<Route> routes) {

        this.flights = flights;
        this.routes = routes;
    }

    public ArrayList<Flight> getFlights() {

        return flights;
    }

    public void setFlights(ArrayList<Flight> flights) {

        this.flights = flights;
    }

    public ArrayList<Route> getRoutes() {

        return routes;
    }

    public void setRoutes(ArrayList<Route> routes) {
        this.routes = routes;
    }
}

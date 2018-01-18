package transport;

import model.Flight;
import model.Route;

/**
 * Class for sending data range
 */

import java.util.ArrayList;
import java.util.function.Predicate;

public class Data {

    private Collection<Flight> flights;
    private Collection<Route>  routes;
    public  Predicate<String> predicate;

    public Data(Collection<Flight> flights, Collection<Route> routes) {

        this.flights = flights;
        this.routes = routes;
    }

    public Collection<Flight> getFlights() {

        return flights;
    }

    public void setFlights(Collection<Flight> flights) {

        this.flights = flights;
    }

    public Collection<Route> getRoutes() {

        return routes;
    }

    public void setRoutes(Collection<Route> routes) {
        this.routes = routes;
    }
}

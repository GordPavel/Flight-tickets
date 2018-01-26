package transport;

import model.Flight;
import model.Route;

import java.util.Collection;
import java.util.Map;

/**
 Class for sending data range
 */

public class Data{

    private Collection<Flight>  flights;
    private Collection<Route>   routes;
    private Map<String, String> bases;


    public Data( Collection<Flight> flights , Collection<Route> routes ){

        this.flights = flights;
        this.routes = routes;
    }

    public Collection<Flight> getFlights(){

        return flights;
    }

    public void setFlights( Collection<Flight> flights ){

        this.flights = flights;
    }

    public Collection<Route> getRoutes(){

        return routes;
    }

    public void setRoutes( Collection<Route> routes ){
        this.routes = routes;
    }

    public Map<String, String> getBases(){
        return bases;
    }

    public void setBases( Map<String, String> bases ){
        this.bases = bases;
    }


}

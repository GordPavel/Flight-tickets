package transport;

import exceptions.FlightAndRouteException;
import model.Flight;
import model.Route;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 Class for sending data range
 */

public class Data{
    private Collection<Route>       routes;
    private Collection<Flight>      flights;
    private Map<String, String>     bases;
    private List<ListChangeAdapter> changes;
    private FlightAndRouteException exception;

    public Data(){
    }

    public Collection<Route> getRoutes(){
        return routes;
    }

    public void setRoutes( Collection<Route> routes ){
        this.routes = routes;
    }

    public Collection<Flight> getFlights(){
        return flights;
    }

    public void setFlights( Collection<Flight> flights ){
        this.flights = flights;
    }

    public Map<String, String> getBases(){
        return bases;
    }

    public void setBases( Map<String, String> bases ){
        this.bases = bases;
    }

    public void setException( FlightAndRouteException exception ){
        this.exception = exception;
    }

    public FlightAndRouteException getException(){
        return exception;
    }

    public boolean notHasException(){
        return ( exception == null );
    }

    public List<ListChangeAdapter> getChanges(){
        return changes;
    }

    public void setChanges( List<ListChangeAdapter> changes ){
        this.changes = changes;
    }

}

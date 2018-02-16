package transport;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import exceptions.FlightAndRouteException;
import model.Flight;
import model.Route;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 Class for sending data range
 */

public class Data{
    private Collection<Route>       routes;
    private Collection<Flight>      flights;
    private Map<String, String>     bases;
    private List<ListChangeAdapter> changes;
    @JsonSerialize( using = ExceptionSerializer.class )
    @JsonDeserialize( using = ExceptionDeserializer.class )
    private FlightAndRouteException exception;

    public Data(){
    }

    public Data( Collection<Route> routes , Collection<Flight> flights ){
        this.routes = routes;
        this.flights = flights;
    }

    public Data( List<ListChangeAdapter> changes ){
        this.changes = changes;
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

    public List<ListChangeAdapter> getChanges(){
        return changes;
    }

    public void setChanges( List<ListChangeAdapter> changes ){
        this.changes = changes;
    }

    public FlightAndRouteException getException(){
        return exception;
    }

    public void setException( FlightAndRouteException exception ){
        this.exception = exception;
    }

    public void withoutExceptionOrWith( Consumer<Data> allRight , Consumer<FlightAndRouteException> withException ){
        if( exception == null ){
            allRight.accept( this );
        }else{
            withException.accept( exception );
        }
    }

    public Boolean hasNotException(){
        return exception == null;
    }
}

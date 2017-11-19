package model;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 Util class to manage routes and flights data. It looks after correct of this data, can export and import to binary
 files, merge two different databases in one. Singleton class to not allow different databases in one app.
 Concurrency saved.
 */
public class DataModel{

    /**
     Default constructor
     */
    private DataModel(){
    }

    private static class InstanceHolder{
        private static final DataModel instance = new DataModel();
    }

    public synchronized DataModel getInstance(){
        return InstanceHolder.instance;
    }

    /**
     *  Stores all flights, do
     */
    private CopyOnWriteArraySet<Flight> flights = new CopyOnWriteArraySet<>();

    /**
     *
     */
    private Set<Route> routes = new CopyOnWriteArraySet<>();


    /**
     @param predicate
     */
    public List<Flight> listFlightsWithPredicate( Predicate<Flight> predicate ){
        return flights.stream().filter( predicate ).collect( Collectors.toList() );
    }

    /**
     @param flight
     */
    public Boolean addFlight( Flight flight ){
        return flights.add( flight );
    }

    /**
     @param number
     */
    public Boolean removeFlight( String number ){
        return flights.removeIf( flight -> flight.getNumber().equals( number ) );
    }

    /**
     @param flight
     */
    public Boolean editFlight( Flight flight ){
        // TODO implement here
    }

    /**
     @param predicate
     */
    public Set<Route> listRoutesWithPredicate( Predicate<Route> predicate ){
        return routes.stream().filter( predicate ).collect( Collectors.toSet() );
    }

    /**
     @param route
     */
    public Boolean addRoute( Route route ){
        return routes.add( route );
    }

    /**
     @param route
     */
    public Boolean removeRoute( Route route ){
        return routes.remove( route );
    }

    /**
     @param route
     */
    public void editRoute( Route route ){
        // TODO implement here
    }

    /**
     @param file
     */
    public void importToFile( File file ){
        // TODO implement here
    }

    /**
     @param file
     */
    public void exportToFile( File file ){
        // TODO implement here
    }

    /**
     @param main
     @param additional
     */
    public void mergeData( File main , File additional ){
        // TODO implement here
    }
}
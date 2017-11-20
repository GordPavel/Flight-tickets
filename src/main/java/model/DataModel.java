package model;

import java.io.File;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
     Stores all flights, do
     */
    private Set<Flight> flights = new CopyOnWriteArraySet<>();

    /**
     *
     */
    private Set<Route> routes = new CopyOnWriteArraySet<>();


    /**
     @param predicate specifies flights what to choose

     @return specified flights
     */
    public Stream<Flight> listFlightsWithPredicate( Predicate<Flight> predicate ){
        return flights.stream().filter( predicate );
    }

    /**
     @param flight create new flight, which has unique number, instead it won't be added

     @return true , if flight was added, false in other case
     */
    public Boolean addFlight( Flight flight ){
        return flights.add( flight );
    }

    /**
     @param number number of flight to be removed

     @return true , if this flight was removed, false in other case
     */
    public Boolean removeFlight( String number ){
        return flights.removeIf( flight -> flight.getNumber().equals( number ) );
    }

    /**
     @param flight specify the number of flight, that you want to edit. Other attributes could not match with old
     version. Editing doesn't allow the same route and planeId, arriveDate and departureDate because in this case
     it'll duplicate another flight. So this flight won't be edited.

     @return true , if database exists flight with specified number and new data doesn't duplicate another flights.
     false in other case
     */
    public Boolean editFlight( Flight flight ){
        Optional<Flight> flightOptional =
                listFlightsWithPredicate( baseFlight -> baseFlight.getNumber().equals( flight.getNumber() ) )
                        .findFirst();
        if( !flightOptional.isPresent() ) return false;
        Optional<Flight> anyDuplicatedFlight = listFlightsWithPredicate(
                baseFlight -> baseFlight.getRoute().equals( flight.getRoute() ) &&
                              baseFlight.getPlaneID().equals( flight.getPlaneID() ) &&
                              baseFlight.getArriveDate().equals( flight.getArriveDate() ) &&
                              baseFlight.getDepartureDate().equals( flight.getDepartureDate() ) ).findAny();
        if( anyDuplicatedFlight.isPresent() ) return false;
        Flight editingFLight = flightOptional.get();
        editingFLight.setRoute( flight.getRoute() );
        editingFLight.setPlaneID( flight.getPlaneID() );
        editingFLight.setArriveDate( flight.getArriveDate() );
        editingFLight.setDepartureDate( flight.getDepartureDate() );
        return true;
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
     @param additional
     */
    public void mergeData( File additionalData ){
        // TODO implement here
    }
}

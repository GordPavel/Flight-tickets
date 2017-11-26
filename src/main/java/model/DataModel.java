package model;

import exceptions.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 Util class to manage routes and flights data. It looks after correct of this data, can export and import to binary
 files, merge two different databases in one. Singleton class to not allow different databases in one app.
 Concurrency saved.

 @author pavelgordeev */
public class DataModel{
    /**
     Default constructor
     */
    DataModel(){
    }

    private static class InstanceHolder{
        private static final DataModel instance = new DataModel();
    }

    public static synchronized DataModel getInstance(){
        return InstanceHolder.instance;
    }

    /**
     Stores all flights, do
     */
    private CopyOnWriteArrayList<Flight> flights = new CopyOnWriteArrayList<>();

    /**
     *
     */
    private CopyOnWriteArrayList<Route> routes = new CopyOnWriteArrayList<>();

    /**
     List all unique airport, that stores in routes

     @param predicate specifies names of all airports

     @return specified names of airports
     */
    public Stream<String> listAllAirportsWithPredicate( Predicate<String> predicate ){
        Stream<String> from = routes.stream().map( Route::getFrom );
        Stream<String> to   = routes.stream().map( Route::getTo );
        return Stream.concat( from , to ).distinct().sorted().filter( predicate );
    }

    /**
     List all unique planes ID, that stores in flights

     @param predicate specifies names of all planes' IDs

     @return specified IDs of planes
     */
    public Stream<String> listAllPlanesWithPredicate( Predicate<String> predicate ){
        return flights.stream().map( Flight::getPlaneID ).distinct().sorted().filter( predicate );
    }

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

     @throws FaRDateMismatchException if flight has incorrect dates
     @throws FaRNotRelatedData        it has route, that doesn't exist in database
     @throws FaRSameNameException     it duplicates in (planeID && route && arrive date&& departure date ) or
     duplicates number
     */
    public Boolean addFlight( Flight flight ) throws FlightAndRouteException{
        if( flight.getArriveDate().before( flight.getDepartureDate() ) ){
            throw new FaRDateMismatchException( "Flight has incorrect dates" );
        }
        Pattern pattern = Pattern.compile( "[\\w\\d[^\\s .,?*!]]*" );
        if( !pattern.matcher( flight.getNumber() ).matches() || !pattern.matcher( flight.getPlaneID() ).matches() ){
            throw new FaRUnacceptableSymbolException( "Illegal symbols" );
        }
        if( flights.stream().anyMatch( flight1 -> flight1.getNumber().equals( flight.getNumber() ) ) ){
            throw new FaRSameNameException( "Flight's numbers duplicates someone from database" );
        }
        if( flights.stream().anyMatch( flight1 -> Objects.equals( flight1.getRoute() , flight.getRoute() ) &&
                                                  Objects.equals( flight1.getPlaneID() , flight.getPlaneID() ) &&
                                                  Objects.equals( flight1.getArriveDate() , flight.getArriveDate() ) &&
                                                  Objects.equals( flight1.getDepartureDate() ,
                                                                  flight.getDepartureDate() ) ) ){
            throw new FaRSameNameException( "New flight duplicates someone another" );
        }
        if( routes.stream().noneMatch( route -> route.getId().equals( flight.getRoute().getId() ) ) ){
            throw new FaRNotRelatedData( "Flight's routes doesn't exists in database" );
        }
        return flights.addIfAbsent( flight );
    }

    /**
     @param number number of flight to be removed

     @return true , if this flight was removed, false in other case
     */
    public Boolean removeFlight( String number ){
        return flights.removeIf( flight -> Objects.equals( flight.getNumber() , number ) );
    }

    /**
     Use this to set any fields in flight

     @param flight           specify the flight, that you want to edit. if flight has incorrect data, it won't be added.
     @param newRoute         new route to change. if null, value win't be changed
     @param newPlaneId       new plane ID to change. if null, value win't be changed
     @param newDepartureDate new departure date to change. if null, value win't be changed
     @param newArriveDate    new arrive date to change. if null, value win't be changed

     @return true , if database exists flight with specified number and new data doesn't duplicate another flights.
     false in other case

     @throws FaRDateMismatchException if flight has incorrect dates
     @throws FaRIllegalEditedData     previous version of this flight doesn't exist in database
     @throws FaRNotRelatedData        it has route, that doesn't exist in database
     @throws FaRSameNameException     it   duplicates in ( planeID && route && arrive date && departure date ).
     */
    public Boolean editFlight( Flight flight , Route newRoute , String newPlaneId , Date newDepartureDate ,
                               Date newArriveDate ) throws FlightAndRouteException{
        if( ( newArriveDate != null ? newArriveDate : flight.getArriveDate() )
                .before( newDepartureDate != null ? newDepartureDate : flight.getDepartureDate() ) ){
            throw new FaRDateMismatchException( "Flight has incorrect dates" );
        }
        Pattern pattern = Pattern.compile( "[\\w\\d[^\\s .,*?!]]*" );
        if( !pattern.matcher( flight.getNumber() ).matches() || !pattern.matcher( flight.getPlaneID() ).matches() ){
            throw new FaRUnacceptableSymbolException( "Illegal symbols" );
        }
        if( routes.stream().noneMatch(
                route -> route.getId().equals( newRoute != null ? newRoute.getId() : flight.getRoute().getId() ) ) ){
            throw new FaRNotRelatedData( "Flight's routes doesn't exists in database" );
        }
        Optional<Flight> flightOptional =
                flights.stream().filter( flight1 -> flight1.getNumber().equals( flight.getNumber() ) ).findAny();
        if( !flightOptional.isPresent() ){
            throw new FaRIllegalEditedData(
                    String.format( "Flight with number %s doesn't consists" , flight.getNumber() ) );
        }
        if( flights.stream().anyMatch(
                flight1 -> Objects.equals( flight1.getRoute() , newRoute != null ? newRoute : flight.getRoute() ) &&
                           Objects.equals( flight1.getPlaneID() ,
                                           newPlaneId != null ? newPlaneId : flight.getPlaneID() ) &&
                           Objects.equals( flight1.getArriveDate() ,
                                           newArriveDate != null ? newArriveDate : flight.getArriveDate() ) &&
                           Objects.equals( flight1.getDepartureDate() , newDepartureDate != null ? newDepartureDate :
                                                                        flight.getDepartureDate() ) ) ){
            throw new FaRSameNameException( "New flight duplicates someone another" );
        }
        Flight editedFLight = flightOptional.get();
        editedFLight.setRoute( newRoute != null ? newRoute : flight.getRoute() );
        editedFLight.setPlaneID( newPlaneId != null ? newPlaneId : flight.getPlaneID() );
        editedFLight.setArriveDate( newArriveDate != null ? newArriveDate : flight.getArriveDate() );
        editedFLight.setDepartureDate( newDepartureDate != null ? newDepartureDate : flight.getDepartureDate() );
        return true;
    }


    /**
     @param predicate to specify the routes that to choose

     @return specified routes
     */
    public Stream<Route> listRoutesWithPredicate( Predicate<Route> predicate ){
        return routes.stream().filter( predicate );
    }

    private Iterator<Integer> routesIdIterator =
            IntStream.range( routes.stream().mapToInt( Route::getId ).max().orElse( 0 ) + 1 , Integer.MAX_VALUE )
                     .iterator();

    /**
     @param route unique route to be added

     @return true , if route's unique and was added, false instead

     @throws FaRSameNameException if new route's arrival and departure points duplicate someone another in database
     */
    public Boolean addRoute( Route route ){
        Pattern pattern = Pattern.compile( "[\\w\\d[^\\s .,*?!]]*" );
        if( !pattern.matcher( route.getFrom() ).matches() || !pattern.matcher( route.getTo() ).matches() ){
            throw new FaRUnacceptableSymbolException( "Illegal symbols" );
        }
        route.setId( routesIdIterator.next() );
        if( routes.stream().anyMatch(
                route1 -> route1.getFrom().equals( route.getFrom() ) && route1.getTo().equals( route.getTo() ) ) ){
            throw new FaRSameNameException( "This new route duplicates someone another" );
        }
        return routes.addIfAbsent( route );
    }

    /**
     @param route to remove

     @return true , if database contains him and deleted, false instead
     */
    public Boolean removeRoute( Route route ){
        Optional<Route> routeOptional =
                routes.stream().filter( route1 -> Objects.equals( route.getId() , route1.getId() ) ).findFirst();
        if( !routeOptional.isPresent() ) return false;
        Route removingRoute = routeOptional.get();
        flights.removeIf( flight -> Objects.equals( flight.getRoute() , removingRoute ) );
        routes.remove( removingRoute );
        return true;
    }

    /**
     Use this method instead Route.setFrom() or Route.setTo()

     @param route                 edited route.
     @param newArrivalAirport     new value of arrival airport. if it's null, value won't change
     @param newDestinationAirport new value of departure airport. if it's null, value won't change

     @return true , if it has correct data and doesn't duplicate someone another, false instead.

     @throws FaRIllegalEditedData if database doesn't contain previous version of route
     @throws FaRSameNameException it duplicates someone
     another
     */
    public Boolean editRoute( Route route , String newArrivalAirport , String newDestinationAirport ){
        Pattern pattern = Pattern.compile( "[\\w\\d[^\\s .,*?!]]*" );
        if( !pattern.matcher( route.getFrom() ).matches() || !pattern.matcher( route.getTo() ).matches() ){
            throw new FaRUnacceptableSymbolException( "Illegal symbols" );
        }
        Optional<Route> routeOptional =
                routes.stream().filter( route1 -> Objects.equals( route.getId() , route1.getId() ) ).findFirst();
        if( !routeOptional.isPresent() ){
            throw new FaRIllegalEditedData( "This route doesn't contains in database" );
        }
        if( routes.stream().anyMatch( route1 -> Objects.equals( route1.getFrom() ,
                                                                newArrivalAirport != null ? newArrivalAirport :
                                                                route.getFrom() ) && Objects.equals( route1.getTo() ,
                                                                                                     newDestinationAirport !=
                                                                                                     null ?
                                                                                                     newDestinationAirport :
                                                                                                     route.getTo() ) ) ){
            throw new FaRSameNameException( "This new route duplicates someone another" );
        }
        Route editedRoute = routeOptional.get();
        editedRoute.setFrom( newArrivalAirport != null ? newArrivalAirport : route.getFrom() );
        editedRoute.setTo( newDestinationAirport != null ? newDestinationAirport : route.getTo() );
        return true;
    }

    /**
     Deserialize data from file, swap data contains in RAM to data from file. This method doesn't merge RAM and file
     data, like when you just open another file. Method closes stream.

     @param file that contains serialized data.

     @throws IllegalArgumentException if file contains not just flights and routes
     @throws FaRSameNameException     if file has data duplicates
     @throws FaRNotRelatedData        flight has route that doesn't exist in this file
     @throws IOException              If other I/O error has occurred.
     */
    public void importFromFile( File file ) throws IOException, FlightAndRouteException{
        List<Serializable> data = deserializeData( file );
        try{
            if( !data.stream().allMatch( serializable -> serializable.getClass().equals( Flight.class ) ||
                                                         serializable.getClass().equals( Route.class ) ) ){
                throw new IllegalArgumentException( "File must contain just flights and routes" );
            }
            Map<Boolean, List<Serializable>> flightsAndRoutes =
                    data.stream().collect( Collectors.partitioningBy( item -> item instanceof Flight ) );
            List<Route> tempRoutes = new ArrayList<>();
            flightsAndRoutes.get( false ).stream().map( serializable -> ( Route ) serializable ).forEach( route -> {
                if( !tempRoutes.contains( route ) ){
                    tempRoutes.add( route );
                }else{
                    throw new FaRSameNameException( "Duplicates routes" );
                }
            } );
            List<Flight> tempFlights = new ArrayList<>();
            flightsAndRoutes.get( true ).stream().map( serializable -> ( Flight ) serializable ).forEach( flight -> {
                if( tempFlights.contains( flight ) ) throw new FaRSameNameException( "Duplicate flights" );
                if( !tempRoutes.contains( flight.getRoute() ) ){
                    throw new FaRNotRelatedData( "Flight's route doesn't exist in file" );
                }
                tempFlights.add( flight );
            } );
            routes = new CopyOnWriteArrayList<>( tempRoutes );
            flights = new CopyOnWriteArrayList<>( tempFlights );
        }catch( IllegalArgumentException | FlightAndRouteException e ){
            throw new IllegalArgumentException( "File contains illegal data" , e );
        }
    }

    /**
     This method try to add each item of data in this file. If it can't, it puts this to collection to give a report
     of fail of adding. Clear data will be added.
     Method closes stream.

     @param additionalData file with data to be merged with current data

     @return collection of data, that can't been added.

     @throws IOException If other I/O error has occurred.
     */
    public Collection<Serializable> mergeData( File additionalData ) throws IOException, FlightAndRouteException{
        List<Serializable> data       = deserializeData( additionalData );
        List<Serializable> failedData = new ArrayList<>();
        try{
            if( !data.stream().allMatch( serializable -> serializable.getClass().equals( Flight.class ) ||
                                                         serializable.getClass().equals( Route.class ) ) ){
                throw new IllegalArgumentException( "File must contain just flights and routes" );
            }
            Map<Boolean, List<Serializable>> flightsAndRoutes =
                    data.stream().collect( Collectors.partitioningBy( item -> item instanceof Flight ) );
            flightsAndRoutes.get( false ).stream().map( serializable -> ( Route ) serializable ).forEach( route -> {
                try{
                    if( !addRoute( route ) ) failedData.add( route );
                }catch( FaRSameNameException e ){
                    failedData.add( route );
                }
            } );
            flightsAndRoutes.get( true ).stream().map( serializable -> ( Flight ) serializable ).forEach( flight -> {
                try{
                    if( !addFlight( flight ) ) failedData.add( flight );
                }catch( FlightAndRouteException e ){
                    failedData.add( flight );
                }
            } );
        }catch( IllegalArgumentException | FlightAndRouteException e ){
            throw new IllegalArgumentException( "File contains illegal data" , e );
        }
        return failedData;
    }

    private List<Serializable> deserializeData( File file ) throws IOException{
        List<Serializable> data = new ArrayList<>();
        try( ObjectInputStream objectInputStream = new ObjectInputStream( new FileInputStream( file ) ) ){
            int size = objectInputStream.readInt();
            int i    = 0;
            while( i++ < size ){
                Serializable ser = ( Serializable ) objectInputStream.readObject();
                data.add( ser );
            }
        }catch( ClassNotFoundException e ){
            throw new IllegalArgumentException( "File contains illegal data" , e );
        }
        return data;
    }

    /**
     Look at method {@code void exportSpecifiedData( Collection<Serializable> , File )}. Export all data in database
     */
    public void exportToFile( File file ) throws IOException{
        List<Serializable> data = new ArrayList<>();
        data.addAll( flights );
        data.addAll( routes );
        exportSpecifiedData( data , file );
    }

    /**
     Serialize collected data from RAM to computer's storage ( NvRAM ).Serialize just Flight and Route classes.
     Method closes stream.

     @param data collection of data to serialize
     @param file where serialize the data

     @throws IllegalArgumentException if collection contains not just flights and routes
     */
    public void exportSpecifiedData( Collection<Serializable> data , File file ) throws IOException{
        if( data.parallelStream().anyMatch( serializable -> !isFLightOrRoute( serializable ) ) ){
            throw new IllegalArgumentException( "Can't serialize not Flight or Route classes" );
        }
        try( ObjectOutputStream objectOutputStream = new ObjectOutputStream( new FileOutputStream( file ) ) ){
            objectOutputStream.writeInt( data.size() );
            for( Serializable serializable : data ){
                objectOutputStream.writeObject( serializable );
            }
        }
    }

    private Boolean isFLightOrRoute( Serializable d ){
        return d.getClass().equals( Flight.class ) || d.getClass().equals( Route.class );
    }

    void clear(){
        routes = new CopyOnWriteArrayList<>();
        flights = new CopyOnWriteArrayList<>();
    }
}

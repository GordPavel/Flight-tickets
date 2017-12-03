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

    private Pattern legalSymbolsChecker = Pattern.compile( "[\\w\\d[^\\s .,*?!]]+" );

    /**
     @param flight create new flight, which has unique number, instead it won't be added

     @return true , if flight was added, false in other case

     @throws FaRUnacceptableSymbolException if flight has illegal symbols
     @throws FaRDateMismatchException       if flight has incorrect dates
     @throws FaRNotRelatedData              it has route, that doesn't exist in database
     @throws FaRSameNameException           it duplicates in (planeID && route && ( departure date || arrive date ) ) or
     <p>
     duplicates number
     */
    public Boolean addFlight( Flight flight ) throws FlightAndRouteException{
        if( !( legalSymbolsChecker.matcher( flight.getNumber() ).matches() &&
               legalSymbolsChecker.matcher( flight.getPlaneID() ).matches() ) ){
            throw new FaRUnacceptableSymbolException( "Flights has illegal symbols" );
        }
        if( !flight.getDepartureDate().before( flight.getArriveDate() ) ){
            throw new FaRDateMismatchException( "Flight has incorrect dates" );
        }
        if( flights.stream().anyMatch( flight1 -> Objects.equals( flight1.getNumber() , flight.getNumber() ) ) ){
            throw new FaRSameNameException( "Flight duplicates someone's number" );
        }
        if( routes.stream().noneMatch( route -> Objects.equals( route.getId() , flight.getRoute().getId() ) ) ){
            throw new FaRNotRelatedData( "FLight's route doesn't exist in database" );
        }
        if( flights.stream().anyMatch( flight1 -> Objects.equals( flight1.getPlaneID() , flight.getPlaneID() ) &&
                                                  Objects.equals( flight1.getRoute() , flight.getRoute() ) &&
                                                  ( Objects.equals( flight1.getDepartureDate() ,
                                                                    flight.getDepartureDate() ) ||
                                                    Objects.equals( flight1.getArriveDate() ,
                                                                    flight.getArriveDate() ) ) ) ){
            throw new FaRSameNameException( "Flight duplicates someone from database" );
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
     @throws FaRSameNameException     it duplicates in ( planeID && route && arrive date && departure date ).
     */
    public Boolean editFlight( Flight flight , Route newRoute , String newPlaneId , Date newDepartureDate ,
                               Date newArriveDate ) throws FlightAndRouteException{
        if( !legalSymbolsChecker.matcher( newPlaneId != null ? newPlaneId : flight.getPlaneID() ).matches() ){
            throw new FaRUnacceptableSymbolException( "Flights has illegal symbols" );
        }
        if( !( newDepartureDate != null ? newDepartureDate : flight.getDepartureDate() )
                .before( newArriveDate != null ? newArriveDate : flight.getArriveDate() ) ){
            throw new FaRDateMismatchException( "Flight has incorrect dates" );
        }
        if( routes.stream().noneMatch( route -> Objects
                .equals( route.getId() , ( newRoute != null ? newRoute : flight.getRoute() ).getId() ) ) ){
            throw new FaRNotRelatedData( "FLight's route doesn't exist in database" );
        }
        if( flights.stream().anyMatch( flight1 -> Objects.equals( flight1.getPlaneID() ,
                                                                  newPlaneId != null ? newPlaneId :
                                                                  flight.getPlaneID() ) &&
                                                  Objects.equals( flight1.getRoute() ,
                                                                  newRoute != null ? newRoute : flight.getRoute() ) &&
                                                  ( Objects.equals( flight1.getDepartureDate() ,
                                                                    newDepartureDate != null ? newDepartureDate :
                                                                    flight.getDepartureDate() ) ||
                                                    Objects.equals( flight1.getArriveDate() ,
                                                                    newArriveDate != null ? newArriveDate :
                                                                    flight.getArriveDate() ) ) ) ){
            throw new FaRSameNameException( "Flight duplicates someone from database" );
        }
        Flight editingFlight =
                flights.stream().filter( flight1 -> Objects.equals( flight1.getNumber() , flight.getNumber() ) )
                       .findFirst().orElseThrow(
                        () -> new FaRIllegalEditedData( "Database doesn't contain previous version of flight" ) );
        editingFlight.setPlaneID( newPlaneId != null ? newPlaneId : flight.getPlaneID() );
        editingFlight.setRoute( newRoute != null ? newRoute : flight.getRoute() );
        editingFlight.setDepartureDate( newDepartureDate != null ? newDepartureDate : flight.getDepartureDate() );
        editingFlight.setArriveDate( newArriveDate != null ? newArriveDate : flight.getArriveDate() );
        return true;
    }


    /**
     @param predicate to specify the routes that to choose

     @return specified routes
     */
    public Stream<Route> listRoutesWithPredicate( Predicate<Route> predicate ){
        return routes.stream().filter( predicate );
    }

    /**
     @param route unique route to be added

     @return true , if route's unique and was added, false instead

     @throws FaRSameNameException           if new route's arrival and departure points duplicate someone another in database
     @throws IllegalArgumentException       if departure and destination airports are similar
     @throws FaRUnacceptableSymbolException if airports name contain illegal symbols
     */
    public Boolean addRoute( Route route ){
        if( route.getFrom().equals( route.getTo() ) ){
            throw new IllegalArgumentException( "Departure and destination airports are similar" );
        }
        if( !( legalSymbolsChecker.matcher( route.getFrom() ).matches() &&
               legalSymbolsChecker.matcher( route.getTo() ).matches() ) ){
            throw new FaRUnacceptableSymbolException( "Illegal symbols" );
        }
        if( routes.contains( route ) ){
            throw new FaRSameNameException( "Route duplicates someone from current database" );
        }
        route.setId( routesPrimaryKeysGenerator.next() );
        return routes.addIfAbsent( route );
    }

    private Iterator<Integer> routesPrimaryKeysGenerator = IntStream.rangeClosed( 1 , Integer.MAX_VALUE ).iterator();

    /**
     @param route to remove

     @return true , if database contains him and deleted, false instead
     */
    public Boolean removeRoute( Route route ){
        flights.removeIf( flight -> Objects.equals( flight.getRoute().getId() , route.getId() ) );
        return routes.remove( route );
    }

    /**
     Use this method instead Route.setFrom() or Route.setTo()

     @param route                 edited route.
     @param newDepartureAirport   new value of arrival airport. if it's null, value won't change
     @param newDestinationAirport new value of departure airport. if it's null, value won't change

     @return true , if it has correct data and doesn't duplicate someone another, false instead.

     @throws FaRIllegalEditedData if database doesn't contain previous version of route
     @throws FaRSameNameException it duplicates someone
     another
     */
    public Boolean editRoute( Route route , String newDepartureAirport , String newDestinationAirport ){
        Route editingRoute =
                routes.stream().filter( route1 -> Objects.equals( route1.getId() , route.getId() ) ).findFirst()
                      .orElseThrow(
                              () -> new FaRIllegalEditedData( "Database doesn't contain previous version of route" ) );
        if( route.getId() == null ){
            throw new FaRIllegalEditedData( "Database doesn't contain previous version of route" );
        }
        if( routes.stream().filter( route1 -> ( newDepartureAirport != null ? newDepartureAirport : route.getFrom() )
                                                      .equals( route1.getFrom() ) &&
                                              ( newDestinationAirport != null ? newDestinationAirport : route.getTo() )
                                                      .equals( route1.getTo() ) ).count() > 0 ){
            throw new FaRSameNameException( "New item duplicates someone" );
        }
        editingRoute.setFrom( newDepartureAirport != null ? newDepartureAirport : route.getFrom() );
        editingRoute.setTo( newDestinationAirport != null ? newDestinationAirport : route.getTo() );
        return true;
    }

    /**
     Deserialize data from file, swap data contains in RAM to data from file. This method doesn't merge RAM and file
     data, like when you just open another file.

     @param file that contains serialized data.

     @throws IllegalArgumentException if file contains not just flights and routes
     @throws FaRSameNameException     if file has data duplicates
     @throws FaRNotRelatedData        flight has route that doesn't exist in this file
     @throws IOException              If other I/O error has occurred.
     */
    public void importFromFile( File file ) throws IOException, FlightAndRouteException{
        Map<Boolean, List<Serializable>> routesAndFlights = deserializeData( file );
        List<Route> routes =
                routesAndFlights.get( true ).parallelStream().map( Route.class::cast ).collect( Collectors.toList() );
        List<Flight> flights =
                routesAndFlights.get( false ).parallelStream().map( Flight.class::cast ).collect( Collectors.toList() );
//        check routes duplicate
        Set<Route> routeSet = new HashSet<>();
        if( !routes.stream().allMatch( routeSet::add ) ){
            throw new FaRSameNameException( "Routes set contains duplicate" );
        }
        Set<Flight> flightSet = new HashSet<>();
        if( !flights.stream().allMatch( flightSet::add ) ){
            throw new FaRSameNameException( "Flights set contains duplicate" );
        }
        if( !flights.stream().map( Flight::getRoute ).allMatch( routes::contains ) ){
            throw new FaRNotRelatedData( "One flight has route not from database" );
        }
        routesPrimaryKeysGenerator = IntStream
                .rangeClosed( routes.stream().mapToInt( Route::getId ).max().orElse( 0 ) + 1 , Integer.MAX_VALUE )
                .iterator();
        this.routes = new CopyOnWriteArrayList<>( routes );
        this.flights = new CopyOnWriteArrayList<>( flights );
    }

    /**
     This method try to add each item of data in this file. If it can't, it puts this to collection to give a report
     of fail of adding. Clear data will be added.

     @param additionalData file with data to be merged with current data

     @return collection of data, that can't been added.

     @throws IOException If other I/O error has occurred.
     */
    public Collection<Serializable> mergeData( File additionalData ) throws IOException, FlightAndRouteException{
        Map<Boolean, List<Serializable>> routesAndFlights = deserializeData( additionalData );
        List<Route>                      failedRoutes     = new ArrayList<>();
        List<Flight>                     failedFlights    = new ArrayList<>();
        List<Route> routes =
                routesAndFlights.get( true ).parallelStream().map( Route.class::cast ).collect( Collectors.toList() );
        List<Flight> flights =
                routesAndFlights.get( false ).parallelStream().map( Flight.class::cast ).collect( Collectors.toList() );
        Set<Route>  routeSet  = new HashSet<>( this.routes );
        Set<Flight> flightSet = new HashSet<>( this.flights );
        routes.forEach( route -> {
            if( !routeSet.add( route ) ){
                failedRoutes.add( route );
            }
        } );
        routes.removeAll( failedRoutes );
        flights.forEach( flight -> {
            if( !routeSet.contains( flight.getRoute() ) ){
                failedFlights.add( flight );
                return;
            }
            if( !flightSet.add( flight ) ){
                failedFlights.add( flight );
            }
        } );
        flights.removeAll( failedFlights );
        routes.forEach( route -> route.setId( routesPrimaryKeysGenerator.next() ) );
        this.routes.addAll( routes );
        this.flights.addAll( flights );
        return Stream.concat( failedRoutes.stream() , failedFlights.stream() ).collect( Collectors.toList() );
    }

    private Map<Boolean, List<Serializable>> deserializeData( File file ) throws IOException{
        try( ObjectInput input = new ObjectInputStream( new FileInputStream( file ) ) ){
            int                i    = 0, size = input.readInt();
            List<Serializable> data = new ArrayList<>();
            while( i++ < size ){
                data.add( ( Serializable ) input.readObject() );
            }
            if( !data.parallelStream().allMatch( serializable -> serializable.getClass().equals( Route.class ) ||
                                                                 serializable.getClass().equals( Flight.class ) ) ){
                throw new IllegalArgumentException( "One object neither Route, nor Flight class" );
            }
            return data.stream().collect(
                    Collectors.partitioningBy( serializable -> serializable.getClass().equals( Route.class ) ) );
        }catch( ClassNotFoundException e ){
            throw new IllegalArgumentException( "File contains illegal data" , e );
        }
    }

    /**
     Look at method {@code void exportSpecifiedData( Collection<Serializable> , File )}. Export all data in database
     */
    public void exportToFile( File file ) throws IOException{
        exportSpecifiedData(
                Stream.concat( this.routes.stream() , this.flights.stream() ).collect( Collectors.toList() ) , file );
    }

    /**
     Serialize collected data from RAM to computer's storage ( NvRAM ).Serialize just Flight and Route classes.
     Method closes stream.

     @param data collection of data to serialize
     x   @param file where serialize the data

     @throws IllegalArgumentException if collection contains not just flights and routes
     */
    public void exportSpecifiedData( Collection<Serializable> data , File file ) throws IOException{
        if( !data.parallelStream().allMatch( serializable -> serializable.getClass().equals( Route.class ) ||
                                                             serializable.getClass().equals( Flight.class ) ) ){
            throw new IllegalArgumentException( "Can't serialize not Flight or Route classes" );
        }
        try( ObjectOutput output = new ObjectOutputStream( new FileOutputStream( file ) ) ){
            output.writeInt( data.size() );
            for( Serializable item : data ){
                output.writeObject( item );
            }
        }
    }

    public void clear(){
        routes = new CopyOnWriteArrayList<>();
        flights = new CopyOnWriteArrayList<>();
        routesPrimaryKeysGenerator = IntStream.rangeClosed( 1 , Integer.MAX_VALUE ).iterator();
    }
}

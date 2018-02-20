package model;

import exceptions.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.io.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantReadWriteLock;
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
    private final ReentrantReadWriteLock flightsLock                = new ReentrantReadWriteLock( true );
    private final ObservableList<Flight> flights                    = FXCollections.observableList( new ArrayList<>() );
    private final ReentrantReadWriteLock routesLock                 = new ReentrantReadWriteLock( true );
    private final ObservableList<Route>  routes                     = FXCollections.observableList( new ArrayList<>() );
    private final Pattern                legalSymbolsChecker        = Pattern.compile( "[\\w\\d[^\\s .,*?!]]+" );
    private final Semaphore              keysGeneratorSemaphore     = new Semaphore( 1 , true );
    private       Iterator<Integer>
                                         routesPrimaryKeysGenerator =
            IntStream.rangeClosed( 1 , Integer.MAX_VALUE ).iterator();

    DataModel(){
    }

    /**
     Methods to manage listeners for each collection
     */
    public void addFlightsListener( ListChangeListener<Flight> listener ){
        flights.addListener( listener );
    }

    public void removeFlightsListener( ListChangeListener<Flight> listener ){
        flights.removeListener( listener );
    }

    public void addRoutesListener( ListChangeListener<Route> listener ){
        routes.addListener( listener );
    }

    public void removeRoutesListener( ListChangeListener<Route> listener ){
        routes.removeListener( listener );
    }

    public ObservableList<Flight> getFlightObservableList(){
        return flights;
    }

    public ObservableList<Route> getRouteObservableList(){
        return routes;
    }

    /**
     Add new flight in current database, if it's correct, and immediately saves all changes

     @param flight create new flight, which has unique number, instead it won't be added

     @throws FaRUnacceptableSymbolException if flight has illegal symbols
     @throws FaRDateMismatchException       if flight has incorrect dates
     @throws FaRNotRelatedData              it has route, that doesn't exist in database
     @throws FaRSameNameException           it duplicates in (planeID && route && ( departure date || arrive date ) ) or
     <p>
     duplicates number
     */
    public void addFlight( Flight flight ) throws FlightAndRouteException{
        //        Check legal symbols in data
        if( !( legalSymbolsChecker.matcher( flight.getNumber() ).matches() &&
               legalSymbolsChecker.matcher( flight.getPlaneID() ).matches() ) ){
            throw new FaRUnacceptableSymbolException( "Flights has illegal symbols" );
        }
//        Check departure and arrival times of flight
        if( flight.getTravelTime() <= 0 ){
            throw new FaRDateMismatchException( "Flight has incorrect dates" );
        }
        try{
            flightsLock.readLock().lock();
//            Try to find any flight's number duplicates
            if( flights.stream()
                       .map( Flight::getNumber )
                       .map( String::toUpperCase )
                       .anyMatch( Predicate.isEqual( flight.getNumber().toUpperCase() ) ) ){
                throw new FaRSameNameException( "Flight duplicates someone's number" );
            }
//            If flight has route reference not from routes collection
            if( routes.stream().map( Route::getId ).noneMatch( Predicate.isEqual( flight.getRoute().getId() ) ) ){
                throw new FaRNotRelatedData( "FLight's route doesn't exist in database" );
            }
//            If new flight copies someone's else data
            if( flights.stream()
                       .anyMatch( flight1 -> Objects.equals( flight1.getPlaneID().toUpperCase() ,
                                                             flight.getPlaneID().toUpperCase() ) &&
                                             Objects.equals( flight1.getRoute() , flight.getRoute() ) &&
                                             flight1.departureDateTime.isEqual( flight.departureDateTime ) &&
                                             flight1.arriveDateTime.isEqual( flight.arriveDateTime ) ) ){
                throw new FaRSameNameException( "Flight duplicates someone from database" );
            }
        }finally{
            flightsLock.readLock().unlock();
        }
        flightsLock.writeLock().lock();
        flights.add( flight );
        flightsLock.writeLock().unlock();
    }

    /**
     Delete specified flight from current database and immediately saves all changes

     @param number number of flight to be removed
     */
    public void removeFlight( String number ){
        try{
            flightsLock.readLock().lock();
            if( flights.stream().noneMatch( flight -> flight.getNumber().equals( number ) ) ){
                throw new FaRNotRelatedData( "Database doesn't contain flight " + number );
            }
        }finally{
            flightsLock.readLock().unlock();
        }
        flightsLock.writeLock().lock();
        flights.removeIf( flight -> Pattern.compile( number , Pattern.CASE_INSENSITIVE )
                                           .matcher( flight.getNumber() )
                                           .matches() );
        flightsLock.writeLock().unlock();
    }

    /**
     Edit specified flight in current database and immediately saves all changes

     @param flight           specify the flight, that you want to edit. if flight has incorrect data, it won't be added.
     @param newRoute         new route to change. if null, value win't be changed
     @param newPlaneId       new plane ID to change. if null, value win't be changed
     @param newDepartureDate new departure date to change. if null, value win't be changed
     @param newArriveDate    new arrive date to change. if null, value win't be changed

     @throws FaRDateMismatchException if flight has incorrect dates
     @throws FaRIllegalEditedData     previous version of this flight doesn't exist in database
     @throws FaRNotRelatedData        it has route, that doesn't exist in database
     @throws FaRSameNameException     it duplicates in ( planeID && route && arrive date && departure date ).
     */
    @SuppressWarnings( "ConstantConditions" )
    public void editFlight( Flight flight ,
                            Route newRoute ,
                            String newPlaneId ,
                            ZonedDateTime newDepartureDate ,
                            ZonedDateTime newArriveDate ) throws FlightAndRouteException{
        if( !legalSymbolsChecker.matcher( newPlaneId != null ? newPlaneId : flight.getPlaneID() ).matches() ){
            throw new FaRUnacceptableSymbolException( "Flights has illegal symbols" );
        }
        if( ChronoUnit.MILLIS.between( ( newDepartureDate != null ? newDepartureDate : flight.getDepartureDateTime() ) ,
                                       ( newArriveDate != null ? newArriveDate : flight.getArriveDateTime() ) ) <= 0 ){
            throw new FaRDateMismatchException( "Flight has incorrect dates" );
        }
        try{
            routesLock.readLock().lock();
            if( routes.stream()
                      .noneMatch( route -> Objects.equals( route.getId() ,
                                                           ( newRoute != null ?
                                                             newRoute :
                                                             flight.getRoute() ).getId() ) ) ){
                throw new FaRNotRelatedData( "FLight's route doesn't exist in database" );
            }
        }finally{
            routesLock.readLock().unlock();
        }
        try{
            flightsLock.readLock().lock();
            if( flights.stream()
                       .anyMatch( flight1 -> Objects.equals( flight1.getPlaneID().toUpperCase() ,
                                                             newPlaneId != null ?
                                                             newPlaneId.toUpperCase() :
                                                             flight.getPlaneID().toUpperCase() ) &&
                                             Objects.equals( flight1.getRoute() ,
                                                             newRoute != null ? newRoute : flight.getRoute() ) &&
                                             Objects.equals( flight1.getDepartureDateTime() ,
                                                             newDepartureDate != null ?
                                                             newDepartureDate :
                                                             flight.getDepartureDateTime() ) &&
                                             Objects.equals( flight1.getArriveDateTime() ,
                                                             newArriveDate != null ?
                                                             newArriveDate :
                                                             flight.getArriveDateTime() ) ) ){
                throw new FaRSameNameException( "Flight duplicates someone from database" );
            }
        }finally{
            flightsLock.readLock().unlock();
        }
        try{
            flightsLock.writeLock().lock();
            Flight
                    editingFlight =
                    flights.stream()
                           .filter( flight1 -> Objects.equals( flight1.getNumber().toUpperCase() ,
                                                               flight.getNumber().toUpperCase() ) )
                           .findFirst()
                           .orElseThrow( () -> new FaRIllegalEditedData(
                                   "Database doesn't contain previous version of flight" ) );
            Flight
                    newFlight =
                    new Flight( editingFlight.getNumber() ,
                                newRoute != null ? newRoute : editingFlight.getRoute() ,
                                newPlaneId != null ? newPlaneId : editingFlight.getPlaneID() ,
                                newDepartureDate != null ? newDepartureDate : editingFlight.getDepartureDateTime() ,
                                newArriveDate != null ? newArriveDate : editingFlight.getArriveDateTime() );
//        To produce update event on list
            flights.set( flights.indexOf( editingFlight ) , newFlight );
        }finally{
            flightsLock.writeLock().unlock();
        }
    }

    /**
     Add new route in current database, if it's correct, and immediately saves all changes

     @param route unique route to be added

     @throws FaRSameNameException           if new route's arrival and departure points duplicate someone another in database
     @throws IllegalArgumentException       if departure and destination airports are similar
     @throws FaRUnacceptableSymbolException if airports name contain illegal symbols
     */
    public void addRoute( Route route ){
        if( route.getFrom().equals( route.getTo() ) ){
            throw new FaRSameNameException( "Departure and destination airports are similar" );
        }
        try{
            routesLock.readLock().lock();
            if( routes.stream()
                      .anyMatch( route1 -> route.getFrom().equals( route1.getFrom() ) &&
                                           route.getTo().equals( route1.getTo() ) ) ){
                throw new FaRSameNameException( "Route duplicates someone from current database" );
            }
        }finally{
            routesLock.readLock().unlock();
        }
        setId( route );
        routesLock.writeLock().lock();
        routes.add( route );
        routesLock.writeLock().unlock();
    }

    /**
     Method acquires id for new route. keysGeneratorSemaphore must give new id sequential for each new route

     @param route new route without id
     */
    private void setId( Route route ){
        try{
            keysGeneratorSemaphore.acquire();
            route.id = routesPrimaryKeysGenerator.next();
        }catch( InterruptedException e ){
            e.printStackTrace();
        }finally{
            keysGeneratorSemaphore.release();
        }
    }

    /**
     Delete specified flight from current database and immediately saves all changes

     @param route to remove
     */
    public void removeRoute( Route route ){
        try{
            routesLock.readLock().lock();
            if( !routes.contains( route ) ){
                throw new FaRNotRelatedData( String.format( "Database doesn't contain route %s->%s" ,
                                                            route.getFrom() ,
                                                            route.getTo() ) );
            }
        }finally{
            routesLock.readLock().unlock();
        }
        routesLock.writeLock().lock();
        flightsLock.writeLock().lock();
        flights.removeIf( flight -> Objects.equals( flight.getRoute().getId() , route.getId() ) );
        routes.remove( route );
        routesLock.writeLock().unlock();
        flightsLock.writeLock().unlock();
    }

    /**
     Edit specified flight in current database and immediately saves all changes

     @param route                 edited route.
     @param newDepartureAirport   new value of arrival airport. if it's null, value won't change
     @param newDestinationAirport new value of departure airport. if it's null, value won't change

     @throws FaRIllegalEditedData if database doesn't contain previous version of route
     @throws FaRSameNameException it duplicates someone another or same airports
     */
    public void editRoute( Route route , ZoneId newDepartureAirport , ZoneId newDestinationAirport ){
        if( ( newDepartureAirport != null ? newDepartureAirport : route.getFrom() ).equals( newDestinationAirport !=
                                                                                            null ?
                                                                                            newDestinationAirport :
                                                                                            route.getTo() ) ){
            throw new FaRSameNameException( "Same airports" );
        }
        if( route.getId() == null ){
            throw new FaRIllegalEditedData( "Database doesn't contain previous version of route" );
        }
        try{
            routesLock.readLock().lock();
            if( routes.stream()
                      .anyMatch( route1 -> ( newDepartureAirport != null ?
                                             newDepartureAirport :
                                             route.getFrom() ).equals( route1.getFrom() ) &&
                                           ( newDestinationAirport != null ?
                                             newDestinationAirport :
                                             route.getTo() ).equals( route1.getTo() ) ) ){
                throw new FaRSameNameException( "New item duplicates someone" );
            }
        }finally{
            routesLock.readLock().unlock();
        }
        try{
            routesLock.writeLock().lock();
            Route
                    editingRoute =
                    routes.stream()
                          .filter( route1 -> Objects.equals( route1.getId() , route.getId() ) )
                          .findFirst()
                          .orElseThrow( () -> new FaRIllegalEditedData(
                                  "Database doesn't contain previous version of route" ) );
            Route
                    newRoute =
                    new Route( editingRoute.id ,
                               newDepartureAirport != null ? newDepartureAirport : route.getFrom() ,
                               editingRoute.getTo() );
//        To produce update event on list
            flights.stream().filter( flight -> flight.getRoute().getId().equals( editingRoute.getId() ) )
//                   to produce update events on list
                   .forEach( flight -> editFlight( flight , newRoute , null , null , null ) );
            routes.set( routes.indexOf( editingRoute ) , newRoute );
        }finally{
            routesLock.writeLock().unlock();
        }
    }

    /**
     Deserialize data from file, swap data contains in RAM to data from file. This method doesn't merge RAM and file
     data, like when you just open another file.

     @param inputStream that contains serialized data.

     @throws IllegalArgumentException if file contains not just flights and routes
     @throws FaRSameNameException     if file has data duplicates
     @throws FaRNotRelatedData        flight has route that doesn't exist in this file
     @throws IOException              If other I/O error has occurred.
     */
    @SuppressWarnings( "ConstantConditions" )
    public void importFrom( InputStream inputStream ) throws IOException, FlightAndRouteException{
        Map<Boolean, List<Serializable>> routesAndFlights = deserializeData( inputStream );
        if( !routesAndFlights.values()
                             .parallelStream()
                             .flatMap( Collection::stream )
                             .allMatch( serializable -> serializable.getClass().equals( Route.class ) ||
                                                        serializable.getClass().equals( Flight.class ) ) ){
            throw new FaRIllegalDataException( "One object neither Route, nor Flight class" );
        }
        List<Route>
                routes =
                routesAndFlights.get( true ).parallelStream().map( Route.class::cast ).collect( Collectors.toList() );
        List<Flight>
                flights =
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
        try{
            keysGeneratorSemaphore.acquire();
            /*key generator starts from max id from routes collection + 1. if collection is empty, key generator
            starts from 1
             */
            routesPrimaryKeysGenerator =
                    IntStream.rangeClosed( routes.stream().mapToInt( Route::getId ).max().orElse( 0 ) + 1 ,
                                           Integer.MAX_VALUE ).iterator();
        }catch( InterruptedException e ){
            e.printStackTrace();
        }finally{
            keysGeneratorSemaphore.release();
        }
        routesLock.writeLock().lock();
        flightsLock.writeLock().lock();
        this.routes.setAll( FXCollections.observableList( new ArrayList<>( routes ) ) );
        this.flights.setAll( FXCollections.observableList( new ArrayList<>( flights ) ) );
        routesLock.writeLock().unlock();
        flightsLock.writeLock().unlock();
    }

    /**
     This method try to add each item of data in this file. If it can't, it puts this to collection to give a report
     of fail of adding. Clear data will be added.

     @param additionalData file with data to be merged with current data

     @return collection of data, that can't been added.

     @throws IOException If other I/O error has occurred.
     */
    public Stream<Serializable> mergeData( InputStream additionalData ) throws IOException, FlightAndRouteException{
        Map<Boolean, List<Serializable>> routesAndFlights = deserializeData( additionalData );
        List<Serializable>               failedData       = new ArrayList<>();
        routesAndFlights.get( true ).removeIf( route -> {
            Boolean isNotRoute = !route.getClass().equals( Route.class );
            if( isNotRoute ) failedData.add( route );
            return isNotRoute;
        } );
        routesAndFlights.get( false ).removeIf( flight -> {
            Boolean isNotFlight = !flight.getClass().equals( Flight.class );
            if( isNotFlight ) failedData.add( flight );
            return isNotFlight;
        } );
        List<Route>  failedRoutes  = new ArrayList<>();
        List<Flight> failedFlights = new ArrayList<>();
        List<Route>
                routes =
                routesAndFlights.get( true ).parallelStream().map( Route.class::cast ).collect( Collectors.toList() );
        List<Flight>
                flights =
                routesAndFlights.get( false ).parallelStream().map( Flight.class::cast ).collect( Collectors.toList() );
        routesLock.readLock().lock();
        flightsLock.readLock().lock();
        Set<Route>  routeSet  = new HashSet<>( this.routes );
        Set<Flight> flightSet = new HashSet<>( this.flights );
        routesLock.readLock().unlock();
        flightsLock.readLock().unlock();
        routes.forEach( route -> {
            if( routeSet.stream()
                        .anyMatch( route1 -> route.getFrom().equals( route1.getFrom() ) &&
                                             route.getTo().equals( route1.getTo() ) ) ){
                failedRoutes.add( route );
            }else{
                routeSet.add( route );
            }
        } );
        routes.removeAll( failedRoutes );
        flights.stream().filter( flight -> routeSet.contains( flight.getRoute() ) ).forEach( flight -> {
            if( flightSet.stream()
                         .anyMatch( flight1 -> Pattern.compile( flight.getNumber() , Pattern.CASE_INSENSITIVE )
                                                      .matcher( flight1.getNumber() )
                                                      .matches() ) ){
                failedFlights.add( flight );
                return;
            }
            if( !flightSet.add( flight ) ){
                failedFlights.add( flight );
            }
        } );
        flights.removeAll( failedFlights );
        routes.forEach( this::setId );
        routesLock.writeLock().lock();
        flightsLock.writeLock().lock();
        this.routes.addAll( routes );
        this.flights.addAll( flights );
        routesLock.writeLock().unlock();
        flightsLock.writeLock().unlock();
        return Stream.concat( Stream.concat( failedRoutes.stream() , failedFlights.stream() ) , failedData.stream() );
    }

    private Map<Boolean, List<Serializable>> deserializeData( InputStream inputStream ) throws IOException{
        try( ObjectInput input = new ObjectInputStream( inputStream ) ){
            int                i    = 0, size = input.readInt();
            List<Serializable> data = new ArrayList<>();
            while( i++ < size ){
                data.add( ( Serializable ) input.readObject() );
            }
            return data.stream()
                       .collect( Collectors.partitioningBy( serializable -> serializable.getClass()
                                                                                        .equals( Route.class ) ) );
        }catch( ClassNotFoundException e ){
            throw new IllegalArgumentException( "File contains illegal data" , e );
        }
    }

    /**
     Look at method {@code void exportSpecifiedDataTo( Collection<Serializable> , File )}. Export all data in database
     */
    public void saveTo( OutputStream outputStream ) throws IOException{
        routesLock.readLock().lock();
        flightsLock.readLock().lock();
        List<Serializable>
                data =
                Stream.concat( this.routes.stream() , this.flights.stream() ).collect( Collectors.toList() );
        routesLock.readLock().unlock();
        flightsLock.readLock().unlock();
        exportSpecifiedDataTo( data , outputStream );
    }

    /**
     Serialize collected data from RAM to computer's storage ( NvRAM ).Serialize just Flight and Route classes.
     Method closes stream.

     @param data         collection of data to serialize
     @param outputStream stream to serialize data

     @throws IllegalArgumentException if collection contains not just flights and routes
     */
    @SuppressWarnings( "WeakerAccess" )
    public void exportSpecifiedDataTo( Collection<Serializable> data , OutputStream outputStream ) throws IOException{
        if( !data.parallelStream()
                 .allMatch( serializable -> serializable.getClass().equals( Route.class ) ||
                                            serializable.getClass().equals( Flight.class ) ) ){
            throw new IllegalArgumentException( "Can't serialize not Flight or Route classes" );
        }
        try( ObjectOutput output = new ObjectOutputStream( outputStream ) ){
            output.writeInt( data.size() );
            for( Serializable item : data ){
                output.writeObject( item );
            }
        }
    }

    public void clear(){
        try{
            keysGeneratorSemaphore.acquire();
            routesLock.writeLock().lock();
            flightsLock.writeLock().lock();
            routesPrimaryKeysGenerator = IntStream.rangeClosed( 1 , Integer.MAX_VALUE ).iterator();
            routes.clear();
            flights.clear();
        }catch( InterruptedException e ){
            e.printStackTrace();
        }finally{
            keysGeneratorSemaphore.release();
            routesLock.writeLock().unlock();
            flightsLock.writeLock().unlock();
        }
    }
}

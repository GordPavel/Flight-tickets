package model;

import java.io.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
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
    private DataModel(){
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
     @param predicate specifies flights what to choose

     @return specified flights
     */
    public Stream<Flight> listFlightsWithPredicate( Predicate<Flight> predicate ){
        return flights.stream().filter( predicate );
    }

    /**
     @param flight create new flight, which has unique number, instead it won't be added

     @return true , if flight was added, false in other case

     @throws IllegalArgumentException if flight has incorrect dates / it has route, that doesn't exist in database / it duplicates in ( planeID && route && arrive date
     && departure date ).
     */
    public Boolean addFlight( Flight flight ){
        if( !checkFlightsDate( flight ) ) throw new IllegalArgumentException( "Flight has incorrect dates" );
        if( !checkNumbersDuplicate( flight ) ){
            throw new IllegalArgumentException( "Flight's numbers duplicates someone from database" );
        }
        if( !checkFlightsRoute( flight ) ){
            throw new IllegalArgumentException( "Flight's routes doesn't exists in database" );
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
     @param flight specify the flight, that you want to edit. if flight has incorrect data, it won't be added.

     @return true , if database exists flight with specified number and new data doesn't duplicate another flights.
     false in other case

     @throws IllegalArgumentException if flight has incorrect dates / previous version of this flight doesn't exist
     in database / it has route, that doesn't exist in database / it duplicates in ( planeID && route && arrive date
     && departure date ).
     */
    public Boolean editFlight( Flight flight ){
        if( !checkFlightsDate( flight ) ) throw new IllegalArgumentException( "Flight has incorrect dates" );
        if( !checkFlightsRoute( flight ) ){
            throw new IllegalArgumentException( "Flight's routes doesn't exists in database" );
        }
        Optional<Flight> flightOptional =
                flights.stream().filter( flight1 -> Objects.equals( flight.getNumber() , flight1.getNumber() ) )
                       .findFirst();
        if( !flightOptional.isPresent() ){
            throw new IllegalArgumentException(
                    String.format( "Flight with number %s doesn't consists" , flight.getNumber() ) );
        }
        if( flights.stream().anyMatch( flight1 -> Objects.equals( flight.getRoute() , flight1.getRoute() ) &&
                                                  Objects.equals( flight.getPlaneID() , flight1.getPlaneID() ) &&
                                                  Objects.equals( flight.getArriveDate() , flight1.getArriveDate() ) &&
                                                  Objects.equals( flight.getDepartureDate() ,
                                                                  flight1.getDepartureDate() ) ) ){
            throw new IllegalArgumentException( "New flight duplicates someone another" );
        }
        Flight editedFLight = flightOptional.get();
        editedFLight.setRoute( flight.getRoute() );
        editedFLight.setPlaneID( flight.getPlaneID() );
        editedFLight.setArriveDate( flight.getArriveDate() );
        editedFLight.setDepartureDate( flight.getDepartureDate() );
        return true;
    }

    private Boolean checkFlightsRoute( Flight flight ){
        return routes.contains( flight.getRoute() );
    }

    private Boolean checkNumbersDuplicate( Flight flight ){
        return flights.stream().noneMatch( flight1 -> Objects.equals( flight1.getNumber() , flight.getNumber() ) );
    }

    private Boolean checkFlightsDate( Flight flight ){
        return flight.getArriveDate().before( flight.getDepartureDate() );
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
     */
    public Boolean addRoute( Route route ){
        route.setId( routesIdIterator.next() );
        return routes.addIfAbsent( route );
    }

    /**
     @param route to remove

     @return true , if database contains him and deleted, false instead
     */
    public Boolean removeRoute( Route route ){
        return routes.removeIf( route1 -> route1.getId().equals( route.getId() ) );
    }

    /**
     @param route edited route.

     @return true , if it has correct data and doesn't duplicate someone another, false instead.

     @throws IllegalArgumentException if database doesn't contain previous version of route / it duplicates someone
     another
     */
    public Boolean editRoute( Route route ){
        Optional<Route> routeOptional =
                routes.stream().filter( route1 -> Objects.equals( route.getId() , route1.getId() ) ).findFirst();
        if( !routeOptional.isPresent() ){
            throw new IllegalArgumentException( "This route doesn't contains in database" );
        }
        if( routes.stream().anyMatch( route1 -> Objects.equals( route , route1 ) ) ){
            throw new IllegalArgumentException( "This new route duplicates someone another" );
        }
        Route editedRoute = routeOptional.get();
        editedRoute.setFrom( route.getFrom() );
        editedRoute.setTo( route.getTo() );
        return true;
    }

    /**
     Deserialize data from file, swap data contains in RAM to data from file. This method doesn't merge RAM and file
     data, like when you just open another file. Method closes stream.

     @param file that contains serialized data.

     @throws IllegalArgumentException if file contains not just flights and routes / flight has route that doesn't
     exist in this file
     @throws IOException              If other I/O error has occurred.
     */
    public void importFromFile( File file ) throws IOException{
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
                    throw new IllegalArgumentException( "Duplicates routes" );
                }
            } );
            List<Flight> tempFlights = new ArrayList<>();
            flightsAndRoutes.get( true ).stream().map( serializable -> ( Flight ) serializable ).forEach( flight -> {
                if( tempFlights.contains( flight ) ) throw new IllegalArgumentException( "Duplicate flights" );
                if( !tempRoutes.contains( flight.getRoute() ) ){
                    throw new IllegalArgumentException( "Flight's route doesn't exist in file" );
                }
                tempFlights.add( flight );
            } );
            routes = new CopyOnWriteArrayList<>( tempRoutes );
            flights = new CopyOnWriteArrayList<>( tempFlights );
        }catch( IllegalArgumentException e ){
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
    public Collection<Serializable> mergeData( File additionalData ) throws IOException{
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
                }catch( IllegalArgumentException e ){
                    failedData.add( route );
                }
            } );
            flightsAndRoutes.get( true ).stream().map( serializable -> ( Flight ) serializable ).forEach( flight -> {
                try{
                    if( ! addFlight( flight ) )
                        failedData.add( flight );
                }catch( IllegalArgumentException e ){
                    failedData.add( flight );
                }
            } );
        }catch( IllegalArgumentException e ){
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
}

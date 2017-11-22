package model;

import java.io.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.IntSupplier;
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

    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

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
    private List<Flight> flights = new CopyOnWriteArrayList<>();

    /**
     *
     */
    private List<Route> routes = new CopyOnWriteArrayList<>();


    /**
     @param predicate specifies flights what to choose

     @return specified flights
     */
    public Stream<Flight> listFlightsWithPredicate( Predicate<Flight> predicate ){
        lock.readLock().lock();
        Stream<Flight> result = flights.stream().filter( predicate );
        lock.readLock().unlock();
        return result;
    }

    /**
     @param flight create new flight, which has unique number, instead it won't be added

     @return true , if flight was added, false in other case
     */
    public Boolean addFlight( Flight flight ){
        lock.readLock().lock();
        if( flights.stream().anyMatch( flight1 -> flight1.getNumber().equals( flight.getNumber() ) ) ){
            lock.readLock().unlock();
            return false;
        }
        lock.readLock().unlock();
        lock.writeLock().lock();
        Boolean result = flights.add( flight );
        lock.writeLock().unlock();
        return result;
    }

    /**
     @param number number of flight to be removed

     @return true , if this flight was removed, false in other case
     */
    public Boolean removeFlight( String number ){
        lock.writeLock().lock();
        Boolean result = flights.removeIf( flight -> flight.getNumber().equals( number ) );
        lock.writeLock().unlock();
        return result;
    }

    /**
     @param flight specify the number of flight, that you want to edit. Other attributes could not match with old
     version. Editing doesn't allow the same route and planeId, arriveDate and departureDate because in this case
     it'll duplicate another flight. So this flight won't be edited.

     @return true , if database exists flight with specified number and new data doesn't duplicate another flights.
     false in other case
     */
    public Boolean editFlight( Flight flight ){
        lock.readLock().lock();
        Optional<Flight> flightOptional =
                flights.stream().filter( flight1 -> flight1.getNumber().equals( flight.getNumber() ) ).findFirst();
        if( !flightOptional.isPresent() ) return false;
        if( flights.stream().anyMatch( baseFlight -> baseFlight.getRoute().equals( flight.getRoute() ) &&
                                                     baseFlight.getPlaneID().equals( flight.getPlaneID() ) &&
                                                     baseFlight.getArriveDate().equals( flight.getArriveDate() ) &&
                                                     baseFlight.getDepartureDate()
                                                               .equals( flight.getDepartureDate() ) ) ){
            lock.writeLock().unlock();
            return false;
        }
        lock.readLock().unlock();
        Flight editingFLight = flightOptional.get();
        lock.writeLock().lock();
        editingFLight.setRoute( flight.getRoute() );
        editingFLight.setPlaneID( flight.getPlaneID() );
        editingFLight.setArriveDate( flight.getArriveDate() );
        editingFLight.setDepartureDate( flight.getDepartureDate() );
        lock.writeLock().unlock();
        return true;
    }

    /**
     @param predicate to specify the routes that to choose

     @return specified routes
     */
    public Stream<Route> listRoutesWithPredicate( Predicate<Route> predicate ){
        lock.readLock().lock();
        Stream<Route> routesSet = routes.stream().filter( predicate );
        lock.readLock().unlock();
        return routesSet;
    }

    private Iterator<Integer> routesIdIterator =
            IntStream.range( routes.stream().mapToInt( Route::getId ).max().orElse( 0 ) + 1 , Integer.MAX_VALUE )
                     .iterator();

    /**
     @param route unique route to be added

     @return true , if route's unique and was added, false instead
     */
    public Boolean addRoute( Route route ){
        lock.writeLock().lock();
        route.setId( routesIdIterator.next() );
        Boolean result = routes.add( route );
        lock.writeLock().unlock();
        return result;
    }

    /**
     @param route
     */
    public Boolean removeRoute( Route route ){
        lock.writeLock().lock();
        Boolean result = routes.removeIf( route1 -> route1.getId().equals( route.getId() ) );
        lock.writeLock().unlock();
        return result;
    }

    /**
     @param route
     */
    public Boolean editRoute( Route route ){
        lock.readLock().lock();
        Optional<Route> routeOptional =
                routes.stream().filter( route1 -> route1.getId().equals( route.getId() ) ).findFirst();
        if( !routeOptional.isPresent() ) return false;
        if( routes.stream().anyMatch(
                route1 -> route1.getFrom().equals( route.getFrom() ) && route1.getTo().equals( route.getTo() ) ) ){
            lock.readLock().unlock();
            return false;
        }
        lock.readLock().unlock();
        Route editingRoute = routeOptional.get();
        lock.writeLock().lock();
        editingRoute.setFrom( route.getFrom() );
        editingRoute.setTo( route.getTo() );
        lock.writeLock().unlock();
        return true;
    }

    /**
     Deserialize data from file, swap data contains in RAM to data from file. This method doesn't merge RAM and file
     data, like when you just open another file.

     @param file that contains serialized data.
     */
    public void importFromFile( File file ) throws IOException{
        try( ObjectInputStream objectInputStream = new ObjectInputStream( new FileInputStream( file ) ) ){
            List<Serializable> data = new ArrayList<>();
            int                size = objectInputStream.readInt();
            int                i    = 0;
            while( i++ < size ){
                Serializable ser = ( Serializable ) objectInputStream.readObject();
                if( !isFLightOrRoute( ser ) ){
                    throw new ClassNotFoundException();
                }
                data.add( ser );
            }
            Map<Boolean, List<Serializable>> flightsAndRoutes =
                    data.stream().collect( Collectors.partitioningBy( item -> item instanceof Flight ) );
            flights = flightsAndRoutes.get( true ).stream().map( ser -> ( Flight ) ser ).collect( Collectors.toList() );
            routes = flightsAndRoutes.get( false ).stream().map( ser -> ( Route ) ser ).collect( Collectors.toList() );
        }catch( ClassNotFoundException e ){
            throw new IllegalArgumentException( "File contains illegal data" );
        }
    }


    /**
     Serialize data from RAM to computer's storage ( NvRAM )

     @param file where serialize the data
     */
    public void exportToFile( File file ) throws IOException{
        List<Serializable> data = new ArrayList<>();
        data.addAll( flights );
        data.addAll( routes );
        exportSpecifiedData( data , file );
    }

    /**
     from RAM to computer's storage ( NvRAM ).Serialize just Flight and Route classes.

     @param file where serialize the data
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

    /**
     @param additionalData
     */
    public void mergeData( File additionalData ){
        // TODO implement here
    }
}

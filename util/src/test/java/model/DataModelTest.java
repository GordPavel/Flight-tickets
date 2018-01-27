package model;

import exceptions.FaRDateMismatchException;
import exceptions.FaRIllegalEditedData;
import exceptions.FaRNotRelatedData;
import exceptions.FaRSameNameException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings( "ResultOfMethodCallIgnored" )
class DataModelTest{

    private DataModel    dataModel;
    private Set<ZoneId>  airports;
    private List<Route>  routes;
    private List<Flight> flights;
    private Pattern                   pattern  = Pattern.compile( "^([\\w/]+)/(\\w+)$" );
    private Map<String, List<String>> zones    = ZoneId.getAvailableZoneIds()
                                                       .stream()
                                                       .sorted()
                                                       .filter( pattern.asPredicate() )
                                                       .map( s -> {
                                                           Matcher matcher = pattern.matcher( s );
                                                           matcher.find();
                                                           return matcher.group( 1 ) + "/" + matcher.group( 2 );
                                                       } )
                                                       .filter( Pattern.compile( "(Etc|SystemV)/.+" )
                                                                       .asPredicate()
                                                                       .negate() )
                                                       .distinct()
                                                       .collect( Collectors.groupingBy( s -> {
                                                           Matcher matcher = pattern.matcher( s );
                                                           matcher.find();
                                                           return matcher.group( 1 );
                                                       } ) );
    private Random                    random   = new Random( System.currentTimeMillis() );
    private List<ZoneId>              allZones = zones.values()
                                                      .stream()
                                                      .flatMap( Collection::stream )
                                                      .map( ZoneId::of )
                                                      .collect( ArrayList::new , List::add , List::addAll );

    @BeforeEach
    void setUp(){
        dataModel = new DataModel();
        routes = new ArrayList<Route>(){{
            Iterator<Integer> iterator =
                    random.ints( 20 , 0 , zones.values().stream().mapToInt( Collection::size ).sum() )
                          .distinct()
                          .iterator();
            while( iterator.hasNext() ){
                try{
                    add( new Route( allZones.get( iterator.next() ) , allZones.get( iterator.next() ) ) );
                }catch( NoSuchElementException ignored ){
                }
            }
        }};
        airports = Stream.concat( routes.stream().map( Route::getFrom ) , routes.stream().map( Route::getTo ) )
                         .distinct()
                         .collect( Collectors.toSet() );
        routes.forEach( dataModel::addRoute );
        flights = IntStream.rangeClosed( 1 , 10 ).mapToObj( i -> {
            Route         flightRoute = routes.get( random.nextInt( routes.size() ) );
            ZonedDateTime departure   =
                    LocalDateTime.of( 2009 + i , 12 , 15 , 10 , 30 ).atZone( flightRoute.getFrom() );
            return new Flight( String.format( "number%d" , i ) , flightRoute , String.format( "planeId%d" , i + 1 ) ,
                               departure , departure.withZoneSameInstant( flightRoute.getTo() )
                                                    .plusHours( Math.abs( random.nextLong() ) % 9 + 1 ) );
        } ).collect( Collectors.toList() );
        flights.forEach( dataModel::addFlight );
    }

    @AfterEach
    void tearDown(){
        dataModel.clear();
    }

    @Test
    void listAllAirports(){
        assertTrue( airports.containsAll( dataModel.listAllAirportsWithPredicate( airport -> true ) ) ,
                    "Check all airports" );
        ZoneId randomAirport = new ArrayList<>( airports ).get( random.nextInt( airports.size() ) );
        assertIterableEquals( airports.stream()
                                      .map( ZoneId::getId )
                                      .filter( s -> s.endsWith( randomAirport.toString()
                                                                             .substring(
                                                                                     randomAirport.toString().length() -
                                                                                     2 ) ) )
                                      .collect( Collectors.toList() ) , dataModel.listAllAirportsWithPredicate(
                airport -> airport.toString()
                                  .endsWith( randomAirport.toString()
                                                          .substring( randomAirport.toString().length() - 2 ) ) )
                                                                                 .stream()
                                                                                 .map( ZoneId::getId )
                                                                                 .collect( Collectors.toList() ) ,
                              "Check filtered airports" );
    }

    @Test
    void addRoute(){
        Route addition = new Route( allZones.get( random.nextInt( allZones.size() ) ) ,
                                    allZones.get( random.nextInt( allZones.size() ) ) );
        airports.addAll( Arrays.asList( addition.getFrom() , addition.getTo() ) );
        dataModel.addRoute( addition );
        assertTrue( airports.containsAll( dataModel.listAllAirportsWithPredicate( airport -> true ) ) ,
                    "Database has new airport" );
        assertEquals( addition , dataModel.listRoutesWithPredicate( route -> route.equals( addition ) ).get( 0 ) ,
                      "Route " + "is in database" );
        assertThrows( FaRSameNameException.class , () -> dataModel.addRoute( addition ) ,
                      "Can't add this route more times" );
    }

    @Test
    void removeRoute(){
        Route route = dataModel.listRoutesWithPredicate( route1 -> true )
                               .get( random.nextInt( dataModel.getRouteObservableList().size() ) );
        dataModel.removeRoute( route );
        assertTrue( dataModel.listFlightsWithPredicate( flight -> flight.getRoute().equals( route ) ).isEmpty() ,
                    "No " + "flights" + " with this route" );
        assertTrue( dataModel.listRoutesWithPredicate( route1 -> route1.equals( route ) ).isEmpty() ,
                    "This route doesn't exist in database" );
        assertTrue( dataModel.listFlightsWithPredicate( flight -> flight.getRoute().equals( route ) ).isEmpty() ,
                    "All related flights were removed" );
        assertThrows( FaRNotRelatedData.class , () -> dataModel.removeRoute( route ) ,
                      "Can't remove this route more times" );
    }

    @Test
    void editRoute() throws CloneNotSupportedException{
        Route route = dataModel.listRoutesWithPredicate( route1 -> true )
                               .get( random.nextInt( dataModel.getRouteObservableList().size() ) );
        Route oldCopy = ( Route ) route.clone();
        dataModel.editRoute( route , allZones.get( random.nextInt( allZones.size() ) ) , null );
        assertTrue( dataModel.listRoutesWithPredicate(
                route1 -> route1.getFrom().equals( oldCopy.getFrom() ) && route1.getTo().equals( oldCopy.getTo() ) )
                             .isEmpty() , "Database hasn't old route" );
        assertThrows( FaRIllegalEditedData.class , () -> dataModel.editRoute(
                new Route( allZones.get( random.nextInt( allZones.size() ) ) ,
                           allZones.get( random.nextInt( allZones.size() ) ) ) , null , null ) ,
                      "Can't edit routes not from database" );
        assertThrows( FaRSameNameException.class , () -> dataModel.editRoute(
                dataModel.listRoutesWithPredicate( route1 -> true )
                         .get( random.nextInt( dataModel.getRouteObservableList().size() ) ) , null , null ) ,
                      "New data can't duplicate another route" );
    }

    @Test
    void listAllFlights(){
        Flight expected = flights.get( random.nextInt( flights.size() ) );
        assertEquals( expected ,
                      dataModel.listFlightsWithPredicate( flight -> flight.getNumber().equals( expected.getNumber() ) )
                               .get( 0 ) , "Find one flight" );
        assertIterableEquals( flights , dataModel.listFlightsWithPredicate( flight -> true ) , "Check all flights" );
        assertIterableEquals( flights.stream()
                                     .filter( flight -> flight.getTravelTime() <= 1000 * 60 * 60 * 4 )
                                     .collect( Collectors.toList() ) , dataModel.listFlightsWithPredicate(
                flight -> flight.getTravelTime() <= 1000 * 60 * 60 * 4 ) , "Filter by travel time" );
        ZonedDateTime startRange =
                LocalDateTime.parse( "15/12/2019 21:00" , DateTimeFormatter.ofPattern( "dd/MM/yyyy HH:mm" ) )
                             .atZone( ZoneId.of( "Europe/Samara" ) );
        ZonedDateTime endRange =
                LocalDateTime.parse( "15/12/2019 22:00" , DateTimeFormatter.ofPattern( "dd/MM/yyyy HH:mm" ) )
                             .atZone( ZoneId.of( "Europe/Samara" ) );
        assertIterableEquals( flights.stream()
                                     .filter( flight -> checkDateBetweenTwoDates(
                                             flight.getArriveDateTime().toLocalDateTime() ,
                                             startRange.toLocalDateTime() , endRange.toLocalDateTime() ) )
                                     .sorted( Comparator.comparing( Flight::getNumber ) )
                                     .collect( Collectors.toList() ) , dataModel.listFlightsWithPredicate(
                flight -> checkDateBetweenTwoDates( flight.getArriveDateTime().toLocalDateTime() ,
                                                    startRange.toLocalDateTime() , endRange.toLocalDateTime() ) )
                                                                                .stream()
                                                                                .sorted( Comparator.comparing(
                                                                                        Flight::getNumber ) )
                                                                                .collect( Collectors.toList() ) ,
                              "Filter by Arrive date" );
    }


    @Test
    void addNewFLight(){
        int i = 11;
        ZonedDateTime departure =
                LocalDateTime.of( 2009 + i , 12 , 15 , 10 , 30 ).atZone( ZoneId.of( "Europe/Samara" ) );
        ZonedDateTime arrive =
                LocalDateTime.of( 2009 + i , 12 , 15 , 11 + i , 30 ).atZone( ZoneId.of( "Europe/Samara" ) );
        Flight newFlight = new Flight( String.format( "number%d" , i ) , routes.get( random.nextInt( routes.size() ) ) ,
                                       String.format( "planeId%d" , i + 1 ) , departure , arrive );
        dataModel.addFlight( newFlight );
        assertFalse( dataModel.listFlightsWithPredicate( Predicate.isEqual( newFlight ) ).isEmpty() ,
                     "Flight is already in database" );
        assertThrows( FaRSameNameException.class , () -> dataModel.addFlight( newFlight ) ,
                      "Can't add this flight more times" );
        i = 12;
        Flight newFlight1 = new Flight( String.format( "number%d" , i ) ,
                                        new Route( allZones.get( random.nextInt( allZones.size() ) ) ,
                                                   allZones.get( random.nextInt( allZones.size() ) ) ) ,
                                        String.format( "planeId%d" , i + 1 ) , departure , arrive );
        assertThrows( FaRNotRelatedData.class , () -> dataModel.addFlight( newFlight1 ) ,
                      "Can't add flight, that has route not form database" );
    }

    @Test
    void removeFlight(){
        String flightNumber = dataModel.listFlightsWithPredicate( flight -> true )
                                       .stream()
                                       .map( Flight::getNumber )
                                       .collect( Collectors.toList() )
                                       .get( random.nextInt( dataModel.getFlightObservableList().size() ) );
        dataModel.removeFlight( flightNumber );
        assertTrue(
                dataModel.listFlightsWithPredicate( flight -> flight.getNumber().equals( flightNumber ) ).isEmpty() ,
                "There is no flight with this number" );
        assertThrows( FaRNotRelatedData.class , () -> dataModel.removeFlight( flightNumber ) ,
                      "Can't remove this flight more times" );
    }

    @Test
    void editFlight(){
        Flight editedFLight = dataModel.listFlightsWithPredicate( flight -> true )
                                       .get( random.nextInt( dataModel.getFlightObservableList().size() ) );
        dataModel.editFlight( editedFLight , null , null , null , editedFLight.getArriveDateTime().plusHours( 1 ) );
        assertThrows( FaRDateMismatchException.class , () -> dataModel.editFlight( editedFLight , null , null , null ,
                                                                                   editedFLight.getDepartureDateTime()
                                                                                               .minusHours( 1 ) ) ,
                      "Can't set arrival date before departure" );
        int i = 11;
        assertThrows( FaRIllegalEditedData.class , () -> dataModel.editFlight(
                new Flight( String.format( "number%d" , i ) , routes.get( random.nextInt( routes.size() ) ) ,
                            String.format( "planeId%d" , i + 1 ) ,
                            LocalDateTime.of( 2009 + i , 12 , 15 , 10 , 30 ).atZone( ZoneId.of( "Europe/Samara" ) ) ,
                            LocalDateTime.of( 2009 + i , 12 , 15 , 11 + i , 30 )
                                         .atZone( ZoneId.of( "Europe/Samara" ) ) ) , null , null , null , null ) ,
                      "Must take previous version from database" );
    }

    private Boolean checkDateBetweenTwoDates( LocalDateTime actual , LocalDateTime startRange ,
                                              LocalDateTime endRange ){
        return ( startRange != null ? startRange :
                 LocalDateTime.ofInstant( Instant.ofEpochMilli( 0L ) , ZoneId.of( "Europe/Samara" ) ) ).isBefore(
                actual ) && actual.isBefore( endRange != null ? endRange :
                                             LocalDateTime.ofInstant( Instant.ofEpochMilli( Long.MAX_VALUE ) ,
                                                                      ZoneId.of( "Europe/Samara" ) ) );
    }

    @Test
    void serializationAndDeserialization() throws IOException{
        List<Serializable> data = Stream.concat( dataModel.listFlightsWithPredicate( flight -> true ).stream() ,
                                                 dataModel.listRoutesWithPredicate( route -> true ).stream() )
                                        .collect( Collectors.toList() );
        File file = new File( Files.createFile( Paths.get( "test" ) ).toUri() );
        try{
            dataModel.saveToFile( file );
            dataModel.importFromFile( file );
            assertTrue( Stream.concat( dataModel.listFlightsWithPredicate( flight -> true ).stream() ,
                                       dataModel.listRoutesWithPredicate( route -> true ).stream() )
                              .parallel()
                              .allMatch( data::contains ) , "All deserialize data exists in old data" );
        }finally{
            Files.deleteIfExists( file.toPath() );
        }
        DataModel   anotherModel = new DataModel();
        List<Route> oldRoutes    = dataModel.listRoutesWithPredicate( route -> true );
        List<Route> copyRoutes = new ArrayList<Route>(){{
            random.ints( 3 , 0 , oldRoutes.size() ).distinct().forEach( value -> add( oldRoutes.get( value ) ) );
        }};
        List<Route> newRoutes = Stream.of( new Route( allZones.get( random.nextInt( allZones.size() ) ) ,
                                                      allZones.get( random.nextInt( allZones.size() ) ) ) ,
                                           new Route( allZones.get( random.nextInt( allZones.size() ) ) ,
                                                      allZones.get( random.nextInt( allZones.size() ) ) ) )
                                      .collect( Collectors.toList() );
        Stream.concat( copyRoutes.stream() , newRoutes.stream() ).forEach( anotherModel::addRoute );
        List<Route>  routes      = anotherModel.listRoutesWithPredicate( route -> true );
        List<Flight> copyFlights = dataModel.listFlightsWithPredicate( flight -> routes.contains( flight.getRoute() ) );
        List<Flight> newFlights = IntStream.rangeClosed( 11 , 15 )
                                           .mapToObj( i -> new Flight( String.format( "number%d" , i ) ,
                                                                       routes.get( random.nextInt( routes.size() ) ) ,
                                                                       String.format( "planeId%d" , i + 1 ) ,
                                                                       LocalDateTime.of( 2000 + i , 12 , 15 , 7 , 30 )
                                                                                    .atZone( ZoneId.of(
                                                                                            "Europe/Samara" ) ) ,
                                                                       LocalDateTime.of( 2000 + i , 12 , 15 , 8 + i ,
                                                                                         30 )
                                                                                    .atZone( ZoneId.of(
                                                                                            "Europe/Samara" ) ) ) )
                                           .collect( Collectors.toList() );
        Stream.concat( copyFlights.stream() , newFlights.stream() ).forEach( anotherModel::addFlight );
        file = new File( Files.createFile( Paths.get( "test" ) ).toUri() );
        try{
            anotherModel.saveToFile( file );
            List<Serializable> copyData =
                    Stream.concat( copyFlights.stream() , copyRoutes.stream() ).collect( Collectors.toList() );
            assertTrue( dataModel.mergeData( file ).collect( Collectors.toList() ).containsAll( copyData ) ,
                        "All copies were returned from method" );
            assertTrue( newRoutes.stream()
                                 .noneMatch( route1 -> dataModel.listRoutesWithPredicate( route1::pointsEquals )
                                                                .isEmpty() ) , "All new routes in the base" );
            assertTrue( newFlights.stream()
                                  .noneMatch( flight1 -> dataModel.listFlightsWithPredicate( flight1::pointsEquals )
                                                                  .isEmpty() ) , "All new flights in the base" );
        }finally{
            Files.deleteIfExists( file.toPath() );
        }
    }

    @RepeatedTest( 20 )
    void concurrency() throws InterruptedException{
        dataModel.clear();
        CountDownLatch  routesLatch   = new CountDownLatch( routes.size() );
        ExecutorService routesService = Executors.newFixedThreadPool( routes.size() );
        routesService.invokeAll( routes.stream().map( ( Function<Route, Callable<Void>> ) route -> ( () -> {
            try{
                dataModel.addRoute( route );
            }catch( Throwable e ){
                e.printStackTrace();
            }finally{
                routesLatch.countDown();
            }
            return null;
        } ) ).collect( Collectors.toList() ) );
        routesLatch.await();
        List<Route> dataBaseRoutes = dataModel.listRoutesWithPredicate( route -> true );
        assertTrue( routes.stream().anyMatch( dataBaseRoutes::contains ) ,
                    "All routes were added from different threads" );
        CountDownLatch  flightsLatch   = new CountDownLatch( flights.size() );
        ExecutorService flightsService = Executors.newFixedThreadPool( 10 );
        flightsService.invokeAll( flights.stream().map( ( Function<Flight, Callable<Void>> ) flight -> () -> {
            try{
                dataModel.addFlight( flight );
            }catch( Throwable e ){
                e.printStackTrace();
            }finally{
                flightsLatch.countDown();
            }
            return null;
        } ).collect( Collectors.toList() ) );
        flightsLatch.await();
        List<Flight> databaseFlights = dataModel.listFlightsWithPredicate( flight -> true );
        assertTrue( flights.stream().allMatch( databaseFlights::contains ) ,
                    "All flights were added from different threads" );
    }

    @Test
    void timeZones(){
        assertTrue( dataModel.listFlightsWithPredicate( flight -> true )
                             .stream()
                             .mapToLong( Flight::getTravelTime )
                             .allMatch( time -> time > 0 ) , "Travel times are right" );
    }

    @Test
    void testObservable(){
        List<Route> addedRoutes =
                Collections.singletonList( new Route( ZoneId.of( "Europe/Samara" ) , ZoneId.of( "Europe/Moscow" ) ) );
        dataModel.addRoutesListener( change -> {
            while( change.next() ){
                if( change.wasAdded() ){
                    assertTrue( change.getAddedSubList()
                                      .stream()
                                      .noneMatch( ( Predicate<Route> ) route -> addedRoutes.stream()
                                                                                           .noneMatch(
                                                                                                   route::pointsEquals ) ) ,
                                "Observe added routes" );
                }
            }
        } );
        addedRoutes.forEach( dataModel::addRoute );
    }

}
package model;

import exceptions.FaRDateMismatchException;
import exceptions.FaRIllegalEditedData;
import exceptions.FaRNotRelatedData;
import exceptions.FaRSameNameException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.concurrent.*;
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
        assertTrue( airports.containsAll(
                dataModel.listAllAirportsWithPredicate( airport -> true ).collect( Collectors.toList() ) ) ,
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
        assertTrue( airports.containsAll(
                dataModel.listAllAirportsWithPredicate( airport -> true ).collect( Collectors.toList() ) ) ,
                    "Database has new airport" );
        assertEquals( addition , dataModel.listRoutesWithPredicate( route -> route.equals( addition ) )
                                          .findFirst()
                                          .orElseThrow( IllegalArgumentException::new ) , "Route is in database" );
        assertThrows( FaRSameNameException.class , () -> dataModel.addRoute( addition ) ,
                      "Can't add this route more times" );
    }

    @Test
    void removeRoute(){
        Route route = dataModel.listRoutesWithPredicate( route1 -> true )
                               .collect( Collectors.toList() )
                               .get( random.nextInt(
                                       ( int ) dataModel.listRoutesWithPredicate( route1 -> true ).count() ) );
        dataModel.removeRoute( route );
        assertFalse( dataModel.listFlightsWithPredicate( flight -> flight.getRoute().equals( route ) )
                              .findAny()
                              .isPresent() , "No flights with this route" );
        assertFalse( dataModel.listRoutesWithPredicate( route1 -> route1.equals( route ) ).findAny().isPresent() ,
                     "This route doesn't exist in database" );
        assertTrue( dataModel.listFlightsWithPredicate( flight -> flight.getRoute().equals( route ) ).count() == 0 ,
                    "All related flights were removed" );
        assertThrows( FaRNotRelatedData.class , () -> dataModel.removeRoute( route ) ,
                      "Can't remove this route more times" );
    }

    @Test
    void editRoute() throws CloneNotSupportedException{
        Route route = dataModel.listRoutesWithPredicate( route1 -> true )
                               .collect( Collectors.toList() )
                               .get( random.nextInt(
                                       ( int ) dataModel.listRoutesWithPredicate( route1 -> true ).count() ) );
        Route oldCopy = ( Route ) route.clone();
        dataModel.editRoute( route , allZones.get( random.nextInt( allZones.size() ) ) , null );
        assertFalse( dataModel.listRoutesWithPredicate(
                route1 -> route1.getFrom().equals( oldCopy.getFrom() ) && route1.getTo().equals( oldCopy.getTo() ) )
                              .findAny()
                              .isPresent() , "Database hasn't old route" );
        assertThrows( FaRIllegalEditedData.class , () -> dataModel.editRoute(
                new Route( allZones.get( random.nextInt( allZones.size() ) ) ,
                           allZones.get( random.nextInt( allZones.size() ) ) ) , null , null ) ,
                      "Can't edit routes not from database" );
        assertThrows( FaRSameNameException.class , () -> dataModel.editRoute(
                dataModel.listRoutesWithPredicate( route1 -> true )
                         .collect( Collectors.toList() )
                         .get( random.nextInt( ( int ) dataModel.listRoutesWithPredicate( route1 -> true ).count() ) ) ,
                null , null ) , "New data can't duplicate another route" );
    }

    @Test
    void listAllFlights(){
        Flight expected = flights.get( random.nextInt( flights.size() ) );
        assertEquals( expected ,
                      dataModel.listFlightsWithPredicate( flight -> flight.getNumber().equals( expected.getNumber() ) )
                               .findFirst()
                               .get() , "Find one flight" );
        assertIterableEquals( flights ,
                              dataModel.listFlightsWithPredicate( flight -> true ).collect( Collectors.toList() ) ,
                              "Check all flights" );
        assertIterableEquals( flights.stream()
                                     .filter( flight -> flight.getTravelTime() <= 1000 * 60 * 60 * 4 )
                                     .collect( Collectors.toList() ) , dataModel.listFlightsWithPredicate(
                flight -> flight.getTravelTime() <= 1000 * 60 * 60 * 4 ).collect( Collectors.toList() ) ,
                              "Filter by travel time" );
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
                                                                                .sorted( Comparator.comparing(
                                                                                        Flight::getNumber ) )
                                                                                .collect( Collectors.toList() ) ,
                              "Filter by Arrive date" );
    }


    @Test
    void addNewFLight(){
//        todo: Протестировать неправильное время в часовых поясах
        int i = 11;
        ZonedDateTime departure =
                LocalDateTime.of( 2009 + i , 12 , 15 , 10 , 30 ).atZone( ZoneId.of( "Europe/Samara" ) );
        ZonedDateTime arrive =
                LocalDateTime.of( 2009 + i , 12 , 15 , 11 + i , 30 ).atZone( ZoneId.of( "Europe/Samara" ) );
        Flight newFlight = new Flight( String.format( "number%d" , i ) , routes.get( random.nextInt( routes.size() ) ) ,
                                       String.format( "planeId%d" , i + 1 ) , departure , arrive );
        dataModel.addFlight( newFlight );
        assertTrue( dataModel.listFlightsWithPredicate( Predicate.isEqual( newFlight ) ).findAny().isPresent() ,
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
                                       .map( Flight::getNumber )
                                       .collect( Collectors.toList() )
                                       .get( random.nextInt(
                                               ( int ) dataModel.listFlightsWithPredicate( flight -> true ).count() ) );
        dataModel.removeFlight( flightNumber );
        assertFalse( dataModel.listFlightsWithPredicate( flight -> flight.getNumber().equals( flightNumber ) )
                              .findAny()
                              .isPresent() , "There is no flight with this number" );
        assertThrows( FaRNotRelatedData.class , () -> dataModel.removeFlight( flightNumber ) ,
                      "Can't remove this flight more times" );
    }

    @Test
    void editFlight(){
        Flight editedFLight = dataModel.listFlightsWithPredicate( flight -> true )
                                       .collect( Collectors.toList() )
                                       .get( random.nextInt(
                                               ( int ) dataModel.listFlightsWithPredicate( flight -> true ).count() ) );
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
        List<Serializable> data = Stream.concat( dataModel.listFlightsWithPredicate( flight -> true ) ,
                                                 dataModel.listRoutesWithPredicate( route -> true ) )
                                        .collect( Collectors.toList() );
        File file = new File( Files.createFile( Paths.get( "test" ) ).toUri() );
        try{
            dataModel.saveToFile( file );
            dataModel.importFromFile( file );
            assertTrue( Stream.concat( dataModel.listFlightsWithPredicate( flight -> true ) ,
                                       dataModel.listRoutesWithPredicate( route -> true ) )
                              .parallel()
                              .allMatch( data::contains ) , "All deserialize data exists in old data" );
        }finally{
            Files.deleteIfExists( file.toPath() );
        }
        DataModel   anotherModel = new DataModel();
        List<Route> oldRoutes    = dataModel.listRoutesWithPredicate( route -> true ).collect( Collectors.toList() );
        List<Route> copyRoutes = new ArrayList<Route>(){{
            random.ints( 3 , 0 , oldRoutes.size() ).distinct().forEach( value -> add( oldRoutes.get( value ) ) );
        }};
        List<Route> newRoutes = Stream.of( new Route( allZones.get( random.nextInt( allZones.size() ) ) ,
                                                      allZones.get( random.nextInt( allZones.size() ) ) ) ,
                                           new Route( allZones.get( random.nextInt( allZones.size() ) ) ,
                                                      allZones.get( random.nextInt( allZones.size() ) ) ) )
                                      .collect( Collectors.toList() );
        Stream.concat( copyRoutes.stream() , newRoutes.stream() ).forEach( anotherModel::addRoute );
        List<Route> routes = anotherModel.listRoutesWithPredicate( route -> true ).collect( Collectors.toList() );
        List<Flight> copyFlights = dataModel.listFlightsWithPredicate( flight -> routes.contains( flight.getRoute() ) ).
                collect( Collectors.toList() );
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
                                 .allMatch( route1 -> dataModel.listRoutesWithPredicate( route1::pointsEquals )
                                                               .findFirst()
                                                               .isPresent() ) , "All new routes in the base" );
            assertTrue( newFlights.stream()
                                  .allMatch( flight1 -> dataModel.listFlightsWithPredicate( flight1::pointsEquals )
                                                                 .findFirst()
                                                                 .isPresent() ) );
        }finally{
            Files.deleteIfExists( file.toPath() );
        }
    }

    @Test
    void concurrency() throws InterruptedException, ExecutionException{
        int addingRoutes  = 10;
        int addingFlights = 12;
        routes = new ArrayList<Route>(){{
            Iterator<Integer> iterator =
                    random.ints( addingRoutes , 0 , zones.values().stream().mapToInt( Collection::size ).sum() )
                          .distinct()
                          .iterator();
            while( iterator.hasNext() ){
                try{
                    add( new Route( allZones.get( iterator.next() ) , allZones.get( iterator.next() ) ) );
                }catch( NoSuchElementException ignored ){
                }
            }
        }};
        CountDownLatch  routesLatch   = new CountDownLatch( routes.size() );
        ExecutorService routesService = Executors.newFixedThreadPool( routes.size() );
        routesService.invokeAll( routes.stream().map( ( Function<Route, Callable<Void>> ) route -> ( () -> {
            dataModel.addRoute( route );
            routesLatch.countDown();
            return null;
        } ) ).collect( Collectors.toList() ) );
        CountDownLatch startFlights = new CountDownLatch( 1 );
        assertTrue( Executors.newSingleThreadExecutor().submit( () -> {
            routesLatch.await();
            List<Route> dataBaseRoutes =
                    dataModel.listRoutesWithPredicate( route -> true ).collect( Collectors.toList() );
            Boolean result = routes.stream().anyMatch( dataBaseRoutes::contains );
            startFlights.countDown();
            return result;
        } ).get() , "All routes were added from different threads" );
        List<Route> databaseRoutes = dataModel.listRoutesWithPredicate( route -> true ).collect( Collectors.toList() );
        List<Flight> flights = IntStream.range( 11 , 11 + addingFlights )
                                        .mapToObj( i -> new Flight( String.format( "number%d" , i ) ,
                                                                    databaseRoutes.get(
                                                                            random.nextInt( databaseRoutes.size() ) ) ,
                                                                    String.format( "planeId%d" , i + 1 ) , LocalDateTime
                                                                            .now()
                                                                            .minusDays( i )
                                                                            .atZone( ZoneId.of( "Europe/Samara" ) ) ,
                                                                    LocalDateTime.now()
                                                                                 .plusDays( i )
                                                                                 .atZone( ZoneId.of(
                                                                                         "Europe/Samara" ) ) ) )
                                        .limit( addingFlights )
                                        .collect( Collectors.toList() );
        CountDownLatch  flightsLatch   = new CountDownLatch( addingFlights );
        ExecutorService flightsService = Executors.newFixedThreadPool( addingFlights );
        flightsService.invokeAll( flights.stream().map( ( Function<Flight, Callable<Void>> ) flight -> () -> {
            dataModel.addFlight( flight );
            flightsLatch.countDown();
            return null;
        } ).collect( Collectors.toList() ) );
        assertTrue( Executors.newSingleThreadExecutor().submit( () -> {
            flightsLatch.await();
            List<Flight> databaseFlights =
                    dataModel.listFlightsWithPredicate( flight -> true ).collect( Collectors.toList() );
            return flights.stream().allMatch( databaseFlights::contains );
        } ).get() , "All flights were added from different threads" );
    }

    @Test
    void timeZones(){
        assertTrue( dataModel.listFlightsWithPredicate( flight -> true )
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
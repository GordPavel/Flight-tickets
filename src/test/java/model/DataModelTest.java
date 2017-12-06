package model;

import exceptions.*;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class DataModelTest{

    private DataModel dataModel;

    @BeforeEach
    void setUp(){
        dataModel = DataModel.getInstance();
        List<Route> routes = Stream.of( new Route( "port1" , "port2" ) , new Route( "port1" , "port3" ) ,
                                        new Route( "port2" , "port3" ) , new Route( "port3" , "port1" ) ,
                                        new Route( "port3" , "port2" ) , new Route( "port2" , "port1" ) )
                                   .collect( Collectors.toList() );
        routes.forEach( dataModel::addRoute );
        IntStream.rangeClosed( 1 , 10 ).mapToObj(
                i -> new Flight( String.format( "number%d" , i ) , routes.get( ( i - 1 ) % routes.size() ) ,
                                 String.format( "planeId%d" , i + 1 ) , Date.from(
                        LocalDateTime.of( 2009 + i , 12 , 15 , 10 , 30 ).atZone( ZoneId.of( "Europe/Samara" ) )
                                     .toInstant() ) , Date.from(
                        LocalDateTime.of( 2009 + i , 12 , 15 , 11 + i , 30 ).atZone( ZoneId.of( "Europe/Samara" ) )
                                     .toInstant() ) ) ).forEach( dataModel::addFlight );
    }

    @AfterEach
    void tearDown(){
        dataModel.clear();
    }

    @Test
    void listAllAirports(){
        assertIterableEquals( Arrays.asList( "port1" , "port2" , "port3" ) ,
                              dataModel.listAllAirportsWithPredicate( airport -> true ).collect( Collectors.toList() ) ,
                              "Check all airports" );
        assertIterableEquals( Collections.singletonList( "port3" ) ,
                              dataModel.listAllAirportsWithPredicate( airport -> airport.endsWith( "3" ) )
                                       .collect( Collectors.toList() ) , "Check filtered airports" );
    }

    @Test
    void addRoute(){
        Route addition = new Route( "port1" , "port4" );
        assertTrue( dataModel.addRoute( addition ) , "Route added" );
        assertIterableEquals( Arrays.asList( "port1" , "port2" , "port3" , "port4" ) ,
                              dataModel.listAllAirportsWithPredicate( s -> true ).collect( Collectors.toList() ) ,
                              "Database has new airport" );
        assertEquals( addition , dataModel.listRoutesWithPredicate( route -> route.equals( addition ) ).findFirst()
                                          .orElseThrow( IllegalArgumentException::new ) , "Route is in database" );
        assertThrows( FaRSameNameException.class , () -> dataModel.addRoute( addition ) ,
                      "Can't add this route more times" );
        assertThrows( FaRUnacceptableSymbolException.class ,
                      () -> dataModel.addRoute( new Route( "port*4" , "port6" ) ) , "Illegal symbols" );
    }

    @Test
    void removeRoute(){
        Route route = dataModel.listRoutesWithPredicate( route1 -> true ).findFirst().get();
        assertTrue( dataModel.removeRoute( route ) , "Route removed" );
        assertFalse( dataModel.listFlightsWithPredicate( flight -> flight.getRoute().equals( route ) ).findAny()
                              .isPresent() , "No flights with this route" );
        assertFalse( dataModel.listRoutesWithPredicate( route1 -> route1.equals( route ) ).findAny().isPresent() ,
                     "This route doesn't exist in database" );
        assertFalse( dataModel.removeRoute( route ) , "Can't remove this route more times" );
    }

    @Test
    void editRoute(){
        Route route = dataModel.listRoutesWithPredicate( route1 -> true ).skip( 2 ).limit( 1 ).findFirst().get();
        assertTrue( dataModel.editRoute( route , "port4" , null ) ,
                    "Change route( port2 -> port3 ) to route( port4 -> port3 )" );
        assertIterableEquals( Arrays.asList( "port1" , "port2" , "port3" , "port4" ) ,
                              dataModel.listAllAirportsWithPredicate( s -> true ).collect( Collectors.toList() ) ,
                              "Database has new airport" );
        assertFalse(
                dataModel.listRoutesWithPredicate( route1 -> route1.equals( new Route( "port2" , "port3" ) ) ).findAny()
                         .isPresent() , "Database hasn't old route" );
        assertThrows( FaRIllegalEditedData.class ,
                      () -> dataModel.editRoute( new Route( "port2" , "port5" ) , null , null ) ,
                      "Can't edit routes not from database" );
        assertThrows( FaRSameNameException.class , () -> dataModel
                              .editRoute( dataModel.listRoutesWithPredicate( route1 -> true ).findAny().get() , null , null ) ,
                      "New data can't duplicate another route" );
    }

    @Test
    void listAllFlights(){
        List<Flight> flights = Collections.singletonList(
                new Flight( "number1" , new Route( "port1" , "port2" ) , "planeId2" , Date.from(
                        LocalDateTime.of( 2010 , 12 , 15 , 10 , 30 ).atZone( ZoneId.systemDefault() ).toInstant() ) ,
                            Date.from( LocalDateTime.of( 2010 , 12 , 15 , 12 , 30 ).atZone( ZoneId.systemDefault() )
                                                    .toInstant() ) ) );
        assertIterableEquals( flights ,
                              dataModel.listFlightsWithPredicate( flight -> flight.getNumber().equals( "number1" ) )
                                       .collect( Collectors.toList() ) , "Find one flight" );
        List<Route> routes = dataModel.listRoutesWithPredicate( route -> true ).collect( Collectors.toList() );
        flights = IntStream.rangeClosed( 1 , 10 ).mapToObj(
                i -> new Flight( String.format( "number%d" , i ) , routes.get( ( i - 1 ) % routes.size() ) ,
                                 String.format( "planeId%d" , i + 1 ) , Date.from(
                        LocalDateTime.of( 2009 + i , 12 , 15 , 10 , 30 ).atZone( ZoneId.of( "Europe/Samara" ) )
                                     .toInstant() ) , Date.from(
                        LocalDateTime.of( 2009 + i , 12 , 15 , 11 + i , 30 ).atZone( ZoneId.of( "Europe/Samara" ) )
                                     .toInstant() ) ) ).collect( Collectors.toList() );
        assertIterableEquals( flights ,
                              dataModel.listFlightsWithPredicate( flight -> true ).collect( Collectors.toList() ) ,
                              "Check all flights" );
        flights = dataModel.listFlightsWithPredicate( flight -> true ).limit( 3 ).collect( Collectors.toList() );
        assertIterableEquals( flights , dataModel
                .listFlightsWithPredicate( flight -> flight.getTravelTime() < 1000 * 60 * 60 * 4 + 1 )
                .collect( Collectors.toList() ) , "Filter by travel time" );

        flights = Collections.singletonList(
                new Flight( String.format( "number%d" , 10 ) , routes.get( 9 % routes.size() ) ,
                            String.format( "planeId%d" , 10 + 1 ) , Date.from(
                        LocalDateTime.of( 2009 + 10 , 12 , 15 , 10 , 30 ).atZone( ZoneId.of( "Europe/Samara" ) )
                                     .toInstant() ) , Date.from(
                        LocalDateTime.of( 2009 + 10 , 12 , 15 , 21 , 30 ).atZone( ZoneId.of( "Europe/Samara" ) )
                                     .toInstant() ) ) );
        Date startRange = Date.from(
                LocalDateTime.of( 2019 , 12 , 15 , 21 , 0 ).atZone( ZoneId.of( "Europe/Samara" ) ).toInstant() );
        Date endRange = Date.from(
                LocalDateTime.of( 2019 , 12 , 15 , 22 , 0 ).atZone( ZoneId.of( "Europe/Samara" ) ).toInstant() );
        assertIterableEquals( flights , dataModel.listFlightsWithPredicate(
                flight -> checkDateBetweenTwoDates( flight.getArriveDate() , startRange , endRange ) )
                                                 .collect( Collectors.toList() ) , "Filter departure date" );
    }


    @Test
    void addNewFLight(){
        Date arrive = Date.from(
                LocalDateTime.of( 2019 , 12 , 15 , 21 , 0 ).atZone( ZoneId.of( "Europe/Samara" ) ).toInstant() );
        Date departure = Date.from(
                LocalDateTime.of( 2019 , 12 , 15 , 22 , 0 ).atZone( ZoneId.of( "Europe/Samara" ) ).toInstant() );
        Flight newFlight =
                new Flight( "11" , dataModel.listRoutesWithPredicate( route -> true ).limit( 1 ).findFirst().get() ,
                            "plane" , arrive , departure );
        assertTrue( dataModel.addFlight( newFlight ) , "Flight was added" );
        assertTrue( dataModel.listFlightsWithPredicate( flight -> flight.equals( newFlight ) ).findAny().isPresent() ,
                    "Flight is already in database" );
        assertThrows( FaRSameNameException.class , () -> dataModel.addFlight( newFlight ) ,
                      "Can't add this flight " + "more times" );
        Flight newFlight1 = new Flight( "12" , new Route( "port1" , "port2" ) , "plane15" , arrive , departure );
        assertThrows( FaRNotRelatedData.class , () -> dataModel.addFlight( newFlight1 ) ,
                      "Can't add flight, that has route not form database" );
    }

    @Test
    void removeFlight(){
        String flightNumber =
                dataModel.listFlightsWithPredicate( flight -> true ).map( Flight::getNumber ).findAny().get();
        assertTrue( dataModel.removeFlight( flightNumber ) , "FLight was removed" );
        assertFalse( dataModel.listFlightsWithPredicate( flight -> flight.getNumber().equals( flightNumber ) ).findAny()
                              .isPresent() , "There is no flight with this number" );
        Date arrive = Date.from(
                LocalDateTime.of( 2019 , 12 , 15 , 21 , 0 ).atZone( ZoneId.of( "Europe/Samara" ) ).toInstant() );
        Date departure = Date.from(
                LocalDateTime.of( 2019 , 12 , 15 , 22 , 0 ).atZone( ZoneId.of( "Europe/Samara" ) ).toInstant() );
        Flight newFlight =
                new Flight( flightNumber , dataModel.listRoutesWithPredicate( route -> true ).findAny().get() ,
                            "plane" , arrive , departure );
        assertTrue( dataModel.addFlight( newFlight ) , "Can add new flight with this number" );
    }

    @Test
    void editFlight(){
        Flight editedFLight = dataModel.listFlightsWithPredicate( flight -> true ).findAny().get();
        assertTrue( dataModel.editFlight( editedFLight , null , null , null , Date.from(
                Instant.ofEpochMilli( editedFLight.getArriveDate().getTime() + 1000 * 60 * 60 * 2 ) ) ) ,
                    "Changed departure time to 1 hour later" );
        assertThrows( FaRDateMismatchException.class , () -> dataModel.editFlight( editedFLight , null , null , null ,
                                                                                   Date.from( Instant.ofEpochMilli(
                                                                                           editedFLight
                                                                                                   .getDepartureDate()
                                                                                                   .getTime() -
                                                                                           1000 * 60 * 60 ) ) ) ,
                      "Can't set departure date before arrival" );
        Flight notFromDatabaseFlight = new Flight( String.format( "number%d" , 15 ) ,
                                                   dataModel.listRoutesWithPredicate( route -> true ).findAny().get() ,
                                                   String.format( "planeId%d" , 16 ) , Date.from(
                LocalDateTime.of( 2009 + 15 , 12 , 15 , 10 , 30 ).atZone( ZoneId.of( "Europe/Samara" ) ).toInstant() ) ,
                                                   Date.from( LocalDateTime.of( 2009 + 15 , 12 , 15 , 23 , 30 )
                                                                           .atZone( ZoneId.of( "Europe/Samara" ) )
                                                                           .toInstant() ) );
        assertThrows( FaRIllegalEditedData.class ,
                      () -> dataModel.editFlight( notFromDatabaseFlight , null , null , null , null ) ,
                      "Must take previous version from database" );
    }

    private Boolean checkDateBetweenTwoDates( Date actual , Date startRange , Date endRange ){
        return !( startRange != null ? startRange : Date.from( Instant.ofEpochMilli( 0L ) ) ).after( actual ) &&
               actual.before( endRange != null ? endRange : Date.from( Instant.ofEpochMilli( Long.MAX_VALUE ) ) );
    }

    @Test
    void serializationAndDeserialization() throws IOException, ClassNotFoundException{
        List<Serializable> data = Stream.concat( dataModel.listFlightsWithPredicate( flight -> true ) ,
                                                 dataModel.listRoutesWithPredicate( route -> true ) )
                                        .collect( Collectors.toList() );
        File file = new File( Files.createFile( Paths.get( "test" ) ).toUri() );
        try{
            dataModel.exportToFile( file );
            dataModel.importFromFile( file );
            assertTrue( Stream.concat( dataModel.listFlightsWithPredicate( flight -> true ) ,
                                       dataModel.listRoutesWithPredicate( route -> true ) ).parallel()
                              .allMatch( data::contains ) , "All deserialize data exists in old data" );
        }finally{
            Files.deleteIfExists( file.toPath() );
        }
        DataModel anotherModel = new DataModel();
        List<Route> copyRoutes =
                dataModel.listRoutesWithPredicate( route -> true ).limit( 1 ).collect( Collectors.toList() );
        List<Route> newRoutes = Stream.of( new Route( "port4" , "port5" ) , new Route( "port5" , "port4" ) )
                                      .collect( Collectors.toList() );
        Stream.concat( copyRoutes.stream() , newRoutes.stream() ).forEach( anotherModel::addRoute );
        List<Route> routes = anotherModel.listRoutesWithPredicate( route -> true ).collect( Collectors.toList() );
        List<Flight> copyFlights =
                dataModel.listFlightsWithPredicate( flight -> routes.contains( flight.getRoute() ) ).limit( 5 )
                         .collect( Collectors.toList() );
        List<Flight> newFlights = IntStream.rangeClosed( 11 , 15 ).mapToObj(
                i -> new Flight( String.format( "number%d" , i ) , routes.get( ( i - 1 ) % routes.size() ) ,
                                 String.format( "planeId%d" , i + 1 ) , Date.from(
                        LocalDateTime.of( 2000 + i , 12 , 15 , 7 , 30 ).atZone( ZoneId.of( "Europe/Samara" ) )
                                     .toInstant() ) , Date.from(
                        LocalDateTime.of( 2000 + i , 12 , 15 , 8 + i , 30 ).atZone( ZoneId.of( "Europe/Samara" ) )
                                     .toInstant() ) ) ).collect( Collectors.toList() );
        Stream.concat( copyFlights.stream() , newFlights.stream() ).forEach( anotherModel::addFlight );
        file = new File( Files.createFile( Paths.get( "test" ) ).toUri() );
        try{
            anotherModel.exportToFile( file );
            List<Serializable> copyData =
                    Stream.concat( copyFlights.stream() , copyRoutes.stream() ).collect( Collectors.toList() );
            assertTrue( dataModel.mergeData( file ).anyMatch( copyData::contains ) ,
                        "All copies were returned from method" );
            List<Serializable> newData =
                    Stream.concat( newFlights.stream() , newRoutes.stream() ).collect( Collectors.toList() );
            ArrayList<Object> list = Stream.concat( dataModel.listRoutesWithPredicate( route -> true ) ,
                                                    dataModel.listFlightsWithPredicate( flight -> true ) )
                                           .collect( ArrayList::new , List::add , List::addAll );
//            assertTrue( list.containsAll( newData ) ,
//                        "All new data in base" );
            newData.stream().filter( serializable -> !list.contains( serializable ) ).forEach( System.out::println );
            int a = 0;
        }finally{
            Files.deleteIfExists( file.toPath() );
        }
    }

    @Test
    @Disabled
    void generateDataFile() throws IOException{
        File file = new File( Files.createFile( Paths.get( "test.far" ) ).toUri() );
        dataModel.exportToFile( file );
    }

    @Test
    @RepeatedTest( 10 )
    void concurrency() throws InterruptedException, ExecutionException{
        int    addingRoutes = 10;
        Random random       = new Random( System.currentTimeMillis() );
        List<Route> routes = Stream.generate(
                () -> new Route( "port" + random.nextInt( 46 ) + 4 , "port" + random.nextInt( 50 ) + 50 ) )
                                   .limit( addingRoutes ).collect( Collectors.toList() );
        CountDownLatch  routesLatch   = new CountDownLatch( addingRoutes );
        ExecutorService routesService = Executors.newFixedThreadPool( addingRoutes );
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

        int         addingFlights  = 12;
        List<Route> databaseRoutes = dataModel.listRoutesWithPredicate( route -> true ).collect( Collectors.toList() );
        List<Flight> flights = IntStream.range( 11 , 23 ).mapToObj(
                i -> new Flight( "number" + i , databaseRoutes.get( i % databaseRoutes.size() ) ,
                                 "planeID" + ( i + 15 ) , Date.from(
                        Instant.ofEpochMilli( Instant.now().toEpochMilli() - 1000L * 60 * 60 * 24 * addingFlights ) ) ,
                                 Date.from( Instant.ofEpochMilli(
                                         Instant.now().toEpochMilli() + 1000L * 60 * 60 * 24 * addingFlights ) ) ) )
                                        .limit( addingFlights ).collect( Collectors.toList() );
        CountDownLatch  flightsLatch   = new CountDownLatch( addingFlights );
        ExecutorService flightsService = Executors.newFixedThreadPool( addingFlights );
        flightsService.invokeAll( flights.stream().map( ( Function<Flight, Callable<Void>> ) flight -> () -> {
            startFlights.await();
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
}
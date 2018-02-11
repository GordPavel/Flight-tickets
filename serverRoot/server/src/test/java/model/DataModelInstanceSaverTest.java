package model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import transport.ListChangeAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataModelInstanceSaverTest{
    private final String                     baseName = "test";
    private       Random                     random   = new Random( System.currentTimeMillis() );
    private       Pattern                    pattern  = Pattern.compile( "^([\\w/]+)/(\\w+)$" );
    private       Map<String, List<String>>  zones    = ZoneId.getAvailableZoneIds()
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
    private       List<ZoneId>               allZones = zones.values()
                                                             .stream()
                                                             .flatMap( Collection::stream )
                                                             .map( ZoneId::of )
                                                             .collect( ArrayList::new , List::add , List::addAll );
    private       Map<Boolean, List<Route>>  routes   = new ArrayList<Route>(){{
        Iterator<Integer> iterator = random.ints( 20 , 0 , allZones.size() ).distinct().iterator();
        while( iterator.hasNext() ){
            try{
                add( new Route( allZones.get( iterator.next() ) , allZones.get( iterator.next() ) ) );
            }catch( NoSuchElementException ignored ){
            }
        }
    }}.stream().collect( Collectors.partitioningBy( route -> random.nextBoolean() ) );
    private       Map<Boolean, List<Flight>> flights  = IntStream.rangeClosed( 1 , 10 ).mapToObj( i -> {
        List<Route> allRoutes = Stream.concat( routes.get( true ).stream() , routes.get( false ).stream() )
                                      .collect( Collectors.toList() );
        Route flightRoute = allRoutes.get( random.nextInt( allRoutes.size() ) );
        ZonedDateTime departure = LocalDateTime.of( 2009 + i , 12 , 15 , 10 , 30 ).atZone( flightRoute.getFrom() );
        return new Flight( String.format( "number%d" , i ) , flightRoute , String.format( "planeId%d" , i + 1 ) ,
                           departure , departure.withZoneSameInstant( flightRoute.getTo() )
                                                .plusHours( Math.abs( random.nextLong() ) % 9 + 1 ) );
    } ).collect( Collectors.partitioningBy( flight -> routes.get( true ).contains( flight.getRoute() ) ) );

    @Test
    void testChanges() throws IOException{
        DataModel dataModel = new DataModel();
        routes.get( true ).forEach( dataModel::addRoute );
        flights.get( true ).forEach( dataModel::addFlight );
        try( OutputStream outputStream = Files.newOutputStream( Paths.get(
                "/Users/pavelgordeev/IdeaProjects/Flight-tickets/serverRoot/server/src/test/resources/serverfiles" +
                "/bases/test.far" ) ) ){
            dataModel.saveTo( outputStream );
        }

        DataModel dataModel1 = DataModelInstanceSaver.getInstance( baseName )
                                                     .map( dataModelWithLock -> dataModelWithLock.model )
                                                     .orElse( null ), dataModel2 = new DataModel();
        try( InputStream inputStream = Files.newInputStream(
                Paths.get( DataModelInstanceSaver.basesFolder + baseName + "" + ".far" ) ) ){
            dataModel2.importFrom( inputStream );
        }
        routes.get( false ).forEach( dataModel1::addRoute );
        flights.get( false ).forEach( dataModel1::addFlight );
        dataModel1.getFlightObservableList().removeIf( flight -> random.nextBoolean() );
        dataModel1.editRoute( dataModel1.getRouteObservableList()
                                        .get( random.nextInt( dataModel1.getRouteObservableList().size() ) ) ,
                              allZones.get( random.nextInt( allZones.size() ) ) , null );
        dataModel1.getFlightObservableList().sort( Comparator.comparing( Flight::getNumber ).reversed() );

        Files.list( Paths.get( DataModelInstanceSaver.basesCacheFiles ) )
             .filter( path -> !Files.isDirectory( path ) )
             .filter( path -> path.toString().matches( "^.+/" + baseName + "_.+$" ) )
             .flatMap( path -> {
                 try{
                     return Files.lines( path );
                 }catch( IOException e ){
                     e.printStackTrace();
                     return null;
                 }
             } )
             .map( ListChangeAdapter::new )
             .forEach( listChangeAdapter -> {
                 try{
                     listChangeAdapter.apply( dataModel2 );
                 }catch( IOException e ){
                     e.printStackTrace();
                 }
             } );

        assertIterableEquals( dataModel1.getRouteObservableList() , dataModel2.getRouteObservableList() ,
                              "All routes were added" );
        assertIterableEquals( dataModel1.getFlightObservableList() , dataModel2.getFlightObservableList() ,
                              "All flights were added" );
    }

    @Test
    void testCacheDatabases(){
        assertTrue( DataModelInstanceSaver.getInstance( baseName ).get() ==
                    DataModelInstanceSaver.getInstance( baseName ).get() , "Server stores in cache recent databases" );
    }

    @AfterEach
    void tearDown() throws IOException{
        Files.delete( Paths.get(
                "/Users/pavelgordeev/IdeaProjects/Flight-tickets/serverRoot/server/src/test/resources/serverfiles/bases/test.far" ) );
        Files.write( Paths.get(
                "/Users/pavelgordeev/IdeaProjects/Flight-tickets/serverRoot/server/src/test/resources/serverfiles" +
                "/clientUpdates/test_User1" ) , "".getBytes() , StandardOpenOption.TRUNCATE_EXISTING );
    }
}
package searchengine;


import model.DataModel;
import model.Flight;
import model.Route;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

class SearchEngineTest{

    private DataModel    dataModel    = DataModel.getInstance();
    private SearchEngine searchEngine = new SearchEngine( dataModel );

    @BeforeEach
    void setUp(){
        List<Route> routes = Stream.of( new Route( "port11" , "port2" ) , new Route( "port1" , "port3" ) ,
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

    @Test
    void searchRoutes(){
        assertIterableEquals( dataModel.listRoutesWithPredicate( route -> true ).collect( Collectors.toList() ) ,
                              searchEngine.findRoute( "port" , null ).collect( Collectors.toList() ) , "All routes" );
        assertIterableEquals( Arrays.asList( new Route( "port11" , "port2" ) , new Route( "port1" , "port3" ) ) ,
                              searchEngine.findRoute( "port1" , null ).collect( Collectors.toList() ) ,
                              "Starts with port1" );
        Route newRoute = new Route( "port15" , "port1" );
        dataModel.addRoute( newRoute );
        assertEquals( newRoute , searchEngine.findRoute( "port15" , "port1" ).findFirst().get() , "Find new route" );
    }

    @Test
    void searchFlights(){
        assertIterableEquals(
                dataModel.listFlightsWithPredicate( flight -> flight.getRoute().getFrom().equals( "port3" ) )
                         .collect( Collectors.toList() ) ,
                searchEngine.findFlight( null , "port3" , null , null , null , null , null , null )
                            .collect( Collectors.toList() ) , "Find by departure airport" );
        Flight flight = dataModel.listFlightsWithPredicate( flight1 -> true ).findAny()
                                 .orElseThrow( IllegalStateException::new );
        assertEquals( flight , searchEngine.findFlight( null , null , null , null , Date.from(
                Instant.ofEpochMilli( flight.getDepartureDate().getTime() - 86400000L ) ) , Date.from(
                Instant.ofEpochMilli( flight.getDepartureDate().getTime() + 86400000L ) ) , null , null ).findFirst()
                                           .get() , "Search by departure time in one day range" );
        List<Flight> flights =
                dataModel.listFlightsWithPredicate( flight1 -> flight1.getArriveDate().after( flight.getArriveDate() ) )
                         .collect( Collectors.toList() );
        assertIterableEquals( flights , searchEngine
                .findFlight( null , null , null , null , null , null , flight.getArriveDate() , null )
                .collect( Collectors.toList() ) );
    }

}
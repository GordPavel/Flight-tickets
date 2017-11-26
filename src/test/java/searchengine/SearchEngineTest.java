package searchengine;


import model.DataModel;
import model.Flight;
import model.Route;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
        searchEngine.findRoute( "ort" , "3 " ).forEach( System.out::println );
    }

}
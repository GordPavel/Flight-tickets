package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.crypto.Data;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class DataModelTest{

    private DataModel dataModel = DataModel.getInstance();
    private Random    random    = new Random( System.currentTimeMillis() );

    private File file;

    DataModelTest() throws IOException{}

    @BeforeEach
    void setUp() throws IOException{
        Path path = Paths.get( "/Users/pavelgordeev/Desktop/test" );
        if( !Files.exists( path ) ){
            file = new File( Files.createFile( path ).toUri() );
        }else{
            file = new File( path.toUri() );
        }
        routes = Stream.of( new Route( "MCS" , "SMR" ) , new Route( "SMR" , "PTR" ) , new Route( "MSC" , "PTR" ) ,
                            new Route( "SMR" , "MCS" ) , new Route( "PTR" , "SMR" ) , new Route( "PTR" , "MSC" ) )
                       .collect( Collectors.toList() );
        List<Flight> flights = Stream.generate( this::getRandomFlight ).limit( 10 ).collect( Collectors.toList() );
        flights.forEach( dataModel::addFlight );
        routes.forEach( dataModel::addRoute );
    }

    private List<Route> routes;

    private Flight getRandomFlight(){
        return new Flight( String.valueOf( random.nextInt( 1000 ) ) , routes.get( random.nextInt( 5 ) ) ,
                           String.valueOf( random.nextInt( 1000 ) ) ,
                           getRandomDate( Instant.parse( "2007-12-03T10:15:30.00Z" ) , Instant.now() ) ,
                           getRandomDate( Instant.parse( "2007-12-03T10:15:30.00Z" ) , Instant.now() ) );
    }

    private Date getRandomDate( Instant early , Instant late ){
        return Date.from( Instant.ofEpochMilli(
                early.toEpochMilli() + ( long ) ( Math.random() * late.toEpochMilli() - early.toEpochMilli() + 1 ) ) );
    }

    @Test
    void exportSpecifiedData() throws IOException{
        dataModel.exportToFile( file );

        dataModel.listFlightsWithPredicate( flight -> true ).forEach( System.out::println );
        System.out.println();
        dataModel.listRoutesWithPredicate( route -> true ).forEach( System.out::println );

    }

    @Test
    void importFromFile() throws IOException{
        dataModel.importFromFile( file );

        dataModel.listFlightsWithPredicate( flight -> true ).forEach( System.out::println );
        System.out.println();
        dataModel.listRoutesWithPredicate( route -> true ).forEach( System.out::println );
    }


}
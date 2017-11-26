package searchengine;

import model.DataModel;
import model.Flight;
import model.Route;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/*
Search Engine allows to get collections of flights and routs, stored in DataModel, sorted by parameters, specified in each method
@author teomant */

public class SearchEngine{

    /**
     store data model, connected to this search engine
     */

    private DataModel data;


    /**
     @param data - datamodel
     */
    SearchEngine( DataModel data ){
        this.data = data;
    }

    /**
     @param from regex to match departure airport. if null, search all departure airports
     @param to   regex to match arrival airport. if null, search all arrival airports

     @return all routes that matches this requirements
     */
    public Stream<Route> findRoute( String from , String to ){
        return data.listRoutesWithPredicate( route -> generatePredicate( from ).test( route.getFrom() ) &&
                                                      generatePredicate( to ).test( route.getTo() ) );
    }

    public Stream<Flight> findFlight( String number , String from , String to , String plane ,
                                      Date startDepartureDateRange , Date endDepartureDateRange ,
                                      Date startArriveDateRange , Date endArriveDateRange ){
        return data.listFlightsWithPredicate( flight -> generatePredicate( number ).test( flight.getNumber() ) &&
                                                        generatePredicate( from ).test( flight.getRoute().getFrom() ) &&
                                                        generatePredicate( to ).test( flight.getRoute().getTo() ) &&
                                                        generatePredicate( plane ).test( flight.getPlaneID() ) &&
                                                        checkDateBetweenTwoDates( flight.getDepartureDate() ,
                                                                                  startDepartureDateRange ,
                                                                                  endDepartureDateRange ) &&
                                                        checkDateBetweenTwoDates( flight.getArriveDate() ,
                                                                                  startArriveDateRange ,
                                                                                  endArriveDateRange ) );
    }

    private Predicate<String> generatePredicate( String from ){
        return Optional.ofNullable( from ).map( s -> Pattern.compile( ".*" + s + ".*" ).asPredicate() )
                       .orElse( s -> true );
    }

    private Boolean checkDateBetweenTwoDates( Date actual , Date startRange , Date endRange ){
        return !( startRange != null ? startRange : Date.from( Instant.ofEpochMilli( 0L ) ) ).after( actual ) &&
               actual.before( endRange != null ? endRange : Date.from( Instant.ofEpochMilli( Long.MAX_VALUE ) ) );
    }
}

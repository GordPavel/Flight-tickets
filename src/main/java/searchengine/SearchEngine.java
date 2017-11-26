package searchengine;

import model.*;

import java.time.Instant;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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
        return data.listRoutesWithPredicate(
                route -> ( from == null || Pattern.matches( ".*" + from + ".*" , route.getFrom() ) ) &&
                         ( to == null || Pattern.matches( ".*" + to + ".*" , route.getTo() ) ) );
    }

    public Stream<Flight> findFlight( String number , String from , String to , String plane ,
                                      Date startDepartureDateRange , Date endDepartureDateRange ,
                                      Date startArriveDateRange , Date endArriveDateRange ){
        return data.listFlightsWithPredicate(
                flight -> ( number == null || Pattern.matches( ".*" + number + ".*" , flight.getNumber() ) ) &&
                          ( from == null || Pattern.matches( ".*" + from + ".*" , flight.getRoute().getFrom() ) ) &&
                          ( to == null || Pattern.matches( ".*" + to + ".*" , flight.getRoute().getTo() ) ) &&
                          ( plane == null || Pattern.matches( ".*" + plane + ".*" , flight.getPlaneID() ) ) &&
                          checkDateBetweenTwoDates( flight.getDepartureDate().getTime() ,
                                                    startDepartureDateRange != null ? startArriveDateRange.getTime() :
                                                    Date.from( Instant.MIN ).getTime() ,
                                                    endDepartureDateRange != null ? endDepartureDateRange.getTime() :
                                                    Date.from( Instant.MAX ).getTime() ) &&
                          checkDateBetweenTwoDates( flight.getDepartureDate().getTime() ,
                                                    startArriveDateRange != null ? startArriveDateRange.getTime() :
                                                    Date.from( Instant.MIN ).getTime() ,
                                                    endArriveDateRange != null ? endArriveDateRange.getTime() :
                                                    Date.from( Instant.MAX ).getTime() ) );
    }

    private Boolean checkDateBetweenTwoDates( Long actual , Long start , Long end ){
        return start <= actual && actual < end;
    }
}

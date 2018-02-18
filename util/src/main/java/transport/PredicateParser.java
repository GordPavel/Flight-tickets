package transport;

import com.fasterxml.jackson.annotation.JsonIgnore;
import model.Flight;
import model.Route;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PredicateParser{
    @Override
    public boolean equals( Object obj ){
        if( !( obj instanceof PredicateParser ) ) return false;
        PredicateParser predicateParser = ( PredicateParser ) obj;
        if( isRoutePredicate ){
            return routeFrom.equals( predicateParser.routeFrom ) && routeTo.equals( predicateParser.routeTo );
        }else{
            return flightNumber.equals( predicateParser.flightNumber ) &&
                   flightPlane.equals( predicateParser.flightPlane ) &&
                   flightDepartureFromDate.equals( predicateParser.flightPlane ) &&
                   flightDepartureToDate.equals( predicateParser.flightDepartureToDate ) &&
                   flightArriveFromDate.equals( predicateParser.flightArriveFromDate ) &&
                   flightArriveToDate.equals( predicateParser.flightArriveToDate ) &&
                   flightTimeFrom.equals( predicateParser.flightTimeFrom ) &&
                   flightTimeTo.equals( predicateParser.flightTimeTo ) &&
                   flightFrom.equals( predicateParser.flightFrom ) &&
                   flightTo.equals( predicateParser.flightTo );
        }
    }

    @Override
    public int hashCode(){
        if( isRoutePredicate ){
            return routeFrom.hashCode() ^ routeTo.hashCode();
        }else{
            return flightNumber.hashCode() ^
                   flightPlane.hashCode() ^
                   flightDepartureFromDate.hashCode() ^
                   flightDepartureToDate.hashCode() ^
                   flightArriveFromDate.hashCode() ^
                   flightArriveToDate.hashCode() ^
                   flightTimeFrom.hashCode() ^
                   flightTimeTo.hashCode() ^
                   flightFrom.hashCode() ^
                   flightTo.hashCode();
        }
    }

    private String routeFrom;
    private String routeTo;
    private String flightNumber;
    private String flightPlane;
    private String flightDepartureFromDate;
    private String flightDepartureToDate;
    private String flightArriveFromDate;
    private String flightArriveToDate;
    private String flightTimeFrom;
    private String flightTimeTo;
    private String flightFrom;
    private String flightTo;
    //    If false, it's flight predicate

    private Boolean isRoutePredicate;

    public PredicateParser(){
    }

    private PredicateParser( String routeFrom , String routeTo , Boolean isRoutePredicate ){
        this.routeFrom = routeFrom;
        this.routeTo = routeTo;
        this.isRoutePredicate = isRoutePredicate;
    }

    private PredicateParser( String flightNumber , String flightPlane , String flightDepartureFromDate ,
                             String flightDepartureToDate , String flightArriveFromDate , String flightArriveToDate ,
                             String flightTimeFrom , String flightTimeTo , String flightFrom , String flightTo ,
                             Boolean isRoutePredicate ){
        this.flightNumber = flightNumber;
        this.flightPlane = flightPlane;
        this.flightDepartureFromDate = flightDepartureFromDate;
        this.flightDepartureToDate = flightDepartureToDate;
        this.flightArriveFromDate = flightArriveFromDate;
        this.flightArriveToDate = flightArriveToDate;
        this.flightTimeFrom = flightTimeFrom;
        this.flightTimeTo = flightTimeTo;
        this.flightFrom = flightFrom;
        this.flightTo = flightTo;
        this.isRoutePredicate = isRoutePredicate;
    }

    public static PredicateParser createRoutePredicate( String routeFrom , String routeTo ){
        return new PredicateParser( routeFrom , routeTo , true );
    }

    public static PredicateParser createFlightPredicate( String flightNumber , String flightPlane ,
                                                         String flightDepartureFromDate , String flightDepartureToDate ,
                                                         String flightArriveFromDate , String flightArriveToDate ,
                                                         String flightTimeFrom , String flightTimeTo ,
                                                         String flightFrom , String flightTo ){
        return new PredicateParser( flightNumber ,
                                    flightPlane ,
                                    flightDepartureFromDate ,
                                    flightDepartureToDate ,
                                    flightArriveFromDate ,
                                    flightArriveToDate ,
                                    flightTimeFrom ,
                                    flightTimeTo ,
                                    flightFrom ,
                                    flightTo ,
                                    true );
    }

    @JsonIgnore
    // Each field nullable, means empty predicate
    public Predicate<Route> getRoutePredicate(){
        if( !isRoutePredicate ) throw new IllegalStateException( "Not route predicate" );
        return route -> Optional.ofNullable( routeFrom )
                                .map( from -> getStringPattern( from ).asPredicate() )
                                .orElse( s -> true )
                                .test( route.getFrom().getId() ) &&
                        Optional.ofNullable( routeTo )
                                .map( to -> getStringPattern( to ).asPredicate() )
                                .orElse( s -> true )
                                .test( route.getTo().getId() );
    }

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern( "dd.MM.yyyy" );

    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern( "HH:mm" );

    @JsonIgnore
    // Each field nullable, means empty predicate
    public Predicate<Flight> getFlightPredicate(){
        if( isRoutePredicate ) throw new IllegalStateException( "Not flight predicate" );
        return flight -> Optional.ofNullable( flightNumber )
                                 .map( number -> getStringPattern( number ).asPredicate() )
                                 .orElse( s -> true )
                                 .test( flight.getNumber() ) &&
                         Optional.ofNullable( flightPlane )
                                 .map( planeId -> getStringPattern( planeId ).asPredicate() )
                                 .orElse( s -> true )
                                 .test( flight.getPlaneID() ) &&
//                   todo : Разобраться, лучше приводить время полета к местному или нет
                         Optional.ofNullable( flightDepartureFromDate )
                                 .map( fromDate -> LocalDate.parse( fromDate , dateFormatter ) )
                                 .map( fromDate -> ( Predicate<LocalDate> ) fromDate::isBefore )
                                 .orElse( date -> true )
                                 .test( flight.getDepartureDateTime()
                                              .withZoneSameInstant( ZoneId.systemDefault() )
                                              .toLocalDate() ) &&
                         Optional.ofNullable( flightDepartureToDate )
                                 .map( toDate -> LocalDate.parse( toDate , dateFormatter ) )
                                 .map( toDate -> ( Predicate<LocalDate> ) toDate::isAfter )
                                 .orElse( date -> true )
                                 .test( flight.getDepartureDateTime()
                                              .withZoneSameInstant( ZoneId.systemDefault() )
                                              .toLocalDate() ) &&
                         Optional.ofNullable( flightArriveFromDate )
                                 .map( fromDate -> LocalDate.parse( fromDate , dateFormatter ) )
                                 .map( fromDate -> ( Predicate<LocalDate> ) fromDate::isBefore )
                                 .orElse( date -> true )
                                 .test( flight.getArriveDateTime()
                                              .withZoneSameInstant( ZoneId.systemDefault() )
                                              .toLocalDate() ) &&
                         Optional.ofNullable( flightArriveToDate )
                                 .map( toDate -> LocalDate.parse( toDate , dateFormatter ) )
                                 .map( toDate -> ( Predicate<LocalDate> ) toDate::isAfter )
                                 .orElse( date -> true )
                                 .test( flight.getArriveDateTime()
                                              .withZoneSameInstant( ZoneId.systemDefault() )
                                              .toLocalDate() ) &&

                         Optional.ofNullable( flightTimeFrom )
                                 .map( this::getMillisFromHoursAndMinutes )
                                 .map( fromTime -> ( Predicate<Long> ) time -> time > fromTime )
                                 .orElse( time -> true )
                                 .test( flight.getTravelTime() ) &&
                         Optional.ofNullable( flightTimeTo )
                                 .map( this::getMillisFromHoursAndMinutes )
                                 .map( toTime -> ( Predicate<Long> ) time -> time < toTime )
                                 .orElse( time -> true )
                                 .test( flight.getTravelTime() ) &&
                         Optional.ofNullable( flightFrom )
                                 .map( from -> getStringPattern( from ).asPredicate() )
                                 .orElse( s -> true )
                                 .test( flight.getRoute().getFrom().getId() ) &&
                         Optional.ofNullable( flightTo )
                                 .map( to -> getStringPattern( to ).asPredicate() )
                                 .orElse( s -> true )
                                 .test( flight.getRoute().getTo().getId() );
    }

    private Long getMillisFromHoursAndMinutes( String duration ){
        Matcher matcher = Pattern.compile( "^(?<hours>[^0]\\d+):(?<minutes>[0-5]\\d)$" ).matcher( duration );
        if( !matcher.matches() ){
            throw new IllegalArgumentException( "Bad duration format " + duration );
        }
        Long hours   = Long.parseLong( matcher.group( "hours" ) );
        Long minutes = Long.parseLong( matcher.group( "minutes" ) );
        return Duration.ofHours( hours ).plus( Duration.ofMinutes( minutes ) ).toMillis();
    }

    private Pattern getStringPattern( String searchText ){
        return Pattern.compile( ".*" + searchText.replaceAll( "\\*" , ".*" ).replaceAll( "\\?" , "." ) + ".*" ,
                                Pattern.CASE_INSENSITIVE );
    }

    public String getRouteFrom(){
        return routeFrom;
    }

    public void setRouteFrom( String routeFrom ){
        this.routeFrom = routeFrom;
    }

    public String getRouteTo(){
        return routeTo;
    }

    public void setRouteTo( String routeTo ){
        this.routeTo = routeTo;
    }

    public String getFlightNumber(){
        return flightNumber;
    }

    public void setFlightNumber( String flightNumber ){
        this.flightNumber = flightNumber;
    }

    public String getFlightPlane(){
        return flightPlane;
    }

    public void setFlightPlane( String flightPlane ){
        this.flightPlane = flightPlane;
    }

    public String getFlightDepartureFromDate(){
        return flightDepartureFromDate;
    }

    public void setFlightDepartureFromDate( String flightDepartureFromDate ){
        this.flightDepartureFromDate = flightDepartureFromDate;
    }

    public String getFlightDepartureToDate(){
        return flightDepartureToDate;
    }

    public void setFlightDepartureToDate( String flightDepartureToDate ){
        this.flightDepartureToDate = flightDepartureToDate;
    }

    public String getFlightArriveFromDate(){
        return flightArriveFromDate;
    }

    public void setFlightArriveFromDate( String flightArriveFromDate ){
        this.flightArriveFromDate = flightArriveFromDate;
    }

    public String getFlightArriveToDate(){
        return flightArriveToDate;
    }

    public void setFlightArriveToDate( String flightArriveToDate ){
        this.flightArriveToDate = flightArriveToDate;
    }

    public String getFlightTimeFrom(){
        return flightTimeFrom;
    }

    public void setFlightTimeFrom( String flightTimeFrom ){
        this.flightTimeFrom = flightTimeFrom;
    }

    public String getFlightTimeTo(){
        return flightTimeTo;
    }

    public void setFlightTimeTo( String flightTimeTo ){
        this.flightTimeTo = flightTimeTo;
    }

    public String getFlightFrom(){
        return flightFrom;
    }

    public void setFlightFrom( String flightFrom ){
        this.flightFrom = flightFrom;
    }

    public String getFlightTo(){
        return flightTo;
    }

    public void setFlightTo( String flightTo ){
        this.flightTo = flightTo;
    }

    public void setRoutePredicate( Boolean routePredicate ){
        isRoutePredicate = routePredicate;
    }
}

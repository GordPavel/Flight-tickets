package transport;

import model.Flight;
import model.FlightOrRoute;
import model.Route;
import org.danekja.java.util.function.serializable.SerializablePredicate;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PredicateParser {
    private String                                         routeTo;
    private String                                         routeFrom;
    private String                                         flightTo;
    private String                                         flightFrom;
    private String                                         flightNumber;
    private String                                         flightPlane;
    private String                                         depFromDate;
    private String                                         depToDate;
    private String                                         arrFromDate;
    private String                                         arrToDate;
    private String                                         flightToTime;
    private String                                         flightFromTime;

    public static String getPredicateString(String routeFrom,String routeTo){

        String from = routeFrom==null? "" : "routeFrom:"+routeFrom+" ";
        String to = routeTo==null? "" : "routeTo:"+routeTo;
        return from+to;
    }

    public static String getPredicateString(String flightNumber,String flightPlane, String depFromDate, String depToDate,
                                            String arrFromDate, String arrToDate, String flightFromTime, String flightToTime,
                                            String flightFrom, String flightTo){
        String number = flightNumber.equals("") ? "" : "flightNumber:"+flightNumber+" ";
        String plane = flightPlane.equals("") ? "" : "flightPlane:"+flightPlane+" ";
        String dFromDate = depFromDate.equals("") ? "" : "depFromDate:"+depFromDate+" ";
        String dToDate = depToDate.equals("") ? "" : "depToDate:"+depToDate+" ";
        String aFromDate = arrFromDate.equals("") ? "" : "arrFromDate:"+arrFromDate+" ";
        String aToDate = arrToDate.equals("") ? "" : "arrToDate:"+arrToDate+" ";
        String fromTime = flightFromTime.equals("") ? "" : "flightFromTime:"+flightFromTime+" ";
        String toTime = flightToTime.equals("") ? "" : "flightToTime:"+flightToTime+" ";
        String from = flightFrom.equals("") ? "" : "flightFrom:"+flightFrom+" ";
        String to = flightTo.equals("") ? "" : "flightTo:"+flightTo;

        return number+plane+dFromDate+dToDate+aFromDate+aToDate+fromTime+toTime+from+to;

    }

    public static Predicate<? extends FlightOrRoute> getPredicate(String stringPredicate){
        Predicate<? extends FlightOrRoute> predicate = flightOrRoute -> true;
        Pattern           datePattern   = Pattern.compile( "^([0-2]\\d|3[0-1]).[0-1]\\d.\\d{4}$" );
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern( "dd.MM.yyyy" );
        SimpleDateFormat  dateFormat    = new SimpleDateFormat( "dd.MM.yyyy" );

        String[] parts = stringPredicate.split(" ");
        for (String part : parts)
        {
            String[] fields=part.split(":");
            switch (fields[0]){
                case "routeFrom":
                    predicate.and(route -> getRoutePattern( fields[1] ).matcher( ((Route)route).getFrom().getId() ).matches());
                    break;
                case "routeTo":
                    predicate.and(route -> getRoutePattern( fields[1] ).matcher( ((Route)route).getTo().getId() ).matches());
                    break;
                case "flightNumber":
                    predicate.and(flight -> Pattern.compile(
                            "^" + ".*" + fields[1].replaceAll( "\\*" , ".*" ).replaceAll( "\\?" , "." ) + ".*" +
                                    "$" , Pattern.CASE_INSENSITIVE ).matcher( ((Flight)flight).getNumber() ).matches());
                    break;
                case "flightPlane":
                    predicate.and(flight -> Pattern.compile(
                            "^" + ".*" + fields[1].replaceAll( "\\*" , ".*" ).replaceAll( "\\?" , "." ) + ".*" +
                                    "$" , Pattern.CASE_INSENSITIVE ).matcher( ((Flight)flight).getPlaneID() ).matches());
                    break;
                case "depFromDate":
                    predicate.and(flight ->
                            getDateTimePredicate( fields[1] , datePattern , dateFormatter ,
                                    dateFormat , true ).test( ((Flight)flight).getDepartureDateTime() ));
                    break;
                case "depToDate":
                    predicate.and(flight ->
                            getDateTimePredicate( fields[1] , datePattern , dateFormatter ,
                                    dateFormat , false ).test( ((Flight)flight).getDepartureDateTime() ));
                    break;
                case "arrFromDate":
                    predicate.and(flight ->
                            getDateTimePredicate( fields[1] , datePattern , dateFormatter ,
                                    dateFormat , true ).test( ((Flight)flight).getArriveDateTime() ));
                    break;
                case "arrToDate":
                    predicate.and(flight ->
                            getDateTimePredicate( fields[1] , datePattern , dateFormatter ,
                                    dateFormat , false ).test( ((Flight)flight).getArriveDateTime() ));
                    break;
                case "flightFromTime":
                    predicate.and(flight ->
                            Long.parseLong(fields[1],10) <=
                                    ((Flight)flight).getTravelTime());
                    break;
                case "flightToTime":
                    predicate.and(flight ->
                            Long.parseLong(fields[1],10) >=
                                    ((Flight)flight).getTravelTime());
                    break;
                case "flightFrom":
                    predicate.and(flight -> getRoutePattern( fields[1] ).matcher( ((Flight)flight).getRoute().getFrom().getId() ).matches());
                    break;
                case "flightTo":
                    predicate.and(flight -> getRoutePattern( fields[1] ).matcher( ((Flight)flight).getRoute().getTo().getId() ).matches());
                    break;
            }
        }

        return predicate;
    }

    private static Pattern getRoutePattern( String searchText ){
        return Pattern.compile( ".*" + searchText.replaceAll( "\\*" , ".*" ).replaceAll( "\\?" , "." ) + ".*" ,
                Pattern.CASE_INSENSITIVE );
    }

    private static SerializablePredicate<ZonedDateTime> getDateTimePredicate(String inputDate , Pattern datePattern ,
                                                                      DateTimeFormatter dateFormatter ,
                                                                      SimpleDateFormat dateFormat , Boolean before ){
        SerializablePredicate<ZonedDateTime> datePredicate;
        if( datePattern.matcher( inputDate ).matches() ){
            datePredicate = date -> {
                LocalDate inputLocalDate  = LocalDate.parse( inputDate , dateFormatter );
                LocalDate flightLocalDate = LocalDate.parse( dateFormat.format( date ) , dateFormatter );
                return before ? !inputLocalDate.isAfter( flightLocalDate ) :
                        !inputLocalDate.isBefore( flightLocalDate );
            };
        }else{
            datePredicate = date -> true;
        }
        return datePredicate;
    }
}

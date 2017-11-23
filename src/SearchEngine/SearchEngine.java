package SearchEngine;

import model.*;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/*

Search Engine allows to get collections of flights and routs, stored in DataModel, sorted by parameters, specified in each method
 */

public class SearchEngine {


    DataModel data;

    SearchEngine (DataModel data)
    {
        this.data=data;
    }

    /**
     @param to
     */
    public List<Route> findRoutesByArrivalAirport(String to) {

        List<Route> set =  data.listRoutesWithPredicate(route -> true ).collect(Collectors.toList());
        List<Route> result = new ArrayList<>();
        Pattern pattern = Pattern.compile(to.toUpperCase().replace("*",".*").replace("?","."));

        for (Route route:set) {
            Matcher matcher=pattern.matcher(route.getFrom().toUpperCase());
            if (matcher.matches()){
                result.add(route);
            }
        }
        return result;
    }

    /**
     @param from
     */
    public List<Route> findRoutesByDepartureAirport(String from) {

        List<Route> set =  data.listRoutesWithPredicate(route -> true ).collect(Collectors.toList());
        List<Route> result = new ArrayList<>();
        Pattern pattern = Pattern.compile(from.toUpperCase().replace("*",".*").replace("?","."));

        for (Route route:set) {
            Matcher matcher=pattern.matcher(route.getTo().toUpperCase());
            if (matcher.matches()){
                result.add(route);
            }
        }
        return result;
    }

    /**
     @param number
     */
    public List<Flight> findFlightByNumber(String number) {

        List<Flight> list =  data.listFlightsWithPredicate(flight -> true ).collect(Collectors.toList());
        List<Flight> result = new ArrayList<Flight>();
        Pattern pattern = Pattern.compile(number.toUpperCase().replace("*",".*").replace("?","."));

        for (Flight flight:list) {
            Matcher matcher=pattern.matcher(flight.getNumber().toUpperCase());
            if (matcher.matches()){
                result.add(flight);
            }
        }
        return result;
    }

    /**
     @param route
     */
    public List<Flight> findFlightsByRoute(Route route) {

        List<Flight> list =  data.listFlightsWithPredicate(flight -> true ).collect(Collectors.toList());
        List<Flight> result = new ArrayList<Flight>();
        Pattern patternTo = Pattern.compile(route.getTo().toUpperCase().replace("*",".*").replace("?","."));
        Pattern patternFrom = Pattern.compile(route.getFrom().toUpperCase().replace("*",".*").replace("?","."));
        for (Flight flight:list) {
            Matcher matcherTo=patternTo.matcher(flight.getRoute().getTo().toUpperCase());
            Matcher matcherFrom=patternFrom.matcher(flight.getRoute().getFrom().toUpperCase());
            if (matcherTo.matches()&&matcherFrom.matches()){
                result.add(flight);
            }
        }
        return result;
    }

    /**
     @param to
     */
    public List<Flight> findFlightsByArrivalAirport(String to) {

        List<Flight> list =  data.listFlightsWithPredicate(flight -> true ).collect(Collectors.toList());
        List<Flight> result = new ArrayList<Flight>();
        Pattern patternTo = Pattern.compile(to.toUpperCase().replace("*",".*").replace("?","."));
        for (Flight flight:list) {
            Matcher matcherTo=patternTo.matcher(flight.getRoute().getTo().toUpperCase());
            if (matcherTo.matches()){
                result.add(flight);
            }
        }
        return result;
    }

    /**
     @param from
     */
    public List<Flight> findFlightsByDepartureAirport(String from) {

        List<Flight> list =  data.listFlightsWithPredicate(flight -> true ).collect(Collectors.toList());
        List<Flight> result = new ArrayList<Flight>();
        Pattern patternFrom = Pattern.compile(from.toUpperCase().replace("*",".*").replace("?","."));
        for (Flight flight:list) {
            Matcher matcherFrom=patternFrom.matcher(flight.getRoute().getFrom().toUpperCase());
            if (matcherFrom.matches()){
                result.add(flight);
            }
        }
        return result;
    }

    /**
     @param id
     */
    public List<Flight> findFlightsByPlane(String id) {

        List<Flight> list =  data.listFlightsWithPredicate(flight -> true ).collect(Collectors.toList());
        List<Flight> result = new ArrayList<Flight>();
        Pattern pattern = Pattern.compile(id.toUpperCase().replace("*",".*").replace("?","."));
        for (Flight flight:list) {
            Matcher matcher=pattern.matcher(flight.getRoute().getFrom().toUpperCase());
            if (matcher.matches()){
                result.add(flight);
            }
        }
        return result;
    }

    /**
     @param from
     @param to
     */
    public List<Flight> findFlightsByArrivalTime(Date from, Date to) {

        List<Flight> list =  data.listFlightsWithPredicate(flight -> true ).collect(Collectors.toList());
        List<Flight> result = new ArrayList<Flight>();
        for (Flight flight:list) {
            if (flight.getArriveDate().after(from)&&flight.getArriveDate().before(to)){
                result.add(flight);
            }
        }
        return result;
    }

    /**
     @param exact
     */
    public List<Flight> findFlightsByArrivalTime(Date exact) {

        List<Flight> list =  data.listFlightsWithPredicate(flight -> true ).collect(Collectors.toList());
        List<Flight> result = new ArrayList<Flight>();
        for (Flight flight:list) {
            if (flight.getArriveDate().equals(exact)){
                result.add(flight);
            }
        }
        return result;
    }

    /**
     @param from
     @param to
     */
    public List<Flight> findFlightsByDepartureTime(Date from, Date to) {

        List<Flight> list =  data.listFlightsWithPredicate(flight -> true ).collect(Collectors.toList());
        List<Flight> result = new ArrayList<Flight>();
        for (Flight flight:list) {
            if (flight.getDepartureDate().after(from)&&flight.getDepartureDate().before(to)){
                result.add(flight);
            }
        }
        return result;
    }

    /**
     @param exact
     */
    public List<Flight> findFlightsByDepartureTime(Date exact) {

        List<Flight> list =  data.listFlightsWithPredicate(flight -> true ).collect(Collectors.toList());
        List<Flight> result = new ArrayList<Flight>();
        for (Flight flight:list) {
            if (flight.getDepartureDate().equals(exact)){
                result.add(flight);
            }
        }
        return result;

    }

    /**
     @param fromArrival
     @param toArrival
     @param fromDeparture
     @param toDeparture
     */
    public List<Flight> findFlightsByArrivalAndDepartureTime(Date fromArrival, Date toArrival, Date fromDeparture, Date toDeparture) {

        List<Flight> list =  data.listFlightsWithPredicate(flight -> true ).collect(Collectors.toList());
        List<Flight> result = new ArrayList<Flight>();
        for (Flight flight:list) {
            if (flight.getDepartureDate().after(fromDeparture)&&flight.getDepartureDate().before(toDeparture)&&flight.getArriveDate().after(fromArrival)&&flight.getArriveDate().before(toArrival)){
                result.add(flight);
            }
        }
        return result;
    }

    /**
     @param exactArrival
     @param exactDeparture
     */
    public List<Flight> findFlightsByArrivalAndDepartureTime(Date exactArrival, Date exactDeparture) {

        List<Flight> list =  data.listFlightsWithPredicate(flight -> true ).collect(Collectors.toList());
        List<Flight> result = new ArrayList<Flight>();
        for (Flight flight:list) {
            if (flight.getArriveDate().equals(exactArrival)&&flight.getDepartureDate().equals(exactDeparture)){
                result.add(flight);
            }
        }
        return result;
    }


    public List<Flight> searchFlight(String number, Route route, String from, String to, String planeID, Date startArrival, Date stopArrival, Date startDeparture, Date stopDeparture)
    {

        Pattern numberPattern = Pattern.compile(number.replace("*",".*").replace("?","."));
        Pattern fromPattern = Pattern.compile(from.replace("*",".*").replace("?","."));
        Pattern toPattern = Pattern.compile(to.replace("*",".*").replace("?","."));
        Pattern planeIDPattern = Pattern.compile(planeID.replace("*",".*").replace("?","."));

        return data.listFlightsWithPredicate(flight ->
                (number != "" ? numberPattern.matcher(flight.getNumber()).matches() : true )
                && ( from != "" ? fromPattern.matcher(flight.getRoute().getFrom()).matches() : true )
                && ( to != "" ? toPattern.matcher(flight.getRoute().getTo()).matches() : true )
                && ( planeID != "" ? planeIDPattern.matcher(flight.getPlaneID()).matches() : true )
                && ( from != "" ? fromPattern.matcher(flight.getRoute().getFrom()).matches() : true )
                && ( route!= null ? route.equals(flight.getRoute()) : true )
                && ( (startArrival!= null)&&(stopArrival==null) ? startArrival.equals(flight.getArriveDate()) : true)
                && ( (startArrival!= null)&&(stopArrival!=null) ? flight.getArriveDate().after(startArrival)&&flight.getArriveDate().before(stopArrival) : true)
                && ( (startDeparture!= null)&&(stopDeparture==null) ? startDeparture.equals(flight.getDepartureDate()) : true)
                && ( (startDeparture!= null)&&(stopDeparture!=null) ? flight.getDepartureDate().after(startDeparture)&&flight.getDepartureDate().before(stopDeparture) : true)
        ).collect(Collectors.toList());
    }

    public List<Route> searchRoute(String from, String to)
    {

        Pattern fromPattern = Pattern.compile(from.replace("*",".*").replace("?","."));
        Pattern toPattern = Pattern.compile(to.replace("*",".*").replace("?","."));

        return data.listRoutesWithPredicate(route ->
                ( to != "" ? toPattern.matcher(route.getTo()).matches() : true )
                        && ( from != "" ? fromPattern.matcher(route.getFrom()).matches() : true )
        ).collect(Collectors.toList());
    }

}

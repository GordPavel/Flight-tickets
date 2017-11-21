package SearchEngine;

import model.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public Set<Route> findRoutesByArrivalAirport(String to) {

        Set<Route> set =  data.listRoutesWithPredicate(route -> true );
        Set<Route> result = new HashSet<Route>();
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
    public Set<Route> findRoutesByDepartureAirport(String from) {

        Set<Route> set =  data.listRoutesWithPredicate(route -> true );
        Set<Route> result = new HashSet<Route>();
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

        List<Flight> list =  data.listFlightsWithPredicate(flight -> true );
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

        List<Flight> list =  data.listFlightsWithPredicate(flight -> true );
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

        List<Flight> list =  data.listFlightsWithPredicate(flight -> true );
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

        List<Flight> list =  data.listFlightsWithPredicate(flight -> true );
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

        List<Flight> list =  data.listFlightsWithPredicate(flight -> true );
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

        List<Flight> list =  data.listFlightsWithPredicate(flight -> true );
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

        List<Flight> list =  data.listFlightsWithPredicate(flight -> true );
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

        List<Flight> list =  data.listFlightsWithPredicate(flight -> true );
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

        List<Flight> list =  data.listFlightsWithPredicate(flight -> true );
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

        List<Flight> list =  data.listFlightsWithPredicate(flight -> true );
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

        List<Flight> list =  data.listFlightsWithPredicate(flight -> true );
        List<Flight> result = new ArrayList<Flight>();
        for (Flight flight:list) {
            if (flight.getArriveDate().equals(exactArrival)&&flight.getDepartureDate().equals(exactDeparture)){
                result.add(flight);
            }
        }
        return result;
    }

}

package searchengine;

import model.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/*

Search Engine allows to get collections of flights and routs, stored in DataModel, sorted by parameters, specified in each method


@author teomant */

public class SearchEngine {


    /**
     store data model, connected to this search engine
     */

    DataModel data;


    /**
      @param data - datamodel
     */
    SearchEngine (DataModel data)
    {
        this.data=data;
    }

    /**
     @param to - arraival airport name

     @return list of routs, filtred by arraival airport
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
     @param from - departure airport name

     @return list of routs, filtred by departure airport
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
     @param number - flight number

     @return list of flights, filtred by flight number. List can have 0-1 elements, because number - unique parameter
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
     @param route - flight`s route

     @return list of flights, filtred by route
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
     @param to - flight`s arrival airport name

     @return list of flights, filtred by arrival airport name
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
     @param from - flight`s arrival airport name

     @return list of flights, filtred by departure airport name
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
     @param id - flight`s airplane name

     @return list of flights, filtred by airplane name
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
     @param from - start of arraival time interval
     @param to - stop of arraival time interval

     @return list of flights, filtred by arraival time (arraival time between from and to)
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
     @param exact - exact arraival time

     @return list of flights, filtred by arraival time (only exact time)
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
     @param from - start of departure time interval
     @param to - stop of departure time interval

     @return list of flights, filtred by departure time (arraival time between from and to)
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
     @param exact - exact departure time

     @return list of flights, filtred by departure time (only exact time)
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
     @param fromArrival - start of arrival time interval
     @param toArrival - stop of arrival time interval
     @param fromDeparture - start of departure time interval
     @param toDeparture - stop of departure time interval

     @return list of flights, filtred by departure time (arraival time between fromArraival and toArraival, departure time between fromDeparture and toDeparture)
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
     @param exactArrival - exact arraival time
     @param exactDeparture - exact departure time

     @return list of flights, filtred by arraival and departure time (only exact time)
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

    /**
     @param number - flight number pattern
     @param route - flight`s route
     @param from - flight`s departure airport pattern
     @param to - flight`s arraival airport pattern
     @param planeID - flight`s airplain ID pattern
     @param startArrival - start of arraival date interval (if stopArraival==null, counts as exact date)
     @param stopArrival  - stop of arraival date interval
     @param startDeparture - start of departure date interval (if stopDeparture==null, counts as exact date)
     @param stopDeparture - stop of departure date interval

     @return list of flights, filtred by predicate, generated from params
     */

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


    /**
     @param from - departure airport pattern
     @param to - arraival airport pattern

     @return list of routes, flitref by predicate, generated from params
     */

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

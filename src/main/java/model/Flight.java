package model;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 Entity to store data about each flight.

 @author pavelgordeev email: pvgord@icloud.com */
public class Flight implements Serializable, Cloneable{

    private static final long serialVersionUID = 1L;

    public Flight( String number , Route route , String planeID , ZonedDateTime departureDate ,
                   ZonedDateTime arriveDate ){
        this.number = number;
        this.route = route;
        this.planeID = planeID;
        this.departureDate = departureDate;
        this.arriveDate = arriveDate;
    }

    /**
     Stores unique number of each flight
     */
    private String number;

    public String getNumber(){
        return number;
    }

    void setNumber( String number ){
        this.number = number;
    }

    /**
     Stores route that this flight connects
     */
    private Route route;

    public Route getRoute(){
        return route;
    }

    void setRoute( Route route ){
        this.route = route;
    }

    /**
     Stores unique ID of plane that makes this flight
     */
    private String planeID;

    public String getPlaneID(){
        return planeID;
    }

    void setPlaneID( String planeID ){
        this.planeID = planeID;
    }

    /**
     Stores date and time, when plane have to take off
     */
    private ZonedDateTime departureDate;

    public ZonedDateTime getDepartureDate(){
        return departureDate;
    }

    void setDepartureDate( ZonedDateTime date ){
        this.departureDate = date;
    }

    /**
     Stores date and time, when plane have to launch
     */
    private ZonedDateTime arriveDate;

    public ZonedDateTime getArriveDate(){
        return arriveDate;
    }

    void setArriveDate( ZonedDateTime date ){
        this.arriveDate = date;
    }

    /**
     @return countable field, difference between departureDate and arriveDate in milliseconds
     */
    public Long getTravelTime(){
        return Math.abs( ChronoUnit.MILLIS.between( departureDate.toLocalDateTime() , arriveDate.toLocalDateTime() ) );
    }

    @Override
    public int hashCode(){
        return number.hashCode() ^ route.hashCode() ^ planeID.hashCode() ^ arriveDate.hashCode() ^
               departureDate.hashCode();
    }

    @Override
    public boolean equals( Object obj ){
        if( !( obj instanceof Flight ) ) return false;
        Flight flight = ( Flight ) obj;
        return this.number.equals( flight.number ) && this.route.equals( flight.route ) &&
               planeID.equals( flight.planeID ) && departureDate.equals( flight.departureDate ) &&
               arriveDate.equals( flight.arriveDate );
    }

    boolean pointsEquals( Object obj ){
        if( !( obj instanceof Flight ) ) return false;
        Flight flight = ( Flight ) obj;
        return this.number.equals( flight.number ) && this.route.pointsEquals( flight.route ) &&
               planeID.equals( flight.planeID ) && departureDate.equals( flight.departureDate ) &&
               arriveDate.equals( flight.arriveDate );
    }

    @Override
    protected Object clone() throws CloneNotSupportedException{
        Flight clone = ( Flight ) super.clone();
        clone.number = this.number;
        clone.route = ( Route ) this.route.clone();
        clone.planeID = this.planeID;
        clone.arriveDate = this.arriveDate;
        clone.departureDate = this.departureDate;
        return clone;
    }


    /**
     Looks like "Flight number (number), takes at (dd.MM.yyyy HH:mm) from (arrival airportId), launches at
     (dd.MM.yyyy HH:mm) at (departure airportId),
     flies by (planeId) plane"

     @return String view of flight
     */
    @Override
    public String toString(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "dd.MM.yyyy HH:mm" );
        return String.format(
                "Flight number %s\n takes at %s from %s\n launches at %s at %s\n flight time %s\n flies by %s " +
                "plane" , number , departureDate.format( formatter ) , route.getFrom() ,
                arriveDate.format( formatter ) , route.getTo() , millisToHoursAndMinutes( getTravelTime() ) , planeID );
    }

    public String millisToHoursAndMinutes( Long startMilli ){
        startMilli /= 1000; // sum of second
        long sumMinute = startMilli / 60; // sum of minute
        long minute    = sumMinute % 60; // minute
        long hour      = sumMinute / 60; // hour
        return String.format( "%d:%02d" , hour , minute );
    }
}
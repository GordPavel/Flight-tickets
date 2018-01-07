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

    public Flight( String number , Route route , String planeID , ZonedDateTime departureDateTime ,
                   ZonedDateTime arriveDateTime ){
        this.number = number;
        this.route = route;
        this.planeID = planeID;
        this.departureDateTime = departureDateTime;
        this.arriveDateTime = arriveDateTime;
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
    private ZonedDateTime departureDateTime;

    public ZonedDateTime getDepartureDateTime(){
        return departureDateTime;
    }

    void setDepartureDateTime( ZonedDateTime date ){
        this.departureDateTime = date;
    }

    /**
     Stores date and time, when plane have to launch
     */
    private ZonedDateTime arriveDateTime;

    public ZonedDateTime getArriveDateTime(){
        return arriveDateTime;
    }

    void setArriveDateTime( ZonedDateTime date ){
        this.arriveDateTime = date;
    }

    /**
     @return countable field, difference between departureDateTime and arriveDateTime in milliseconds
     */
    public Long getTravelTime(){
        return ChronoUnit.MILLIS.between( departureDateTime , arriveDateTime );
    }

    public String getTravelTimeInHoursAndMinutes(){
        long startMilli = getTravelTime();
        startMilli /= 1000; // sum of second
        long sumMinute = startMilli / 60; // sum of minute
        long minute    = sumMinute % 60; // minute
        long hour      = sumMinute / 60; // hour
        return String.format( "%d:%02d" , hour , minute );
    }

    @Override
    public int hashCode(){
        return number.hashCode() ^ route.hashCode() ^ planeID.hashCode() ^ arriveDateTime.hashCode() ^
               departureDateTime.hashCode();
    }

    @Override
    public boolean equals( Object obj ){
        if( !( obj instanceof Flight ) ) return false;
        Flight flight = ( Flight ) obj;
        return this.number.equals( flight.number ) && this.route.equals( flight.route ) &&
               planeID.equals( flight.planeID ) && departureDateTime.equals( flight.departureDateTime ) &&
               arriveDateTime.equals( flight.arriveDateTime );
    }

    boolean pointsEquals( Object obj ){
        if( !( obj instanceof Flight ) ) return false;
        Flight flight = ( Flight ) obj;
        return this.number.equals( flight.number ) && this.route.pointsEquals( flight.route ) &&
               planeID.equals( flight.planeID ) && departureDateTime.equals( flight.departureDateTime ) &&
               arriveDateTime.equals( flight.arriveDateTime );
    }

    @Override
    protected Object clone() throws CloneNotSupportedException{
        Flight clone = ( Flight ) super.clone();
        clone.number = this.number;
        clone.route = ( Route ) this.route.clone();
        clone.planeID = this.planeID;
        clone.arriveDateTime = this.arriveDateTime;
        clone.departureDateTime = this.departureDateTime;
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
                "plane" , number , departureDateTime.format( formatter ) , route.getFrom() ,
                arriveDateTime.format( formatter ) , route.getTo() , getTravelTimeInHoursAndMinutes() , planeID );
    }

}
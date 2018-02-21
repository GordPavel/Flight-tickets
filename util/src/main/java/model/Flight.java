package model;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 Entity to store data about each flight.

 @author pavelgordeev email: pvgord@icloud.com */
@JsonRootName( "Flight" )
public class Flight implements FlightOrRoute, Serializable, Cloneable{

    private static final long serialVersionUID = 1L;
    /**
     Stores date and time, when plane have to take off
     */
    ZonedDateTime departureDateTime;
    /**
     Stores date and time, when plane have to launch
     */
    ZonedDateTime arriveDateTime;
    /**
     Stores unique number of each flight
     */
    private String number;
    /**
     Stores route that this flight connects
     */
    private Route route;
    /**
     Stores unique ID of plane that makes this flight
     */
    private String planeID;

    @JsonCreator
    public Flight(
            @JsonProperty( "number" )
                    String number ,
            @JsonProperty( "route" )
                    Route route ,
            @JsonProperty( "planeID" )
                    String planeID ,
            @JsonProperty( "departureDateTime" )
            @JsonSerialize( using = ZonedDateTimeSerializer.class )
            @JsonDeserialize( using = ZonedDateTimeDeserializer.class )
                    ZonedDateTime departureDateTime ,
            @JsonProperty( "arriveDateTime" )
            @JsonSerialize( using = ZonedDateTimeSerializer.class )
            @JsonDeserialize( using = ZonedDateTimeDeserializer.class )
                    ZonedDateTime arriveDateTime ){
        this.number = number;
        this.route = route;
        this.planeID = planeID;
        this.departureDateTime = departureDateTime;
        this.arriveDateTime = arriveDateTime;
    }

    @JsonGetter( "number" )
    public String getNumber(){
        return number;
    }

    @JsonGetter( "route" )
    public Route getRoute(){
        return route;
    }

    public void setRoute( Route route ){
        this.route = route;
    }

    @JsonGetter( "planeID" )
    public String getPlaneID(){
        return planeID;
    }

    @JsonGetter( "departureDateTime" )
    public ZonedDateTime getDepartureDateTime(){
        return departureDateTime;
    }

    @JsonGetter( "arriveDateTime" )
    public ZonedDateTime getArriveDateTime(){
        return arriveDateTime;
    }

    /**
     @return countable field, difference between departureDateTime and arriveDateTime in milliseconds
     */
    @JsonIgnore
    public Long getTravelTime(){
        return ChronoUnit.MILLIS.between( departureDateTime , arriveDateTime );
    }

    @JsonIgnore
    private String getTravelTimeString(){
        long startMilli = getTravelTime();
        startMilli /= 1000; // sum of second
        long sumMinute = startMilli / 60; // sum of minute
        long minute = sumMinute % 60; // minute
        long hour = sumMinute / 60; // hour
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
               planeID.equals( flight.planeID ) && departureDateTime.isEqual( flight.departureDateTime ) &&
               arriveDateTime.isEqual( flight.arriveDateTime );
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
                "plane" ,
                number ,
                departureDateTime.format( formatter ) ,
                route.getFrom() ,
                arriveDateTime.format( formatter ) ,
                route.getTo() ,
                getTravelTimeString() ,
                planeID );
    }

}
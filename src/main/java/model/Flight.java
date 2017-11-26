package model;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 Entity to store data about each flight.

 @author pavelgordeev email: pvgord@icloud.com */
public class Flight implements Serializable, Cloneable{

    private static final long serialVersionUID = 1L;

    public Flight( String number , Route route , String planeID , Date departureDate , Date arrivalDate ){
        this.number = number;
        this.route = route;
        this.planeID = planeID;
        this.departureDate = departureDate;
        this.arrivalDate = arrivalDate;
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
    private Date departureDate;

    public Date getDepartureDate(){
        return departureDate;
    }

    void setDepartureDate(Date date ){
        this.departureDate = date;
    }

    /**
     Stores date and time, when plane have to land
     */
    private Date arrivalDate;

    public Date getArrivalDate(){
        return arrivalDate;
    }

    void setArrivalDate( Date date ){
        this.arrivalDate = date;
    }

    /**
     @return countable field, difference between arrivalDate and departureDate
     */
    public Date getTravelTime(){
        return Date.from( Instant.ofEpochMilli( arrivalDate.getTime() - departureDate.getTime() ) );
    }

    @Override
    public int hashCode(){
        return number.hashCode() ^ route.hashCode() ^ planeID.hashCode() ^ departureDate.hashCode() ^
               arrivalDate.hashCode();
    }

    @Override
    public boolean equals( Object obj ){
        if( !( obj instanceof Flight ) ) return false;
        Flight flight = ( Flight ) obj;
        return this.number.equals( flight.number ) && this.route.equals( flight.route ) &&
               planeID.equals( flight.planeID ) && departureDate.equals( flight.departureDate) &&
               arrivalDate.equals( flight.arrivalDate );
    }

    @Override
    protected Object clone() throws CloneNotSupportedException{
        Flight clone = ( Flight ) super.clone();
        clone.number = this.number;
        clone.route = ( Route ) this.route.clone();
        clone.planeID = this.planeID;
        clone.departureDate = ( Date ) this.departureDate.clone();
        clone.arrivalDate = ( Date ) this.arrivalDate.clone();
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
        LocalDateTime departureLocalDate =
                LocalDateTime.ofInstant( departureDate.toInstant() , zoneIdFromAirPortId( route.getFrom() ) );
        LocalDateTime arrivalLocalDate =
                LocalDateTime.ofInstant( arrivalDate.toInstant() , zoneIdFromAirPortId( route.getTo() ) );
        return String
                .format( "Flight number %s, takes at %s from %s, launches at %s at %s, flies by %s plane" , number ,
                        departureLocalDate.format( formatter ) , route.getFrom() ,
                        arrivalLocalDate.format( formatter ) , route.getTo() , planeID );
    }

    /**
     @param airportId Specify the airportId, which timezone you want to know

     @return ZoneId of specified airport
     */
    private ZoneId zoneIdFromAirPortId( String airportId ){
        return ZoneId.of( "Europe/Moscow" );
    }
}
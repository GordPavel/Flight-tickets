package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

/**
 Entity to store data about each flight.

 @author pavelgordeev email: pvgord@icloud.com */
public class Flight implements Serializable, Cloneable{

    private static final long serialVersionUID = 1L;

    public Flight( String number , Route route , String planeID , Date arriveDate , Date departureDate ){
        this.number = number;
        this.route = route;
        this.planeID = planeID;
        this.arriveDate = arriveDate;
        this.departureDate = departureDate;
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
    private Date arriveDate;

    public Date getArriveDate(){
        return arriveDate;
    }

    void setArriveDate( Date date ){
        this.arriveDate = date;
    }

    /**
     Stores date and time, when plane have to launch
     */
    private Date departureDate;

    public Date getDepartureDate(){
        return departureDate;
    }

    void setDepartureDate( Date date ){
        this.departureDate = date;
    }

    /**
     @return countable field, difference between departureDate and arriveDate
     */
    public Date getTravelTime(){
        return Date.from( departureDate.toInstant().minusMillis( arriveDate.getTime() ) );
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
               planeID.equals( flight.planeID ) && arriveDate.equals( flight.arriveDate ) &&
               departureDate.equals( flight.departureDate );
    }

    @Override
    protected Object clone() throws CloneNotSupportedException{
        Flight clone = ( Flight ) super.clone();
        clone.number = this.number;
        clone.route = ( Route ) this.route.clone();
        clone.planeID = this.planeID;
        clone.arriveDate = ( Date ) this.arriveDate.clone();
        clone.departureDate = ( Date ) this.departureDate.clone();
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
        LocalDateTime arrivalLocalDate =
                LocalDateTime.ofInstant( arriveDate.toInstant() , zoneIdFromAirPortId( route.getFrom() ) );
        LocalDateTime departureLocalDate =
                LocalDateTime.ofInstant( departureDate.toInstant() , zoneIdFromAirPortId( route.getTo() ) );
        return String
                .format( "Flight number %s, takes at %s from %s, launches at %s at %s, flies by %s plane" , number ,
                         arrivalLocalDate.format( formatter ) , route.getFrom() ,
                         departureLocalDate.format( formatter ) , route.getTo() , planeID );
    }

    /**
     @param airportId Specify the airportId, which timezone you want to know

     @return ZoneId of specified airport
     */
    private ZoneId zoneIdFromAirPortId( String airportId ){
        return ZoneId.of( "Europe/Moscow" );
    }
}
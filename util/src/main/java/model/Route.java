package model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;
import java.time.ZoneId;

/**
 Entity to store data about route between two airports.

 @author pavelgordeev email: pvgord@iclod.com */
@JsonRootName( "Route" )
public class Route implements FlightOrRoute, Serializable, Cloneable, Comparable<Route>{

    private static final long serialVersionUID = 1L;

    @JsonCreator
    public Route(
            @JsonProperty( "id" )
                    Integer id ,
            @JsonProperty( "from" )
                    ZoneId from ,
            @JsonProperty( "to" )
                    ZoneId to ){
        this.id = id;
        this.from = from;
        this.to = to;
    }

    public Route(){}

    public Route( ZoneId from , ZoneId to ){
        this.from = from;
        this.to = to;
    }

    Integer id;

    @JsonGetter( "id" )
    public Integer getId(){
        return id;
    }

    /**
     Stores unique id of arrival airport.
     */
    @JsonSerialize( using = ZoneIdSerializer.class )
    @JsonDeserialize( using = ZoneIdDeserializer.class )
    ZoneId from;

    @JsonGetter( "from" )
    public ZoneId getFrom(){
        return from;
    }

    /**
     Stores unique id of departure airport
     */
    @JsonSerialize( using = ZoneIdSerializer.class )
    @JsonDeserialize( using = ZoneIdDeserializer.class )
    ZoneId to;

    @JsonGetter( "to" )
    public ZoneId getTo(){
        return to;
    }

    @Override
    public int compareTo( Route o ){
        int from = this.from.toString().compareToIgnoreCase( o.from.toString() );
        return from != 0 ? from : this.to.toString().compareToIgnoreCase( o.to.toString() );
    }

    @Override
    public int hashCode(){
        return from.hashCode() ^ ( to.hashCode() + 31 );
    }

    @SuppressWarnings( "EqualsWhichDoesntCheckParameterClass" )
    @Override
    public boolean equals( Object obj ){
        if( !( obj instanceof Route ) ) return false;
        Route   route = ( Route ) obj;
        boolean test;
        if( id == null ){
            test = route.id == null;
        }else{
            test = this.id.equals( route.id );
        }
        return pointsEquals( route ) && test;
    }

    boolean pointsEquals( Route route ){
        return this.from.equals( route.from ) && this.to.equals( route.to );
    }

    @Override
    protected Object clone() throws CloneNotSupportedException{
        Route clone = ( Route ) super.clone();
        clone.from = ZoneId.of( from.getId() );
        clone.to = ZoneId.of( to.getId() );
        return clone;
    }

    @Override
    public String toString(){
        return String.format( "%s -> %s" , from , to );
    }
}
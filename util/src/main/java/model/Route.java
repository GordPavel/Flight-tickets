package model;

import java.io.Serializable;
import java.time.ZoneId;

/**
 Entity to store data about route between two airports.

 @author pavelgordeev email: pvgord@iclod.com */
public class Route implements Serializable, Cloneable, Comparable<Route>{

    private static final long serialVersionUID = 1L;

    public Route( ZoneId from , ZoneId to ){
        this.from = from;
        this.to = to;
    }


    private Integer id;

    Integer getId(){
        return id;
    }

    void setId( Integer id ){
        this.id = id;
    }

    /**
     Stores unique id of arrival airport.
     */
    private ZoneId from;

    public ZoneId getFrom(){
        return from;
    }

    void setFrom( ZoneId from ){
        this.from = from;
    }

    /**
     Stores unique id of departure airport
     */
    private ZoneId to;

    public ZoneId getTo(){
        return to;
    }

    void setTo( ZoneId to ){
        this.to = to;
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
        return pointsEquals( obj ) && this.id.equals( ( ( Route ) obj ).id );
    }

    boolean pointsEquals( Object obj ){
        if( !( obj instanceof Route ) ) return false;
        Route route = ( Route ) obj;
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
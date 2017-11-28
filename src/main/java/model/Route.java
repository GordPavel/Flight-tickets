package model;

import java.io.Serializable;

/**
 Entity to store data about route between two airports.

 @author pavelgordeev email: pvgord@iclod.com */
public class Route implements Serializable, Cloneable, Comparable<Route>{

    private static final long serialVersionUID = 1L;


    public Route( String from , String to ){
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
    private String from;

    public String getFrom(){
        return from;
    }

    void setFrom( String from ){
        this.from = from;
    }

    /**
     Stores unique id of departure airport
     */
    private String to;

    public String getTo(){
        return to;
    }

    void setTo( String to ){
        this.to = to;
    }

    @Override
    public int compareTo( Route o ){
        int from = this.from.compareToIgnoreCase( o.from );
        return from != 0 ? from : this.to.compareToIgnoreCase( o.to );
    }

    @Override
    public int hashCode(){
        return from.hashCode() ^ to.hashCode();
    }

    @Override
    public boolean equals( Object obj ){
        if( !( obj instanceof Route ) ) return false;
        Route route = ( Route ) obj;
        return this.from.equals( route.from ) && this.to.equals( route.to );
    }

    @Override
    protected Object clone() throws CloneNotSupportedException{
        Route clone = ( Route ) super.clone();
        clone.from = from + "";
        clone.to = to + "";
        return clone;
    }

    @Override
    public String toString(){
        return String.format( "Route from %s to %s" , from , to );
    }

}
package transport;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import model.Flight;
import model.FlightOrRoute;
import model.Route;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class ObjectSerializer extends StdSerializer<FlightOrRoute>{

    public ObjectSerializer(){
        this( null );
    }

    private ObjectSerializer( Class<FlightOrRoute> t ){
        super( t );
    }

    @Override
    public void serialize( FlightOrRoute flightOrRoute , JsonGenerator jsonGenerator ,
                           SerializerProvider serializerProvider ) throws IOException{
        if( flightOrRoute.getClass().equals( Flight.class ) ){
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "dd.MM.yyyy HH:mm" );
            Flight            flight    = ( Flight ) flightOrRoute;
            jsonGenerator.writeString(
                    String.format( "number %s, route id %s, from %s at %s, to %s at %s, on %s" , flight.getNumber() ,
                                   Optional.ofNullable( flight.getRoute().getId() )
                                           .map( String::valueOf )
                                           .orElse( "null" ) , flight.getRoute().getFrom().getId() ,
                                   formatter.format( flight.getDepartureDateTime() ) ,
                                   flight.getRoute().getTo().getId() , formatter.format( flight.getArriveDateTime() ) ,
                                   flight.getPlaneID() ) );
        }else{
            Route route = ( Route ) flightOrRoute;
            jsonGenerator.writeString( String.format( "route id %s, from %s, to %s" ,
                                                      Optional.ofNullable( route.getId() )
                                                              .map( String::valueOf )
                                                              .orElse( "null" ) , route.getFrom().getId() ,
                                                      route.getTo().getId() ) );
        }
    }
}

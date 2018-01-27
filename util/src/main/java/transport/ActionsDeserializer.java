package transport;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import model.Flight;
import model.FlightOrRoute;
import model.Route;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActionsDeserializer extends StdDeserializer<FlightOrRoute>{

    public ActionsDeserializer(){
        super( ( Class<?> ) null );
    }

    @Override
    public FlightOrRoute deserialize( JsonParser jsonParser , DeserializationContext deserializationContext ) throws
                                                                                                              IOException{
        try{
            String anyRegex = "([\\w\\d]+)";
            String dateTimeRegex =
                    "((0?[1-9]|[12]\\d|3[01]).(0?[1-9]|1[012]).((19|20)\\d\\d) ([01]?\\d|2[0-3]):([0-5]\\d))";
            String zoneIdRegex = "([/\\w]+)";
            Pattern flightPattern = Pattern.compile(
                    String.format( "number %s, route id %s, from %s at %s, to %s at %s, on %s" , anyRegex , anyRegex ,
                                   zoneIdRegex , dateTimeRegex , zoneIdRegex , dateTimeRegex , anyRegex ) ),
                    routePattern = Pattern.compile(
                            String.format( "route id %s, from %s, to %s" , anyRegex , zoneIdRegex , zoneIdRegex ) );
            if( flightPattern.matcher( jsonParser.getText() ).matches() ){
                DateTimeFormatter formatter     = DateTimeFormatter.ofPattern( "dd.MM.yyyy HH:mm" );
                Matcher           flightMatcher = flightPattern.matcher( jsonParser.getText() );
                flightMatcher.find();
                String number = flightMatcher.group( 1 );
                Integer routeId =
                        flightMatcher.group( 2 ).matches( "\\d+" ) ? Integer.parseInt( flightMatcher.group( 2 ) ) :
                        null;
                String routeFrom     = flightMatcher.group( 3 );
                String departureTime = flightMatcher.group( 4 );
                String routeTo       = flightMatcher.group( 11 );
                String arriveTime    = flightMatcher.group( 12 );
                String planeId       = flightMatcher.group( 19 );
                Route  route         = new Route( routeId , ZoneId.of( routeFrom ) , ZoneId.of( routeTo ) );
                return new Flight( number , route , planeId ,
                                   ZonedDateTime.of( LocalDateTime.parse( departureTime , formatter ) ,
                                                     route.getFrom() ) ,
                                   ZonedDateTime.of( LocalDateTime.parse( arriveTime , formatter ) , route.getTo() ) );
            }else if( routePattern.matcher( jsonParser.getText() ).matches() ){
                Matcher routeMatcher = routePattern.matcher( jsonParser.getText() );
                routeMatcher.find();
                Integer routeId =
                        routeMatcher.group( 1 ).matches( "\\d+" ) ? Integer.parseInt( routeMatcher.group( 1 ) ) : null;
                String routeFrom = routeMatcher.group( 2 );
                String routeTo   = routeMatcher.group( 3 );
                return new Route( routeId , ZoneId.of( routeFrom ) , ZoneId.of( routeTo ) );
            }else{
                throw new JsonParseException( jsonParser , "Doesn't match regex" );
            }
        }catch( Throwable e ){
            throw new JsonParseException( jsonParser , "Wrong token" , e );
        }
    }
}

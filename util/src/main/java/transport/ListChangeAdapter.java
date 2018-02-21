package transport;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.ListChangeListener;
import model.DataModel;
import model.Flight;
import model.FlightOrRoute;
import model.Route;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ListChangeAdapter{

    private static final ObjectMapper mapper = new ObjectMapper();
    private final String update;

    private final Pattern updatePattern = Pattern.compile(
            "^(?<entity>flight|route) \\{ (?<list>\\{.+}) (?<type>changed to|removed|added) (?<new>\\{.+} )?}\n?$" );

    @JsonCreator
    public ListChangeAdapter(
            @JsonProperty( "update" )
                    String update ){
        this.update = update;
    }

    @JsonIgnore
    public Boolean isRouteUpdate(){
        return update.startsWith( "route" );
    }

    @JsonIgnore
    public Route getRouteUpdate(){
        try{
            Matcher matcher = updatePattern.matcher( this.update );
            matcher.find();
            return mapper.readerFor( Route.class ).readValue( matcher.group( "list" ) );
        }catch( IOException e ){
            throw new IllegalStateException( "Wrong type casting" , e );
        }
    }

    @JsonIgnore
    public Boolean isFlightUpdate(){
        return update.startsWith( "flight" );
    }

    @JsonIgnore
    public Flight getFlightUpdate(){
        try{
            Matcher matcher = updatePattern.matcher( this.update );
            matcher.find();
            return mapper.readerFor( Flight.class ).readValue( matcher.group( "list" ) );
        }catch( IOException e ){
            throw new IllegalStateException( "Wrong type casting" , e );
        }
    }

    public static ListChangeAdapter addRoute( Route route ) throws JsonProcessingException{
        return new ListChangeAdapter( String.format( "route { %s added }" , mapper.writeValueAsString( route ) ) );
    }

    public static ListChangeAdapter addFlight( Flight flight ) throws JsonProcessingException{
        return new ListChangeAdapter( String.format( "flight { %s added }" , mapper.writeValueAsString( flight ) ) );
    }

    public static ListChangeAdapter removeRoute( Route route ) throws JsonProcessingException{
        return new ListChangeAdapter( String.format( "route { %s removed }" , mapper.writeValueAsString( route ) ) );
    }

    public static ListChangeAdapter removeFlight( Flight flight ) throws JsonProcessingException{
        return new ListChangeAdapter( String.format( "flight { %s removes }" , mapper.writeValueAsString( flight ) ) );
    }

    public static ListChangeAdapter editRoute( Route oldRoute , Route newRoute ) throws JsonProcessingException{
        return new ListChangeAdapter( String.format( "route { %s changed to %s }" ,
                                                     mapper.writeValueAsString( oldRoute ) ,
                                                     mapper.writeValueAsString( newRoute ) ) );
    }

    public static ListChangeAdapter editFlight( Flight oldFlight , Flight newFlight ) throws JsonProcessingException{
        return new ListChangeAdapter( String.format( "flight { %s changed to %s }" ,
                                                     mapper.writeValueAsString( oldFlight ) ,
                                                     mapper.writeValueAsString( newFlight ) ) );
    }

    public static ListChangeAdapter flightChange( ListChangeListener.Change<? extends Flight> change ){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append( "flight " );
        changeToString( change , stringBuilder );
        return new ListChangeAdapter( stringBuilder.toString() );
    }

    public static ListChangeAdapter routeChange( ListChangeListener.Change<? extends Route> change ){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append( "route " );
        changeToString( change , stringBuilder );
        return new ListChangeAdapter( stringBuilder.toString() );
    }

    private static void changeToString( ListChangeListener.Change<? extends FlightOrRoute> change ,
                                        StringBuilder stringBuilder ){
        try{
            while( change.next() ){
                if( change.wasReplaced() ){
                    stringBuilder.append( String.format( "{ %s changed to %s }" ,
                                                         mapper.writeValueAsString( change.getRemoved() ) ,
                                                         mapper.writeValueAsString( change.getAddedSubList() ) ) );
                }else if( change.wasRemoved() ){
                    stringBuilder.append( String.format( "{ %s removed }" ,
                                                         mapper.writeValueAsString( change.getRemoved() ) ) );
                }else if( change.wasAdded() ){
                    stringBuilder.append( String.format( "{ %s added }" ,
                                                         mapper.writeValueAsString( change.getAddedSubList() ) ) );
                }else{
                    throw new IllegalArgumentException( "Unsupported change type " + change.toString() );
                }
                stringBuilder.append( "\n" );
            }
        }catch( IOException e ){
            e.printStackTrace();
        }
    }

    @JsonGetter( "update" )
    public String getUpdate(){
        return update;
    }

    @SuppressWarnings( { "ResultOfMethodCallIgnored" , "unchecked" } )
    public void apply( DataModel dataModel ) throws IllegalArgumentException{
        Matcher matcher = updatePattern.matcher( this.update );
        if( !matcher.matches() ){
            throw new IllegalArgumentException( "Error while parsing update" );
        }
        String type    = matcher.group( "type" );
        String entity  = matcher.group( "entity" );
        String list    = matcher.group( "list" );
        String newList = matcher.group( "new" );
        try{
            switch( type ){
                case "added":
                    switch( entity ){
                        case "flight":
                            dataModel.addFlight( mapper.readerFor( Flight.class ).readValue( list ) );
                            break;
                        case "route":
                            //noinspection unchecked
                            dataModel.addRoute( mapper.readerFor( Route.class ).readValue( list ) );
                            break;
                    }
                    break;
                case "removed":
                    switch( entity ){
                        case "flight":
                            dataModel.removeFlight( ( ( Flight ) mapper.readerFor( Flight.class )
                                                                       .readValue( list ) ).getNumber() );
                            break;
                        case "route":
                            dataModel.removeRoute( mapper.readerFor( Route.class ).readValue( list ) );
                            break;
                    }
                    break;
                case "changed to":
                    switch( entity ){
                        case "flight":
                            Flight newFlight = mapper.readerFor( Flight.class ).readValue( newList );
                            dataModel.editFlight( mapper.readerFor( Flight.class ).readValue( list ) ,
                                                  newFlight.getRoute() ,
                                                  newFlight.getPlaneID() ,
                                                  newFlight.getDepartureDateTime() ,
                                                  newFlight.getArriveDateTime() );
                            break;
                        case "route":
                            Route newRoute = mapper.readerFor( Route.class ).readValue( newList );
                            dataModel.editRoute( mapper.readerFor( Route.class ).readValue( list ) ,
                                                 newRoute.getFrom() ,
                                                 newRoute.getTo() );
                            break;
                    }
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        }catch( IOException ignored ){
            ignored.printStackTrace();
        }
    }

    @Override
    public int hashCode(){
        return update.hashCode();
    }

    @Override
    public boolean equals( Object obj ){
        return obj instanceof ListChangeAdapter && update.equals( ( ( ListChangeAdapter ) obj ).update );
    }

    @SuppressWarnings( "ResultOfMethodCallIgnored" )
    public Boolean equalsEntities( ListChangeAdapter another ){
        try{
            Matcher thisUpdate    = this.updatePattern.matcher( this.update );
            Matcher anotherUpdate = this.updatePattern.matcher( another.update );
            thisUpdate.find();
            anotherUpdate.find();
            if( !thisUpdate.group( "entity" ).equals( anotherUpdate.group( "entity" ) ) ) return false;
            if( !thisUpdate.group( "type" ).equals( anotherUpdate.group( "type" ) ) ) return false;
            switch( thisUpdate.group( "entity" ) ){
                case "route":
//                    Check, that in two requests the same routes have same end points
                    Boolean firstList = checkEntities( thisUpdate , anotherUpdate , Route.class , "list" );
                    if( thisUpdate.group( "type" ).equals( "changed to" ) ){
                        return firstList && checkEntities( thisUpdate , anotherUpdate , Route.class , "new" );
                    }else{
                        return firstList;
                    }
                case "flight":
                    firstList = checkEntities( thisUpdate , anotherUpdate , Flight.class , "list" );
                    if( thisUpdate.group( "type" ).equals( "changed to" ) ){
                        return firstList && checkEntities( thisUpdate , anotherUpdate , Flight.class , "new" );
                    }else{
                        return firstList;
                    }
                default:
                    return false;
            }
        }catch( Throwable e ){
            e.printStackTrace();
            return false;
        }
    }

    private Boolean checkEntities( Matcher thisUpdate , Matcher anotherUpdate ,
                                   Class<? extends FlightOrRoute> entityClass , String listName ) throws IOException{
        if( entityClass.equals( Route.class ) ){
            Route thisRoute    = mapper.readerFor( entityClass ).readValue( thisUpdate.group( listName ) );
            Route anotherRoute = mapper.readerFor( entityClass ).readValue( anotherUpdate.group( listName ) );
            return thisRoute.getFrom().equals( anotherRoute.getFrom() ) &&
                   thisRoute.getTo().equals( anotherRoute.getTo() );
        }else{
            Flight thisRoute    = mapper.readerFor( entityClass ).readValue( thisUpdate.group( listName ) );
            Flight anotherRoute = mapper.readerFor( entityClass ).readValue( anotherUpdate.group( listName ) );
            return thisRoute.getNumber().equals( anotherRoute.getNumber() );
        }
    }
}


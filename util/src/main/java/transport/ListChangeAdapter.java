package transport;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.ListChangeListener;
import model.DataModel;
import model.Flight;
import model.FlightOrRoute;
import model.Route;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ListChangeAdapter{
    private static final ObjectMapper mapper = new ObjectMapper();
    private final String update;
    private final Pattern updatePattern = Pattern.compile(
            "^(?<entity>flight|route) \\{ (?<list>\\[.+]) (?<type>changed to|removed|added) (?<new>\\[.+] )?}\n?$" );

    @JsonCreator
    public ListChangeAdapter(
            @JsonProperty( "update" )
                    String update ){
        this.update = update;
    }

    public static ListChangeAdapter addRoute( List<Route> routes ) throws JsonProcessingException{
        return new ListChangeAdapter( String.format( "route { %s added }" , mapper.writeValueAsString( routes ) ) );
    }

    public static ListChangeAdapter addFlight( List<Flight> flights ) throws JsonProcessingException{
        return new ListChangeAdapter( String.format( "flight { %s added }" , mapper.writeValueAsString( flights ) ) );
    }

    public static ListChangeAdapter removeRoute( List<Route> routes ) throws JsonProcessingException{
        return new ListChangeAdapter( String.format( "route { %s removed }" , mapper.writeValueAsString( routes ) ) );
    }

    public static ListChangeAdapter removeFlight( List<Flight> flights ) throws JsonProcessingException{
        return new ListChangeAdapter( String.format( "flight { %s removes }" , mapper.writeValueAsString( flights ) ) );
    }

    public static ListChangeAdapter editRoute( List<Route> oldRoutes , List<Route> newRoutes ) throws
                                                                                               JsonProcessingException{
        return new ListChangeAdapter( String.format( "route { %s changed to %s }" ,
                                                     mapper.writeValueAsString( oldRoutes ) ,
                                                     mapper.writeValueAsString( newRoutes ) ) );
    }

    public static ListChangeAdapter editFlight( List<Flight> oldFlights , List<Flight> newFlights ) throws
                                                                                                    JsonProcessingException{
        return new ListChangeAdapter( String.format( "flight { %s changed to %s }" ,
                                                     mapper.writeValueAsString( oldFlights ) ,
                                                     mapper.writeValueAsString( newFlights ) ) );
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
        String type = matcher.group( "type" );
        String entity = matcher.group( "entity" );
        String list = matcher.group( "list" );
        String newList = matcher.group( "new" );
        try{
            switch( type ){
                case "added":
                    switch( entity ){
                        case "flight":
                            ( ( List<Flight> ) mapper.readerFor( mapper.getTypeFactory()
                                                                       .constructCollectionType( List.class ,
                                                                                                 Flight.class ) )
                                                     .readValue( list ) ).forEach( dataModel::addFlight );
                            break;
                        case "route":
                            //noinspection unchecked
                            ( ( List<Route> ) mapper.readerFor( mapper.getTypeFactory()
                                                                      .constructCollectionType( List.class ,
                                                                                                Route.class ) )
                                                    .readValue( list ) ).forEach( dataModel::addRoute );
                            break;
                    }
                    break;
                case "removed":
                    switch( entity ){
                        case "flight":
                            ( ( List<Flight> ) mapper.readerFor( mapper.getTypeFactory()
                                                                       .constructCollectionType( List.class ,
                                                                                                 Flight.class ) )
                                                     .readValue( list ) ).stream()
                                                                         .map( Flight::getNumber )
                                                                         .forEach( dataModel::removeFlight );
                            break;
                        case "route":
                            ( ( List<Route> ) mapper.readerFor( mapper.getTypeFactory()
                                                                      .constructCollectionType( List.class ,
                                                                                                Route.class ) )
                                                    .readValue( list ) ).forEach( dataModel::removeRoute );
                            break;
                    }
                    break;
                case "changed to":
                    switch( entity ){
                        case "flight":
                            Flux.zip( Flux.fromStream( ( ( List<Flight> ) mapper.readerFor( mapper.getTypeFactory()
                                                                                                  .constructCollectionType(
                                                                                                          List.class ,
                                                                                                          Flight.class ) )
                                                                                .readValue( list ) ).stream() ) ,
                                      Flux.fromStream( ( ( List<Flight> ) mapper.readerFor( mapper.getTypeFactory()
                                                                                                  .constructCollectionType(
                                                                                                          List.class ,
                                                                                                          Flight.class ) )
                                                                                .readValue( newList ) ).stream() ) )
                                .subscribe( tuple2 -> dataModel.editFlight( tuple2.getT1() ,
                                                                            tuple2.getT2().getRoute() ,
                                                                            tuple2.getT2().getPlaneID() ,
                                                                            tuple2.getT2().getDepartureDateTime() ,
                                                                            tuple2.getT2().getArriveDateTime() ) );
                            break;
                        case "route":
                            Flux.zip( Flux.fromStream( ( ( List<Route> ) mapper.readerFor( mapper.getTypeFactory()
                                                                                                 .constructCollectionType(
                                                                                                         List.class ,
                                                                                                         Route.class ) )
                                                                               .readValue( list ) ).stream() ) ,
                                      Flux.fromStream( ( ( List<Route> ) mapper.readerFor( mapper.getTypeFactory()
                                                                                                 .constructCollectionType(
                                                                                                         List.class ,
                                                                                                         Route.class ) )
                                                                               .readValue( newList ) ).stream() ) )
                                .subscribe( tuple2 -> dataModel.editRoute( tuple2.getT1() ,
                                                                           tuple2.getT2().getFrom() ,
                                                                           tuple2.getT2().getTo() ) );
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
}


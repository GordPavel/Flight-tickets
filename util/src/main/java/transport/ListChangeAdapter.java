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
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ListChangeAdapter{
    private static final ObjectMapper mapper = new ObjectMapper();
    private final String update;
    private final Pattern
            updatePattern =
            Pattern.compile(
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
        return new ListChangeAdapter( String.format( "flight { %s removed }" , mapper.writeValueAsString( flights ) ) );
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
    public void apply( DataModel dataModel , Boolean isServer ) throws IllegalArgumentException{
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
                                                    .readValue( list ) ).forEach( route -> dataModel.addRoute( route ,
                                                                                                               isServer ) );
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

    @JsonIgnore
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
                    Boolean firstList = checkListInUpdates( thisUpdate , anotherUpdate , Route.class , "list" );
                    if( thisUpdate.group( "type" ).equals( "changed to" ) ){
                        return firstList && checkListInUpdates( thisUpdate , anotherUpdate , Route.class , "new" );
                    }else{
                        return firstList;
                    }
                case "flight":
                    firstList = checkListInUpdates( thisUpdate , anotherUpdate , Flight.class , "list" );
                    if( thisUpdate.group( "type" ).equals( "changed to" ) ){
                        return firstList && checkListInUpdates( thisUpdate , anotherUpdate , Flight.class , "new" );
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

    private Boolean checkListInUpdates( Matcher thisUpdate ,
                                        Matcher anotherUpdate ,
                                        Class<? extends FlightOrRoute> entityClass ,
                                        String listName ) throws IOException{
        Flux<Object>
                thisList =
                Flux.fromIterable( mapper.readerFor( mapper.getTypeFactory()
                                                           .constructCollectionType( List.class , entityClass ) )
                                         .readValue( thisUpdate.group( listName ) ) );
        Flux<Object>
                anotherList =
                Flux.fromIterable( mapper.readerFor( mapper.getTypeFactory()
                                                           .constructCollectionType( List.class , entityClass ) )
                                         .readValue( anotherUpdate.group( listName ) ) );
        return Flux.zip( thisList , anotherList ).map( tuple -> {
            if( entityClass.equals( Route.class ) ){
                return ( ( Route ) tuple.getT1() ).getFrom()
                                                  .getId()
                                                  .equals( ( ( Route ) tuple.getT2() ).getFrom().getId() ) &&
                       ( ( Route ) tuple.getT1() ).getTo()
                                                  .getId()
                                                  .equals( ( ( Route ) tuple.getT2() ).getTo().getId() );
            }else{
                return ( ( Flight ) tuple.getT1() ).getNumber().equals( ( ( Flight ) tuple.getT2() ).getNumber() );
            }
        } ).toStream().allMatch( Boolean::booleanValue );
    }
}


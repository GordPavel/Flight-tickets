package transport;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import exceptions.FaRNotRelatedData;
import javafx.collections.ListChangeListener;
import javafx.util.Pair;
import model.DataModel;
import model.Flight;
import model.FlightOrRoute;
import model.Route;

import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ListChangeAdapter{
    private String update;
    private static ObjectMapper mapper = new ObjectMapper();


    @JsonCreator
    public ListChangeAdapter(
            @JsonProperty( "update" )
                    String update ){
        this.update = update;
    }

    public ListChangeAdapter( ListChangeListener.Change<? extends FlightOrRoute> change ) throws JsonProcessingException{
        this.update = changeToString( change );
    }

    @JsonGetter( "update" )
    public String getUpdate(){
        return update;
    }

    @SuppressWarnings( "ResultOfMethodCallIgnored" )
    public void apply( DataModel dataModel ) throws IOException{
        Matcher matcher;
        if( setPattern.matcher( update ).matches() ){
            matcher = setPattern.matcher( update );
            matcher.find();
            int index = Integer.parseInt( matcher.group( 3 ) );
            switch( matcher.group( 1 ) ){
                case "route":
                    Route updateRoute = mapper.readerFor( Route.class ).readValue( matcher.group( 2 ) );
                    dataModel.editRoute( dataModel.getRouteObservableList().get( index ) , updateRoute.getFrom() ,
                                         updateRoute.getTo() );
                    break;
                case "flight":
                    Flight updateFlight = mapper.readerFor( Flight.class ).readValue( matcher.group( 2 ) );
                    dataModel.editFlight( dataModel.getFlightObservableList().get( index ) , updateFlight.getRoute() ,
                                          updateFlight.getPlaneID() , updateFlight.getDepartureDateTime() ,
                                          updateFlight.getArriveDateTime() );
                    break;
            }
        }else if( removePattern.matcher( update ).matches() ){
            matcher = removePattern.matcher( update );
            matcher.find();
            int from = Integer.parseInt( matcher.group( 2 ) );
            int to   = Integer.parseInt( matcher.group( 3 ) );
            switch( matcher.group( 1 ) ){
                case "route":
                    IntStream.range( from , to )
                             .forEach( i -> dataModel.removeRoute( dataModel.getRouteObservableList().get( i ) ) );
                    break;
                case "flight":
                    IntStream.range( from , to )
                             .forEach( i -> dataModel.removeFlight(
                                     dataModel.getFlightObservableList().get( i ).getNumber() ) );
                    break;
            }
        }else if( addPattern.matcher( update ).matches() ){
            matcher = addPattern.matcher( update );
            matcher.find();
            int index = Integer.parseInt( matcher.group( 3 ) );
            switch( matcher.group( 1 ) ){
                case "route":
                    List<Route> addRoutes = mapper.readerFor(
                            mapper.getTypeFactory().constructCollectionType( List.class , Route.class ) )
                                                  .readValue( matcher.group( 2 ) );
                    addRoutes.forEach( route -> dataModel.addRoute( index , route ) );
                    break;
                case "flight":
                    List<Flight> addFlights = mapper.readerFor(
                            mapper.getTypeFactory().constructCollectionType( List.class , Flight.class ) )
                                                    .readValue( matcher.group( 2 ) );
                    addFlights.forEach( flight -> {
                        flight.setRoute( dataModel.getRouteObservableList()
                                                  .stream()
                                                  .filter( route -> route.getId().equals( flight.getRoute().getId() ) )
                                                  .findFirst()
                                                  .orElseThrow( () -> new FaRNotRelatedData(
                                                          "Route not exists in database" ) ) );
                        dataModel.addFlight( index , flight );
                    } );
                    break;
            }
        }else if( permutationPattern.matcher( update ).matches() ){
            matcher = permutationPattern.matcher( update );
            matcher.find();
            Iterator<Integer> permutation = ( ( List<Integer> ) mapper.readerFor(
                    mapper.getTypeFactory().constructCollectionType( List.class , Integer.class ) )
                                                                      .readValue( matcher.group( 2 ) ) ).iterator();
            switch( matcher.group( 1 ) ){
                case "route":
                    dataModel.getRouteObservableList()
                             .setAll( dataModel.getRouteObservableList()
                                               .stream()
                                               .map( route -> new Pair<>( route , permutation.next() ) )
                                               .sorted( Comparator.comparingInt( Pair::getValue ) )
                                               .map( Pair::getKey )
                                               .collect( Collectors.toList() ) );
                    break;
                case "flight":
                    dataModel.getFlightObservableList()
                             .setAll( dataModel.getFlightObservableList()
                                               .stream()
                                               .map( route -> new Pair<>( route , permutation.next() ) )
                                               .sorted( Comparator.comparingInt( Pair::getValue ) )
                                               .map( Pair::getKey )
                                               .collect( Collectors.toList() ) );
                    break;
            }
        }else{
            throw new IllegalArgumentException( "Illegal change string" );
        }
    }

    private Pattern setPattern                                                                    =
            Pattern.compile( "^(flight|route) \\{ (.+) set at (\\d+) }$" ), removePattern         =
            Pattern.compile( "^(flight|route) \\{ removed from (\\d+) to (\\d+) }$" ), addPattern =
            Pattern.compile( "^(flight|route) \\{ (.+) added at (\\d+) }$" ), permutationPattern  =
            Pattern.compile( "^(flight|route) \\{ permutated by (.+) }$" );

    public static String changeToString( ListChangeListener.Change<? extends FlightOrRoute> change ) throws
                                                                                                     JsonProcessingException{
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append( change.getList().get( 0 ).getClass().equals( Route.class ) ? "route " : "flight " );
        while( change.next() ){
            if( change.wasRemoved() && change.wasAdded() ){
                stringBuilder.append( String.format( "{ %s set at %d }" ,
                                                     mapper.writeValueAsString( change.getAddedSubList().get( 0 ) ) ,
                                                     change.getFrom() ) );

            }else if( change.wasRemoved() ){
                stringBuilder.append( String.format( "{ removed from %d to %d }" , change.getFrom() ,
                                                     change.getFrom() + change.getRemovedSize() ) );
            }else if( change.wasAdded() ){
                stringBuilder.append(
                        String.format( "{ %s added at %d }" , mapper.writeValueAsString( change.getAddedSubList() ) ,
                                       change.getFrom() ) );
            }else if( change.wasPermutated() ){
                stringBuilder.append( change.toString() );
            }else{
                throw new IllegalArgumentException( "Unsupported change type " + change.toString() );
            }
            stringBuilder.append( "\n" );
        }
        return stringBuilder.toString();
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

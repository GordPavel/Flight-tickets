package transport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.ListChangeListener;
import model.DataModel;
import model.Flight;
import model.FlightOrRoute;
import model.Route;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// todo: Закончить
public class ListChangeAdapter{
    private String update;
    private static ObjectMapper mapper = new ObjectMapper();

    public ListChangeAdapter( String update ){
        this.update = update;
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
                    dataModel.getRouteObservableList().set( index , updateRoute );
                    break;
                case "flight":
                    Flight updateFlight = mapper.readerFor( Flight.class ).readValue( matcher.group( 2 ) );
                    dataModel.getFlightObservableList().set( index , updateFlight );
                    break;
            }
        }else if( removePattern.matcher( update ).matches() ){
            matcher = removePattern.matcher( update );
            matcher.find();
            int from = Integer.parseInt( matcher.group( 2 ) );
            int to   = Integer.parseInt( matcher.group( 3 ) );
            switch( matcher.group( 1 ) ){
                case "route":
                    dataModel.getRouteObservableList().remove( from , to );
                    break;
                case "flight":
                    dataModel.getFlightObservableList().remove( from , to );
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
                    List<Flight> addFLights = mapper.readerFor(
                            mapper.getTypeFactory().constructCollectionType( List.class , Flight.class ) )
                                                    .readValue( matcher.group( 2 ) );
                    addFLights.forEach( flight -> dataModel.addFlight( index , flight ) );
                    break;
            }
        }else if( permutationPattern.matcher( update ).matches() ){
            matcher = permutationPattern.matcher( update );
            matcher.find();
            List<Integer> permutation =
                    mapper.readerFor( mapper.getTypeFactory().constructCollectionType( List.class , Integer.class ) )
                          .readValue( matcher.group( 2 ) );

            switch( matcher.group( 1 ) ){
                case "route":
                    dataModel.getRouteObservableList()
                             .sort( Comparator.comparingInt( value -> permutation.indexOf(
                                     dataModel.getRouteObservableList().indexOf( value ) ) ) );
                    break;
                case "flight":
                    dataModel.getFlightObservableList()
                             .sort( Comparator.comparingInt( value -> permutation.indexOf(
                                     dataModel.getFlightObservableList().indexOf( value ) ) ) );
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
}

package transport;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.Flight;
import model.Route;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ActionsTransportTest{

    static               Random       random           = new Random( System.currentTimeMillis() );
    static               ObjectMapper mapper           = new ObjectMapper();
    static private final List<ZoneId> availableZoneIds = ZoneId.getAvailableZoneIds()
                                                               .stream()
                                                               .sorted()
                                                               .filter( Pattern.compile( "(Etc|SystemV)/.+" )
                                                                               .asPredicate()
                                                                               .negate()
                                                                               .and( Pattern.compile(
                                                                                       "^([\\w/]+)/(\\w+)$" )
                                                                                            .asPredicate() ) )
                                                               .map( ZoneId::of )
                                                               .collect( Collectors.toList() );
    private static final Route        route            =
            new Route( availableZoneIds.get( random.nextInt( availableZoneIds.size() ) ) ,
                       availableZoneIds.get( random.nextInt( availableZoneIds.size() ) ) );
    private static final Flight       flight           =
            new Flight( "number" , route , "plane" , ZonedDateTime.now() , ZonedDateTime.now().plusHours( 1 ) );
    private static       Actions      actions          = new Actions( flight , Actions.ActionsType.ADD , null );


    @Test
    void testSerializationAndDeserialization() throws IOException{
        assertEquals( actions , mapper.readerFor( Actions.class ).readValue( mapper.writeValueAsString( actions ) ) ,
                      "Object after deserialization is equal before." );
    }
}
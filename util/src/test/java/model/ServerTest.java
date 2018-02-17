package model;

import com.fasterxml.jackson.databind.ObjectMapper;
import transport.Data;

import java.io.IOException;
import java.time.ZoneId;
import java.util.Collections;

public class ServerTest{
    public static void main( String[] args ) throws IOException{
        ObjectMapper mapper = new ObjectMapper();
        Data data = new Data(
                Collections.singletonList( new Route( ZoneId.of( "Europe/Samara" ) , ZoneId.of( "Europe/Moscow" ) ) ) ,
                Collections.emptyList() );
        String s = mapper.writeValueAsString( data );
        System.out.println( s );
        Data another = mapper.readerFor( Data.class ).readValue( s );
        System.out.println( another );
    }
}

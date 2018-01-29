package model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ZonedDateTimeDeserializer extends StdDeserializer<ZonedDateTime>{

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "dd.MM.yyyy HH:mm Z" );

    public ZonedDateTimeDeserializer(){
        super( ( Class<?> ) null );
    }

    @Override
    public ZonedDateTime deserialize( JsonParser p , DeserializationContext ctxt ) throws IOException{
        return ZonedDateTime.parse( p.getText() , formatter );
    }
}

package model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.ZoneId;

@SuppressWarnings( "WeakerAccess" )
public class ZoneIdDeserializer extends StdDeserializer<ZoneId>{

    public ZoneIdDeserializer(){
        super( ( Class<?> ) null );
    }

    @Override
    public ZoneId deserialize( JsonParser p , DeserializationContext context ) throws IOException{
        return ZoneId.of( p.getText() );
    }
}

package model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.ZoneId;

@SuppressWarnings( "WeakerAccess" )
public class ZoneIdSerializer extends StdSerializer<ZoneId>{

    public ZoneIdSerializer(){
        super( ( Class<ZoneId> ) null );
    }

    @Override
    public void serialize( ZoneId value , JsonGenerator gen , SerializerProvider serializers ) throws IOException{
        gen.writeString( value.getId() );
    }
}

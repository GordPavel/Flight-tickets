package transport;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import exceptions.FlightAndRouteException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

@SuppressWarnings( "WeakerAccess" )
public class ExceptionDeserializer extends StdDeserializer<FlightAndRouteException>{

    public ExceptionDeserializer(){
        super( ( Class<?> ) null );
    }

    @Override
    public FlightAndRouteException deserialize( JsonParser jsonParser ,
                                                DeserializationContext deserializationContext ) throws IOException{
        try( ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream( jsonParser.getBinaryValue() ) ;
             ObjectInputStream inputStream = new ObjectInputStream( byteArrayInputStream ) ){
            return ( FlightAndRouteException ) inputStream.readObject();
        }catch( ClassNotFoundException e ){
            throw new IllegalArgumentException();
        }
    }
}

package transport;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import exceptions.FlightAndRouteException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

@SuppressWarnings( "WeakerAccess" )
public class ExceptionSerializer extends StdSerializer<FlightAndRouteException>{

    public ExceptionSerializer(){
        super( ( Class<FlightAndRouteException> ) null );
    }

    @Override
    public void serialize( FlightAndRouteException e , JsonGenerator jsonGenerator ,
                           SerializerProvider serializerProvider ) throws IOException{
        try( ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream() ;
             ObjectOutputStream outputStream = new ObjectOutputStream( byteArrayOutputStream ) ){
            outputStream.writeObject( e );
            jsonGenerator.writeBinary( byteArrayOutputStream.toByteArray() );
        }
    }
}

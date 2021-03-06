package transport;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.danekja.java.util.function.serializable.SerializablePredicate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

@SuppressWarnings( "WeakerAccess" )
public class PredicateSerializer extends StdSerializer<SerializablePredicate<?>>{

    public PredicateSerializer(){
        super( ( Class<SerializablePredicate<?>> ) null );
    }

    @Override
    public void serialize( SerializablePredicate<?> serializablePredicate , JsonGenerator jsonGenerator ,
                           SerializerProvider serializerProvider ) throws IOException{
        try( ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream() ;
             ObjectOutputStream outputStream = new ObjectOutputStream( byteArrayOutputStream ) ){
            outputStream.writeObject( serializablePredicate );
            jsonGenerator.writeBinary( byteArrayOutputStream.toByteArray() );
        }
    }
}

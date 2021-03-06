package transport;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.danekja.java.util.function.serializable.SerializablePredicate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

@SuppressWarnings( "WeakerAccess" )
public class PredicateDeserializer extends StdDeserializer<SerializablePredicate<?>>{
    public PredicateDeserializer(){
        super( ( Class<SerializablePredicate<?>> ) null );
    }

    @Override
    public SerializablePredicate<?> deserialize( JsonParser jsonParser ,
                                                 DeserializationContext deserializationContext ) throws IOException{
        try( ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream( jsonParser.getBinaryValue() ) ;
             ObjectInputStream inputStream = new ObjectInputStream( byteArrayInputStream ) ){
            return ( SerializablePredicate<?> ) inputStream.readObject();
        }catch( ClassNotFoundException e ){
            throw new IllegalArgumentException( e );
        }
    }
}

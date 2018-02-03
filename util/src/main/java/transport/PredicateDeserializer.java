package transport;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.danekja.java.util.function.serializable.SerializablePredicate;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class PredicateDeserializer extends StdDeserializer<SerializablePredicate<?>>{
    public PredicateDeserializer(){
        super( ( Class<SerializablePredicate<?>> ) null );
    }

    @Override
    public SerializablePredicate<?> deserialize( JsonParser jsonParser ,
                                                 DeserializationContext deserializationContext ) throws IOException,
                                                                                                        JsonProcessingException{
        try{
            PipedOutputStream pipedOutputStream = new PipedOutputStream();
            PipedInputStream  pipedInputStream  = new PipedInputStream( pipedOutputStream );
            pipedOutputStream.write( jsonParser.getBinaryValue() );
            ObjectInputStream inputStream = new ObjectInputStream( pipedInputStream );
            return ( SerializablePredicate<?> ) inputStream.readObject();
        }catch( ClassNotFoundException e ){
            throw new IllegalArgumentException( e );
        }
    }
}

package transport;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.danekja.java.util.function.serializable.SerializablePredicate;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class PredicateSerializer extends StdSerializer<SerializablePredicate<?>>{

    public PredicateSerializer(){
        super( ( Class<SerializablePredicate<?>> ) null );
    }

    @Override
    public void serialize( SerializablePredicate<?> serializablePredicate , JsonGenerator jsonGenerator ,
                           SerializerProvider serializerProvider ) throws IOException{
        try( PipedOutputStream pipedOutputStream = new PipedOutputStream() ;
             PipedInputStream pipedInputStream = new PipedInputStream( pipedOutputStream ) ;
             ObjectOutputStream outputStream = new ObjectOutputStream( pipedOutputStream ) ){
            outputStream.writeObject( serializablePredicate );
            jsonGenerator.writeBinary( pipedInputStream , pipedInputStream.available() );
        }
    }
}

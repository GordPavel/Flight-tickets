package observable;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.ListChangeListener;
import javafx.util.StringConverter;
import model.FlightOrRoute;

public class ListChangeListenerStrinConverter extends StringConverter<ListChangeListener<FlightOrRoute>>{

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public String toString( ListChangeListener<FlightOrRoute> object ){
        return null;
    }

    @Override
    public ListChangeListener<FlightOrRoute> fromString( String string ){
        return null;
    }
}

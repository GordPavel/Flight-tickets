package observable;

import javafx.collections.ListChangeListener;
import javafx.util.StringConverter;
import model.FlightOrRoute;

public class ChangeStringConverter extends StringConverter<ListChangeListener.Change<FlightOrRoute>>{

    @Override
    public String toString( ListChangeListener.Change<FlightOrRoute> change ){
        StringBuilder stringBuilder = new StringBuilder();
        while( change.next() ){

        }
    }

    @Override
    public ListChangeListener.Change<FlightOrRoute> fromString( String string ){
        return null;
    }
}

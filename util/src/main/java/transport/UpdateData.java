package transport;

import javafx.collections.ListChangeListener;
import model.FlightOrRoute;

import java.util.List;

public class UpdateData{
    List<ListChangeListener<FlightOrRoute>> changes;

    public UpdateData(){
    }

    public List<ListChangeListener<FlightOrRoute>> getChanges(){
        return changes;
    }

    public void setChanges( List<ListChangeListener<FlightOrRoute>> changes ){
        this.changes = changes;
    }
}

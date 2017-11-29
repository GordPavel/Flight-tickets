package sample;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SearchFlightsController{
    public TextField numberTextField;
    public TextField planeIdTextField;

    public TextField departureAirportTextField;
    public TextField arrivalAirportTextField;

    public DatePicker departureDatePicker;
    public TextField  departureStartTimeRangeTextField;
    public TextField  departureEndTimeRangeTextField;

    public DatePicker arrivalDatePicker;
    public TextField  arrivalStartTimeRangeTextField;
    public TextField  arrivalEndTimeRangeTextField;

    public TextField startFlightTimeRangeTextField;
    public TextField endFlightTimeRangeTextField;

    private MainWindowController mainWindowController;
    private Stage                thisStage;

    SearchFlightsController( MainWindowController mainWindowController , Stage thisStage ){
        this.mainWindowController = mainWindowController;
        this.thisStage = thisStage;
    }

    @FXML
    private void initialize(){

    }

}

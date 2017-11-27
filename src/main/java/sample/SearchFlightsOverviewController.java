package sample;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

//import java.awt.*;

public class SearchFlightsOverviewController{

    @FXML
    TextField numberField;
    @FXML
    TextField planeID;
    @FXML
    TextField flightTimeToTextField;
    @FXML
    TextField flightTimeFromTextField;
    @FXML
    TextField departureDateFromTextField;
    @FXML
    TextField departureDateToTextField;
    @FXML
    TextField arrivingDateFromTextField;
    @FXML
    TextField arrivingDateToTextField;
    @FXML
    DatePicker arrivingDateFromDatePicker;
    @FXML
    DatePicker arrivingDateToDatePicker;
    @FXML
    DatePicker departureDateFromDatePicker;
    @FXML
    DatePicker departureDateToDatePicker;


    private void closeWindow(Event event) {
        Stage stage = (Stage) ((Parent) event.getSource()).getScene().getWindow();
        stage.close();
    }
    public void handleCancelAction(ActionEvent actionEvent) {
        closeWindow(actionEvent);

    }

    public void handleClearAction(ActionEvent actionEvent) {
        departureDateFromDatePicker.setValue(null);
        departureDateToDatePicker.setValue(null);
        arrivingDateToDatePicker.setValue(null);
        arrivingDateFromDatePicker.setValue(null);
    }

    public void handleSearchAction(ActionEvent event){

        DateFormat format = new SimpleDateFormat("hh:mm");

        Date dfDate=null;

        if (departureDateFromDatePicker.getValue()!=null)
        {
            LocalDate depFromDate = departureDateFromDatePicker.getValue();
            Instant dfinstant = Instant.from(depFromDate.atStartOfDay(ZoneId.systemDefault()));
            dfDate = Date.from(dfinstant);
            Date dfTime = new Date();
            try {
                dfTime = format.parse(departureDateFromTextField.getText());
            }
            catch (ParseException e)
            {

            }
            dfDate.setHours(dfTime.getHours());
            dfDate.setMinutes(dfTime.getMinutes());
            dfDate.setSeconds(dfTime.getSeconds());
        }

        Date dtDate=null;

        if (departureDateToDatePicker.getValue()!=null)
        {
            LocalDate depToDate = departureDateToDatePicker.getValue();
            Instant dtinstant = Instant.from(depToDate.atStartOfDay(ZoneId.systemDefault()));
            dtDate = Date.from(dtinstant);
            Date dtTime = new Date();
            try {
                dtTime = format.parse(departureDateToTextField.getText());
            }
            catch (ParseException e)
            {

            }
            dtDate.setHours(dtTime.getHours());
            dtDate.setMinutes(dtTime.getMinutes());
            dtDate.setSeconds(dtTime.getSeconds());
        }


        Date afDate=null;

        if (arrivingDateFromDatePicker.getValue()!=null)
        {
            LocalDate arrFromDate = arrivingDateFromDatePicker.getValue();
            Instant afinstant = Instant.from(arrFromDate.atStartOfDay(ZoneId.systemDefault()));
            afDate = Date.from(afinstant);
            Date afTime = new Date();
            try {
                afTime = format.parse(arrivingDateFromTextField.getText());
            }
            catch (ParseException e)
            {

            }
            afDate.setHours(afTime.getHours());
            afDate.setMinutes(afTime.getMinutes());
            afDate.setSeconds(afTime.getSeconds());
        }

        Date atDate=null;

        if (arrivingDateToDatePicker.getValue()!=null)
        {
            LocalDate arrToDate = arrivingDateToDatePicker.getValue();
            Instant atinstant = Instant.from(arrToDate.atStartOfDay(ZoneId.systemDefault()));
            atDate = Date.from(atinstant);
            Date atTime = new Date();
            try {
                atTime = format.parse(arrivingDateToTextField.getText());
            }
            catch (ParseException e)
            {

            }
            atDate.setHours(atTime.getHours());
            atDate.setMinutes(atTime.getMinutes());
            atDate.setSeconds(atTime.getSeconds());
        }

        System.out.println(dfDate);
        System.out.println(dtDate);
        System.out.println(Main.getEngine().searchFlight(numberField.getText(),null,"","", planeID.getText(),
            dfDate, dtDate, afDate, atDate));


    }
}

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
        numberField.clear();
        planeID.clear();
        arrivingDateFromTextField.clear();
        arrivingDateToTextField.clear();
        departureDateFromTextField.clear();
        departureDateToTextField.clear();
        flightTimeFromTextField.clear();
        flightTimeToTextField.clear();

    }

    public void handleSearchAction(ActionEvent event){

        DateFormat format = new SimpleDateFormat("dd.MM.yyyy hh:mm");

        Date dfDate=null;

        if (departureDateFromDatePicker.getValue()!=null)
        {
            try {
                dfDate = format.parse(departureDateFromDatePicker.getEditor().getText()+" "+departureDateFromTextField.getText());
            }
            catch (ParseException e)
            {

            }
        }

        Date dtDate=null;

        if (departureDateToDatePicker.getValue()!=null)
        {
            try {
                dtDate = format.parse(departureDateToDatePicker.getEditor().getText()+" "+departureDateToTextField.getText());
            }
            catch (ParseException e)
            {

            }
        }

        Date afDate=null;

        if (arrivingDateFromDatePicker.getValue()!=null)
        {
            try {
                afDate = format.parse(arrivingDateFromDatePicker.getEditor().getText()+" "+arrivingDateFromTextField.getText());
            }
            catch (ParseException e)
            {

            }
        }

        Date atDate=null;

        if (arrivingDateToDatePicker.getValue()!=null)
        {
            try {
                atDate = format.parse(arrivingDateToDatePicker.getEditor().getText()+" "+arrivingDateToTextField.getText());
            }
            catch (ParseException e)
            {

            }
        }

        System.out.println(dfDate);
        System.out.println(departureDateFromDatePicker.getEditor().getText()+" "+departureDateFromTextField.getText());
        System.out.println(Main.getEngine().searchFlight(numberField.getText(),null,"","", planeID.getText(),
            dfDate, dtDate, afDate, atDate));


    }




}

package sample;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Route;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import java.awt.*;


/**
 * Controller for flight search view
 * Allows to search flights in DataModel with params
 *
 */
public class SearchFlightsOverviewController{


    /**
     * Connecting to FXML items
     */
    @FXML
    TextField numberField;                      //text field, pattern string for searching by flight number
    @FXML
    TextField planeID;                          //text field, pattern string for searching by plain id
    @FXML
    TextField flightTimeToTextField;            //text field, converting to date for searching by travel time (less then this)
    @FXML
    TextField flightTimeFromTextField;          //text field, converting to date for searching by travel time (more then this)
    @FXML
    TextField departureDateFromTextField;       //text field, add time component to departureFrom date
    @FXML
    TextField departureDateToTextField;         //text field, add time component to departureTo date
    @FXML
    TextField arrivingDateFromTextField;        //text field, add time component to arrivingFrom date
    @FXML
    TextField arrivingDateToTextField;          //text field, add time component to arrivingTo date
    @FXML
    DatePicker arrivingDateFromDatePicker;      //DatePicker, date for searching by arriving date (more then this or exact if arrivingTo==null)
    @FXML
    DatePicker arrivingDateToDatePicker;        //DatePicker, date for searching by arriving date (less then this)
    @FXML
    DatePicker departureDateFromDatePicker;     //DatePicker, date for searching by departure date (more then this or exact if departureTo==null)
    @FXML
    DatePicker departureDateToDatePicker;       //DatePicker, date for searching by departure date (less then this)
    @FXML
    ChoiceBox<Route> routeBox;                  //ChoiceBox with routes, existing in DataModel, search by route
    @FXML
    Button searchSearchFlightsButton;           //Search button
    @FXML
    CheckBox flightTimeCheckBox;                //CheckBox, if checked - search will use travelDate



    private void closeWindow(Event event) {
        Stage stage = (Stage) ((Parent) event.getSource()).getScene().getWindow();
        stage.close();
    }

    /**
     * Cancel button, close window
     * @param actionEvent
     */
    @FXML
    public void handleCancelAction(ActionEvent actionEvent) {
        closeWindow(actionEvent);

    }

    /**
     * Clear button. Clears all fields
     * @param actionEvent
     */
    @FXML
    public void handleClearAction(ActionEvent actionEvent) {
        departureDateFromDatePicker.setValue(null);
        departureDateToDatePicker.setValue(null);
        arrivingDateToDatePicker.setValue(null);
        arrivingDateFromDatePicker.setValue(null);
        routeBox.setValue(null);
        numberField.clear();
        planeID.clear();
        flightTimeCheckBox.setSelected(false);
        departureDateToTextField.setText("00:00");
        departureDateFromTextField.setText("00:00");
        arrivingDateToTextField.setText("00:00");
        arrivingDateFromTextField.setText("00:00");
        flightTimeFromTextField.setText("00:00");
        flightTimeToTextField.setText("00:00");


    }

    /**
     * Search button. Collect all parameters from window and generate request to search in flights, stored in model
     * @param event
     */
    @FXML
    public void handleSearchAction(ActionEvent event){


        /**
         * converting dates for search
         */
        DateFormat format = new SimpleDateFormat("dd.MM.yyyy hh:mm");
        DateFormat travelFormat = new SimpleDateFormat("hh:mm");

        Date dfDate=null;

        if (departureDateFromDatePicker.getValue()!=null) {
            try {
                dfDate = format.parse(departureDateFromDatePicker.getEditor().getText()+" "+departureDateFromTextField.getText());
            }
            catch (ParseException e) {

            }
        }

        Date dtDate=null;

        if (departureDateToDatePicker.getValue()!=null) {
            try {
                dtDate = format.parse(departureDateToDatePicker.getEditor().getText()+" "+departureDateToTextField.getText());
            }
            catch (ParseException e) {

            }
        }

        Date afDate=null;

        if (arrivingDateFromDatePicker.getValue()!=null) {
            try {
                afDate = format.parse(arrivingDateFromDatePicker.getEditor().getText()+" "+arrivingDateFromTextField.getText());
            }
            catch (ParseException e) {

            }
        }

        Date atDate=null;

        if (arrivingDateToDatePicker.getValue()!=null) {
            try {
                atDate = format.parse(arrivingDateToDatePicker.getEditor().getText()+" "+arrivingDateToTextField.getText());
            }
            catch (ParseException e) {

            }
        }

        Date tfDate=null;

            try {
                tfDate = travelFormat.parse(flightTimeFromTextField.getText());
            }
            catch (ParseException e) {

            }


        Date ttDate=null;

            try {
                ttDate = travelFormat.parse(flightTimeToTextField.getText());
            }
            catch (ParseException e) {

            }


        System.out.println(Main.getEngine().searchFlight(numberField.getText(), routeBox.getValue(),"","", planeID.getText(),
            dfDate, dtDate, afDate, atDate, flightTimeCheckBox.isSelected() ? tfDate:null,flightTimeCheckBox.isSelected() ? ttDate:null));

    }


    /**
     * initialization of view
     */
    @FXML
    public void initialize() {
        routeBox.getItems().addAll(Main.getEngine().searchRoute("*","*"));
        departureDateToTextField.setText("00:00");
        departureDateFromTextField.setText("00:00");
        arrivingDateToTextField.setText("00:00");
        arrivingDateFromTextField.setText("00:00");
        flightTimeFromTextField.setText("00:00");
        flightTimeToTextField.setText("00:00");

        departureDateFromTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            Pattern pattern = Pattern.compile("[0-2][0-9][:][0-5][0-9]");
            Matcher matcher = pattern.matcher(departureDateFromTextField.getCharacters());
            if (!matcher.matches())
            {
                departureDateFromTextField.setStyle("-fx-text-inner-color: red;");
            }
            else {
                departureDateFromTextField.setStyle("-fx-text-inner-color: black;");
            }
            checkTimeTextFields();
        });

        departureDateToTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            Pattern pattern = Pattern.compile("[0-2][0-9][:][0-5][0-9]");
            Matcher matcher = pattern.matcher(departureDateToTextField.getCharacters());
            if (!matcher.matches())
            {
                departureDateToTextField.setStyle("-fx-text-inner-color: red;");
            }
            else {
                departureDateToTextField.setStyle("-fx-text-inner-color: black;");
            }
            checkTimeTextFields();
        });

        arrivingDateFromTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            Pattern pattern = Pattern.compile("[0-2][0-9][:][0-5][0-9]");
            Matcher matcher = pattern.matcher(arrivingDateFromTextField.getCharacters());
            if (!matcher.matches())
            {
                arrivingDateFromTextField.setStyle("-fx-text-inner-color: red;");
            }
            else {
                arrivingDateFromTextField.setStyle("-fx-text-inner-color: black;");
            }
            checkTimeTextFields();
        });

        arrivingDateToTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            Pattern pattern = Pattern.compile("[0-2][0-9][:][0-5][0-9]");
            Matcher matcher = pattern.matcher(arrivingDateToTextField.getCharacters());
            if (!matcher.matches())
            {
                arrivingDateToTextField.setStyle("-fx-text-inner-color: red;");
            }
            else {
                arrivingDateToTextField.setStyle("-fx-text-inner-color: black;");
            }
            checkTimeTextFields();
        });

        flightTimeFromTextField.textProperty().addListener((observable,oldValue,newValue) -> {
            Pattern pattern = Pattern.compile("[0-9][0-9][:][0-5][0-9]");
            Matcher matcher = pattern.matcher(flightTimeFromTextField.getCharacters());
            if (!matcher.matches())
            {
                flightTimeFromTextField.setStyle("-fx-text-inner-color: red;");
            }
            else {
                flightTimeFromTextField.setStyle("-fx-text-inner-color: black;");
            }
            checkTimeTextFields();
        });

        flightTimeToTextField.textProperty().addListener((observable,oldValue,newValue) -> {
            Pattern pattern = Pattern.compile("[0-9][0-9][:][0-5][0-9]");
            Matcher matcher = pattern.matcher(flightTimeToTextField.getCharacters());
            if (!matcher.matches())
            {
                flightTimeToTextField.setStyle("-fx-text-inner-color: red;");
            }
            else {
                flightTimeToTextField.setStyle("-fx-text-inner-color: black;");
            }
            checkTimeTextFields();
        });

    }

    /**
     * Correct time format checking. If any time is incorrect - search button disables
     */
    private void checkTimeTextFields(){

        Pattern pattern = Pattern.compile("[0-2][0-9][:][0-5][0-9]");
        Pattern travelPattern = Pattern.compile("[0-9][0-9][:][0-5][0-9]");
        if (pattern.matcher(arrivingDateToTextField.getCharacters()).matches()
                &&pattern.matcher(arrivingDateFromTextField.getCharacters()).matches()
                &&pattern.matcher(departureDateFromTextField.getCharacters()).matches()
                &&pattern.matcher(departureDateToTextField.getCharacters()).matches()
                &&travelPattern.matcher(flightTimeFromTextField.getCharacters()).matches()
                &&travelPattern.matcher(flightTimeFromTextField.getCharacters()).matches()) {
            searchSearchFlightsButton.setDisable(false);
        }
        else {
            searchSearchFlightsButton.setDisable(true);
        }
    }


}

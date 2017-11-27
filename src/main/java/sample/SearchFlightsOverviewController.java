package sample;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import model.Route;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    @FXML
    ChoiceBox<Route> RouteBox;
    @FXML
    Button searchSearchFlightsButton;
    @FXML
    CheckBox flightTimeCheckBox;



    private void closeWindow(Event event) {
        Stage stage = (Stage) ((Parent) event.getSource()).getScene().getWindow();
        stage.close();
    }
    @FXML
    public void handleCancelAction(ActionEvent actionEvent) {
        closeWindow(actionEvent);

    }
    @FXML
    public void handleClearAction(ActionEvent actionEvent) {
        departureDateFromDatePicker.setValue(null);
        departureDateToDatePicker.setValue(null);
        arrivingDateToDatePicker.setValue(null);
        arrivingDateFromDatePicker.setValue(null);
        numberField.clear();
        planeID.clear();
        departureDateToTextField.setText("00:00");
        departureDateFromTextField.setText("00:00");
        arrivingDateToTextField.setText("00:00");
        arrivingDateFromTextField.setText("00:00");
        flightTimeFromTextField.setText("00:00");
        flightTimeToTextField.setText("00:00");


    }
    @FXML
    public void handleSearchAction(ActionEvent event){

        DateFormat format = new SimpleDateFormat("dd.MM.yyyy hh:mm");
        DateFormat travelFormat = new SimpleDateFormat("hh:mm");

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

        Date tfDate=null;


            try {
                tfDate = travelFormat.parse(flightTimeFromTextField.getText());
            }
            catch (ParseException e)
            {

            }


        Date ttDate=null;

            try {
                ttDate = travelFormat.parse(flightTimeToTextField.getText());
            }
            catch (ParseException e)
            {

            }

        System.out.println(RouteBox.getValue());
        System.out.println(Main.getEngine().searchFlight(numberField.getText(),RouteBox.getValue(),"","", planeID.getText(),
            dfDate, dtDate, afDate, atDate, flightTimeCheckBox.isSelected() ? tfDate:null,flightTimeCheckBox.isSelected() ? ttDate:null));


    }




    @FXML
    public void initialize() {
        RouteBox.getItems().addAll(Main.getEngine().searchRoute("*","*"));
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

    private void checkTimeTextFields(){
        Pattern pattern = Pattern.compile("[0-2][0-9][:][0-5][0-9]");
        Pattern travelPattern = Pattern.compile("[0-9][0-9][:][0-5][0-9]");
        if (pattern.matcher(arrivingDateToTextField.getCharacters()).matches()
                &&pattern.matcher(arrivingDateFromTextField.getCharacters()).matches()
                &&pattern.matcher(departureDateFromTextField.getCharacters()).matches()
                &&pattern.matcher(departureDateToTextField.getCharacters()).matches()
                &&travelPattern.matcher(flightTimeFromTextField.getCharacters()).matches()
                &&travelPattern.matcher(flightTimeFromTextField.getCharacters()).matches())
        {
            searchSearchFlightsButton.setDisable(false);
        }
        else {
            searchSearchFlightsButton.setDisable(true);
        }
    }


}

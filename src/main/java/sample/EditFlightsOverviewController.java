package sample;


import exceptions.FlightAndRouteException;
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
import java.time.LocalDate;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 Controller for editing a flight view
 Allows to enter data for editing the chosen flight
 */

public class EditFlightsOverviewController{

    private Controller controller = Controller.getInstance();

    @FXML ChoiceBox<Route> box;
    @FXML TextField        number;
    @FXML TextField        planeID;
    @FXML DatePicker       departureDate;
    @FXML DatePicker       arrivingDate;
    @FXML Button           editEditFlightsOverview;
    @FXML TextField        arrivingTime;
    @FXML TextField        departureTime;

    /**
     initialization of view
     */
    @FXML
    private void initialize(){
        box.setItems( controller.getRoutes() );

        number.setDisable( true );

        departureDate.getEditor().setDisable( true );
        arrivingDate.getEditor().setDisable( true );

        arrivingTime.textProperty().addListener( ( observable , oldValue , newValue ) -> {
            Pattern pattern = Pattern.compile( "[0-1][0-9][:][0-5][0-9]|[2][0-3][:][0-5][0-9]" );
            Matcher matcher = pattern.matcher( arrivingTime.getText() );
            if( !matcher.matches() ){
                arrivingTime.setStyle( "-fx-text-inner-color: red;" );
                arrivingTime.setTooltip( new Tooltip( "Time format: hh:mm" ) );
            }else{
                arrivingTime.setStyle( "-fx-text-inner-color: black;" );
                arrivingTime.setTooltip( null );
            }
            checkTimeTextFields();
        } );

        departureTime.textProperty().addListener( ( observable , oldValue , newValue ) -> {
            Pattern pattern = Pattern.compile( "[0-1][0-9][:][0-5][0-9]|[2][0-3][:][0-5][0-9]" );
            Matcher matcher = pattern.matcher( departureTime.getText() );
            if( !matcher.matches() ){
                departureTime.setStyle( "-fx-text-inner-color: red;" );
                departureTime.setTooltip( new Tooltip( "Time format: hh:mm" ) );
            }else{
                departureTime.setStyle( "-fx-text-inner-color: black;" );
                departureTime.setTooltip( null );
            }
            checkTimeTextFields();
        } );


        number.textProperty().addListener( ( observable , oldValue , newValue ) -> {
            Pattern pattern = Pattern.compile( "[0-9\\-_\\w]*" );
            Matcher matcher = pattern.matcher( number.getCharacters() );
            if( !matcher.matches() ){
                number.setStyle( "-fx-text-inner-color: red;" );
                number.setTooltip( new Tooltip( "Acceptable symbols: 0-9, a-z, -, _" ) );
            }else{
                number.setStyle( "-fx-text-inner-color: black;" );
                number.setTooltip( null );
            }
            checkTimeTextFields();
        } );

        planeID.textProperty().addListener( ( observable , oldValue , newValue ) -> {
            Pattern pattern = Pattern.compile( "[0-9\\-_\\w]*" );
            Matcher matcher = pattern.matcher( planeID.getCharacters() );
            if( !matcher.matches() ){
                planeID.setStyle( "-fx-text-inner-color: red;" );
                planeID.setTooltip( new Tooltip( "Acceptable symbols: 0-9, a-z, -, _" ) );
            }else{
                planeID.setStyle( "-fx-text-inner-color: black;" );
                planeID.setTooltip( null );
            }
            checkTimeTextFields();
        } );
    }


    /**
     */
    @FXML
    private void clearData(){
        number.clear();
        planeID.clear();
        departureDate.setValue( LocalDate.now() );
        arrivingDate.setValue( LocalDate.now() );
    }

    /**
     @param actionEvent Edit Button. Edit data about the chosen flight
     */
    @FXML
    private void handleEditAction( ActionEvent actionEvent ){
        DateFormat format     = new SimpleDateFormat( "dd.MM.yyyy hh:mm" );
        Date       arriveDate = new Date();
        Date       departDate = new Date();
        try{
            arriveDate = format.parse( arrivingDate.getEditor().getText() + " " + arrivingTime.getText() );
        }catch( ParseException ignored ){
        }
        try{
            departDate = format.parse( departureDate.getEditor().getText() + " " + departureTime.getText() );
        }catch( ParseException ignored ){
        }
        if( arriveDate.getTime() <= departDate.getTime() ){
            Alert alert = new Alert( Alert.AlertType.WARNING );
            alert.setTitle( "Incorrect data about date" );
            alert.setHeaderText( "Flight has incorrect dates" );
            alert.setContentText( "Please enter correct parameters for a flight." );

            alert.showAndWait();
        }else{
            try{
                Controller.model.editFlight( Controller.flightForEdit , box.getSelectionModel().getSelectedItem() ,
                                             planeID.getText() , departDate , arriveDate );
                controller.updateFlights();
                Main.changed = true;
                closeWindow( actionEvent );
            }catch( FlightAndRouteException e ){
                Alert alert = new Alert( Alert.AlertType.WARNING );
                alert.setTitle( "Model`s message" );
                alert.setHeaderText( "Edited data incorrect:" );
                alert.setContentText( e.getMessage() );

                alert.showAndWait();
            }
        }
    }

    /**
     @param actionEvent Cancel Button. Close the window.
     */
    public void handleCancelAction( ActionEvent actionEvent ){
        closeWindow( actionEvent );
    }

    private void closeWindow( Event event ){
        Stage stage = ( Stage ) ( ( Parent ) event.getSource() ).getScene().getWindow();
        stage.close();
    }

    private void checkTimeTextFields(){
        Pattern pattern     = Pattern.compile( "[0-9\\-_\\w]*" );
        Pattern timePattern = Pattern.compile( "[0-1][0-9][:][0-5][0-9]|[2][0-3][:][0-5][0-9]" );

        if( pattern.matcher( number.getText() ).matches() && pattern.matcher( planeID.getText() ).matches() &&
            timePattern.matcher( departureTime.getText() ).matches() &&
            timePattern.matcher( arrivingTime.getText() ).matches() ){
            editEditFlightsOverview.setDisable( false );
        }else{
            editEditFlightsOverview.setDisable( true );
        }
    }

}

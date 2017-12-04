package sample;


import exceptions.FlightAndRouteException;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Flight;
import model.Route;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 Controller for adding flight view
 Allows to enter data for adding a new flight
 */

public class AddFlightsOverviewController{

    private Controller controller = Controller.getInstance();

    @FXML ChoiceBox<Route> box;
    @FXML TextField        number;
    @FXML TextField        planeID;
    @FXML DatePicker       departureDate;
    @FXML DatePicker       arrivingDate;
    @FXML Button           addAddFlightsOverview;
    @FXML TextField        arrivingTime;
    @FXML TextField        departureTime;

    /**
     initialization of view
     */
    @FXML
    private void initialize(){


        departureDate.setValue( LocalDate.now() );
        arrivingDate.setValue( LocalDate.now().plusDays( 1 ) );
        box.setItems( controller.getRoutes() );

        departureDate.getEditor().setDisable( true );
        arrivingDate.getEditor().setDisable( true );

        arrivingTime.setText( "00:00" );
        departureTime.setText( "00:00" );

        arrivingTime.textProperty().addListener( ( observable , oldValue , newValue ) -> {
            Pattern pattern = Pattern.compile( "[0-1][0-9][:][0-5][0-9]|[2][0-3][:][0-5][0-9]" );
            Matcher matcher = pattern.matcher( arrivingTime.getText() );
            if( !matcher.matches() ){
                arrivingTime.setStyle( "-fx-text-inner-color: red;" );
            }else{
                arrivingTime.setStyle( "-fx-text-inner-color: black;" );
            }
            checkTimeTextFields();
        } );

        departureTime.textProperty().addListener( ( observable , oldValue , newValue ) -> {
            Pattern pattern = Pattern.compile( "[0-1][0-9][:][0-5][0-9]|[2][0-3][:][0-5][0-9]" );
            Matcher matcher = pattern.matcher( departureTime.getText() );
            if( !matcher.matches() ){
                departureTime.setStyle( "-fx-text-inner-color: red;" );
            }else{
                departureTime.setStyle( "-fx-text-inner-color: black;" );
            }
            checkTimeTextFields();
        } );

        number.textProperty().addListener( ( observable , oldValue , newValue ) -> {
            Pattern pattern = Pattern.compile( "[0-9\\-_\\w]*" );
            Matcher matcher = pattern.matcher( number.getCharacters() );
            if( !matcher.matches() ){
                number.setStyle( "-fx-text-inner-color: red;" );
            }else{
                number.setStyle( "-fx-text-inner-color: black;" );
            }
            checkTimeTextFields();
        } );

        planeID.textProperty().addListener( ( observable , oldValue , newValue ) -> {
            Pattern pattern = Pattern.compile( "[0-9\\-_\\w]*" );
            Matcher matcher = pattern.matcher( planeID.getCharacters() );
            if( !matcher.matches() ){
                planeID.setStyle( "-fx-text-inner-color: red;" );
            }else{
                planeID.setStyle( "-fx-text-inner-color: black;" );
            }
            checkTimeTextFields();
        } );

    }

    /**
     @param actionEvent Add Button. Add a new flight into the DataModel
     */
    @FXML
    private void handleAddAction( ActionEvent actionEvent ){

        DateFormat format = new SimpleDateFormat( "dd.MM.yyyy hh:mm" );

        Date arrivDate  = new Date();
        Date departDate = new Date();

        try{
            arrivDate = format.parse( arrivingDate.getEditor().getText() + " " + arrivingTime.getText() );
        }catch( ParseException e ){

        }

        try{
            departDate = format.parse( departureDate.getEditor().getText() + " " + departureTime.getText() );
        }catch( ParseException e ){

        }

        if( arrivDate.getTime() <= departDate.getTime() ){

            Alert alert = new Alert( Alert.AlertType.WARNING );
            alert.setTitle( "Incorrect data about date" );
            alert.setHeaderText( "Flight has incorrect dates" );
            alert.setContentText( "Please enter correct parameters for a new flight." );

            alert.showAndWait();
        }else if( controller.getFlights().stream()
                            .anyMatch( flight -> flight.getNumber().equals( number.getText() ) ) ){
            Alert alert = new Alert( Alert.AlertType.WARNING );
            alert.setTitle( "Number already exist" );
            alert.setHeaderText( "Flight with this number already exist" );
            alert.setContentText( "Please enter correct number." );

            alert.showAndWait();
        }else if( box.getValue() == null ){
            Alert alert = new Alert( Alert.AlertType.WARNING );
            alert.setTitle( "Route isn`t chosen" );
            alert.setHeaderText( "Flight must have route" );
            alert.setContentText( "Choose route" );

            alert.showAndWait();
        }else if( planeID.getText().equals( "" ) ){
            Alert alert = new Alert( Alert.AlertType.WARNING );
            alert.setTitle( "You have no plain" );
            alert.setHeaderText( "Flight must have plain" );
            alert.setContentText( "Write plain data" );

            alert.showAndWait();
        }else if( controller.getFlights().stream()
                            .anyMatch( flight -> flight.getPlaneID().equals( planeID.getText() ) ) ){
            Alert alert = new Alert( Alert.AlertType.WARNING );
            alert.setTitle( "Two flights for plain" );
            alert.setHeaderText( "Some flight have same plain" );
            alert.setContentText( "Write another plain data" );

            alert.showAndWait();
        }else{

            try{
                Controller.model.addFlight(
                        new Flight( number.getText() , box.getSelectionModel().getSelectedItem() , planeID.getText() ,
                                    departDate , arrivDate ) );
                controller.updateFlights();
                closeWindow( actionEvent );
            }catch( FlightAndRouteException e ){
                Alert alert = new Alert( Alert.AlertType.WARNING );
                alert.setTitle( "Model exception" );
                alert.setHeaderText( "Model throw an exception" );
                alert.setContentText( e.getMessage() );

                alert.showAndWait();
            }
        }
    }

    /**
     @param event Clear Button. Clear all fields in the window
     */
    @FXML
    private void clearData( ActionEvent event ){

        number.clear();
        planeID.clear();
        departureDate.setValue( LocalDate.now() );
        arrivingDate.setValue( LocalDate.now() );
        arrivingTime.setText( "00:00" );
        departureTime.setText( "00:00" );
    }

    /**
     @param actionEvent Cancel Button. Close a window for adding a new flight
     */
    @FXML
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
            addAddFlightsOverview.setDisable( false );
        }else{
            addAddFlightsOverview.setDisable( true );
        }
    }

}

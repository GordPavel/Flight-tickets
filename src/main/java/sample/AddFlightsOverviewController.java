package sample;


import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTimePicker;
import com.sun.deploy.net.proxy.pac.PACFunctions;
import exceptions.FlightAndRouteException;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import model.DataModel;
import model.Flight;
import model.Route;
import np.com.ngopal.control.AutoFillTextBox;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 Controller for adding flight view
 Allows to enter data for adding a new flight
 */

public class AddFlightsOverviewController{

    private Controller controller = Controller.getInstance();

    @FXML ChoiceBox<Route>        box;
    @FXML TextField               number;
    @FXML Label                   errorNumberLabel;
    @FXML AutoFillTextBox<String> planeID;
    @FXML JFXDatePicker           departureDate;
    @FXML JFXDatePicker           arrivingDate;
    @FXML Button                  addAddFlightsOverview;
    //@FXML TextField               arrivingTime;
    @FXML JFXTimePicker           departuretime;
    @FXML JFXTimePicker           arrivingtime;

    //@FXML TextField               departureTime;

    private DataModel dataModel = DataModel.getInstance();

    /**
     initialization of view
     */
    @FXML
    private void initialize() throws IOException{
        departureDate.setValue( LocalDate.now() );
        arrivingDate.setValue( LocalDate.now().plusDays( 1 ) );
        box.setItems( controller.getRoutes() );

        departuretime.setIs24HourView(true);
        departuretime.setValue(LocalTime.MIDNIGHT);

        arrivingtime.setIs24HourView(true);
        arrivingtime.setValue(LocalTime.MIDNIGHT);


        departureDate.getEditor().setDisable( true );
        arrivingDate.getEditor().setDisable( true );

        //arrivingTime.setText( "00:00" );
        //departureTime.setText( "00:00" );

        /*arrivingTime.textProperty().addListener( ( observable , oldValue , newValue ) -> {
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
        } );*/

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

        planeID.setData( dataModel.listAllPlanesWithPredicate( plane -> true ).collect(
                Collectors.collectingAndThen( toList() , FXCollections::observableArrayList ) ) );
        planeID.getTextbox().textProperty().addListener( ( observable , oldValue , newValue ) -> {
            Pattern pattern = Pattern.compile( "[0-9\\-_\\w]*" );
            Matcher matcher = pattern.matcher( planeID.getTextbox().getCharacters() );
            if( !matcher.matches() ){
                planeID.setStyle( "-fx-text-inner-color: red;" );
                planeID.setTooltip( new Tooltip( "Acceptable symbols: 0-9, a-z, -, _" ) );
            }else{
                planeID.setStyle( "-fx-text-inner-color: black;" );
                planeID.setTooltip( null );
            }
            checkTimeTextFields();
        } );
        number.setFont( Font.loadFont( getClass().getResource( "/PT_Mono.ttf" ).openStream() , 15 ) );
        setErrorSymbol( number.getLayoutX() + 8 , errorNumberLabel );
        number.textProperty().addListener( ( observable , oldValue , newValue ) -> {
            Matcher matcher = Pattern.compile( "(\\s+)" ).matcher( newValue );
            if( matcher.find() ){
                errorNumberLabel.setVisible( true );
                errorNumberLabel.setLayoutX(
                        number.getLayoutX() + 8 + matcher.start() * ( errorNumberLabel.getFont().getSize() / 1.45 ) );
            }else{
                errorNumberLabel.setVisible( false );
            }
        } );
    }

    private void setErrorSymbol( Double position , Label errorLabel ){
        if( position == -1.0 ){
            errorLabel.setVisible( false );
        }else{
            errorLabel.setVisible( true );
            errorLabel.setLayoutX( position );
        }
    }

    /**
     @param actionEvent Add Button. Add a new flight into the DataModel
     */
    @FXML
    private void handleAddAction( ActionEvent actionEvent ){
        DateFormat format = new SimpleDateFormat( "dd.MM.yyyy hh:mm" );

        Date arriveDate = new Date();
        Date departDate = new Date();

        try{
            arriveDate = format.parse( arrivingDate.getEditor().getText() + " " + arrivingtime.getEditor().getText() );
        }catch( ParseException ignored ){

        }

        try{
            departDate = format.parse( departureDate.getEditor().getText() + " " + departuretime.getEditor().getText() );
        }catch( ParseException ignored ){

        }

        if( arriveDate.getTime() <= departDate.getTime() ){

            Alert alert = new Alert( Alert.AlertType.WARNING );
            alert.setTitle( "Incorrect data about date" );
            alert.setHeaderText( "Flight has incorrect dates" );
            alert.setContentText( "Please enter correct parameters for a new flight." );

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
        }else{

            try{
                Controller.model.addFlight(
                        new Flight( number.getText() , box.getSelectionModel().getSelectedItem() , planeID.getText() ,
                                    departDate , arriveDate ) );
                controller.updateFlights();
                Main.changed = true;
                closeWindow( actionEvent );
            }catch( FlightAndRouteException e ){
                Alert alert = new Alert( Alert.AlertType.WARNING );
                alert.setTitle( "Model`s message" );
                alert.setHeaderText( "Model send message" );
                alert.setContentText( e.getMessage() );

                alert.showAndWait();
            }
        }
    }


    /**
     Clear Button. Clear all fields in GUI
     */
    @FXML
    private void clearData(){
        number.clear();
        planeID.getTextbox().clear();
        departureDate.setValue( LocalDate.now() );
        arrivingDate.setValue( LocalDate.now().plusDays( 1 ));
        arrivingtime.setValue( LocalTime.MIDNIGHT );
        departuretime.setValue( LocalTime.MIDNIGHT  );
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

    /**
     Check for enable/disable add button. Switch it off, if user use unacceptable symbols
     */
    private void checkTimeTextFields(){

        Pattern pattern     = Pattern.compile( "[0-9\\-_\\w]*" );
        Pattern timePattern = Pattern.compile( "[0-1][0-9][:][0-5][0-9]|[2][0-3][:][0-5][0-9]" );

        if( pattern.matcher( number.getText() ).matches() && pattern.matcher( planeID.getText() ).matches() &&
            timePattern.matcher( departuretime.getEditor().getText() ).matches() &&
            timePattern.matcher( arrivingtime.getEditor().getText() ).matches() ){
            addAddFlightsOverview.setDisable( false );
        }else{
            addAddFlightsOverview.setDisable( true );
        }
    }
}

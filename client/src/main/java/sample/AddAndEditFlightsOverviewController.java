package sample;


import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTimePicker;
import exceptions.FlightAndRouteException;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.Flight;
import model.Route;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 Controller for adding flight view
 Allows to enter data for adding a new flight
 */

public class AddAndEditFlightsOverviewController{

    private Controller controller = Controller.getInstance();

    @FXML ChoiceBox<Route> box;
    @FXML TextField        number;
    @FXML Label            errorNumberLabel;
    @FXML TextField        planeID;
    @FXML Label            errorPlaneIdLabel;
    @FXML JFXDatePicker    departureDate;
    @FXML JFXDatePicker    arrivingDate;
    @FXML JFXTimePicker    departureTime;
    @FXML JFXTimePicker    arrivingTime;
    @FXML Button           addAndEditFlightButton;
    @FXML Label            mainLabel;

    private Flight editingFLight;

    AddAndEditFlightsOverviewController( Flight editingFLight ){
        this.editingFLight = editingFLight;
    }

    /**
     initialization of view
     */
    @FXML
    private void initialize() throws IOException{
        departureDate.setValue( LocalDate.now() );
        arrivingDate.setValue( LocalDate.now().plusDays( 1 ) );
        box.setItems( controller.getRoutes() );

        StringConverter<LocalTime> localTimeStringConverter = new StringConverter<LocalTime>(){
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern( "HH:mm" );

            @Override
            public String toString( LocalTime object ){
                return timeFormatter.format( object );
            }

            @Override
            public LocalTime fromString( String string ){
                return LocalTime.parse( string , timeFormatter );
            }
        };

        departureTime.setIs24HourView( true );
        departureTime.setConverter( localTimeStringConverter );
        departureTime.setValue( LocalTime.MIDNIGHT );

        arrivingTime.setIs24HourView( true );
        arrivingTime.setConverter( localTimeStringConverter );
        arrivingTime.setValue( LocalTime.MIDNIGHT );


        departureDate.getEditor().setDisable( true );
        arrivingDate.getEditor().setDisable( true );

        Font PT_Mono = Font.loadFont( getClass().getResource("/PT_Mono.ttf").openStream() , 15 );

        number.setFont( PT_Mono );
        errorNumberLabel.setVisible( false );
        errorNumberLabel.setLayoutX( number.getLayoutX() + 8.5 );
        errorNumberLabel.setLayoutY( number.getLayoutY() + 16 );
        number.textProperty().addListener(
                ( observable , oldValue , newValue ) -> setErrorLabel( newValue , oldValue , errorNumberLabel ,
                                                                       number ) );

        planeID.setFont( PT_Mono );
        errorPlaneIdLabel.setVisible( false );
        errorPlaneIdLabel.setLayoutX( planeID.getLayoutX() + 8.5 );
        errorPlaneIdLabel.setLayoutY( planeID.getLayoutY() + 16 );
        planeID.textProperty().addListener(
                ( observable , oldValue , newValue ) -> setErrorLabel( newValue , oldValue , errorPlaneIdLabel ,
                                                                       planeID ) );
        DateFormat format = new SimpleDateFormat( "dd.MM.yyyy hh:mm" );
        addAndEditFlightButton.setOnAction( event -> {
            Date arriveDate = new Date();
            Date departDate = new Date();

            try{
                arriveDate =
                        format.parse( arrivingDate.getEditor().getText() + " " + arrivingTime.getEditor().getText() );
            }catch( ParseException ignored ){

            }

            try{
                departDate =
                        format.parse( departureDate.getEditor().getText() + " " + departureTime.getEditor().getText() );
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
                            new Flight( number.getText() , box.getSelectionModel().getSelectedItem() ,
                                        planeID.getText() , departDate , arriveDate ) );
                    controller.updateFlights();
                    /**
                     * TODO: put here request to server to add flight
                     */
                    Main.changed = true;
                    closeWindow( event );
                }catch( FlightAndRouteException e ){
                    Alert alert = new Alert( Alert.AlertType.WARNING );
                    alert.setTitle( "Model`s message" );
                    alert.setHeaderText( "Model send message" );
                    alert.setContentText( e.getMessage() );
                    alert.showAndWait();
                }
            }
        } );

        if( editingFLight != null ){
            setEditingFlightData();
            addAndEditFlightButton.setOnAction( event -> {
                Date arriveDate = new Date();
                Date departDate = new Date();
                try{
                    arriveDate = format.parse(
                            arrivingDate.getEditor().getText() + " " + arrivingTime.getEditor().getText() );
                }catch( ParseException ignored ){
                }
                try{
                    departDate = format.parse(
                            departureDate.getEditor().getText() + " " + departureTime.getEditor().getText() );
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
                        Controller.model
                                .editFlight( Controller.flightForEdit , box.getSelectionModel().getSelectedItem() ,
                                             planeID.getText() , departDate , arriveDate );
                        controller.updateFlights();
                        /**
                         * TODO: put here request to server to edit flight
                         */
                        Main.changed = true;
                        closeWindow( event );
                    }catch( FlightAndRouteException e ){
                        Alert alert = new Alert( Alert.AlertType.WARNING );
                        alert.setTitle( "Model`s message" );
                        alert.setHeaderText( "Edited data incorrect:" );
                        alert.setContentText( e.getMessage() );

                        alert.showAndWait();
                    }
                }
            } );
            number.setEditable( false );
            addAndEditFlightButton.setText( "Edit" );
            mainLabel.setText( "Enter new data." );
        }
    }

    private void setEditingFlightData(){
        number.textProperty().setValue( editingFLight.getNumber() );
        planeID.textProperty().setValue( editingFLight.getPlaneID() );
        box.getSelectionModel().select( editingFLight.getRoute() );
        SimpleDateFormat  dateFormat    = new SimpleDateFormat( "dd.MM.yyyy" );
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern( "dd.MM.yyyy" );
        SimpleDateFormat  timeFormat    = new SimpleDateFormat( "HH:mm" );
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern( "HH:mm" );
        departureDate
                .setValue( LocalDate.parse( dateFormat.format( editingFLight.getDepartureDate() ) , dateFormatter ) );
        arrivingDate.setValue( LocalDate.parse( dateFormat.format( editingFLight.getArriveDate() ) , dateFormatter ) );
        departureTime
                .setValue( LocalTime.parse( timeFormat.format( editingFLight.getDepartureDate() ) , timeFormatter ) );
        arrivingTime.setValue( LocalTime.parse( timeFormat.format( editingFLight.getArriveDate() ) , timeFormatter ) );
    }

    private void setErrorLabel( String newValue , String oldValue , Label errorLabel , TextField handlingTextField ){
        if( newValue.length() > 20 ){
            handlingTextField.textProperty().setValue( oldValue );
        }
        Matcher matcher = Pattern.compile( "[^0-9\\-_\\w]+" ).matcher( newValue );
        if( matcher.find() ){
            handlingTextField.setStyle( "-fx-text-inner-color: red;" );
            handlingTextField.setTooltip( new Tooltip( "Acceptable symbols: 0-9, a-z, -, _" ) );
            errorLabel.setVisible( true );
            errorLabel.setLayoutX( handlingTextField.getLayoutX() + 8.5 +
                                   matcher.start() * ( errorLabel.getFont().getSize() / 1.43 ) );
        }else{
            errorLabel.setVisible( false );
            handlingTextField.setStyle( "-fx-text-inner-color: black;" );
            handlingTextField.setTooltip( null );
        }
        checkErrors();
    }

    /**
     Clear Button. Clear all fields in GUI
     */
    @FXML
    private void clearData(){
        if( editingFLight != null ){
            setEditingFlightData();
        }else{
            number.clear();
            planeID.clear();
            departureDate.setValue( LocalDate.now() );
            arrivingDate.setValue( LocalDate.now().plusDays( 1 ) );
            arrivingTime.setValue( LocalTime.MIDNIGHT );
            departureTime.setValue( LocalTime.MIDNIGHT );
        }
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
    private void checkErrors(){
        Pattern pattern = Pattern.compile( "[0-9\\-_\\w]*" );
        if( pattern.matcher( number.getText() ).matches() && pattern.matcher( planeID.getText() ).matches() ){
            addAndEditFlightButton.setDisable( false );
        }else{
            addAndEditFlightButton.setDisable( true );
        }
    }
}

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

import com.browniebytes.javafx.control.DateTimePicker;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
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
    @FXML DateTimePicker   departureDate;
    @FXML DateTimePicker   arrivingDate;
    @FXML Button           addAddFlightsOverview;
    //@FXML TextField        arrivingTime;
    //@FXML TextField        departureTime;

    /**
     initialization of view
     */
    @FXML
    private void initialize(){


        departureDate.dateTimeProperty().setValue(LocalDateTime.now());
        arrivingDate.dateTimeProperty().setValue(LocalDateTime.now().plusDays( 1 ));
        //departureDate.setValue( LocalDate.now() );
        //arrivingDate.setValue( LocalDate.now().plusDays( 1 ) );
        box.setItems( controller.getRoutes() );

        //departureDate.setDisable(true);
        //arrivingDate.setDisable(true);
        //departureDate.getEditor().setDisable( true );
       // arrivingDate.getEditor().setDisable( true );

       // arrivingTime.setText( "00:00" );
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
        } );*/

        /*departureTime.textProperty().addListener( ( observable , oldValue , newValue ) -> {
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
            //checkTimeTextFields();
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
            //checkTimeTextFields();
        } );

    }

    /**
     @param actionEvent Add Button. Add a new flight into the DataModel
     */
    @FXML
    private void handleAddAction( ActionEvent actionEvent ){

        DateFormat format = new SimpleDateFormat( "dd.MM.yyyy hh:mm" );

        LocalDateTime localArrivDate = arrivingDate.dateTimeProperty().getValue();
       // LocalTime arriveTime = arrivingDate.dateTimeProperty().getValue().toLocalTime();

        LocalDateTime localDepartDate = departureDate.dateTimeProperty().getValue();
        //LocalTime departTime = departureDate.dateTimeProperty().getValue().toLocalTime();


        Date arriveDate = new Date();
        Date departDate = new Date();

        arriveDate = Date.from(localArrivDate.atZone(ZoneId.systemDefault()).toInstant());
        departDate = Date.from(localDepartDate.atZone(ZoneId.systemDefault()).toInstant());

        //departureDate.dateTimeProperty().getValue().toLocalDate();
        //arrivingDate.dateTimeProperty().getValue().toLocalDate();


        /*try{
            arriveDate = format.parse( localArrivDate. + " " + arriveTime arrivingDate.getEditor().getText() + " " + arrivingTime.getText() );
        }catch( ParseException e ){

        }

        try{
            departDate = format.parse( localDepartDate + " " + departTime departureDate.getEditor().getText() + " " + departureTime.getText() );
        }catch( ParseException e ){

        }*/


        System.out.println(arriveDate);
        System.out.println(departDate);
        System.out.println(arrivingDate.dateTimeProperty().getValue().toLocalDate().toString());
        System.out.println(arrivingDate.dateTimeProperty().getValue().toLocalTime().toString());
        System.out.println(departureDate.dateTimeProperty().getValue().toLocalDate().toString());
        System.out.println(departureDate.dateTimeProperty().getValue().toLocalTime().toString());
        if(/*arriveDate.getTime() <= departDate.getTime()*/  arrivingDate.dateTimeProperty().getValue().isBefore(departureDate.dateTimeProperty().getValue())){

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
     @param event Clear Button. Clear all fields in the window
     */
    @FXML
    private void clearData( ActionEvent event ){

        number.clear();
        planeID.clear();
        departureDate.dateTimeProperty().setValue( LocalDateTime.now() );
        arrivingDate.dateTimeProperty().setValue( LocalDateTime.now().plusDays( 1 ) );
        //arrivingTime.setText( "00:00" );
        //departureTime.setText( "00:00" );
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


   /* private void checkTimeTextFields(){

        Pattern pattern     = Pattern.compile( "[0-9\\-_\\w]*" );
        Pattern timePattern = Pattern.compile( "[0-1][0-9][:][0-5][0-9]|[2][0-3][:][0-5][0-9]" );

        if( pattern.matcher( number.getText() ).matches() && pattern.matcher( planeID.getText() ).matches() &&
            timePattern.matcher( departureTime.getText() ).matches() &&
            timePattern.matcher( arrivingTime.getText() ).matches() ){
            addAddFlightsOverview.setDisable( false );
        }else{
            addAddFlightsOverview.setDisable( true );
        }
    }*/

}

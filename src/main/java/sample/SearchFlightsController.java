package sample;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

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

    public Button clear;

    private MainWindowController mainWindowController;
    private Stage                thisStage;

    SearchFlightsController( MainWindowController mainWindowController , Stage thisStage ){
        this.mainWindowController = mainWindowController;
        this.thisStage = thisStage;
    }

    @FXML
    private void initialize(){
        presetFiltersStrings();
        setTextFieldsListeners();
        setDateTimeFilters();
        clear.setOnAction( event -> {
            mainWindowController.number = null;
            numberTextField.setText( "" );
            mainWindowController.plane = null;
            planeIdTextField.setText( "" );
            mainWindowController.from = null;
            departureAirportTextField.setText( "" );
            mainWindowController.to = null;
            arrivalAirportTextField.setText( "" );
            mainWindowController.departureDate = null;
            departureDatePicker.getEditor().setText( "" );
            mainWindowController.startDepartureTimeRange = null;
            departureStartTimeRangeTextField.setText( "" );
            mainWindowController.endDepartureTimeRange = null;
            departureEndTimeRangeTextField.setText( "" );
            mainWindowController.arrivalDate = null;
            arrivalDatePicker.getEditor().setText( "" );
            mainWindowController.startArrivalTimeRange = null;
            arrivalStartTimeRangeTextField.setText( "" );
            mainWindowController.endArrivalTimeRange = null;
            arrivalEndTimeRangeTextField.setText( "" );
            mainWindowController.startFLightTimeRange = null;
            startFlightTimeRangeTextField.setText( "" );
            mainWindowController.endFLightTimeRange = null;
            endFlightTimeRangeTextField.setText( "" );
            mainWindowController.updateFlightsList();
        } );
        thisStage.setOnCloseRequest( event -> {
            mainWindowController.updateFlightsList();
        } );
    }

    private void setTextFieldsListeners(){
        numberTextField.textProperty().addListener( ( observable , oldValue , newValue ) -> {
            mainWindowController.number = newValue != null && !newValue.isEmpty() ? newValue : null;
            mainWindowController.updateFlightsList();
        } );
        planeIdTextField.textProperty().addListener( ( observable , oldValue , newValue ) -> {
            mainWindowController.plane = newValue != null && !newValue.isEmpty() ? newValue : null;
            mainWindowController.updateFlightsList();
        } );
        departureAirportTextField.textProperty().addListener( ( observable , oldValue , newValue ) -> {
            mainWindowController.from = newValue != null && !newValue.isEmpty() ? newValue : null;
            mainWindowController.updateFlightsList();
        } );
        arrivalAirportTextField.textProperty().addListener( ( observable , oldValue , newValue ) -> {
            mainWindowController.to = newValue != null && !newValue.isEmpty() ? newValue : null;
            mainWindowController.updateFlightsList();
        } );
        startFlightTimeRangeTextField.textProperty().addListener( ( observable , oldValue , newValue ) -> {
            if( newValue != null && newValue.matches( "\\d+:[0-5]\\d" ) ){
                try{
                    String[] hoursAndMinutes = newValue.split( ":" );
                    Long hours = Long.parseUnsignedLong( hoursAndMinutes[ 0 ] ), minutes =
                            Long.parseUnsignedLong( hoursAndMinutes[ 1 ] );
                    mainWindowController.startFLightTimeRange = ( ( hours * 60 ) + minutes ) * 60 * 1000;
                    mainWindowController.updateFlightsList();
                }catch( NumberFormatException e ){
                    mainWindowController.startFLightTimeRange = null;
                    mainWindowController.updateFlightsList();
                }
            }else{
                mainWindowController.startFLightTimeRange = null;
                mainWindowController.updateFlightsList();
            }
        } );
        endFlightTimeRangeTextField.textProperty().addListener( ( observable , oldValue , newValue ) -> {
            if( newValue != null && newValue.matches( "\\d+:[0-5]\\d" ) ){
                try{
                    String[] hoursAndMinutes = newValue.split( ":" );
                    Long hours = Long.parseUnsignedLong( hoursAndMinutes[ 0 ] ), minutes =
                            Long.parseUnsignedLong( hoursAndMinutes[ 1 ] );
                    mainWindowController.endFLightTimeRange = ( ( hours * 60 ) + minutes ) * 60 * 1000;
                    mainWindowController.updateFlightsList();
                }catch( NumberFormatException e ){
                    mainWindowController.endFLightTimeRange = null;
                    mainWindowController.updateFlightsList();
                }
            }else{
                mainWindowController.endFLightTimeRange = null;
                mainWindowController.updateFlightsList();
            }
        } );
    }

    private void presetFiltersStrings(){
        numberTextField.setText( mainWindowController.number != null ? mainWindowController.number : "" );
        planeIdTextField.setText( mainWindowController.plane != null ? mainWindowController.plane : "" );
        departureAirportTextField.setText( mainWindowController.from != null ? mainWindowController.from : "" );
        departureAirportTextField.setText( mainWindowController.to != null ? mainWindowController.to : "" );
        departureDatePicker.getEditor().setText( mainWindowController.departureDate != null ?
                                                 DateTimeFormatter.ofPattern( "dd.MM.yyyy" )
                                                                  .format( mainWindowController.departureDate ) : "" );
        departureStartTimeRangeTextField.setText( mainWindowController.startDepartureTimeRange != null ?
                                                  DateTimeFormatter.ofPattern( "HH:mm" ).format(
                                                          mainWindowController.startDepartureTimeRange ) : "" );
        departureEndTimeRangeTextField.setText( mainWindowController.endDepartureTimeRange != null ?
                                                DateTimeFormatter.ofPattern( "HH:mm" )
                                                                 .format( mainWindowController.endDepartureTimeRange ) :
                                                "" );
        arrivalDatePicker.getEditor().setText( mainWindowController.arrivalDate != null ?
                                               DateTimeFormatter.ofPattern( "dd.MM.yyyy" )
                                                                .format( mainWindowController.arrivalDate ) : "" );
        arrivalStartTimeRangeTextField.setText( mainWindowController.startArrivalTimeRange != null ?
                                                DateTimeFormatter.ofPattern( "HH:mm" )
                                                                 .format( mainWindowController.startArrivalTimeRange ) :
                                                "" );
        arrivalEndTimeRangeTextField.setText( mainWindowController.endArrivalTimeRange != null ?
                                              DateTimeFormatter.ofPattern( "HH:mm" )
                                                               .format( mainWindowController.endArrivalTimeRange ) :
                                              "" );
        startFlightTimeRangeTextField.setText( mainWindowController.startFLightTimeRange != null ?
                                               DateTimeFormatter.ofPattern( "HH:mm" ).format( Instant.ofEpochMilli(
                                                       mainWindowController.startFLightTimeRange ) ) : "" );
        endFlightTimeRangeTextField.setText( mainWindowController.endFLightTimeRange != null ?
                                             DateTimeFormatter.ofPattern( "HH:mm" ).format(
                                                     Instant.ofEpochMilli( mainWindowController.endFLightTimeRange ) ) :
                                             "" );
    }

    private void setDateTimeFilters(){
        StringConverter<LocalDate> localDateConverter = new StringConverter<LocalDate>(){
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "dd.MM.yyyy" );

            @Override
            public String toString( LocalDate date ){
                return date != null ? formatter.format( date ) : null;
            }

            @Override
            public LocalDate fromString( String string ){
                return string != null ? LocalDate.from( formatter.parse( string ) ) : null;
            }
        };

        departureDatePicker.getEditor().textProperty().addListener( ( observable , oldValue , newValue ) -> {
            if( newValue != null && newValue.matches( "[0-3]\\d.[0-1]\\d.\\d{4}" ) ){
                try{
                    mainWindowController.departureDate =
                            LocalDate.parse( newValue , DateTimeFormatter.ofPattern( "dd.MM.yyyy" ) );
                    mainWindowController.updateFlightsList();
                }catch( Exception e ){
                    mainWindowController.departureDate = null;
                    mainWindowController.updateFlightsList();
                }
            }else{
                mainWindowController.departureDate = null;
                mainWindowController.updateFlightsList();
            }
        } );
        departureStartTimeRangeTextField.textProperty().addListener( ( observable , oldValue , newValue ) -> {
            if( newValue != null && newValue.matches( "[0-2]\\d:[0-5]\\d" ) ){
                try{
                    mainWindowController.startDepartureTimeRange =
                            LocalTime.parse( newValue , DateTimeFormatter.ofPattern( "HH:mm" ) );
                    mainWindowController.updateFlightsList();
                }catch( DateTimeParseException e ){
                    mainWindowController.startDepartureTimeRange = null;
                    mainWindowController.updateFlightsList();
                }
            }else{
                mainWindowController.startDepartureTimeRange = null;
                mainWindowController.updateFlightsList();
            }
        } );
        departureEndTimeRangeTextField.textProperty().addListener( ( observable , oldValue , newValue ) -> {
            if( newValue != null && newValue.matches( "[0-2]\\d:[0-5]\\d" ) ){
                try{
                    mainWindowController.endDepartureTimeRange =
                            LocalTime.parse( newValue , DateTimeFormatter.ofPattern( "HH:mm" ) );
                    mainWindowController.updateFlightsList();
                }catch( DateTimeParseException e ){
                    mainWindowController.endDepartureTimeRange = null;
                    mainWindowController.updateFlightsList();
                }
            }else{
                mainWindowController.endDepartureTimeRange = null;
                mainWindowController.updateFlightsList();
            }
        } );
        departureDatePicker.setConverter( localDateConverter );
        departureDatePicker.setPromptText( "dd.MM.yyyy" );

        arrivalDatePicker.getEditor().textProperty().addListener( ( observable , oldValue , newValue ) -> {
            if( newValue != null && newValue.matches( "[0-3]\\d.[0-1]\\d.\\d{4}" ) ){
                try{
                    mainWindowController.arrivalDate =
                            LocalDate.parse( newValue , DateTimeFormatter.ofPattern( "dd.MM.yyyy" ) );
                    mainWindowController.updateFlightsList();
                }catch( DateTimeParseException e ){
                    mainWindowController.arrivalDate = null;
                    mainWindowController.updateFlightsList();
                }
            }else{
                mainWindowController.departureDate = null;
                mainWindowController.updateFlightsList();
            }
        } );
        arrivalStartTimeRangeTextField.textProperty().addListener( ( observable , oldValue , newValue ) -> {
            if( newValue != null && newValue.matches( "[0-2]\\d:[0-5]\\d" ) ){
                try{
                    mainWindowController.startArrivalTimeRange =
                            LocalTime.parse( newValue , DateTimeFormatter.ofPattern( "HH:mm" ) );
                    mainWindowController.updateFlightsList();
                }catch( DateTimeParseException e ){
                    mainWindowController.startArrivalTimeRange = null;
                    mainWindowController.updateFlightsList();
                }
            }else{
                mainWindowController.startArrivalTimeRange = null;
                mainWindowController.updateFlightsList();
            }
        } );
        arrivalEndTimeRangeTextField.textProperty().addListener( ( observable , oldValue , newValue ) -> {
            if( newValue != null && newValue.matches( "[0-2]\\d:[0-5]\\d" ) ){
                try{
                    mainWindowController.endArrivalTimeRange =
                            LocalTime.parse( newValue , DateTimeFormatter.ofPattern( "HH:mm" ) );
                    mainWindowController.updateFlightsList();
                }catch( DateTimeParseException e ){
                    mainWindowController.endArrivalTimeRange = null;
                    mainWindowController.updateFlightsList();
                }
            }else{
                mainWindowController.endArrivalTimeRange = null;
                mainWindowController.updateFlightsList();
            }
        } );
        arrivalDatePicker.setConverter( localDateConverter );
        arrivalDatePicker.setPromptText( "dd.MM.yyyy" );
    }


}

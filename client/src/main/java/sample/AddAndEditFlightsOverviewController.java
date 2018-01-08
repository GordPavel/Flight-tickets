package sample;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTimePicker;
import exceptions.FlightAndRouteException;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.Flight;
import model.Route;

import java.io.IOException;
import java.sql.Time;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 Controller for adding flight view
 Allows to enter data for adding a new flight
 */

public class AddAndEditFlightsOverviewController{

    private Controller controller = Controller.getInstance();

    @FXML ChoiceBox<Route> routesBox;
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
    @FXML Label            flightTimeErrorLabel;

    private Flight          editingFLight;
    private Stage           thisStage;
    private BooleanProperty ifFlightTimeRight;

    AddAndEditFlightsOverviewController( Flight editingFLight , Stage thisStage ){
        this.editingFLight = editingFLight;
        this.thisStage = thisStage;
    }

    /**
     initialization of view
     */
    @FXML
    private void initialize() throws IOException{
        final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern( "dd.MM.yyyy" );
        final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern( "HH:mm" );
        StringConverter<LocalDate> localDateStringConverter = new StringConverter<LocalDate>(){
            @Override
            public String toString( LocalDate date ){
                return dateFormatter.format( date );
            }

            @Override
            public LocalDate fromString( String string ){
                return LocalDate.parse( string , dateFormatter );
            }
        };

        departureDate.setConverter( localDateStringConverter );
        departureDate.setValue( LocalDate.now() );
        departureDate.getEditor().setDisable( true );

        arrivingDate.setConverter( localDateStringConverter );
        arrivingDate.setValue( LocalDate.now() );
        arrivingDate.getEditor().setDisable( true );

        routesBox.setItems( controller.getRoutes() );

        StringConverter<LocalTime> localTimeStringConverter = new StringConverter<LocalTime>(){
            @Override
            public String toString( LocalTime time ){
                return timeFormatter.format( time );
            }

            @Override
            public LocalTime fromString( String string ){
                return LocalTime.parse( string , timeFormatter );
            }
        };

        departureTime.setIs24HourView( true );
        departureTime.setConverter( localTimeStringConverter );
        departureTime.setValue( LocalTime.NOON );
        departureTime.getEditor().setDisable( true );

        arrivingTime.setIs24HourView( true );
        arrivingTime.setConverter( localTimeStringConverter );
        arrivingTime.setValue( LocalTime.NOON.plusHours( 1 ) );
        arrivingTime.getEditor().setDisable( true );

        flightTimeErrorLabel.setVisible( false );

        departureDate.getEditor().textProperty().addListener( ( observable , oldValue , newValue ) -> {
            String time = departureTime.getValue().format( timeFormatter );
            departureDateTimeMoved( oldValue , time , newValue , time );
        } );
        departureTime.getEditor().textProperty().addListener( ( observable , oldValue , newValue ) -> {
            String date = departureDate.getValue().format( dateFormatter );
            departureDateTimeMoved( date , oldValue , date , newValue );
        } );

//        set minimal offset between departure and arrival ( 0 seconds flight )
        routesBox.getSelectionModel().selectedItemProperty().addListener(
                ( observable , oldValue , newValue ) -> Optional.ofNullable( newValue ).ifPresent( newRoute -> {
                    ZonedDateTime arriveTimeWithOffset;
                    long flightTimeInMinutes = Optional.ofNullable( oldValue ).map( oldRoute -> ChronoUnit.MINUTES
                            .between( LocalDateTime.of( departureDate.getValue() , departureTime.getValue() )
                                                   .atZone( oldRoute.getFrom() ) ,
                                      LocalDateTime.of( arrivingDate.getValue() , arrivingTime.getValue() )
                                                   .atZone( oldRoute.getTo() ) ) ).orElse( 60L );
                    arriveTimeWithOffset = LocalDateTime.of( departureDate.getValue() , departureTime.getValue() )
                                                        .atZone( newRoute.getFrom() )
                                                        .withZoneSameInstant( newRoute.getTo() )
                                                        .plusMinutes( flightTimeInMinutes );
                    arrivingDate.setValue( arriveTimeWithOffset.toLocalDate() );
                    arrivingTime.setValue( arriveTimeWithOffset.toLocalTime() );
                } ) );

        ifFlightTimeRight = new SimpleBooleanProperty( true );
        departureDate.getEditor().textProperty()
                     .addListener( ( observable , oldValue , newValue ) -> checkFlightTime() );
        arrivingDate.getEditor().textProperty()
                    .addListener( ( observable , oldValue , newValue ) -> checkFlightTime() );
        departureTime.getEditor().textProperty()
                     .addListener( ( observable , oldValue , newValue ) -> checkFlightTime() );
        arrivingTime.getEditor().textProperty()
                    .addListener( ( observable , oldValue , newValue ) -> checkFlightTime() );
        addAndEditFlightButton.disableProperty().bind( ifFlightTimeRight.not() );
        flightTimeErrorLabel.visibleProperty().bind( ifFlightTimeRight.not() );

        Font PT_Mono = Font.loadFont( getClass().getResource( "/PT_Mono.ttf" ).openStream() , 15 );

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

        if( editingFLight != null ){
            setEditingFlightData();
            number.setDisable( true );
            addAndEditFlightButton.setText( "Edit" );
            mainLabel.setText( "Enter new data." );
        }
        addAndEditFlightButton.setOnAction( event -> addOrEdit( editingFLight == null ) );
    }

    private void departureDateTimeMoved( String oldDate , String oldTime , String newDate , String newTime ){
        LocalDateTime oldDepartureDateTime = LocalDateTime.of( departureDate.getConverter().fromString( oldDate ) ,
                                                               departureTime.getConverter().fromString( oldTime ) ),
                newDepartureDateTime = LocalDateTime.of( departureDate.getConverter().fromString( newDate ) ,
                                                         departureTime.getConverter().fromString( newTime ) ),
                oldArriveDateTime = LocalDateTime.of( arrivingDate.getValue() , arrivingTime.getValue() ),
                newArriveDateTime;
        Long departureMovedInMinutes = ChronoUnit.MINUTES.between( oldDepartureDateTime , newDepartureDateTime );
        newArriveDateTime = oldArriveDateTime.plusMinutes( departureMovedInMinutes );
        arrivingDate.setValue( newArriveDateTime.toLocalDate() );
        arrivingTime.setValue( newArriveDateTime.toLocalTime() );
    }

    private void checkFlightTime(){
        ifFlightTimeRight.setValue( !LocalDateTime.of( departureDate.getValue() , departureTime.getValue() ).atZone(
                Optional.ofNullable( routesBox.getSelectionModel().getSelectedItem() )
                        .map( Route::getFrom )
                        .orElse( ZoneId.systemDefault() ) )
                                                  .isAfter( LocalDateTime.of( arrivingDate.getValue() , arrivingTime.getValue() )
                                                                         .atZone( Optional.ofNullable( routesBox.getSelectionModel().getSelectedItem() )
                                                                                          .map( Route::getTo )
                                                                                          .orElse( ZoneId.systemDefault() ) ) ) );
    }

    private void addOrEdit( Boolean isAdd ){
        ZonedDateTime departureDateTime = LocalDateTime.of( departureDate.getValue() , departureTime.getValue() )
                                                       .atZone( routesBox.getSelectionModel().getSelectedItem()
                                                                         .getFrom() ), arriveDateTime =
                LocalDateTime.of( arrivingDate.getValue() , arrivingTime.getValue() )
                             .atZone( routesBox.getSelectionModel().getSelectedItem().getTo() );
        if( routesBox.getValue() == null ){
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
                if( isAdd ){
                    Controller.model.addFlight(
                            new Flight( number.getText() , routesBox.getSelectionModel().getSelectedItem() ,
                                        planeID.getText() , departureDateTime , arriveDateTime ) );
                }else{
                    Controller.model.editFlight( editingFLight , routesBox.getSelectionModel().getSelectedItem() ,
                                                 planeID.getText() , departureDateTime , arriveDateTime );
                }
                controller.updateFlights();
                Main.changed = true;
                closeWindow();
            }catch( FlightAndRouteException e ){
                Alert alert = new Alert( Alert.AlertType.ERROR );
                alert.setTitle( "Model`s message" );
                alert.setHeaderText( "Model send message" );
                alert.setContentText( e.getMessage() );
                alert.showAndWait();
            }
        }
    }

    private void setEditingFlightData(){
        number.textProperty().setValue( editingFLight.getNumber() );
        planeID.textProperty().setValue( editingFLight.getPlaneID() );
        routesBox.getSelectionModel().select( editingFLight.getRoute() );
        departureDate.setValue( editingFLight.getDepartureDateTime().toLocalDate() );
        arrivingDate.setValue( editingFLight.getArriveDateTime().toLocalDate() );
        departureTime.setValue( editingFLight.getDepartureDateTime().toLocalTime() );
        arrivingTime.setValue( editingFLight.getArriveDateTime().toLocalTime() );
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
            routesBox.getSelectionModel().clearSelection();
            departureDate.setValue( LocalDate.now() );
            arrivingDate.setValue( LocalDate.now().plusDays( 1 ) );
            arrivingTime.setValue( LocalTime.MIDNIGHT );
            departureTime.setValue( LocalTime.MIDNIGHT );
        }
    }

    /**
     */
    @FXML
    public void handleCancelAction(){
        closeWindow();
    }

    private void closeWindow(){
        thisStage.close();
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

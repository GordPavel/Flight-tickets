package sample;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTimePicker;
import exceptions.FlightAndRouteException;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.Flight;
import model.Route;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
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

    private Flight editingFLight;
    private Stage  thisStage;

    AddAndEditFlightsOverviewController( Flight editingFLight , Stage thisStage ){
        this.editingFLight = editingFLight;
        this.thisStage = thisStage;
    }

    /**
     initialization of view
     */
    @FXML
    private void initialize() throws IOException{
        departureDate.setValue( LocalDate.now() );
        arrivingDate.setValue( LocalDate.now() );
        routesBox.setItems( controller.getRoutes() );
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
        departureTime.setValue( LocalTime.NOON );
        departureTime.getEditor().setDisable( true );

        arrivingTime.setIs24HourView( true );
        arrivingTime.setConverter( localTimeStringConverter );
        arrivingTime.setValue( LocalTime.NOON.plusHours( 1 ) );
        arrivingTime.getEditor().setDisable( true );

//        set minimal offset between departure and arrival ( 0 seconds flight )
        routesBox.getSelectionModel().selectedItemProperty().addListener(
                ( observable , oldValue , newValue ) -> Optional.ofNullable( newValue ).ifPresent( route -> {
                    LocalDateTime departure = LocalDateTime.of( departureDate.getValue() , departureTime.getValue() );
                    long offset = route.getTo().getRules().getOffset( Instant.now() ).getTotalSeconds() -
                                  route.getFrom().getRules().getOffset( Instant.now() ).getTotalSeconds();
                    LocalDateTime arrive;
                    if( offset >= 0 ){
                        arrive = departure.plusSeconds( offset );
                    }else{
                        arrive = departure.minusSeconds( offset );
                    }
                    arrivingDate.setValue( arrive.toLocalDate() );
                    arrivingTime.setValue( arrive.toLocalTime() );
                } ) );

        departureDate.getEditor().setDisable( true );
        arrivingDate.getEditor().setDisable( true );

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

    private void addOrEdit( Boolean isAdd ){
        ZonedDateTime departureDateTime = LocalDateTime.of( departureDate.getValue() , departureTime.getValue() )
                                                       .atZone( routesBox.getSelectionModel().getSelectedItem()
                                                                         .getFrom() ), arriveDateTime =
                LocalDateTime.of( arrivingDate.getValue() , arrivingTime.getValue() )
                             .atZone( routesBox.getSelectionModel().getSelectedItem().getTo() );
        if( !departureDateTime.isBefore( arriveDateTime ) ){
            Alert alert = new Alert( Alert.AlertType.WARNING );
            alert.setTitle( "Incorrect data about date" );
            alert.setHeaderText( "Flight has incorrect dates" );
            alert.setContentText( "Please enter correct parameters for a new flight." );
            alert.showAndWait();
        }else if( routesBox.getValue() == null ){
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

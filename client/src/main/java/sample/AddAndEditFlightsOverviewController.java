package sample;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jfoenix.controls.JFXButton;
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
import model.DataModelInstanceSaver;
import model.Flight;
import model.Route;
import transport.ListChangeAdapter;

import java.io.DataOutputStream;
import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 Controller for adding flight view
 Allows to enter data for adding a new flight
 */

class AddAndEditFlightsOverviewController{

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
    @FXML JFXButton        clearButton;
    @FXML JFXButton        cancelButton;


    Flight editingFlight;
    private Stage           thisStage;
    private BooleanProperty ifFlightTimeRight;
    private BooleanProperty syntaxErrors;


    AddAndEditFlightsOverviewController( Flight editingFlight , Stage thisStage ){
        this.editingFlight = editingFlight;
        this.thisStage = thisStage;
        ifFlightTimeRight = new SimpleBooleanProperty( true );
        syntaxErrors = new SimpleBooleanProperty( false );
    }

    /**
     initialization of view
     */
    @FXML
    void initialize() throws IOException{
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

        routesBox.setItems( DataModelInstanceSaver.getInstance().getRouteObservableList() );

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
        routesBox.getSelectionModel()
                 .selectedItemProperty()
                 .addListener( ( observable , oldValue , newValue ) -> setMinimalOffsetBetweenDepartureAndArrival(
                         oldValue ,
                         newValue ) );

        departureDate.getEditor()
                     .textProperty()
                     .addListener( ( observable , oldValue , newValue ) -> checkFlightTime() );
        arrivingDate.getEditor()
                    .textProperty()
                    .addListener( ( observable , oldValue , newValue ) -> checkFlightTime() );
        departureTime.getEditor()
                     .textProperty()
                     .addListener( ( observable , oldValue , newValue ) -> checkFlightTime() );
        arrivingTime.getEditor()
                    .textProperty()
                    .addListener( ( observable , oldValue , newValue ) -> checkFlightTime() );
        addAndEditFlightButton.disableProperty().bind( ifFlightTimeRight.not() );
        flightTimeErrorLabel.visibleProperty().bind( ifFlightTimeRight.not() );

        Font PT_Mono = Font.loadFont( getClass().getResource( "/PT_Mono.ttf" ).openStream() , 15 );

        number.setFont( PT_Mono );
        errorNumberLabel.setVisible( false );
        errorNumberLabel.setLayoutX( number.getLayoutX() + 8.5 );
        errorNumberLabel.setLayoutY( number.getLayoutY() + 16 );
        number.textProperty().addListener( ( observable , oldValue , newValue ) -> {
            if( newValue.length() > 20 ){
                number.textProperty().setValue( oldValue );
            }
        } );
        number.textProperty()
              .addListener( ( observable , oldValue , newValue ) -> setErrorLabel( newValue ,
                                                                                   errorNumberLabel ,
                                                                                   number ) );

        planeID.setFont( PT_Mono );
        errorPlaneIdLabel.setVisible( false );
        errorPlaneIdLabel.setLayoutX( planeID.getLayoutX() + 8.5 );
        errorPlaneIdLabel.setLayoutY( planeID.getLayoutY() + 16 );
        planeID.textProperty().addListener( ( observable , oldValue , newValue ) -> {
            if( newValue.length() > 20 ){
                planeID.textProperty().setValue( oldValue );
            }
        } );
        planeID.textProperty()
               .addListener( ( observable , oldValue , newValue ) -> setErrorLabel( newValue ,
                                                                                    errorPlaneIdLabel ,
                                                                                    planeID ) );

        if( editingFlight != null ){
            setEditingFlightData();
            number.setDisable( true );
            addAndEditFlightButton.setText( "Edit" );
            mainLabel.setText( "Enter new data." );
        }
        addAndEditFlightButton.setOnAction( event -> addOrEdit( editingFlight == null ) );
        clearButton.setOnAction( event -> clearData() );
        cancelButton.setOnAction( event -> closeWindow() );
    }

    private void setMinimalOffsetBetweenDepartureAndArrival( Route oldValue , Route newValue ){
        Optional.ofNullable( newValue ).ifPresent( newRoute -> {
            ZonedDateTime arriveTimeWithOffset;
            long
                    flightTimeInMinutes =
                    Optional.ofNullable( oldValue )
                            .map( oldRoute -> ChronoUnit.MINUTES.between( LocalDateTime.of( departureDate.getValue() ,
                                                                                            departureTime.getValue() )
                                                                                       .atZone( oldRoute.getFrom() ) ,
                                                                          LocalDateTime.of( arrivingDate.getValue() ,
                                                                                            arrivingTime.getValue() )
                                                                                       .atZone( oldRoute.getTo() ) ) )
                            .orElse( 60L );
            arriveTimeWithOffset =
                    LocalDateTime.of( departureDate.getValue() , departureTime.getValue() )
                                 .atZone( newRoute.getFrom() )
                                 .withZoneSameInstant( newRoute.getTo() )
                                 .plusMinutes( flightTimeInMinutes );
            arrivingDate.setValue( arriveTimeWithOffset.toLocalDate() );
            arrivingTime.setValue( arriveTimeWithOffset.toLocalTime() );
        } );
    }

    private void departureDateTimeMoved( String oldDate , String oldTime , String newDate , String newTime ){
        if( !oldDate.equals( "" ) && !oldTime.equals( "" ) ){
            LocalDateTime
                    oldDepartureDateTime =
                    LocalDateTime.of( departureDate.getConverter().fromString( oldDate ) ,
                                      departureTime.getConverter().fromString( oldTime ) ),
                    newDepartureDateTime =
                            LocalDateTime.of( departureDate.getConverter().fromString( newDate ) ,
                                              departureTime.getConverter().fromString( newTime ) ),
                    oldArriveDateTime =
                            LocalDateTime.of( arrivingDate.getValue() , arrivingTime.getValue() ), newArriveDateTime;
            Long departureMovedInMinutes = ChronoUnit.MINUTES.between( oldDepartureDateTime , newDepartureDateTime );
            newArriveDateTime = oldArriveDateTime.plusMinutes( departureMovedInMinutes );
            arrivingDate.setValue( newArriveDateTime.toLocalDate() );
            arrivingTime.setValue( newArriveDateTime.toLocalTime() );
        }
    }

    private void checkFlightTime(){
        ZonedDateTime
                departureDateTime =
                LocalDateTime.of( departureDate.getValue() , departureTime.getValue() )
                             .atZone( Optional.ofNullable( routesBox.getSelectionModel().getSelectedItem() )
                                              .map( Route::getFrom )
                                              .orElse( ZoneId.systemDefault() ) ),
                arrivalDateTime =
                        LocalDateTime.of( arrivingDate.getValue() , arrivingTime.getValue() )
                                     .atZone( Optional.ofNullable( routesBox.getSelectionModel().getSelectedItem() )
                                                      .map( Route::getTo )
                                                      .orElse( ZoneId.systemDefault() ) );
        ifFlightTimeRight.setValue( !departureDateTime.isAfter( arrivalDateTime ) );
    }

    void addOrEdit( Boolean isAdd ){
        ZonedDateTime
                departureDateTime =
                LocalDateTime.of( departureDate.getValue() , departureTime.getValue() )
                             .atZone( routesBox.getSelectionModel().getSelectedItem().getFrom() ),
                arriveDateTime =
                        LocalDateTime.of( arrivingDate.getValue() , arrivingTime.getValue() )
                                     .atZone( routesBox.getSelectionModel().getSelectedItem().getTo() );
        if( routesBox.getValue() == null ){
            ClientMain.showWarning( "Route isn`t chosen" , "Flight must have route" , "Choose route" );
        }else if( planeID.getText().equals( "" ) ){
            ClientMain.showWarning( "You have no plain" , "Flight must have plain" , "Write plain data" );
        }else{
            try{
                DataOutputStream
                        outClient =
                        new DataOutputStream( Controller.getInstance().getClientSocket().getOutputStream() );
                ObjectMapper                 mapper   = new ObjectMapper();
                ArrayList<ListChangeAdapter> changes1 = new ArrayList<>();

                if( isAdd ){
                    changes1.add( ListChangeAdapter.addFlight( Collections.singletonList( new Flight( number.getText() ,
                                                                                                      routesBox.getSelectionModel()
                                                                                                               .getSelectedItem() ,
                                                                                                      planeID.getText() ,
                                                                                                      departureDateTime ,
                                                                                                      arriveDateTime ) ) ) );
                    RoutesFlightsOverviewController.getChanges()
                                                   .add( ListChangeAdapter.addFlight( Collections.singletonList( new Flight(
                                                           number.getText() ,
                                                           routesBox.getSelectionModel().getSelectedItem() ,
                                                           planeID.getText() ,
                                                           departureDateTime ,
                                                           arriveDateTime ) ) ) );
                }else{
                    changes1.add( ListChangeAdapter.editFlight( Collections.singletonList( editingFlight ) ,
                                                                Collections.singletonList( new Flight( editingFlight.getNumber() ,
                                                                                                       routesBox.getSelectionModel()
                                                                                                                .getSelectedItem() ,
                                                                                                       planeID.getText() ,
                                                                                                       departureDateTime ,
                                                                                                       arriveDateTime ) ) ) );
                    RoutesFlightsOverviewController.getChanges()
                                                   .add( ListChangeAdapter.editFlight( Collections.singletonList(
                                                           editingFlight ) ,
                                                                                       Collections.singletonList( new Flight(
                                                                                               editingFlight.getNumber() ,
                                                                                               routesBox.getSelectionModel()
                                                                                                        .getSelectedItem() ,
                                                                                               planeID.getText() ,
                                                                                               departureDateTime ,
                                                                                               arriveDateTime ) ) ) );
                }

                Controller.getInstance().getUserInformation().setChanges( changes1 );
                outClient.writeUTF( mapper.writeValueAsString( Controller.getInstance().getUserInformation() ) );
                Controller.getInstance().getUserInformation().setChanges( null );
                closeWindow();
            }catch( FlightAndRouteException e ){
                RoutesFlightsOverviewController.showModelAlert( e );
            }catch( IOException e ){
                System.out.println( "Connection problem" );
                System.out.println( e.getMessage() );
                Controller.changed = true;
                closeWindow();
            }
        }
    }

    private void setEditingFlightData(){
        number.textProperty().setValue( editingFlight.getNumber() );
        planeID.textProperty().setValue( editingFlight.getPlaneID() );
        routesBox.getSelectionModel().select( editingFlight.getRoute() );
        departureDate.setValue( editingFlight.getDepartureDateTime().toLocalDate() );
        arrivingDate.setValue( editingFlight.getArriveDateTime().toLocalDate() );
        departureTime.setValue( editingFlight.getDepartureDateTime().toLocalTime() );
        arrivingTime.setValue( editingFlight.getArriveDateTime().toLocalTime() );
    }

    private void setErrorLabel( String newValue , Label errorLabel , TextField handlingTextField ){
        Matcher matcher = Pattern.compile( "[^0-9\\-_\\w]+" ).matcher( newValue );
        if( matcher.find() ){
            handlingTextField.setStyle( "-fx-text-inner-color: red;" );
            handlingTextField.setTooltip( new Tooltip( "Acceptable symbols: 0-9, a-z, -, _" ) );
            errorLabel.setVisible( true );
            errorLabel.setLayoutX( handlingTextField.getLayoutX() +
                                   8.5 +
                                   matcher.start() * ( errorLabel.getFont().getSize() / 1.43 ) );
            syntaxErrors.setValue( true );
        }else{
            errorLabel.setVisible( false );
            handlingTextField.setStyle( "-fx-text-inner-color: black;" );
            handlingTextField.setTooltip( null );
            syntaxErrors.setValue( false );
        }
    }

    /**
     Clear Button. Clear all fields in GUI
     */
    private void clearData(){
        if( editingFlight != null ){
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

    void closeWindow(){
        thisStage.close();
    }

}
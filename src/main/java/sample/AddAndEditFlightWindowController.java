package sample;

import exceptions.FlightAndRouteException;
import javafx.collections.FXCollections;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.DataModel;
import model.Flight;
import model.Route;
import np.com.ngopal.control.AutoFillTextBox;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AddAndEditFlightWindowController implements Initializable{
    public ListView<Route>         routesListView;
    public TextField               searchFrom;
    public TextField               searchTo;
    public TextField               numberField;
    public Label                   planeLabel;
    public AutoFillTextBox<String> planeIdField;
    public DatePicker              departureDate;
    public TextField               departureTime;
    public DatePicker              arrivalDate;
    public TextField               arrivalTime;
    public Button                  add;
    public Button                  cancel;
    public Button                  clear;

    private MainWindowController mainWindowController;
    private Stage                thisWindow;
    private Flight               editingFLight;

    private DataModel dataModel = DataModel.getInstance();

    AddAndEditFlightWindowController( MainWindowController mainWindowController , Stage thisWindow ,
                                      Flight editingFLight ){
        this.mainWindowController = mainWindowController;
        this.thisWindow = thisWindow;
        this.editingFLight = editingFLight;
    }

    @Override
    public void initialize( URL location , ResourceBundle resources ){
        routesListView.getSelectionModel().setSelectionMode( SelectionMode.SINGLE );

        planeIdField.setLayoutX( numberField.getLayoutX() );
        planeIdField.setLayoutY( planeLabel.getLayoutY() );
        planeIdField.setData( dataModel.listAllPlanesWithPredicate( plane -> true ).collect(
                Collectors.collectingAndThen( Collectors.toList() , FXCollections::observableArrayList ) ) );

        updateRoutesList();
        if( editingFLight != null ){
            routesListView.getSelectionModel().select( editingFLight.getRoute() );
            routesListView.getFocusModel().focus( routesListView.getSelectionModel().getSelectedIndex() );
            numberField.setText( editingFLight.getNumber() );
            numberField.setDisable( true );
            planeIdField.getTextbox().setText( editingFLight.getPlaneID() );
            departureDate.getEditor()
                         .setText( new SimpleDateFormat( "dd.MM.yyyy" ).format( editingFLight.getDepartureDate() ) );
            departureTime.setText( new SimpleDateFormat( "HH:mm" ).format( editingFLight.getDepartureDate() ) );
            arrivalDate.getEditor()
                       .setText( new SimpleDateFormat( "dd.MM.yyyy" ).format( editingFLight.getArriveDate() ) );
            arrivalTime.setText( new SimpleDateFormat( "HH:mm" ).format( editingFLight.getArriveDate() ) );
            add.setText( "Edit" );
        }

        searchFrom.textProperty().addListener( ( observable , oldValue , newValue ) -> updateRoutesList() );
        searchTo.textProperty().addListener( ( observable , oldValue , newValue ) -> updateRoutesList() );

        if( editingFLight == null ){
            add.setOnAction( event -> {
                try{
                    Date departureDate = Date.from( LocalDateTime.of( LocalDate.parse( this.departureDate.getEditor()
                                                                                                         .getText() ,
                                                                                       DateTimeFormatter.ofPattern(
                                                                                               "dd.MM.yyyy" ) ) ,
                                                                      LocalTime.parse( this.departureTime.getText() ,
                                                                                       DateTimeFormatter
                                                                                               .ofPattern( "HH:mm" ) ) )
                                                                 .toInstant( ZoneId.of( "Europe/Samara" ).getRules()
                                                                                   .getOffset( Instant.now() ) ) );
                    Date arriveDate = Date.from( LocalDateTime.of( LocalDate.parse( this.arrivalDate.getEditor()
                                                                                                    .getText() ,
                                                                                    DateTimeFormatter.ofPattern(
                                                                                            "dd.MM.yyyy" ) ) , LocalTime
                                                                           .parse( this.arrivalTime.getText() ,
                                                                                   DateTimeFormatter
                                                                                           .ofPattern( "HH:mm" ) ) )
                                                              .toInstant( ZoneId.of( "Europe/Samara" ).getRules()
                                                                                .getOffset( Instant.now() ) ) );
                    dataModel.addFlight(
                            new Flight( numberField.getText() , routesListView.getSelectionModel().getSelectedItem() ,
                                        planeIdField.getText() , departureDate , arriveDate ) );
                }catch( FlightAndRouteException e ){
                    if( new Alert( Alert.AlertType.ERROR , e.getMessage() , ButtonType.FINISH , ButtonType.CLOSE )
                            .showAndWait().get().equals( ButtonType.CLOSE ) ){
                        return;
                    }
                }catch( DateTimeParseException e ){
                    if( new Alert( Alert.AlertType.ERROR , "Time format exception" , ButtonType.FINISH ,
                                   ButtonType.CLOSE ).showAndWait().get().equals( ButtonType.CLOSE ) ){
                        return;
                    }
                }
                thisWindow.close();
                mainWindowController.updateFlightsList();
            } );
        }else{
            add.setOnAction( event -> {
                try{
                    Date departureDate;
                    if( this.departureDate.getEditor().getText().isEmpty() || this.departureTime.getText().isEmpty() ){
                        departureDate = null;
                    }else{
                        departureDate = Date.from( LocalDateTime.of( LocalDate.parse( this.departureDate.getEditor()
                                                                                                        .getText() ,
                                                                                      DateTimeFormatter.ofPattern(
                                                                                              "dd.MM.yyyy" ) ) ,
                                                                     LocalTime.parse( this.departureTime.getText() ,
                                                                                      DateTimeFormatter
                                                                                              .ofPattern( "HH:mm" ) ) )
                                                                .toInstant( ZoneId.of( "Europe/Samara" ).getRules()
                                                                                  .getOffset( Instant.now() ) ) );
                    }
                    Date arrivalDate;
                    if( this.arrivalDate.getEditor().getText().isEmpty() || this.arrivalTime.getText().isEmpty() ){
                        arrivalDate = null;
                    }else{
                        arrivalDate = Date.from( LocalDateTime.of( LocalDate.parse( this.arrivalDate.getEditor()
                                                                                                    .getText() ,
                                                                                    DateTimeFormatter.ofPattern(
                                                                                            "dd.MM.yyyy" ) ) , LocalTime
                                                                           .parse( this.arrivalTime.getText() ,
                                                                                   DateTimeFormatter
                                                                                           .ofPattern( "HH:mm" ) ) )
                                                              .toInstant( ZoneId.of( "Europe/Samara" ).getRules()
                                                                                .getOffset( Instant.now() ) ) );
                    }
                    dataModel.editFlight( editingFLight , routesListView.getSelectionModel().getSelectedItem() ,
                                          this.planeIdField.getText().isEmpty() ? null : this.planeIdField.getText() ,
                                          departureDate , arrivalDate );
                }catch( FlightAndRouteException e ){
                    if( new Alert( Alert.AlertType.ERROR , e.getMessage() , ButtonType.FINISH , ButtonType.CLOSE )
                            .showAndWait().get().equals( ButtonType.CLOSE ) ){
                        return;
                    }
                }
                thisWindow.close();
                mainWindowController.updateFlightsList();
            } );
        }
        clear.setOnAction( event -> {
            searchFrom.setText( "" );
            searchTo.setText( "" );
            numberField.setText( "" );
            planeIdField.getTextbox().setText( "" );
            departureDate.getEditor().setText( "" );
            departureTime.setText( "" );
            arrivalDate.getEditor().setText( "" );
            arrivalTime.setText( "" );
            if( editingFLight != null ) routesListView.getSelectionModel().select( editingFLight.getRoute() );
            updateRoutesList();
        } );
        cancel.setOnAction( event -> thisWindow.close() );
    }

    private void updateRoutesList(){
        routesListView.getItems().setAll( dataModel.listRoutesWithPredicate(
                route1 -> makePredicateForString( searchFrom.getText() ).test( route1.getFrom() ) &&
                          makePredicateForString( searchTo.getText() ).test( route1.getTo() ) )
                                                   .collect( Collectors.toList() ) );
    }

    private Predicate<String> makePredicateForString( String string ){
        if( string != null && string.isEmpty() ) string = null;
        return Optional.ofNullable( string ).map( s -> Pattern.compile( ".*" + s + ".*" ).asPredicate() )
                       .orElse( s -> true );
    }
}

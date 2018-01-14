package sample;

import exceptions.FlightAndRouteException;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.DataModelInstanceSaver;
import model.Flight;
import model.Route;

import java.io.IOException;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 Controller for routes and flights view, client write application.
 disables and hides all buttons/menus, that write client must not see
 */
class RoutesFlightsWriteOverviewController extends RoutesFlightsOverviewController{



    @FXML TextField                   departure;
    @FXML TextField                   destination;
    @FXML TableView<Route>            routeTable;
    @FXML TableColumn<Route, String>  departureColumn;
    @FXML TableColumn<Route, String>  destinationColumn;
    @FXML TableView<Flight>           flightTable;
    @FXML TableColumn<Flight, String> number;
    @FXML TableColumn<Flight, Route>  routeColumnFlight;
    @FXML TextArea                    detailsTextArea;
    @FXML Button                      updateFlightButton;
    @FXML Button                      searchFlightButton;

    RoutesFlightsWriteOverviewController( Stage thisStage ){
        super( thisStage );
    }

    /**
     initialization of view
     Hiding menus, add listners, start thread
     */
    @Override
    @FXML
    void initialize(){
        updateFlightButton.setLayoutX( updateFlightButton.getLayoutX() - 37 );
        searchFlightButton.setLayoutX( searchFlightButton.getLayoutX() - 37 );
        departureColumn.setCellValueFactory( new PropertyValueFactory<>( "from" ) );
        destinationColumn.setCellValueFactory( new PropertyValueFactory<>( "to" ) );
        routeTable.setItems( DataModelInstanceSaver.getInstance().getRouteObservableList() );
        number.setCellValueFactory( new PropertyValueFactory<>( "number" ) );
        routeColumnFlight.setCellValueFactory( new PropertyValueFactory<>( "route" ) );
        flightTable.setItems( DataModelInstanceSaver.getInstance().getFlightObservableList() );
        flightTable.getSelectionModel().selectedItemProperty()
                   .addListener( ( observable , oldValue , newValue ) -> showFlightDetails( newValue ) );

        departure.textProperty()
                 .addListener( ( observable , oldValue , newValue ) -> searchListeners( newValue , departure ) );
        destination.textProperty()
                   .addListener( ( observable , oldValue , newValue ) -> searchListeners( newValue , destination ) );

        thisStage.setOnCloseRequest( event -> Controller.getInstance().stopThread() );

        Controller.getInstance().setThread( new WriteThread() );
        Controller.getInstance().startThread();
    }

    private void searchListeners( String newValue , TextField textField ){
        if( !newValue.matches( "[\\w\\d\\-_?*]*" ) ){
            textField.setStyle( "-fx-text-inner-color: red;" );
            textField.setTooltip( new Tooltip( "Acceptable symbols: 0-9, a-z, -, _, ?, *" ) );
        }else{
            textField.setStyle( "-fx-text-inner-color: black;" );
            textField.setTooltip( null );
            Pattern departurePattern = Pattern.compile(
                    ".*" + departure.getText().toUpperCase().replaceAll( "\\*" , ".*" ).replaceAll( "\\?" , "." ) +
                    ".*" , Pattern.CASE_INSENSITIVE );
            Pattern destinationPattern = Pattern.compile(
                    ".*" + destination.getText().toUpperCase().replaceAll( "\\*" , ".*" ).replaceAll( "\\?" , "." ) +
                    ".*" , Pattern.CASE_INSENSITIVE );
            routeTable.setItems( DataModelInstanceSaver.getInstance().getRouteObservableList().filtered(
                    route -> departurePattern.matcher( route.getFrom().getId() ).matches() &&
                             destinationPattern.matcher( route.getTo().getId() ).matches() ) );
        }
    }

    /**
     @param flight show detail information about chosen flight
     */
    private void showFlightDetails( Flight flight ){
        if( flight != null ){
            detailsTextArea.setWrapText( true );
            detailsTextArea.setText( flight.toString() );
        }else{
            detailsTextArea.setText( "" );
        }
    }

    /**
     * deleting route from DB
     */
    @Override
    @FXML
    void handleDeleteRouteButton(){
        Optional.ofNullable( routeTable.getSelectionModel().getSelectedItem() ).ifPresent( selectedRoute -> {
            try{
                DataModelInstanceSaver.getInstance().removeRoute( selectedRoute );
                // TODO: put here request to server to delete route
            }catch( FlightAndRouteException e ){
                showModelAlert( e );
            }
        } );
    }

    /**
     * deleting flight from DB
     */
    @Override
    @FXML
    void handleDeleteFlightButton(){
        Optional.ofNullable( flightTable.getSelectionModel().getSelectedItem() ).ifPresent( selectedFlight -> {
            try{
                DataModelInstanceSaver.getInstance().removeFlight( selectedFlight.getNumber() );
                ClientMain.changed = true;
                // TODO: put here request to server to delete flight
            }catch( FlightAndRouteException e ){
                showModelAlert( e );
            }
        } );
    }

    /**
     * Update flight list
     */
    @Override
    @FXML
    public void handleUpdateFlightButton(){
        // TODO: put here request to server to update DB about routes
    }

    /**
     * Update route list
     */
    @Override
    @FXML
    public void handleUpdateRouteButton(){
        // TODO: put here request to server to update DB about flights
    }

    /**
     * Search for routes
     */
    @Override
    @FXML
    public void handleSearchRouteButton(){
        // TODO: put here request to server to update DB about flights

    }


}

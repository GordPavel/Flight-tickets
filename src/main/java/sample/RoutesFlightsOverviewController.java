package sample;

import exceptions.FlightAndRouteException;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Flight;
import model.Route;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;


/**
 Controller for routes and flights view
 Shows the information about all routes and flights
 */
public class RoutesFlightsOverviewController{


    private Controller controller = Controller.getInstance();

    private static final String EDIT_ROUTE_WINDOW    = "Edit a route";
    private static final String ADD_ROUTE_WINDOW     = "Add a route";
    private static final String EDIT_FLIGHT_WINDOW   = "Edit a flight";
    private static final String ADD_FLIGHT_WINDOW    = "Add a flight";
    private static final String SEARCH_FLIGHT_WINDOW = "Search a flight";

    @FXML TextField                   departure;
    @FXML TextField                   destination;
    @FXML TableView<Route>            routeTable;
    @FXML TableColumn<Route, String>  departureColumn;
    @FXML TableColumn<Route, String>  destinationColumn;
    @FXML TableView<Flight>           flightTable;
    @FXML TableColumn<Flight, String> number;
    @FXML TableColumn<Flight, Route>  routeColumnFlight;
    @FXML TextArea                    detailsTextArea;

    public RoutesFlightsOverviewController(){
    }

    /**
     initialization of view
     */
    public void initialize(){
        controller.updateFlights();
        controller.updateRoutes();
        departureColumn.setCellValueFactory( new PropertyValueFactory<>( "from" ) );
        destinationColumn.setCellValueFactory( new PropertyValueFactory<>( "to" ) );
        routeTable.setItems( controller.getRoutes() );
        number.setCellValueFactory( new PropertyValueFactory<>( "number" ) );
        routeColumnFlight.setCellValueFactory( new PropertyValueFactory<>( "route" ) );
        flightTable.setItems( controller.getFlights() );
        flightTable.getSelectionModel().selectedItemProperty()
                   .addListener( ( observable , oldValue , newValue ) -> showFlightDetails( newValue ) );
        departure.textProperty().addListener( ( observable , oldValue , newValue ) -> {
            searchListeners( newValue , departure );
        } );
        destination.textProperty().addListener( ( observable , oldValue , newValue ) -> {
            searchListeners( newValue , destination );
        } );
    }

    private void searchListeners( String newValue , TextField textField ){
        if( !newValue.matches( "[\\w\\d[^\\s .,*?!+=-]]*" ) ){
            textField.setStyle( "-fx-text-inner-color: red;" );
        }else{
            textField.setStyle( "-fx-text-inner-color: black;" );
        }
        Pattern departurePattern = Pattern.compile(
                ".*" + departure.getText().toUpperCase().replace( "*" , ".*" ).replace( "?" , "." ) + ".*" );
        Pattern destinationPattern = Pattern.compile(
                ".*" + destination.getText().toUpperCase().replace( "*" , ".*" ).replace( "?" , "." ) + ".*" );

        routeTable.setItems( controller.getRoutes().stream().filter(
                route -> departurePattern.matcher( route.getFrom().toUpperCase() ).matches() &&
                         destinationPattern.matcher( route.getTo().toUpperCase() ).matches() ).collect(
                Collectors.collectingAndThen( toList() , FXCollections::observableArrayList ) ) );
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
     */
    @FXML
    public void handleDeleteRouteButton() throws ClassNotFoundException{
        Route selectedRoute = routeTable.getSelectionModel().getSelectedItem();
        if( selectedRoute != null ){
            try{
                routeTable.getItems().remove( selectedRoute );
                Controller.model.removeRoute( selectedRoute );
                controller.updateRoutes();
                routeTable.refresh();
                controller.updateFlights();
                flightTable.setItems( controller.getFlights() );
                flightTable.refresh();
            }catch( FlightAndRouteException e ){
                Alert alert = new Alert( Alert.AlertType.WARNING );
                alert.setTitle( "Model exception" );
                alert.setHeaderText( "Model throw an exception" );
                alert.setContentText( e.getMessage() );
                alert.showAndWait();
            }
        }else{
            Alert alert = new Alert( Alert.AlertType.WARNING );
            alert.setTitle( "No Selection" );
            alert.setHeaderText( "No Route Selected" );
            alert.setContentText( "Please select a route in the table." );
            alert.showAndWait();
        }
    }

    /**
     */
    @FXML
    public void handleDeleteFlightButton() throws ClassNotFoundException{
        Flight selectedFlight = flightTable.getSelectionModel().getSelectedItem();
        if( selectedFlight != null ){
            try{
                flightTable.getItems().remove( selectedFlight );
                Controller.model.removeFlight( selectedFlight.getNumber() );
                controller.updateFlights();
                flightTable.refresh();
            }catch( FlightAndRouteException e ){
                Alert alert = new Alert( Alert.AlertType.WARNING );
                alert.setTitle( "Model exception" );
                alert.setHeaderText( "Model throw an exception" );
                alert.setContentText( e.getMessage() );

                alert.showAndWait();
            }
        }else{
            Alert alert = new Alert( Alert.AlertType.WARNING );
            alert.setTitle( "No Selection" );
            alert.setHeaderText( "No Flight Selected" );
            alert.setContentText( "Please select a flight in the table." );
            alert.showAndWait();
        }
    }

    /**
     @param actionEvent Add Button (for routes). Create a new window for adding a new route
     */
    @FXML
    public void handleAddRouteButton( ActionEvent actionEvent ){
        Parent addRouteWindow;
        Stage  oldStage = ( Stage ) ( ( Parent ) actionEvent.getSource() ).getScene().getWindow();
        try{
            addRouteWindow = FXMLLoader.load( getClass().getResource( "/fxml/AddRoutesOverview.fxml" ) );
            Scene scene = new Scene( addRouteWindow );
            Stage popUp = new Stage();
            popUp.initModality( Modality.APPLICATION_MODAL );
            popUp.initOwner( oldStage );
            popUp.setTitle( ADD_ROUTE_WINDOW );
            popUp.setScene( scene );
            popUp.setResizable( false );
            oldStage.setOpacity( 0.9 );
            popUp.showAndWait();
            oldStage.setOpacity( 1 );
        }catch( IOException e ){
            e.printStackTrace();
        }
        routeTable.setItems( controller.getRoutes() );
        routeTable.refresh();
        flightTable.setItems( controller.getFlights() );
        flightTable.refresh();
    }

    /**
     @param actionEvent Edit Button (for routes). Create a new window for editing the information about chosen route
     */
    @FXML
    public void handleEditRouteButton( ActionEvent actionEvent ){
        Parent editRouteWindow;
        Stage  oldStage      = ( Stage ) ( ( Parent ) actionEvent.getSource() ).getScene().getWindow();
        Route  selectedRoute = routeTable.getSelectionModel().getSelectedItem();
        if( selectedRoute == null ){
            Alert alert = new Alert( Alert.AlertType.WARNING );
            alert.setTitle( "No Selection" );
            alert.setHeaderText( "No Route Selected" );
            alert.setContentText( "Please select a route to edit in the table." );
            alert.showAndWait();
        }else{
            controller.setRouteForEdit( selectedRoute );
            try{
                editRouteWindow = FXMLLoader.load( getClass().getResource( "/fxml/EditRoutesOverview.fxml" ) );
                Scene scene = new Scene( editRouteWindow );
                Stage popUp = new Stage();
                popUp.initModality( Modality.APPLICATION_MODAL );
                popUp.initOwner( oldStage );
                popUp.setTitle( EDIT_ROUTE_WINDOW );
                popUp.setScene( scene );
                popUp.setResizable( false );
                ( ( TextField ) ( editRouteWindow.getChildrenUnmodifiable().get( 0 ) ) )
                        .setText( selectedRoute.getFrom() );
                ( ( TextField ) ( editRouteWindow.getChildrenUnmodifiable().get( 4 ) ) )
                        .setText( selectedRoute.getTo() );
                oldStage.setOpacity( 0.9 );
                popUp.showAndWait();
                oldStage.setOpacity( 1 );
                routeTable.setItems( controller.getRoutes() );
                routeTable.refresh();
                flightTable.setItems( controller.getFlights() );
                flightTable.refresh();
            }catch( IOException e ){
                e.printStackTrace();
            }
        }
    }

    /**
     @param actionEvent Add Button (for flights). Create a new window for adding a new flight
     */
    @FXML
    public void handleAddFlightButton( ActionEvent actionEvent ){
        Parent addFlightWindow;
        Stage  oldStage = ( Stage ) ( ( Parent ) actionEvent.getSource() ).getScene().getWindow();
        try{
            addFlightWindow = FXMLLoader.load( getClass().getResource( "/fxml/AddFlightsOverview.fxml" ) );
            Scene scene = new Scene( addFlightWindow );
            Stage popUp = new Stage();
            popUp.initModality( Modality.APPLICATION_MODAL );
            popUp.initOwner( oldStage );
            popUp.setTitle( ADD_FLIGHT_WINDOW );
            popUp.setScene( scene );
            popUp.setResizable( false );
            oldStage.setOpacity( 0.9 );
            popUp.showAndWait();
            oldStage.setOpacity( 1 );
            flightTable.setItems( controller.getFlights() );
            flightTable.refresh();
        }catch( IOException e ){
            e.printStackTrace();
        }
    }

    /**
     @param actionEvent Edit Button (for flight). Create a new window for editing the information about chosen flight.
     */
    @FXML
    public void handleEditFlightButton( ActionEvent actionEvent ){
        Parent editFlightWindow;
        Stage  oldStage = ( Stage ) ( ( Parent ) actionEvent.getSource() ).getScene().getWindow();

        Flight selectedFlight = flightTable.getSelectionModel().getSelectedItem();

        if( selectedFlight == null ){

            Alert alert = new Alert( Alert.AlertType.WARNING );
            alert.setTitle( "No Selection" );
            alert.setHeaderText( "No Flight Selected" );
            alert.setContentText( "Please select a flight to edit in the table." );

            alert.showAndWait();
        }else{
            controller.setFlightForEdit( selectedFlight );

            try{
                editFlightWindow = FXMLLoader.load( getClass().getResource( "/fxml/EditFlightsOverview.fxml" ) );

                Scene scene = new Scene( editFlightWindow );
                Stage popUp = new Stage();

                popUp.initModality( Modality.APPLICATION_MODAL );
                popUp.initOwner( oldStage );

                popUp.setTitle( EDIT_FLIGHT_WINDOW );
                popUp.setScene( scene );
                popUp.setResizable( false );

                ( ( TextField ) ( editFlightWindow.getChildrenUnmodifiable().get( 2 ) ) )
                        .setText( selectedFlight.getNumber() );
                ( ( TextField ) ( editFlightWindow.getChildrenUnmodifiable().get( 3 ) ) )
                        .setText( selectedFlight.getPlaneID() );
                ( ( DatePicker ) ( editFlightWindow.getChildrenUnmodifiable().get( 9 ) ) ).setValue(
                        selectedFlight.getDepartureDate().toInstant().atZone( ZoneId.systemDefault() ).toLocalDate() );
                ( ( DatePicker ) ( editFlightWindow.getChildrenUnmodifiable().get( 10 ) ) ).setValue(
                        selectedFlight.getArriveDate().toInstant().atZone( ZoneId.systemDefault() ).toLocalDate() );
                ( ( ChoiceBox<Route> ) ( editFlightWindow.getChildrenUnmodifiable().get( 11 ) ) )
                        .setValue( selectedFlight.getRoute() );
                ( ( TextField ) ( editFlightWindow.getChildrenUnmodifiable().get( 14 ) ) ).setText(
                        selectedFlight.getDepartureDate().toInstant().atZone( ZoneId.systemDefault() ).toLocalTime()
                                      .toString() );
                ( ( TextField ) ( editFlightWindow.getChildrenUnmodifiable().get( 15 ) ) )
                        .setText( selectedFlight.getArriveDate().
                                toInstant().atZone( ZoneId.systemDefault() ).toLocalTime().toString() );
                oldStage.setOpacity( 0.9 );
                popUp.showAndWait();
                oldStage.setOpacity( 1 );

                flightTable.setItems( controller.getFlights() );
                flightTable.refresh();
            }catch( IOException e ){
                e.printStackTrace();
            }
        }
    }

    /**
     @param actionEvent Edit Button (for flight). Create a new window for searching the necessary flight.
     */
    @FXML
    public void handleSearchFlightButton( ActionEvent actionEvent ){
        Parent editFlightWindow;
        Stage  oldStage = ( Stage ) ( ( Parent ) actionEvent.getSource() ).getScene().getWindow();
        try{
            editFlightWindow = FXMLLoader.load( getClass().getResource( "/fxml/SearchFlightsOverview.fxml" ) );
            Scene scene = new Scene( editFlightWindow );
            Stage popUp = new Stage();
            popUp.initModality( Modality.NONE );
            popUp.initOwner( oldStage );
            popUp.setTitle( SEARCH_FLIGHT_WINDOW );
            popUp.setScene( scene );
            popUp.setResizable( false );
            oldStage.setOpacity( 0.9 );
            popUp.showAndWait();
            oldStage.setOpacity( 1 );
        }catch( IOException e ){
            e.printStackTrace();
        }
    }

    @FXML
    private void handleImportAction(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add( new FileChooser.ExtensionFilter( "Flights and routes" , "*.far" ) );
        File file = fileChooser.showOpenDialog( new Stage() );
        if( file != null ){
            try{
                Controller.model.importFromFile( file );
                controller.updateRoutes();
                controller.updateFlights();
            }catch( IOException | FlightAndRouteException e ){
                e.printStackTrace();
            }
            routeTable.setItems( controller.getRoutes() );
            routeTable.refresh();
            flightTable.setItems( controller.getFlights() );
            flightTable.refresh();
        }
    }

    @FXML
    private void handleExportAction(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add( new FileChooser.ExtensionFilter( "Flights and routes" , "*.far" ) );
        File file = fileChooser.showSaveDialog( new Stage() );
        if( file != null ){
            try{
                Controller.model.exportToFile( file );
            }catch( IOException | FlightAndRouteException e ){
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleMergeAction(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add( new FileChooser.ExtensionFilter( "Flights and routes" , "*.far" ) );
        File                    file = fileChooser.showOpenDialog( new Stage() );
        ArrayList<Serializable> failedInMerge;
        if( file != null ){
            try{
                failedInMerge = new ArrayList<>( Controller.model.mergeData( file ) );


                Alert alert = new Alert( Alert.AlertType.WARNING );
                alert.setTitle( "Merge results" );
                alert.setHeaderText( "Model have this problems with merge:" );
                StringBuilder errors = new StringBuilder();
                for( Serializable element : failedInMerge ){
                    errors.append( "-" ).append( element.toString() ).append( "\n" );
                }
                alert.setContentText( errors.toString() );

                alert.showAndWait();


                Parent mergeWindow;

                try{
                    mergeWindow = FXMLLoader.load( getClass().getResource( "/fxml/mergeOverview.fxml" ) );

                    Scene scene = new Scene( mergeWindow );
                    Stage popUp = new Stage();

                    popUp.initModality( Modality.APPLICATION_MODAL );
                    popUp.setTitle( ADD_ROUTE_WINDOW );
                    popUp.setScene( scene );
                    popUp.setResizable( false );

                    popUp.showAndWait();


                }catch( IOException e ){
                    e.printStackTrace();
                }

                controller.updateRoutes();
                controller.updateFlights();
            }catch( IOException | FlightAndRouteException e ){
                e.printStackTrace();
            }
            routeTable.setItems( controller.getRoutes() );
            routeTable.refresh();
            flightTable.setItems( controller.getFlights() );
            flightTable.refresh();
        }

    }
}







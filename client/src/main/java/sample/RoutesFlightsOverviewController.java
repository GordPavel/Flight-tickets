package sample;

import com.jfoenix.controls.JFXButton;
import exceptions.FlightAndRouteException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.DataModelInstanceSaver;
import model.Flight;
import model.Route;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;


/**
 Controller for routes and flights view
 Shows the information about all routes and flights
 */
public class RoutesFlightsOverviewController{

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
    @FXML JFXButton                   editFlight;
    @FXML JFXButton                   deleteFlight;
    @FXML JFXButton                   editRoute;
    @FXML JFXButton                   deleteRoute;

    private Stage thisStage;
    private FileChooser fileChooser = new FileChooser();

    RoutesFlightsOverviewController( Stage thisStage ){
        this.thisStage = thisStage;
        fileChooser.getExtensionFilters().add( new FileChooser.ExtensionFilter( "Flights and routes" , "*.far" ) );
    }

    RoutesFlightsOverviewController(  ){
    }

    /**
     initialization of view
     */
    @FXML
    public void initialize(){
        Controller.getInstance().setFlights( DataModelInstanceSaver.getInstance().getFLightObservableList() );
        Controller.getInstance().setRoutes( DataModelInstanceSaver.getInstance().getRouteObservableList() );

        editFlight.disableProperty().bind( flightTable.getSelectionModel().selectedItemProperty().isNotNull() );
        deleteFlight.disableProperty().bind( flightTable.getSelectionModel().selectedItemProperty().isNotNull() );
        editRoute.disableProperty().bind( routeTable.getSelectionModel().selectedItemProperty().isNotNull() );
        deleteRoute.disableProperty().bind( routeTable.getSelectionModel().selectedItemProperty().isNotNull() );

        departureColumn.setCellValueFactory( new PropertyValueFactory<>( "from" ) );
        destinationColumn.setCellValueFactory( new PropertyValueFactory<>( "to" ) );
        routeTable.setItems( Controller.getInstance().getRoutes() );

        number.setCellValueFactory( new PropertyValueFactory<>( "number" ) );
        routeColumnFlight.setCellValueFactory( new PropertyValueFactory<>( "route" ) );
        flightTable.setItems( Controller.getInstance().getFlights() );
        flightTable.getSelectionModel().selectedItemProperty()
                   .addListener( ( observable , oldValue , newValue ) -> showFlightDetails( newValue ) );

        departure.textProperty().addListener(
                ( observable , oldValue , newValue ) -> searchListeners( departure.getText() ,
                                                                         destination.getText() ) );
        destination.textProperty().addListener(
                ( observable , oldValue , newValue ) -> searchListeners( departure.getText() ,
                                                                         destination.getText() ) );
    }

    private void searchListeners( String departure , String destination ){
        routeTable.setItems( Controller.getInstance().getRoutes().filtered( route -> Pattern.compile(
                ".*" + departure.replaceAll( "\\*" , ".*" ).replaceAll( "\\?" , "." ) + ".*" ,
                Pattern.CASE_INSENSITIVE ).matcher( route.getFrom().getId() ).matches() && Pattern.compile(
                ".*" + destination.replaceAll( "\\*" , ".*" ).replaceAll( "\\?" , "." ) + ".*" ,
                Pattern.CASE_INSENSITIVE ).matcher( route.getTo().toString() ).matches() ) );
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
    public void handleDeleteRouteButton(){
        Optional.ofNullable( routeTable.getSelectionModel().getSelectedItem() ).ifPresent( selectedRoute -> {
            try{
                routeTable.getItems().remove( selectedRoute );
                DataModelInstanceSaver.getInstance().removeRoute( selectedRoute );
                Main.changed = true;
                Controller.getInstance().updateRoutes();
                routeTable.refresh();
                Controller.getInstance().updateFlights();
                flightTable.setItems( Controller.getInstance().getFlights() );
                flightTable.refresh();
            }catch( FlightAndRouteException e ){
                Alert alert = new Alert( Alert.AlertType.WARNING );
                alert.setTitle( "Model exception" );
                alert.setHeaderText( "Model throw an exception" );
                alert.setContentText( e.getMessage() );
                alert.showAndWait();
            }
        } );
    }

    /**
     */
    @FXML
    public void handleDeleteFlightButton(){
        Optional.ofNullable( flightTable.getSelectionModel().getSelectedItem() ).ifPresent( selectedFlight -> {
            try{
                flightTable.getItems().remove( selectedFlight );
                DataModelInstanceSaver.getInstance().removeFlight( selectedFlight.getNumber() );
                Main.changed = true;
                Controller.getInstance().updateFlights();
                flightTable.refresh();
            }catch( FlightAndRouteException e ){
                Alert alert = new Alert( Alert.AlertType.WARNING );
                alert.setTitle( "Model exception" );
                alert.setHeaderText( "Model throw an exception" );
                alert.setContentText( e.getMessage() );
                alert.showAndWait();
            }
        } );
    }

    @FXML
    public void handleAddRouteButton(){
        try{
            FXMLLoader                         loader     =
                    new FXMLLoader( getClass().getResource( "/fxml/AddRoutesOverview.fxml" ) );
            Stage                              popUp      = new Stage();
            AddAndEditRoutesOverviewController controller = new AddAndEditRoutesOverviewController( null , popUp );
            loader.setController( controller );
            popUp.initModality( Modality.APPLICATION_MODAL );
            popUp.initOwner( thisStage );

            popUp.setTitle( ADD_ROUTE_WINDOW );
            popUp.setScene( new Scene( loader.load() ) );
            popUp.setResizable( false );

            thisStage.setOpacity( 0.9 );
            popUp.showAndWait();
            thisStage.setOpacity( 1 );
        }catch( IOException e ){
            e.printStackTrace();
        }
    }

    @FXML
    public void handleUpdateFlightButton(){

    }

    @FXML
    public void handleUpdateRouteButton(){

    }

    @FXML
    public void handleSearchRouteButton(){

    }

    @FXML
    public void handleEditRouteButton(){
        Optional.ofNullable( routeTable.getSelectionModel().getSelectedItem() ).ifPresent( selectedRoute -> {
            try{
                FXMLLoader loader = new FXMLLoader( getClass().getResource( "/fxml/AddRoutesOverview.fxml" ) );
                Stage      popUp  = new Stage();
                AddAndEditRoutesOverviewController controller =
                        new AddAndEditRoutesOverviewController( selectedRoute , popUp );
                loader.setController( controller );

                popUp.initModality( Modality.APPLICATION_MODAL );
                popUp.initOwner( thisStage );

                popUp.setTitle( EDIT_ROUTE_WINDOW );
                popUp.setScene( new Scene( loader.load() ) );
                popUp.setResizable( false );
                thisStage.setOpacity( 0.9 );
                popUp.showAndWait();
                thisStage.setOpacity( 1 );
                routeTable.setItems( Controller.getInstance().getRoutes() );
                routeTable.refresh();
                Controller.getInstance().updateFlights();
                flightTable.setItems( Controller.getInstance().getFlights() );
                flightTable.refresh();
            }catch( IOException e ){
                e.printStackTrace();
            }
        } );
    }

    @FXML
    public void handleAddFlightButton(){
        try{
            FXMLLoader loader = new FXMLLoader( getClass().getResource( "/fxml/AddFlightsOverview.fxml" ) );
            Stage                               popUp      = new Stage();
            AddAndEditFlightsOverviewController controller = new AddAndEditFlightsOverviewController( null , popUp );
            loader.setController( controller );

            popUp.initModality( Modality.APPLICATION_MODAL );
            popUp.initOwner( thisStage );

            popUp.setTitle( ADD_FLIGHT_WINDOW );
            popUp.setScene( new Scene( loader.load() ) );
            popUp.setResizable( false );

            thisStage.setOpacity( 0.9 );
            popUp.showAndWait();
            thisStage.setOpacity( 1 );
            flightTable.setItems( Controller.getInstance().getFlights() );
            flightTable.refresh();
        }catch( IOException e ){
            e.printStackTrace();
        }
    }

    @FXML
    public void handleEditFlightButton(){
        Optional.ofNullable( flightTable.getSelectionModel().getSelectedItem() ).ifPresent( selectedFlight -> {
            try{
                FXMLLoader loader = new FXMLLoader( getClass().getResource( "/fxml/AddFlightsOverview.fxml" ) );
                Stage      popUp  = new Stage();
                AddAndEditFlightsOverviewController controller =
                        new AddAndEditFlightsOverviewController( selectedFlight , popUp );
                loader.setController( controller );
                popUp.initModality( Modality.APPLICATION_MODAL );
                popUp.initOwner( thisStage );

                popUp.setTitle( EDIT_FLIGHT_WINDOW );
                popUp.setScene( new Scene( loader.load() ) );
                popUp.setResizable( false );
                thisStage.setOpacity( 0.9 );
                popUp.showAndWait();
                thisStage.setOpacity( 1 );

                flightTable.setItems( Controller.getInstance().getFlights() );
                flightTable.refresh();
            }catch( IOException e ){
                e.printStackTrace();
            }
        } );
    }

    @FXML
    public void handleSearchFlightButton(){
        if( !Controller.getInstance().isFlightSearchActive() ){
            Controller.getInstance().setFlightSearchActive( true );
            try{
                Stage popUp = new Stage();
                FXMLLoader loader = new FXMLLoader( getClass().getResource( "/fxml/SearchFlightsOverview.fxml" ) );
                SearchFlightsOverviewController searchFlights = new SearchFlightsOverviewController( this , popUp );
                loader.setController( searchFlights );
                Scene scene = new Scene( loader.load() );

                popUp.initModality( Modality.NONE );
                popUp.initOwner( thisStage.getOwner() );
                popUp.setX( thisStage.getX() + thisStage.getWidth() );
                popUp.setY( thisStage.getY() );

                popUp.setTitle( SEARCH_FLIGHT_WINDOW );
                popUp.setScene( scene );
                popUp.setResizable( false );

                thisStage.setOpacity( 0.9 );
                popUp.show();
                thisStage.setOpacity( 1 );
            }catch( IOException e ){
                e.printStackTrace();
            }
            flightTable.setItems( Controller.getInstance().getFlights() );
            flightTable.refresh();
        }
    }

    @FXML
    private void handleOpenAction(){
        Optional.ofNullable( fileChooser.showOpenDialog( new Stage() ) ).ifPresent( file -> {
            try{
                DataModelInstanceSaver.getInstance().importFromFile( file );
                Controller.getInstance().updateRoutes();
                Controller.getInstance().updateFlights();
                Main.changed = false;
                Main.savingFile = file;
                thisStage.setTitle( file.getName() );
            }catch( IOException | FlightAndRouteException e ){
                e.printStackTrace();
            }
            routeTable.setItems( Controller.getInstance().getRoutes() );
            routeTable.refresh();
            flightTable.setItems( Controller.getInstance().getFlights() );
            flightTable.refresh();
        } );
    }

    public void handleSaveAction(){
        if( Main.savingFile == null ){
            Optional.ofNullable( fileChooser.showSaveDialog( new Stage() ) ).ifPresent( file -> {
                Main.savingFile = file;
                thisStage.setTitle( file.getName() );
            } );
        }
        try{
            DataModelInstanceSaver.getInstance().saveToFile( Main.savingFile );
            Main.changed = false;
        }catch( IOException | FlightAndRouteException e ){
            new Alert( Alert.AlertType.ERROR , e.getMessage() ).show();
        }
    }

    @FXML
    private void handleSaveAsAction(){
        Optional.ofNullable( fileChooser.showSaveDialog( new Stage() ) ).ifPresent( file -> {
            try{
                DataModelInstanceSaver.getInstance().saveToFile( file );
            }catch( IOException | FlightAndRouteException e ){
                new Alert( Alert.AlertType.ERROR , e.getMessage() ).show();
            }
        } );
    }

    @FXML
    private void handleMergeAction(){
        Optional.ofNullable( fileChooser.showOpenDialog( new Stage() ) ).ifPresent( file -> {
            try{
                ArrayList<Serializable> failedInMerge =
                        DataModelInstanceSaver.getInstance().mergeData( file ).collect( ArrayList::new , List::add , List::addAll );
                Main.changed = true;
                Alert alert = new Alert( Alert.AlertType.WARNING );
                alert.setTitle( "Merge results" );
                alert.setHeaderText( "Model have this problems with merge:" );
                StringBuilder errors = new StringBuilder();

                ArrayList<Flight> mergeFlights = new ArrayList<>();
                ArrayList<Route>  mergeRoutes  = new ArrayList<>();
                for( Serializable element : failedInMerge ){
                    errors.append( "-" ).append( element.toString() ).append( "\n" );
                    if( element instanceof Flight && DataModelInstanceSaver.getInstance()
                                                                           .listFlightsWithPredicate( flight -> true )
                                                                           .noneMatch( flight -> flight.equals( element ) ) ){
                        mergeFlights.add( ( Flight ) element );
                    }
                    if( element instanceof Route ){
                        mergeRoutes.add( ( Route ) element );
                    }
                }

                Controller.getInstance().setMergeFlights( FXCollections.observableArrayList( mergeFlights ) );
                Controller.getInstance().setMergeRoutes( FXCollections.observableArrayList( mergeRoutes ) );

                if( !failedInMerge.isEmpty() ){
                    alert.setContentText( errors.toString() );
                    alert.showAndWait();
                }

                if( !mergeFlights.isEmpty() ){
                    Scene scene = new Scene( FXMLLoader.load( getClass().getResource( "/fxml/mergeOverview.fxml" ) ) );
                    Stage popUp = new Stage();
                    popUp.initModality( Modality.APPLICATION_MODAL );
                    popUp.setTitle( ADD_ROUTE_WINDOW );
                    popUp.setScene( scene );
                    popUp.setResizable( false );

                    popUp.showAndWait();
                }

                Controller.getInstance().updateRoutes();
                Controller.getInstance().updateFlights();
            }catch( IOException | FlightAndRouteException e ){
                new Alert( Alert.AlertType.ERROR , e.getMessage() ).show();
            }
            routeTable.setItems( Controller.getInstance().getRoutes() );
            routeTable.refresh();
            flightTable.setItems( Controller.getInstance().getFlights() );
            flightTable.refresh();
        } );
    }

    @FXML
    private void handleAboutAction(){
        Alert alert = new Alert( Alert.AlertType.INFORMATION );
        alert.setTitle( "About" );
        alert.setHeaderText( "This program is designed as reference system for flights and routes.\n" +
                             "You can use it to add, edit, delete routes and flights in data base and to search for them." );
        alert.setContentText( " - Don`t forget to fill all fields, when you add/edit route or flight;\n" +
                              " - Use only a-z, 0-9, - and _ in names/numbers;\n" +
                              " - Use * and ? in search field instead of many or one unknown symbol;\n" +
                              " - If you add/edit/delete some route/flight, update search parameters to update tables with routes and flights." );
        alert.showAndWait();
    }
}







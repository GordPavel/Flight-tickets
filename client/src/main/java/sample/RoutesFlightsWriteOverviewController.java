package sample;

import exceptions.FlightAndRouteException;
import javafx.collections.FXCollections;
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
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@SuppressWarnings( "WeakerAccess" )
public class RoutesFlightsWriteOverviewController extends RoutesFlightsOverviewController{

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
    @FXML MenuBar                     menuBar;
    @FXML Menu                        fileMenu;
    @FXML Menu                        helpMenu;
    @FXML Button                      addRouteButton;
    @FXML Button                      editRouteButton;
    @FXML Button                      deleteRouteButton;
    @FXML Button                      addFlightButton;
    @FXML Button                      editFlightButton;
    @FXML Button                      deleteFlightButton;
    @FXML Button                      updateFlightButton;
    @FXML Button                      searchFlightButton;
    @FXML Button                      updateRouteButton;


    private Stage thisStage;
    RoutesFlightsWriteOverviewController( Stage thisStage ){
        this.thisStage = thisStage;
    }

    /**
     initialization of view
     */
    @FXML
    public void initialize(){
        updateFlightButton.setLayoutX( updateFlightButton.getLayoutX() - 37 );
        searchFlightButton.setLayoutX( searchFlightButton.getLayoutX() - 37 );

        Controller.getInstance().updateFlights();
        Controller.getInstance().updateRoutes();

        departureColumn.setCellValueFactory( new PropertyValueFactory<>( "from" ) );
        destinationColumn.setCellValueFactory( new PropertyValueFactory<>( "to" ) );
        routeTable.setItems( Controller.getInstance().getRoutes() );
        number.setCellValueFactory( new PropertyValueFactory<>( "number" ) );
        routeColumnFlight.setCellValueFactory( new PropertyValueFactory<>( "route" ) );
        flightTable.setItems( Controller.getInstance().getFlights() );
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
            routeTable.setItems( Controller.getInstance().getRoutes().stream().filter(
                    route -> departurePattern.matcher( route.getFrom().getId() ).matches() &&
                             destinationPattern.matcher( route.getTo().getId() ).matches() ).collect(
                    Collectors.collectingAndThen( toList() , FXCollections::observableArrayList ) ) );
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
     */
    @Override
    @FXML
    public void handleDeleteRouteButton(){
        Optional.ofNullable( routeTable.getSelectionModel().getSelectedItem() ).ifPresent( selectedRoute -> {
            try{
                routeTable.getItems().remove( selectedRoute );
                DataModelInstanceSaver.getInstance().removeRoute( selectedRoute );
                Controller.getInstance().updateRoutes();
                routeTable.refresh();
                Controller.getInstance().updateFlights();
                flightTable.setItems( Controller.getInstance().getFlights() );
                flightTable.refresh();

                // TODO: put here request to server to delete route
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
    @Override
    @FXML
    public void handleDeleteFlightButton(){
        Optional.ofNullable( flightTable.getSelectionModel().getSelectedItem() ).ifPresent( selectedFlight -> {
            try{
                flightTable.getItems().remove( selectedFlight );
                DataModelInstanceSaver.getInstance().removeFlight( selectedFlight.getNumber() );
                Main.changed = true;
                Controller.getInstance().updateFlights();
                flightTable.refresh();

                // TODO: put here request to server to delete flight
            }catch( FlightAndRouteException e ){
                Alert alert = new Alert( Alert.AlertType.WARNING );
                alert.setTitle( "Model exception" );
                alert.setHeaderText( "Model throw an exception" );
                alert.setContentText( e.getMessage() );
                alert.showAndWait();
            }
        } );
    }

    @Override
    @FXML
    public void handleAddRouteButton(){
        try{
            FXMLLoader loader = new FXMLLoader( getClass().getResource( "/fxml/AddRoutesOverview.fxml" ) );
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
        routeTable.setItems( Controller.getInstance().getRoutes() );
        routeTable.refresh();
        flightTable.setItems( Controller.getInstance().getFlights() );
        flightTable.refresh();
    }

    @FXML
    public void handleUpdateFlightButton(){
        // TODO: put here request to server to update DB about routes
    }

    @FXML
    public void handleUpdateRouteButton(){
        // TODO: put here request to server to update DB about flights
    }

    @FXML
    public void handleSearchRouteButton(){
        // TODO: put here request to server to update DB about flights

    }


    @Override
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

    @Override
    @FXML
    public void handleAddFlightButton(){
        try{
            FXMLLoader                          loader     =
                    new FXMLLoader( getClass().getResource( "/fxml/AddFlightsOverview.fxml" ) );
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

    @Override
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

    @Override
    @FXML
    public void handleSearchFlightButton(){
        if( !Controller.getInstance().isFlightSearchActive() ){
            Controller.getInstance().setFlightSearchActive( true );
            try{
                Stage                           popUp         = new Stage();
                FXMLLoader                      loader        =
                        new FXMLLoader( getClass().getResource( "/fxml/SearchFlightsOverview.fxml" ) );
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
    private void handleChangeDBAction(){
        DataModelInstanceSaver.getInstance().clear();
        Controller.getInstance().stopThread();

        /*
          TODO: selecting new DB

          put here code to open window, that will allow you to download new DB.
         */

        try{
            Stage                    primaryStage = new Stage();
            FXMLLoader               loader       =
                    new FXMLLoader( getClass().getResource( "/fxml/ChoiseOverview.fxml" ) );
            ChoiceOverviewController controller = new ChoiceOverviewController( primaryStage );
            loader.setController( controller );
            primaryStage.setTitle( "Select DB" );
            Scene scene = new Scene( loader.load() );
            primaryStage.setScene( scene );
            primaryStage.setResizable( false );
            primaryStage.show();
            thisStage.close();
        }catch( IOException e ){
            System.out.println( "load problem" );
            System.out.println( e.getMessage() );
        }

    }

    @FXML
    private void handleLogOutAction( Event event ){

        DataModelInstanceSaver.getInstance().clear();
        Controller.getInstance().stopThread();

        /*
          TODO: server logout

          somehow let server know, that you change your login
         */

        try{
            Stage                   loginStage      = new Stage();
            FXMLLoader              loader          =
                    new FXMLLoader( getClass().getResource( "/fxml/LoginOverview.fxml" ) );
            LoginOverviewController logInController = new LoginOverviewController( loginStage );
            loader.setController( logInController );
            loginStage.setTitle( "Login" );
            Scene scene = new Scene( loader.load() );
            loginStage.setScene( scene );
            loginStage.setResizable( false );
            loginStage.show();
            thisStage.close();
        }catch( IOException e ){
            System.out.println( "load problem" );
            System.out.println( e.getMessage() );
        }
    }
}

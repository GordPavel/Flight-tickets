package sample;

import com.jfoenix.controls.JFXButton;
import exceptions.FlightAndRouteException;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.event.Event;
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
import java.util.function.Predicate;
import java.util.regex.Pattern;


/**
 Controller for routes and flights view
 Shows the information about all routes and flights
 */
abstract class RoutesFlightsOverviewController{

    private static final String EDIT_ROUTE_WINDOW    = "Edit a route";
    private static final String ADD_ROUTE_WINDOW     = "Add a route";
    private static final String EDIT_FLIGHT_WINDOW   = "Edit a flight";
    private static final String ADD_FLIGHT_WINDOW    = "Add a flight";
    private static final String SEARCH_FLIGHT_WINDOW = "Search a flight";

    /**
     Two text fields to filter routes table
     */
    @FXML TextField                   departure;
    @FXML TextField                   destination;

    @FXML TableView<Route>           routeTable;
    @FXML TableColumn<Route, String> departureColumn;
    @FXML TableColumn<Route, String> destinationColumn;

    @FXML TableView<Flight>           flightTable;
    @FXML TableColumn<Flight, String> number;
    @FXML TableColumn<Flight, Route>  routeColumnFlight;

    @FXML TextArea                    detailsTextArea;

    @FXML JFXButton                   editFlight;
    @FXML JFXButton                   deleteFlight;
    @FXML JFXButton                   editRoute;
    @FXML JFXButton                   deleteRoute;

    protected Stage thisStage;
    private ObjectProperty<Predicate<Route>> routesPredicate = new SimpleObjectProperty<>( route -> true );
    private FileChooser                      fileChooser     = new FileChooser();

    RoutesFlightsOverviewController( Stage thisStage ){
        this.thisStage = thisStage;
        fileChooser.getExtensionFilters().add( new FileChooser.ExtensionFilter( "Flights and routes" , "*.far" ) );
    }

    /**
     initialization of view
     */
    @FXML
    void initialize(){
        routeTable.setPrefWidth( 600 );
        departureColumn.setPrefWidth( 300 );
        destinationColumn.setPrefWidth( 300 );

        FilteredList<Route> routeFilteredList =
                DataModelInstanceSaver.getInstance().getRouteObservableList().filtered( route -> true );
        routeTable.setItems( routeFilteredList );
        routeFilteredList.predicateProperty().bind( routesPredicate );

        editFlight.disableProperty().bind( flightTable.getSelectionModel().selectedItemProperty().isNull() );
        deleteFlight.disableProperty().bind( flightTable.getSelectionModel().selectedItemProperty().isNull() );
        editRoute.disableProperty().bind( routeTable.getSelectionModel().selectedItemProperty().isNull() );
        deleteRoute.disableProperty().bind( routeTable.getSelectionModel().selectedItemProperty().isNull() );

        departureColumn.setCellValueFactory( new PropertyValueFactory<>( "from" ) );
        destinationColumn.setCellValueFactory( new PropertyValueFactory<>( "to" ) );

        number.setCellValueFactory( new PropertyValueFactory<>( "number" ) );
        routeColumnFlight.setCellValueFactory( new PropertyValueFactory<>( "route" ) );
        flightTable.setItems( DataModelInstanceSaver.getInstance().getFlightObservableList() );
        detailsTextArea.textProperty().bind( new StringBinding(){
            ReadOnlyObjectProperty<Flight> currentFlight = flightTable.getSelectionModel().selectedItemProperty();

            {
                bind( currentFlight );
            }

            @Override
            protected String computeValue(){
                return Optional.ofNullable( currentFlight.getValue() ).map( Flight::toString ).orElse( "" );
            }
        } );

        departure.textProperty().addListener(
                ( observable , oldValue , newValue ) -> searchListeners( departure.getText() ,
                                                                         destination.getText() ) );
        destination.textProperty().addListener(
                ( observable , oldValue , newValue ) -> searchListeners( departure.getText() ,
                                                                         destination.getText() ) );
    }

    private void searchListeners( String departure , String destination ){
        Predicate<Route> v = route ->
                Pattern.compile( ".*" + departure.replaceAll( "\\*" , ".*" ).replaceAll( "\\?" , "." ) + ".*" ,
                                 Pattern.CASE_INSENSITIVE ).matcher( route.getFrom().getId() ).matches() &&
                Pattern.compile( ".*" + destination.replaceAll( "\\*" , ".*" ).replaceAll( "\\?" , "." ) + ".*" ,
                                 Pattern.CASE_INSENSITIVE ).matcher( route.getTo().toString() ).matches();
        routesPredicate.setValue( v );
    }

    /**
     * Open add route view
     */
    @FXML
    void handleAddRouteButton(){
        try{
            Stage                              popUp      = new Stage();
            FXMLLoader loader = new FXMLLoader( getClass().getResource( "/fxml/AddRoutesOverview.fxml" ) );
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

    /**
     * Open edit route view
     */
    @FXML
    void handleEditRouteButton(){
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
            }catch( IOException e ){
                e.printStackTrace();
            }
        } );
    }

    /**
     * Delete route from local DB
     */
    @FXML
    void handleDeleteRouteButton(){
        Optional.ofNullable( routeTable.getSelectionModel().getSelectedItem() ).ifPresent( selectedRoute -> {
            try{
                DataModelInstanceSaver.getInstance().removeRoute( selectedRoute );
                ClientMain.changed = true;
            }catch( FlightAndRouteException e ){
                showModelAlert( e );
            }
        } );
    }

    /**
     * Open add flight view
     */
    @FXML
    void handleAddFlightButton(){
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
        }catch( IOException e ){
            e.printStackTrace();
        }
    }

    /**
     * Open edit flight view
     */
    @FXML
    void handleEditFlightButton(){
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
            }catch( IOException e ){
                e.printStackTrace();
            }
        } );
    }

    /**
     * Delete flight from local DB
     */
    @FXML
    void handleDeleteFlightButton(){
        Optional.ofNullable( flightTable.getSelectionModel().getSelectedItem() ).ifPresent( selectedFlight -> {
            try{
                DataModelInstanceSaver.getInstance().removeFlight( selectedFlight.getNumber() );
            }catch( FlightAndRouteException e ){
                showModelAlert( e );
            }
        } );
    }


    /**
     * Shows alert view with message from model
     */
    static void showModelAlert( FlightAndRouteException e ){
        Alert alert = new Alert( Alert.AlertType.ERROR );
        alert.setTitle( "Model exception" );
        alert.setHeaderText( "Model throw an exception" );
        alert.setContentText( e.getMessage() );
        alert.showAndWait();
    }

    /**
     * Open search flight view
     */
    @FXML
    void handleSearchFlightButton(){
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
        }
    }

    /**
     * Open file with saved data
     */
    @FXML
    void handleOpenAction(){
        Optional.ofNullable( fileChooser.showOpenDialog( new Stage() ) ).ifPresent( file -> {
            try{
                DataModelInstanceSaver.getInstance().importFromFile( file );
                ClientMain.changed = false;
                ClientMain.savingFile = file;
                thisStage.setTitle( file.getName() );
            }catch( IOException | FlightAndRouteException e ){
                e.printStackTrace();
            }
        } );
    }

    /**
     * Save local DB to file
     */
    @FXML
    void handleSaveAction(){
        if( ClientMain.savingFile == null ){
            Optional.ofNullable( fileChooser.showSaveDialog( new Stage() ) ).ifPresent( file -> {
                ClientMain.savingFile = file;
                thisStage.setTitle( file.getName() );
            } );
        }
        try{
            DataModelInstanceSaver.getInstance().saveToFile( ClientMain.savingFile );
            ClientMain.changed = false;
        }catch( IOException | FlightAndRouteException e ){
            new Alert( Alert.AlertType.ERROR , e.getMessage() ).show();
        }
    }

    /**
     * Save local DB to selected/new file
     */
    @FXML
    void handleSaveAsAction(){
        Optional.ofNullable( fileChooser.showSaveDialog( new Stage() ) ).ifPresent( file -> {
            try{
                DataModelInstanceSaver.getInstance().saveToFile( file );
            }catch( IOException | FlightAndRouteException e ){
                new Alert( Alert.AlertType.ERROR , e.getMessage() ).show();
            }
        } );
    }

    /**
     * Merging data from file with local DB
     */
    @FXML
    void handleMergeAction(){
        Optional.ofNullable( fileChooser.showOpenDialog( new Stage() ) ).ifPresent( file -> {
            try{
                ArrayList<Serializable> failedInMerge = DataModelInstanceSaver.getInstance().mergeData( file )
                                                                              .collect( ArrayList::new , List::add ,
                                                                                        List::addAll );
                ClientMain.changed = true;
                Alert alert = new Alert( Alert.AlertType.WARNING );
                alert.setTitle( "Merge results" );
                alert.setHeaderText( "Model have this problems with merge:" );
                StringBuilder errors = new StringBuilder();

                ArrayList<Flight> mergeFlights = new ArrayList<>();
                ArrayList<Route>  mergeRoutes  = new ArrayList<>();
                for( Serializable element : failedInMerge ){
                    errors.append( "-" ).append( element.toString() ).append( "\n" );
                    if( element instanceof Flight &&
                        DataModelInstanceSaver.getInstance().listFlightsWithPredicate( flight -> true )
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
                    Stage popUp = new Stage();
                    FXMLLoader loader = new FXMLLoader( getClass().getResource( "/fxml/mergeOverview.fxml" ) );
                    MergeOverviewController mergeOverviewController = new MergeOverviewController( popUp );
                    loader.setController( mergeOverviewController );
                    Scene scene = new Scene( loader.load() );
                    popUp.initModality( Modality.APPLICATION_MODAL );
                    popUp.setTitle( ADD_ROUTE_WINDOW );
                    popUp.setScene( scene );
                    popUp.setResizable( false );
                    popUp.showAndWait();
                }
            }catch( IOException | FlightAndRouteException e ){
                new Alert( Alert.AlertType.ERROR , e.getMessage() ).show();
            }
        } );
    }


    @FXML
    void handleAboutAction(){
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

    @FXML
    void handleChangeDBAction(){
        DataModelInstanceSaver.getInstance().clear();
        Controller.getInstance().stopThread();

        /*
          TODO: selecting new DB

          put here code to open window, that will allow you to download new DB.
         */

        try{
            Stage primaryStage = new Stage();
            FXMLLoader loader = new FXMLLoader( getClass().getResource( "/fxml/ChoiseOverview.fxml" ) );
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
    void handleLogOutAction( Event event ){
        DataModelInstanceSaver.getInstance().clear();
        Controller.getInstance().stopThread();

        /*
          TODO: server logout

          somehow let server know, that you change your login
         */

        try{
            Stage loginStage = new Stage();
            FXMLLoader loader = new FXMLLoader( getClass().getResource( "/fxml/LoginOverview.fxml" ) );
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







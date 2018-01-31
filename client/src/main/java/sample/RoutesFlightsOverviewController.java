package sample;

import com.jfoenix.controls.JFXButton;
import exceptions.FlightAndRouteException;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
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
import org.codehaus.jackson.map.ObjectMapper;
import transport.Actions;
import transport.Data;
import transport.UserInformation;

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
    private static final String SEARCH_FLIGHT_WINDOW = "Search a flight";
    static final         String ADD_ROUTE_WINDOW     = "Add a route";
    static final         String EDIT_ROUTE_WINDOW    = "Edit a route";
    static final         String EDIT_FLIGHT_WINDOW   = "Edit a flight";
    static final         String ADD_FLIGHT_WINDOW    = "Add a flight";
    SearchFlightsOverviewController searchFlights;

    @FXML Menu     fileMenu;
    @FXML MenuItem openMenuButton;
    @FXML MenuItem saveMenuButton;
    @FXML MenuItem saveAsMenuButton;
    @FXML MenuItem mergeMenuButton;
    @FXML MenuItem infoMenuButton;
    @FXML MenuItem logoutMenuButton;
    @FXML MenuItem changeMenuButton;

    /**
     Two text fields to filter routes table
     */
    @FXML TextField departure;
    @FXML TextField destination;

    @FXML TableView<Route>           routeTable;
    @FXML TableColumn<Route, String> departureColumn;
    @FXML TableColumn<Route, String> destinationColumn;

    @FXML TableView<Flight>           flightTable;
    @FXML TableColumn<Flight, String> number;
    @FXML TableColumn<Flight, Route>  routeColumnFlight;

    @FXML TextArea detailsTextArea;

    @FXML Button    addFlightButton;
    @FXML JFXButton editFlightButton;
    @FXML JFXButton deleteFlightButton;
    @FXML Button    updateFlightButton;
    @FXML Button    searchFlightButton;
    @FXML Button    addRouteButton;
    @FXML JFXButton editRouteButton;
    @FXML JFXButton deleteRouteButton;
    @FXML Button    updateRouteButton;
    @FXML Button    searchRouteButton;


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

        FilteredList<Route> routeFilteredList =
                DataModelInstanceSaver.getInstance().getRouteObservableList().filtered( route -> true );
        routeTable.setItems( routeFilteredList );
        routeFilteredList.predicateProperty().bind( routesPredicate );

        editFlightButton.disableProperty().bind( flightTable.getSelectionModel().selectedItemProperty().isNull() );
        deleteFlightButton.disableProperty().bind( flightTable.getSelectionModel().selectedItemProperty().isNull() );
        editRouteButton.disableProperty().bind( routeTable.getSelectionModel().selectedItemProperty().isNull() );
        deleteRouteButton.disableProperty().bind( routeTable.getSelectionModel().selectedItemProperty().isNull() );

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
        openMenuButton.setOnAction( event -> handleOpenAction() );
        saveMenuButton.setOnAction( event -> handleSaveAction() );
        saveAsMenuButton.setOnAction( event -> handleSaveAsAction() );
        mergeMenuButton.setOnAction( event -> handleMergeAction() );
        infoMenuButton.setOnAction( event -> handleAboutAction() );
        logoutMenuButton.setOnAction( event1 -> handleLogOutAction() );
        changeMenuButton.setOnAction( event -> handleChangeDBAction() );

        searchFlightButton.setOnAction( event -> handleSearchFlightAction() );
        updateFlightButton.setOnAction( event -> handleUpdateFlightAction() );
        searchRouteButton.setOnAction( event -> handleSearchRouteAction() );
        updateRouteButton.setOnAction( event -> handleUpdateRouteAction() );
    }

    private void searchListeners( String departure , String destination ){
        Predicate<Route> v = route ->
                Pattern.compile( ".*" + departure.replaceAll( "\\*" , ".*" ).replaceAll( "\\?" , "." ) + ".*" ,
                                 Pattern.CASE_INSENSITIVE ).matcher( route.getFrom().getId() ).matches() &&
                Pattern.compile( ".*" + destination.replaceAll( "\\*" , ".*" ).replaceAll( "\\?" , "." ) + ".*" ,
                                 Pattern.CASE_INSENSITIVE ).matcher( route.getTo().toString() ).matches();
        routesPredicate.setValue( v );
        searchFlightButton.setOnAction( event -> handleSearchFlightAction() );
    }

    /**
     Shows alert view with message from model
     */
    static void showModelAlert( FlightAndRouteException e ){
        Alert alert = new Alert( Alert.AlertType.ERROR );
        alert.setTitle( "Model exception" );
        alert.setHeaderText( "Model throw an exception" );
        alert.setContentText( e.getMessage() );
        alert.showAndWait();
    }

    /**
     Open file with saved data
     */
    private void handleOpenAction(){
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
     Save local DB to file
     */
    private void handleSaveAction(){
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
     Save local DB to selected/new file
     */
    private void handleSaveAsAction(){
        Optional.ofNullable( fileChooser.showSaveDialog( new Stage() ) ).ifPresent( file -> {
            try{
                DataModelInstanceSaver.getInstance().saveToFile( file );
            }catch( IOException | FlightAndRouteException e ){
                new Alert( Alert.AlertType.ERROR , e.getMessage() ).show();
            }
        } );
    }

    /**
     Merging data from file with local DB
     */
    private void handleMergeAction(){
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
                        DataModelInstanceSaver.getInstance().listFlightsWithPredicate( flight -> true ).stream()
                                .noneMatch( flight -> flight.equals( element ) )){
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
                    Stage                   popUp                   = new Stage();
                    FXMLLoader              loader                  =
                            new FXMLLoader( getClass().getResource( "/fxml/mergeOverview.fxml" ) );
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

    private void handleChangeDBAction(){

        /*
          TODO: selecting new DB

          put here code to open window, that will allow you to download new DB.
         */
        Data data = new Data();
        if (!(ClientMain.getClientSocket() == null)) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                mapper.writeValue(ClientMain.getClientSocket().getOutputStream(), ClientMain.getUserInformation());
                data = (Data) mapper.readValue(ClientMain.getClientSocket().getInputStream(), Data.class);
            } catch (IOException | NullPointerException ex) {
                System.out.println(ex.getMessage());
            }

            if ( data.notHasException() ) {
                DataModelInstanceSaver.getInstance().clear();
                Controller.getInstance().stopThread();
                try {
                    Stage primaryStage = new Stage();
                    FXMLLoader loader =
                            new FXMLLoader(getClass().getResource("/fxml/ChoiseOverview.fxml"));
                    ChoiceOverviewController controller = new ChoiceOverviewController(primaryStage,data);
                    loader.setController(controller);
                    primaryStage.setTitle("Select DB");
                    Scene scene = new Scene(loader.load());
                    primaryStage.setScene(scene);
                    primaryStage.setResizable(false);
                    primaryStage.show();
                    thisStage.close();
                } catch (IOException e) {
                    System.out.println("load problem");
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    private void handleLogOutAction(){
        DataModelInstanceSaver.getInstance().clear();
        Controller.getInstance().stopThread();

        /*
          TODO: server logout

          somehow let server know, that you change your login
         */
        ClientMain.setUserInformation(new UserInformation());
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

    /**
     Update flight list
     */
    private void handleUpdateFlightAction(){
        // TODO: put here request to server to update DB about flights
       requestFlights(flight -> true);
    }

    public void requestFlights(Predicate<Flight> predicate)
    {
        Data data = new Data();
        ObjectMapper mapper = new ObjectMapper();
        Actions actions = new Actions(null, Actions.ActionsType.UPDATE, predicate);
        try {   //add FaR exceprions...
            mapper.writeValue(ClientMain.getClientSocket().getOutputStream(), ClientMain.getUserInformation());
            data = (Data) mapper.readValue(ClientMain.getClientSocket().getInputStream(), Data.class);
            for (Route route : data.getRoutes())
            {
                DataModelInstanceSaver.getInstance().addRoute(route);
            }
            for (Flight flight : data.getFlights())
            {
                DataModelInstanceSaver.getInstance().addFlight(flight);
            }
        } catch (IOException | NullPointerException ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**
     Update route list
     */
    private void handleUpdateRouteAction(){
        // TODO: put here request to server to update DB about routes
        requestRoutes(route -> true);
    }

    public void requestRoutes(Predicate<Route> predicate){
        Data data = new Data();
        ObjectMapper mapper = new ObjectMapper();
        Actions actions = new Actions(null, Actions.ActionsType.UPDATE, predicate);
        try {   //add FaR exceprions...
            mapper.writeValue(ClientMain.getClientSocket().getOutputStream(), ClientMain.getUserInformation());
            data = (Data) mapper.readValue(ClientMain.getClientSocket().getInputStream(), Data.class);
            for (Route route : data.getRoutes())
            {
                DataModelInstanceSaver.getInstance().addRoute(route);
            }
        } catch (IOException | NullPointerException ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**
     Open search flight view
     */
    private void handleSearchFlightAction(){
        if( !Controller.getInstance().isFlightSearchActive() ){
            Controller.getInstance().setFlightSearchActive( true );
            try{
                Stage                           popUp         = new Stage();
                FXMLLoader                      loader        =
                        new FXMLLoader( getClass().getResource( "/fxml/SearchFlightsOverview.fxml" ) );
                searchFlights = new SearchFlightsOverviewController( this , popUp );
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
     Search for routes
     */
    private void handleSearchRouteAction(){
        // TODO: put here request to server to update DB about routes
        requestRoutes(route ->
                Pattern.compile( ".*" + departure.getText().replaceAll( "\\*" , ".*" ).replaceAll( "\\?" , "." ) + ".*" ,
                        Pattern.CASE_INSENSITIVE ).matcher( route.getFrom().getId() ).matches() &&
                        Pattern.compile( ".*" + destination.getText().replaceAll( "\\*" , ".*" ).replaceAll( "\\?" , "." ) + ".*" ,
                                Pattern.CASE_INSENSITIVE ).matcher( route.getTo().toString() ).matches());
    }
}







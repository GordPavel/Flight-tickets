package sample;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jfoenix.controls.JFXButton;
import exceptions.FlightAndRouteException;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
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
import transport.Data;
import transport.ListChangeAdapter;
import transport.PredicateParser;
import transport.UserInformation;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.regex.Pattern;


/**
 Controller for routes and flights view
 Shows the information about all routes and flights
 Parent class for read-only, write and localFile controllers
 */
abstract class RoutesFlightsOverviewController{
    static final String SEARCH_FLIGHT_WINDOW = "Search a flight";
    static final String ADD_ROUTE_WINDOW     = "Add a route";
    static final String EDIT_ROUTE_WINDOW    = "Edit a route";
    static final String EDIT_FLIGHT_WINDOW   = "Edit a flight";
    static final String ADD_FLIGHT_WINDOW    = "Add a flight";
    SearchFlightsOverviewController searchFlights;

    @FXML Menu     fileMenu;
    @FXML MenuItem openMenuButton;
    @FXML MenuItem saveMenuButton;
    @FXML MenuItem saveAsMenuButton;
    @FXML MenuItem mergeMenuButton;
    @FXML MenuItem infoMenuButton;
    @FXML MenuItem logoutMenuButton;
    @FXML MenuItem informationMenuButton;

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

    @FXML Label routeConnectLabel;
    @FXML Label flightConnectLabel;


    protected Stage thisStage;
    private ObjectProperty<Predicate<Route>> routesPredicate = new SimpleObjectProperty<>( route -> true );
    FileChooser fileChooser = new FileChooser();
    static CopyOnWriteArrayList<ListChangeAdapter> changes = new CopyOnWriteArrayList<>();

    static CopyOnWriteArrayList<ListChangeAdapter> getChanges(){
        return changes;
    }

    RoutesFlightsOverviewController( Stage thisStage ){
        this.thisStage = thisStage;
        fileChooser.getExtensionFilters().add( new FileChooser.ExtensionFilter( "Flights and routes" , "*.far" ) );
    }

    /**
     initialization of view
     */
    @FXML
    void initialize(){
        FilteredList<Route>
                routeFilteredList =
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

        departure.textProperty()
                 .addListener( ( observable , oldValue , newValue ) -> searchListeners( departure.getText() ,
                                                                                        destination.getText() ) );
        destination.textProperty()
                   .addListener( ( observable , oldValue , newValue ) -> searchListeners( departure.getText() ,
                                                                                          destination.getText() ) );
        openMenuButton.setOnAction( event -> handleOpenAction() );
        saveMenuButton.setOnAction( event -> handleSaveAction() );
        saveAsMenuButton.setOnAction( event -> handleSaveAsAction() );
        infoMenuButton.setOnAction( event -> handleAboutAction() );
        logoutMenuButton.setOnAction( event -> handleLogOutAction() );
        informationMenuButton.setOnAction( event -> handleInformationAction() );

        searchFlightButton.setOnAction( event -> handleSearchFlightAction() );
        updateFlightButton.setOnAction( event -> handleUpdateFlightAction() );
        searchRouteButton.setOnAction( event -> handleSearchRouteAction() );
        updateRouteButton.setOnAction( event -> handleUpdateRouteAction() );

        thisStage.setOnCloseRequest( event -> {
            if( Controller.getInstance().isFlightSearchActive() ) searchFlights.closeWindow();
        } );
    }

    private void searchListeners( String departure , String destination ){
        Predicate<Route>
                v =
                route -> getRoutePattern( departure ).matcher( route.getFrom().getId() ).matches() &&
                         getRoutePattern( destination ).matcher( route.getTo().getId() ).matches();
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
                DataModelInstanceSaver.getInstance().importFrom( Files.newInputStream( file.toPath() ) );
                Controller.changed = false;
                Controller.savingFile = file;
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
        if( Controller.savingFile == null ){
            Optional.ofNullable( fileChooser.showSaveDialog( new Stage() ) ).ifPresent( file -> {
                Controller.savingFile = file;
                thisStage.setTitle( file.getName() );
            } );
        }
        try{
            DataModelInstanceSaver.getInstance().saveTo( Files.newOutputStream( Controller.savingFile.toPath() ) );
            Controller.changed = false;
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
                DataModelInstanceSaver.getInstance().saveTo( Files.newOutputStream( file.toPath() ) );
            }catch( IOException | FlightAndRouteException e ){
                new Alert( Alert.AlertType.ERROR , e.getMessage() ).show();
            }
        } );
    }

    /**
     * Shows information about programm
     */
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

    /**
     * Shows information about current user
     */
    private void handleInformationAction(){
        String information="";
        if (this instanceof RoutesFlightsLocalFileOverviewController)
        information=information+"Local user\n";
        if (this instanceof RoutesFlightsReadOnlyOverviewController)
        {
            information=information+"Read-only User\n"+
                    "User: "+Controller.getInstance().getUserInformation().getLogin()+"\n"+
                    "Base: "+Controller.getInstance().getUserInformation().getDataBase()+"\n";
        }
        if (this instanceof RoutesFlightsWriteOverviewController)
        {
            information=information+"Read-write User\n"+
                    "User: "+Controller.getInstance().getUserInformation().getLogin()+"\n"+
                    "Base: "+Controller.getInstance().getUserInformation().getDataBase()+"\n";
        }
        Alert alert = new Alert( Alert.AlertType.INFORMATION );
        alert.setTitle( "User Information" );
        alert.setHeaderText( information );

        alert.showAndWait();
    }

    /**
     * Log out button handler. Close current view, open login view
     */
    void handleLogOutAction(){
        DataModelInstanceSaver.getInstance().clear();
        Controller.getInstance().stopThread();
        Controller.getInstance().setUserInformation( new UserInformation() );
        try{
            DataOutputStream
                    dataOutputStream =
                    new DataOutputStream( Controller.getInstance().getClientSocket().getOutputStream() );
            dataOutputStream.writeUTF( "*" );
            Stage                   loginStage      = new Stage();
            FXMLLoader
                                    loader          =
                    new FXMLLoader( getClass().getResource( "/fxml/LoginOverview.fxml" ) );
            LoginOverviewController logInController = new LoginOverviewController( loginStage );
            loader.setController( logInController );
            loginStage.setTitle( "Login" );
            Scene scene = new Scene( loader.load() );
            loginStage.setScene( scene );
            loginStage.setResizable( false );
            loginStage.show();
            closeWindow();
        }catch( IOException e ){
            System.out.println( "load problem" );
            System.out.println( e.getMessage() );
        }
    }

    private void closeWindow(){
        thisStage.close();
    }

    /**
     Update flight list
     */
    void handleUpdateFlightAction(){
        Controller.getInstance()
                  .getUserInformation()
                  .setPredicate( PredicateParser.createFlightPredicate( "*" ,
                                                                        "" ,
                                                                        "" ,
                                                                        "" ,
                                                                        "" ,
                                                                        "" ,
                                                                        "" ,
                                                                        "" ,
                                                                        "" ,
                                                                        "" ) );
        if( this instanceof RoutesFlightsWriteOverviewController ) DataModelInstanceSaver.getInstance().clear();
        requestUpdate();
    }


    /**
     Update route list
     */
    void handleUpdateRouteAction(){
        Controller.getInstance().getUserInformation().setPredicate( PredicateParser.createRoutePredicate( "*" , "*" ) );
        if( this instanceof RoutesFlightsWriteOverviewController ) DataModelInstanceSaver.getInstance().clear();
        requestUpdate();
    }

    /**
     Method used for updates and searches. PredicateParser setuped by other methods, after operation cleared.
     */
    void requestUpdate(){
        if( Controller.getInstance().getClientSocket().isClosed() ){
            Controller.getInstance().reconnect();
        }
        if( !Controller.getInstance().getClientSocket().isConnected() ){
            routeConnectLabel.setText( "Offline" );
            flightConnectLabel.setText( "Offline" );
            Controller.getInstance().reconnect();
        }
        if( Controller.getInstance().getClientSocket().isConnected() ){
            routeConnectLabel.setText( "Online" );
            flightConnectLabel.setText( "Online" );
            ObjectMapper mapper = new ObjectMapper();

            try{
                DataOutputStream
                        dataOutputStream =
                        new DataOutputStream( Controller.getInstance().getClientSocket().getOutputStream() );
                System.out.println( "ready to send" );
                System.out.println( mapper.writeValueAsString( Controller.getInstance().getUserInformation() ) );
                dataOutputStream.writeUTF( mapper.writeValueAsString( Controller.getInstance().getUserInformation() ) );
                System.out.println( "sended" );
            }catch( IOException | NullPointerException ex ){
                System.out.println( ex.getMessage() );
                ex.printStackTrace();
                System.out.println( "Yep" );
            }
            Controller.getInstance().getUserInformation().setPredicate( null );
        }
    }

    /**
    * method, used for receiving updates from server. Checks for socket status, receive data, applt data to current DM
     */
    Data receiveUpdate(){
        Data data = new Data();
        if( Controller.getInstance().getClientSocket().isClosed() ){
            Controller.getInstance().reconnect();
        }
        if( !Controller.getInstance().getClientSocket().isConnected() ){
            routeConnectLabel.setText( "Offline" );
            flightConnectLabel.setText( "Offline" );
            Controller.getInstance().reconnect();
        }
        if( Controller.getInstance().getClientSocket().isConnected() ){
            routeConnectLabel.setText( "Online" );
            flightConnectLabel.setText( "Online" );

            ObjectMapper mapper = new ObjectMapper();

            try{
                DataInputStream
                        inputStream =
                        new DataInputStream( Controller.getInstance().getClientSocket().getInputStream() );
                data = mapper.readerFor( Data.class ).readValue( inputStream.readUTF() );
                System.out.println( mapper.writeValueAsString( data ) );
                if( data.hasNotException() ){
                    Data data1 = data;
                    if( !( data1.getChanges() == null ) ){
                        data1.getChanges()
                             .forEach( update -> update.apply( DataModelInstanceSaver.getInstance() , false ) );
                    }
                    if( !( data1.getRoutes() == null ) ){
                        for( Route route : data1.getRoutes() ){
                            if( !DataModelInstanceSaver.getInstance().containRoute( route ) ){
                                DataModelInstanceSaver.getInstance().addRoute( route , false );
                            }
                        }
                    }
                    if( !( data1.getFlights() == null ) ){
                        for( Flight flight : data1.getFlights() ){
                            if( !DataModelInstanceSaver.getInstance().containFlight( flight ) ){
                                DataModelInstanceSaver.getInstance().addFlight( flight );
                            }
                        }
                    }
                }
            }catch( IOException | NullPointerException ex ){
//                System.out.println( ex.getMessage() );
//                ex.printStackTrace();
//                System.out.println( "Yep" );
            }
            Controller.getInstance().getUserInformation().setPredicate( null );
            flightTable.refresh();
            routeTable.refresh();
        }
        return data;
    }

    /**
     Open search flight view
     */
    private void handleSearchFlightAction(){
        if( !Controller.getInstance().isFlightSearchActive() ){
            Controller.getInstance().setFlightSearchActive( true );
            try{
                Stage      popUp  = new Stage();
                FXMLLoader loader = new FXMLLoader( getClass().getResource( "/fxml/SearchFlightsOverview.fxml" ) );
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
        Controller.getInstance()
                  .getUserInformation()
                  .setPredicate( PredicateParser.createRoutePredicate( departure.getText() , destination.getText() ) );
        requestUpdate();
        if( this instanceof RoutesFlightsWriteOverviewController ) DataModelInstanceSaver.getInstance().clear();
    }


    private Pattern getRoutePattern( String searchText ){
        return Pattern.compile( ".*" + searchText.replaceAll( "\\*" , ".*" ).replaceAll( "\\?" , "." ) + ".*" ,
                                Pattern.CASE_INSENSITIVE );
    }


}







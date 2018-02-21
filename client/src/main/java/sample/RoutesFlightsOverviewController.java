package sample;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jfoenix.controls.JFXButton;
import exceptions.FlightAndRouteException;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import transport.PredicateParser;
import transport.UserInformation;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;


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

    @FXML Label routeConnectLabel;
    @FXML Label flightConnectLabel;


    Stage thisStage;
    private       ObjectProperty<Predicate<Route>> routesPredicate = new SimpleObjectProperty<>( route -> true );
    private       FileChooser                      fileChooser     = new FileChooser();
    private final ObjectMapper                     mapper          = new ObjectMapper();

    RoutesFlightsOverviewController( Stage thisStage ){
        this.thisStage = thisStage;
        fileChooser.getExtensionFilters().add( new FileChooser.ExtensionFilter( "Flights and routes" , "*.far" ) );
    }

    /**
     initialization of view
     */
    @FXML
    void initialize(){
        routeTable.setItems( DataModelInstanceSaver.getInstance()
                                                   .getRouteObservableList()
                                                   .filtered( routesPredicate.get() ) );
        ( ( FilteredList<Route> ) routeTable.getItems() ).predicateProperty().bind( routesPredicate );

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

        ChangeListener<String> routeSearchListener =
                ( observable , oldValue , newValue ) -> routesPredicate.setValue( route -> {
                    return getRoutePattern( departure.getText() ).matcher( route.getFrom().getId() ).matches() &&
                           getRoutePattern( destination.getText() ).matcher( route.getTo().getId() ).matches();
                } );
        departure.textProperty().addListener( routeSearchListener );
        destination.textProperty().addListener( routeSearchListener );
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

        routesPredicate.addListener( ( observable , oldValue , newValue ) -> {
            Controller.getInstance().routePredicate = newValue;
        } );
    }

    /**
     Open file with saved data
     */
    private void handleOpenAction(){
        Optional.ofNullable( fileChooser.showOpenDialog( new Stage() ) ).ifPresent( file -> {
            try{
                DataModelInstanceSaver.getInstance().importFrom( Files.newInputStream( file.toPath() ) );
                Controller.getInstance().changed = false;
                Controller.getInstance().savingFile = file;
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
        if( Controller.getInstance().savingFile == null ){
            Optional.ofNullable( fileChooser.showSaveDialog( new Stage() ) ).ifPresent( file -> {
                Controller.getInstance().savingFile = file;
                thisStage.setTitle( file.getName() );
            } );
        }
        try{
            DataModelInstanceSaver.getInstance()
                                  .saveTo( Files.newOutputStream( Controller.getInstance().savingFile.toPath() ) );
            Controller.getInstance().changed = false;
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
     Merging data from file with local DB
     */
    private void handleMergeAction(){
        Optional.ofNullable( fileChooser.showOpenDialog( new Stage() ) ).ifPresent( file -> {
            try( InputStream mergingFile = Files.newInputStream( file.toPath() ) ){
                Controller.getInstance().changed = true;
                List<Serializable> failedInMerge =
                        DataModelInstanceSaver.getInstance().mergeData( mergingFile ).collect( toList() );

                ObservableList<Flight> failedFlights = failedInMerge.parallelStream()
                                                                    .filter( element -> element.getClass()
                                                                                               .equals( Flight.class ) )
                                                                    .map( Flight.class::cast )
                                                                    .collect( Collectors.collectingAndThen( toList() ,
                                                                                                            FXCollections::observableArrayList ) );
                String errors = failedInMerge.stream()
                                             .map( Serializable::toString )
                                             .collect( Collectors.joining( "\n-" , "-" , "\n" ) );

                if( !failedInMerge.isEmpty() ){
                    ClientMain.showWarning( "Merge results" , "Model have this problems with merge:" , errors );
                }

                if( !failedFlights.isEmpty() ){
                    Stage      popUp  = new Stage();
                    FXMLLoader loader = new FXMLLoader( getClass().getResource( "/fxml/MergeOverview.fxml" ) );
                    MergeOverviewController mergeOverviewController =
                            new MergeOverviewController( popUp , failedFlights );
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
        try{
//            clear
            Controller.getInstance().adminConnection.get().close();
            DataModelInstanceSaver.getInstance().clear();

            try( Socket socket = new Socket( Controller.getInstance().host , Controller.getInstance().port ) ;
                 DataOutputStream outputStream = new DataOutputStream( socket.getOutputStream() ) ;
                 DataInputStream inputStream = new DataInputStream( socket.getInputStream() ) ){

                UserInformation request =
                        new UserInformation( Controller.getInstance().login , Controller.getInstance().password );
                outputStream.writeUTF( mapper.writeValueAsString( request ) );
                Data response = mapper.readerFor( Data.class ).readValue( inputStream.readUTF() );
                response.withoutExceptionOrWith( data -> {
                    ChoiceOverviewController.openChoiceDBScreen( data );
                    closeWindow();
                } , ClientMain::showWarningByError );
            }catch( EOFException e ){
                ClientMain.showWarning( "Error adminConnection" , "НЕизвестная хуйня" , "Server has closed adminConnection" );
            }catch( IOException e ){
                System.err.println( "load problem" );
                e.printStackTrace();
            }
        }catch( IOException e ){
            System.err.println( "Quit error" );
            e.printStackTrace();
        }
    }


    private void handleLogOutAction(){
        try{
            //            clear
            Controller.getInstance().adminConnection.get().close();
            DataModelInstanceSaver.getInstance().clear();

            LoginOverviewController.openLoginScreen( new Stage() );
            closeWindow();
        }catch( IOException e ){
            System.err.println( "Quit error" );
            e.printStackTrace();
        }
    }

    /**
     Update flight list
     */
    void handleUpdateFlightAction(){
//        todo : Обновить рейсы
    }


    /**
     Update route list
     */
    void handleUpdateRouteAction(){
        try( Socket socket = new Socket( Controller.getInstance().host , Controller.getInstance().port ) ;
             DataOutputStream outputStream = new DataOutputStream( socket.getOutputStream() ) ;
             DataInputStream inputStream = new DataInputStream( socket.getInputStream() ) ){
            UserInformation request = new UserInformation( Controller.getInstance().login ,
                                                           Controller.getInstance().password ,
                                                           Controller.getInstance().base );
            request.setPredicate( PredicateParser.createRoutePredicate( departure.getText() , destination.getText() ) );
            outputStream.writeUTF( mapper.writeValueAsString( request ) );
            Data response = mapper.readerFor( Data.class ).readValue( inputStream.readUTF() );
            DataModelInstanceSaver.getInstance().clear();
            DataModelInstanceSaver.getInstance().getRouteObservableList().setAll( response.getRoutes() );
            DataModelInstanceSaver.getInstance().getFlightObservableList().setAll( response.getFlights() );
        }catch( IOException e ){
            e.printStackTrace();
        }
    }


    /**
     Open search flight view
     */
    private void handleSearchFlightAction(){
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

            thisStage.setOpacity( 0.8 );
            popUp.show();
            thisStage.setOpacity( 1 );
        }catch( IOException e ){
            e.printStackTrace();
        }
    }

    /**
     Search for routes
     */
    void handleSearchRouteAction(){
//        todo: Поиск маршрутов
    }


    private void closeWindow(){
        thisStage.close();
    }

    private Pattern getRoutePattern( String searchText ){
        return Pattern.compile( ".*" + searchText.replaceAll( "\\*" , ".*" ).replaceAll( "\\?" , "." ) + ".*" ,
                                Pattern.CASE_INSENSITIVE );
    }
}







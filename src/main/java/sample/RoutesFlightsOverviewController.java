package sample;

import exceptions.FlightAndRouteException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.DataModel;
import model.Flight;
import model.Route;
import np.com.ngopal.control.AutoFillTextBox;

import java.io.IOException;
import java.io.Serializable;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    @FXML AutoFillTextBox<String>     departure;
    @FXML AutoFillTextBox<String>     destination;
    @FXML TableView<Route>            routeTable;
    @FXML TableColumn<Route, String>  departureColumn;
    @FXML TableColumn<Route, String>  destinationColumn;
    @FXML TableView<Flight>           flightTable;
    @FXML TableColumn<Flight, String> number;
    @FXML TableColumn<Flight, Route>  routeColumnFlight;
    @FXML TextArea                    detailsTextArea;

    private Stage thisStage;
    private FileChooser fileChooser = new FileChooser();

    RoutesFlightsOverviewController( Stage thisStage ){
        this.thisStage = thisStage;
        fileChooser.getExtensionFilters().add( new FileChooser.ExtensionFilter( "Flights and routes" , "*.far" ) );
    }

    /**
     initialization of view
     */
    @FXML
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

        departure.setData( airports );
        departure.getTextbox().textProperty()
                 .addListener( ( observable , oldValue , newValue ) -> searchListeners( newValue , departure ) );
        destination.setData( airports );
        destination.getTextbox().textProperty()
                   .addListener( ( observable , oldValue , newValue ) -> searchListeners( newValue , destination ) );
    }

    private DataModel              dataModel = DataModel.getInstance();
    private ObservableList<String> airports  = dataModel.listAllAirportsWithPredicate( airport -> true ).collect(
            Collectors.collectingAndThen( toList() , FXCollections::observableArrayList ) );

    private void searchListeners( String newValue , AutoFillTextBox<String> textField ){
        if( !newValue.matches( "[\\w\\d\\-_\\?\\*]*" ) ){
            textField.setStyle( "-fx-text-inner-color: red;" );
            textField.setTooltip( new Tooltip( "Acceptable symbols: 0-9, a-z, -, _, ?, *" ) );
        }else{
            textField.setStyle( "-fx-text-inner-color: black;" );
            textField.setTooltip( null );
            Pattern departurePattern = Pattern.compile(
                    ".*" + departure.getText().toUpperCase().replaceAll( "\\*" , ".*" ).replaceAll( "\\?" , "." ) +
                    ".*" );
            Pattern destinationPattern = Pattern.compile(
                    ".*" + destination.getText().toUpperCase().replaceAll( "\\*" , ".*" ).replaceAll( "\\?" , "." ) +
                    ".*" );
            routeTable.setItems( controller.getRoutes().stream().filter(
                    route -> departurePattern.matcher( route.getFrom().toUpperCase() ).matches() &&
                             destinationPattern.matcher( route.getTo().toUpperCase() ).matches() ).collect(
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
    @FXML
    public void handleDeleteRouteButton(){
        Route selectedRoute = routeTable.getSelectionModel().getSelectedItem();
        if( selectedRoute != null ){
            try{
                routeTable.getItems().remove( selectedRoute );
                Controller.model.removeRoute( selectedRoute );
                airports.setAll(
                        dataModel.listAllAirportsWithPredicate( airport -> true ).collect( Collectors.toList() ) );
                Main.changed = true;
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
    public void handleDeleteFlightButton(){
        Flight selectedFlight = flightTable.getSelectionModel().getSelectedItem();
        if( selectedFlight != null ){
            try{
                flightTable.getItems().remove( selectedFlight );
                Controller.model.removeFlight( selectedFlight.getNumber() );
                Main.changed = true;
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

    @FXML
    public void handleAddRouteButton(){
        Parent addRouteWindow;
        try{
            addRouteWindow = FXMLLoader.load( getClass().getResource( "/fxml/AddRoutesOverview.fxml" ) );
            Stage popUp = new Stage();
            Scene scene = new Scene( addRouteWindow );
            scene.getStylesheets().add( getClass().getResource( "/fxml/auto-fill.css" ).toExternalForm() );
            popUp.initModality( Modality.APPLICATION_MODAL );
            popUp.initOwner( thisStage );

            popUp.setTitle( ADD_ROUTE_WINDOW );
            popUp.setScene( scene );
            popUp.setResizable( false );

            thisStage.setOpacity( 0.9 );
            popUp.showAndWait();
            thisStage.setOpacity( 1 );
        }catch( IOException e ){
            e.printStackTrace();
        }
        airports.setAll( dataModel.listAllAirportsWithPredicate( airport -> true ).collect( Collectors.toList() ) );
        routeTable.setItems( controller.getRoutes() );
        routeTable.refresh();
        flightTable.setItems( controller.getFlights() );
        flightTable.refresh();
    }

    @FXML
    public void handleEditRouteButton(){
        Parent editRouteWindow;
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

                Stage popUp = new Stage();
                Scene scene = new Scene( editRouteWindow );
                scene.getStylesheets().add( getClass().getResource( "/fxml/auto-fill.css" ).toExternalForm() );

                popUp.initModality( Modality.APPLICATION_MODAL );
                popUp.initOwner( thisStage );

                popUp.setTitle( EDIT_ROUTE_WINDOW );
                popUp.setScene( scene );
                popUp.setResizable( false );

                ( ( AutoFillTextBox<String> ) ( editRouteWindow.getChildrenUnmodifiable().get( 0 ) ) ).getTextbox()
                                                                                                      .setText(
                                                                                                              selectedRoute
                                                                                                                      .getFrom() );
                ( ( AutoFillTextBox<String> ) ( editRouteWindow.getChildrenUnmodifiable().get( 4 ) ) ).getTextbox()
                                                                                                      .setText(
                                                                                                              selectedRoute
                                                                                                                      .getTo() );

                thisStage.setOpacity( 0.9 );
                popUp.showAndWait();
                thisStage.setOpacity( 1 );
                airports.setAll(
                        dataModel.listAllAirportsWithPredicate( airport -> true ).collect( Collectors.toList() ) );
                routeTable.setItems( controller.getRoutes() );
                routeTable.refresh();
                controller.updateFlights();
                flightTable.setItems( controller.getFlights() );
                flightTable.refresh();
            }catch( IOException e ){
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void handleAddFlightButton(){
        try{
            Parent addFlightWindow = FXMLLoader.load( getClass().getResource( "/fxml/AddFlightsOverview.fxml" ) );

            Stage popUp = new Stage();
            Scene scene = new Scene( addFlightWindow );
            scene.getStylesheets().add( getClass().getResource( "/fxml/auto-fill.css" ).toExternalForm() );


            popUp.initModality( Modality.APPLICATION_MODAL );
            popUp.initOwner( thisStage );

            popUp.setTitle( ADD_FLIGHT_WINDOW );
            popUp.setScene( scene );
            popUp.setResizable( false );

            thisStage.setOpacity( 0.9 );
            popUp.showAndWait();
            thisStage.setOpacity( 1 );
            flightTable.setItems( controller.getFlights() );
            flightTable.refresh();
        }catch( IOException e ){
            e.printStackTrace();
        }
    }

    @FXML
    public void handleEditFlightButton(){
        Parent editFlightWindow;
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

                Stage popUp = new Stage();
                Scene scene = new Scene( editFlightWindow );
                scene.getStylesheets().add( getClass().getResource( "/fxml/auto-fill.css" ).toExternalForm() );

                popUp.initModality( Modality.APPLICATION_MODAL );
                popUp.initOwner( thisStage );

                popUp.setTitle( EDIT_FLIGHT_WINDOW );
                popUp.setScene( scene );
                popUp.setResizable( false );

                ( ( TextField ) ( editFlightWindow.getChildrenUnmodifiable().get( 2 ) ) )
                        .setText( selectedFlight.getNumber() );
                ( ( AutoFillTextBox<String> ) ( editFlightWindow.getChildrenUnmodifiable().get( 3 ) ) ).getTextbox()
                                                                                                       .setText(
                                                                                                               selectedFlight
                                                                                                                       .getPlaneID() );
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
                thisStage.setOpacity( 0.9 );
                popUp.showAndWait();
                thisStage.setOpacity( 1 );

                flightTable.setItems( controller.getFlights() );
                flightTable.refresh();
            }catch( IOException e ){
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void handleSearchFlightButton(){
        if( !controller.isFlightSearchActive() ){
            controller.setFlightSearchActive( true );
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
            flightTable.setItems( controller.getFlights() );
            flightTable.refresh();
        }
    }

    @FXML
    private void handleOpenAction(){
        Optional.ofNullable( fileChooser.showOpenDialog( new Stage() ) ).ifPresent( file -> {
            try{
                Controller.model.importFromFile( file );
                controller.updateRoutes();
                controller.updateFlights();
                Main.changed = false;
                airports.setAll(
                        dataModel.listAllAirportsWithPredicate( airport -> true ).collect( Collectors.toList() ) );
                Main.savingFile = file;
                thisStage.setTitle( file.getName() );
            }catch( IOException | FlightAndRouteException e ){
                e.printStackTrace();
            }
            routeTable.setItems( controller.getRoutes() );
            routeTable.refresh();
            flightTable.setItems( controller.getFlights() );
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
            Controller.model.exportToFile( Main.savingFile );
            Main.changed = false;
        }catch( IOException | FlightAndRouteException e ){
            new Alert( Alert.AlertType.ERROR , e.getMessage() ).show();
        }
    }

    @FXML
    private void handleSaveAsAction(){
        Optional.ofNullable( fileChooser.showSaveDialog( new Stage() ) ).ifPresent( file -> {
            try{
                Controller.model.exportToFile( file );
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
                        Controller.model.mergeData( file ).collect( ArrayList::new , List::add , List::addAll );
                Main.changed = true;
                airports.setAll(
                        dataModel.listAllAirportsWithPredicate( airport -> true ).collect( Collectors.toList() ) );
                Alert alert = new Alert( Alert.AlertType.WARNING );
                alert.setTitle( "Merge results" );
                alert.setHeaderText( "Model have this problems with merge:" );
                StringBuilder errors = new StringBuilder();

                ArrayList<Flight> mergeFlights = new ArrayList<>();
                ArrayList<Route>  mergeRoutes  = new ArrayList<>();
                for( Serializable element : failedInMerge ){
                    errors.append( "-" ).append( element.toString() ).append( "\n" );
                    if( element instanceof Flight && Controller.model.listFlightsWithPredicate( flight -> true )
                                                                     .noneMatch( flight -> flight.equals( element ) ) ){
                        mergeFlights.add( ( Flight ) element );
                    }
                    if( element instanceof Route ){
                        mergeRoutes.add( ( Route ) element );
                    }
                }

                controller.setMergeFlights( FXCollections.observableArrayList( mergeFlights ) );
                controller.setMergeRoutes( FXCollections.observableArrayList( mergeRoutes ) );

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

                controller.updateRoutes();
                controller.updateFlights();
            }catch( IOException | FlightAndRouteException e ){
                new Alert( Alert.AlertType.ERROR , e.getMessage() ).show();
            }
            routeTable.setItems( controller.getRoutes() );
            routeTable.refresh();
            flightTable.setItems( controller.getFlights() );
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







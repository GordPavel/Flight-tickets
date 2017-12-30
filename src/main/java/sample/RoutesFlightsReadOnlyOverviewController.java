package sample;

import exceptions.FlightAndRouteException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.DataModel;
import model.Flight;
import model.Route;

import java.io.IOException;
import java.io.Serializable;
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
public class RoutesFlightsReadOnlyOverviewController extends  RoutesFlightsOverviewController{


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
    @FXML Menu                        fileMenu;
    @FXML Button                      addRouteButton;
    @FXML Button                      editRouteButton;
    @FXML Button                      deleteRouteButton;
    @FXML Button                      addFlightButton;
    @FXML Button                      editFlightButton;
    @FXML Button                      deleteFlightButton;


    private Stage thisStage;

    RoutesFlightsReadOnlyOverviewController(Stage thisStage ){
        this.thisStage = thisStage;
    }

    RoutesFlightsReadOnlyOverviewController(  ){
    }



    /**
     initialization of view
     */
    @FXML
    public void initialize(){

        fileMenu.setVisible(false);
        addRouteButton.setVisible(false);
        editRouteButton.setVisible(false);
        deleteRouteButton.setVisible(false);
        addFlightButton.setVisible(false);
        editFlightButton.setVisible(false);
        deleteFlightButton.setVisible(false);
        routeTable.setPrefWidth(600);
        departureColumn.setPrefWidth(300);
        destinationColumn.setPrefWidth(300);

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

        departure.textProperty()
                 .addListener( ( observable , oldValue , newValue ) -> searchListeners( newValue , departure ) );
        destination.textProperty()
                   .addListener( ( observable , oldValue , newValue ) -> searchListeners( newValue , destination ) );


    }

    private DataModel              dataModel = DataModel.getInstance();
    private ObservableList<String> airports  = dataModel.listAllAirportsWithPredicate( airport -> true ).collect(
            Collectors.collectingAndThen( toList() , FXCollections::observableArrayList ) );

    private void searchListeners( String newValue , TextField textField ){
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

                popUp.show();
            }catch( IOException e ){
                e.printStackTrace();
            }
            flightTable.setItems( controller.getFlights() );
            flightTable.refresh();
        }
    }

    @FXML
    private void handleAboutAction(){
        Alert alert = new Alert( Alert.AlertType.INFORMATION );
        alert.setTitle( "About" );
        alert.setHeaderText( "This program is designed as reference system for flights and routes.\n" +
                "You can use it to search for routes and flights in data base." );
        alert.setContentText(" - Use * and ? in search field instead of many or one unknown symbol;\n");
        alert.showAndWait();
    }



}







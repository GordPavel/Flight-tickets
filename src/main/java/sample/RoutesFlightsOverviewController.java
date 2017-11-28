package sample;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import model.DataModel;
import model.Flight;
import model.Route;
import searchengine.SearchEngine;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class RoutesFlightsOverviewController extends Stage implements Initializable{

    @FXML public TableView<Route> routesTableView;
    @FXML public TextField        searchRouteDeparture;
    @FXML public TextField        searchRouteArrival;
    @FXML public Button           addRouteButton;
    public       Button           editRouteButton;
    public       Button           deleteRouteButton;

    @FXML public TableView<Flight> flightsTableView;
    @FXML public Button            addFlightButton;
    @FXML public Button            editFlightButton;
    @FXML public Button            deleteFlightButton;
    @FXML public Button            searchFlightButton;
    @FXML public TextArea          flightDetailsTextArea;
    @FXML public AnchorPane        mainPane;

    private DataModel    dataModel    = DataModel.getInstance();
    private SearchEngine searchEngine = new SearchEngine( dataModel );

    public RoutesFlightsOverviewController(){
    }

    RoutesFlightsOverviewController( Stage primaryStage ) throws IOException{
        Parent root = FXMLLoader.load( getClass().getResource( "/fxml/RoutesFlightsOverview.fxml" ) );
        primaryStage.setTitle( "Routes and flights" );
        primaryStage.setScene( new Scene( root , 700 , 500 ) );
        primaryStage.setResizable( false );
        primaryStage.show();
    }

    @FXML
    @Override
    public void initialize( URL location , ResourceBundle resources ){
//        Routes tab
        routesTableView.setEditable( false );
        routesTableView.getColumns().get( 0 ).setCellValueFactory( new PropertyValueFactory<>( "from" ) );
        routesTableView.getColumns().get( 1 ).setCellValueFactory( new PropertyValueFactory<>( "to" ) );
        updateRoutesSearch( null , null );
        editRouteButton.setDisable( true );
        deleteRouteButton.setDisable( true );
        searchRouteDeparture.textProperty().addListener(
                ( observable , oldValue , newValue ) -> updateRoutesSearch( newValue , searchRouteArrival.getText() ) );
        searchRouteArrival.textProperty().addListener(
                ( observable , oldValue , newValue ) -> updateRoutesSearch( searchRouteDeparture.getText() ,
                                                                            newValue ) );
//        set edit and remove clickable
        routesTableView.setOnMouseClicked( event -> {
            editRouteButton.setDisable( routesTableView.getSelectionModel().getSelectedCells().isEmpty() );
            deleteRouteButton.setDisable( routesTableView.getSelectionModel().getSelectedCells().isEmpty() );
        } );

//        Flights tab
        flightsTableView.setEditable( false );
        flightsTableView.getColumns().get( 0 ).setCellValueFactory( new PropertyValueFactory<>( "number" ) );
        flightsTableView.getColumns().get( 1 ).setCellValueFactory( new PropertyValueFactory<>( "from" ) );
        flightsTableView.getColumns().get( 2 ).setCellValueFactory( new PropertyValueFactory<>( "to" ) );
        updateFlightsList( null , null , null , null , null , null , null , null );
        editFlightButton.setDisable( true );
        deleteFlightButton.setDisable( true );
        flightsTableView.setOnMouseClicked( event -> {
            editFlightButton.setDisable( flightsTableView.getSelectionModel().getSelectedCells().isEmpty() );
            deleteFlightButton.setDisable( flightsTableView.getSelectionModel().getSelectedCells().isEmpty() );
            Flight selected = flightsTableView.getSelectionModel().getSelectedItem();
            flightDetailsTextArea.setText( selected.toString() );
        } );
        searchFlightButton.setOnMouseClicked( event -> {
            try{
                this.openSearchPane();
            }catch( IOException e ){
                e.printStackTrace();
            }
        } );
    }

    private void updateRoutesSearch( String from , String to ){
        routesTableView.setItems( searchEngine.findRoute( from , to )
                                              .collect( FXCollections::observableArrayList , List::add ,
                                                        List::addAll ) );
    }

    void updateFlightsList( String number , String from , String to , String plane , Date startDepartureDateRange ,
                            Date endDepartureDateRange , Date startArriveDateRange , Date endArriveDateRange ){
        flightsTableView.setItems( searchEngine.findFlight( number , from , to , plane , startDepartureDateRange ,
                                                            endDepartureDateRange , startArriveDateRange ,
                                                            endArriveDateRange )
                                               .collect( FXCollections::observableArrayList , List::add ,
                                                         List::addAll ) );
    }


    void openSearchPane() throws IOException{
        new SearchFlightsOverView( this );
    }

}

package sample;

import com.fasterxml.jackson.databind.ObjectMapper;
import exceptions.FlightAndRouteException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.DataModelInstanceSaver;
import model.Flight;
import model.Route;
import transport.Data;
import transport.ListChangeAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Optional;

/**
 Controller for routes and flights view, client write application.
 disables and hides all buttons/menus, that write client must not see
 */
class RoutesFlightsWriteOverviewController extends RoutesFlightsOverviewController{

    RoutesFlightsWriteOverviewController( Stage thisStage ){
        super( thisStage );
    }

    /**
     initialization of view
     Hiding menus, add listeners, start thread
     */
    @Override
    @FXML
    void initialize(){
        super.initialize();
        updateFlightButton.setLayoutX( updateFlightButton.getLayoutX() - 37 );
        searchFlightButton.setLayoutX( searchFlightButton.getLayoutX() - 37 );

        routeConnectLabel.setVisible(false);
        flightConnectLabel.setVisible(false);

        Controller.getInstance().setThread( new WriteThread() );
        Controller.getInstance().startThread();

        addRouteButton.setOnAction( event -> handleAddRouteAction() );
        editRouteButton.setOnAction( event -> handleEditRouteAction() );
        deleteRouteButton.setOnAction( event -> handleDeleteRouteAction() );
        updateRouteButton.setOnAction(event -> handleUpdateRouteAction());

        addFlightButton.setOnAction( event -> handleAddFlightAction() );
        editFlightButton.setOnAction( event -> handleEditFlightAction() );
        deleteFlightButton.setOnAction( event -> handleDeleteFlightAction() );
        updateFlightButton.setOnAction(event -> handleUpdateFlightAction());
    }

    /**
     Open add route view
     */
    private void handleAddRouteAction(){
        try{
            Stage popUp = new Stage();
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
     Open edit route view
     */
    private void handleEditRouteAction(){
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
     Delete route from local DB
     */
    private void handleDeleteRouteAction(){
        Optional.ofNullable( routeTable.getSelectionModel().getSelectedItem() ).ifPresent( selectedRoute -> {
            try{
                DataModelInstanceSaver.getInstance().removeRoute( selectedRoute );
                Controller.getInstance().changed = true;
            }catch( FlightAndRouteException e ){
                showModelAlert( e );
            }
            try {
                OutputStream outClient = Controller.getInstance().getClientSocket().getOutputStream();
                InputStream inClient = Controller.getInstance().getClientSocket().getInputStream();
                Data data = new Data();
                ObjectMapper mapper = new ObjectMapper();

                ArrayList<Route> routes = new ArrayList<>();
                routes.add( selectedRoute );

                ArrayList<ListChangeAdapter> changes = new ArrayList<>();
                changes.add( ListChangeAdapter.removeRoute( routes ) );

                Controller.getInstance().getUserInformation().setChanges( changes ) ;

                mapper.writeValue( outClient, Controller.getInstance().getUserInformation() );
                // get Data
                data = mapper.readValue( Controller.getInstance().getClientSocket().getInputStream() , Data.class );
                Controller.getInstance().getUserInformation().setChanges(null);
            }catch( IOException e ){
                System.out.println("Connection problem");
                System.out.println( e.getMessage() );
            }
        } );
        /*
          TODO: set message to delete route to server
         */
    }

    /**
     Open add flight view
     */
    private void handleAddFlightAction(){
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
     Open edit flight view
     */
    private void handleEditFlightAction(){
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


    private void handleDeleteFlightAction(){
        Optional.ofNullable( flightTable.getSelectionModel().getSelectedItem() ).ifPresent( selectedFlight -> {
            try{
                DataModelInstanceSaver.getInstance().removeFlight( selectedFlight.getNumber() );
            }catch( FlightAndRouteException e ){
                showModelAlert( e );
            }
            try {
                OutputStream outClient = Controller.getInstance().getClientSocket().getOutputStream();
                InputStream inClient = Controller.getInstance().getClientSocket().getInputStream();
                Data data = new Data();
                ObjectMapper mapper = new ObjectMapper();

                ArrayList<Flight> flights = new ArrayList<>();
                flights.add( selectedFlight );

                ArrayList<ListChangeAdapter> changes = new ArrayList<>();
                changes.add( ListChangeAdapter.removeFlight( flights ) );

                Controller.getInstance().getUserInformation().setChanges( changes ) ;

                mapper.writeValue( outClient, Controller.getInstance().getUserInformation() );
                // get Data

                Controller.getInstance().getUserInformation().setChanges(null);
            }catch( IOException e ){
                System.out.println("Connection problem");
                System.out.println( e.getMessage() );
            }
        } );
        /*
          TODO: add message to server to delete flight
         */
    }

    public void handleUpdateRouteAction(){

        requestUpdate(route -> true);
    }

    public void handleUpdateFlightAction(){

        requestUpdate(flight -> true);
    }
}

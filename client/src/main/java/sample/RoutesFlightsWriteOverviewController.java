package sample;

import exceptions.FlightAndRouteException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.DataModelInstanceSaver;

import java.io.IOException;
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
     Hiding menus, add listners, start thread
     */
    @Override
    @FXML
    void initialize(){
        super.initialize();
        updateFlightButton.setLayoutX( updateFlightButton.getLayoutX() - 37 );
        searchFlightButton.setLayoutX( searchFlightButton.getLayoutX() - 37 );

        Controller.getInstance().setThread( new WriteThread() );
        thisStage.setOnCloseRequest( event -> Controller.getInstance().stopThread() );
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
        } );
        /**
         * TODO: set message to delete route to server
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
        } );
        /**
         * TODO: add message to server to delete flight
         */
    }

    public void handleUpdateRouteAction(){

        /**
         * TODO: request all routes from server
         */
    }

    public void handleUpdateFlightAction(){

        /**
         * TODO: request all flights from server
         */
    }
}

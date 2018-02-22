package sample;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import transport.Data;
import transport.ListChangeAdapter;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

/**
 Controller for routes and flights view, client write application.
 disables and hides all buttons/menus, that write client must not see
 */
@SuppressWarnings( "Duplicates" )
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

        routeConnectLabel.setVisible( false );
        flightConnectLabel.setVisible( false );
        fileMenu.setVisible( false );

        Controller.getInstance().setThread( new WriteThread( this ) );
        Controller.getInstance().startThread();

        addRouteButton.setOnAction( event -> handleAddRouteAction() );
        editRouteButton.setOnAction( event -> handleEditRouteAction() );
        deleteRouteButton.setOnAction( event -> handleDeleteRouteAction() );
        updateRouteButton.setOnAction( event -> handleUpdateRouteAction() );

        addFlightButton.setOnAction( event -> handleAddFlightAction() );
        editFlightButton.setOnAction( event -> handleEditFlightAction() );
        deleteFlightButton.setOnAction( event -> handleDeleteFlightAction() );
        updateFlightButton.setOnAction( event -> handleUpdateFlightAction() );

        mergeMenuButton.setOnAction( event -> handleMergeAction() );
    }

    private void handleMergeAction(){
//        todo : запрос на слияние серверу
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
            thisStage.setOpacity( 0.9 );
            popUp.showAndWait();
            thisStage.setOpacity( 1 );
            if( !changes.isEmpty() ){
                Data data = FaRExchanger.exchange();
                processUpdates( data );
            }
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
                AddAndEditRoutesOverviewController
                        controller =
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
                if( !changes.isEmpty() ){
                    Data data = FaRExchanger.exchange();
                    processUpdates( data );
                }
            }catch( IOException e ){
                e.printStackTrace();
            }
        } );
    }

    /**
     Delete route from local DB
     */
    private void handleDeleteRouteAction(){

        Alert alert = new Alert( Alert.AlertType.CONFIRMATION );
        alert.setTitle( " Delete a route " );
        alert.setHeaderText( " Are you sure that you want to delete this route? " );
        alert.setContentText( routeTable.getSelectionModel().getSelectedItem().toString() );

        alert.showAndWait().filter( option -> option == ButtonType.OK ).ifPresent( option -> {
            Optional.ofNullable( routeTable.getSelectionModel().getSelectedItem() ).ifPresent( selectedRoute -> {
                try{
                    DataOutputStream
                            outClient =
                            new DataOutputStream( Controller.getInstance().getClientSocket().getOutputStream() );
                    ObjectMapper mapper = new ObjectMapper();

                    Controller.getInstance()
                              .getUserInformation()
                              .setChanges( Collections.singletonList( ListChangeAdapter.removeRoute( Collections.singletonList(
                                      selectedRoute ) ) ) );

                    outClient.writeUTF( mapper.writeValueAsString( Controller.getInstance().getUserInformation() ) );
                    RoutesFlightsOverviewController.getChanges()
                                                   .add( ListChangeAdapter.removeRoute( Collections.singletonList(
                                                           selectedRoute ) ) );
                    Controller.getInstance().getUserInformation().setChanges( null );
                    Data data = FaRExchanger.exchange();
                    processUpdates( data );
                }catch( IOException e ){
                    System.out.println( "Connection problem" );
                    System.out.println( e.getMessage() );
                }
            } );
        } );

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
            if( !changes.isEmpty() ){
                Data data = FaRExchanger.exchange();
                processUpdates( data );
            }
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
                AddAndEditFlightsOverviewController
                        controller =
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
                if( !changes.isEmpty() ){
                    Data data = FaRExchanger.exchange();
                    processUpdates( data );
                }
            }catch( IOException e ){
                e.printStackTrace();
            }
        } );
    }

    /**
     * Deleting flight
     */
    private void handleDeleteFlightAction(){
        Alert alert = new Alert( Alert.AlertType.CONFIRMATION );
        alert.setTitle( " Delete a flight " );
        alert.setHeaderText( " Are you sure that you want to delete this flight? " );
        alert.setContentText( flightTable.getSelectionModel().getSelectedItem().toString() );

        alert.showAndWait().ifPresent( option -> {
            Optional.ofNullable( flightTable.getSelectionModel().getSelectedItem() ).ifPresent( selectedFlight -> {
                try{
                    DataOutputStream
                            outClient =
                            new DataOutputStream( Controller.getInstance().getClientSocket().getOutputStream() );
                    ObjectMapper mapper = new ObjectMapper();

                    Controller.getInstance()
                              .getUserInformation()
                              .setChanges( Collections.singletonList( ListChangeAdapter.removeFlight( Collections.singletonList(
                                      selectedFlight ) ) ) );

                    outClient.writeUTF( mapper.writeValueAsString( Controller.getInstance().getUserInformation() ) );

                    RoutesFlightsOverviewController.getChanges()
                                                   .add( ListChangeAdapter.removeFlight( Collections.singletonList(
                                                           selectedFlight ) ) );

                    Data data = FaRExchanger.exchange();
                    processUpdates( data );
                    Controller.getInstance().getUserInformation().setChanges( null );
                }catch( IOException e ){
                    System.out.println( "Connection problem" );
                    System.out.println( e.getMessage() );
                }
            } );
        } );
    }

    /**
     * Processing received from server data to find messages about changes that was made by user
     * @param data - Data, that contain information about user`s update (accepted or error)
     */
    static void processUpdates( Data data ){

        if( data.hasNotException() ){
            changes.forEach( listChangeAdapter -> {
                for( ListChangeAdapter listChangeAdapter1 : data.getChanges() ){
                    if( listChangeAdapter.equalsEntities( listChangeAdapter1 ) ){
                        Alert alert = new Alert( Alert.AlertType.INFORMATION );
                        alert.setTitle( " Confirmation " );
                        alert.setHeaderText( " Changes on server " );
                        alert.setContentText( listChangeAdapter.getUpdate() );
                        alert.showAndWait();
                        changes.remove( listChangeAdapter );
                    }
                }
            } );
        }else{
            Alert alert = new Alert( Alert.AlertType.ERROR );
            alert.setTitle( "Error" );
            alert.setHeaderText( data.getException().getMessage() );
            alert.setContentText( changes.get( 0 ).getUpdate() );
            alert.showAndWait();
            changes.remove( changes.get( 0 ) );
        }
    }

}

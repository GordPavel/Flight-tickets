package sample;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.DataModelInstanceSaver;
import model.FlightOrRoute;
import org.danekja.java.util.function.serializable.SerializablePredicate;
import transport.Data;
import transport.ListChangeAdapter;
import transport.PredicateParser;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

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

        routeConnectLabel.setVisible( false );
        flightConnectLabel.setVisible( false );

        Controller.getInstance().setThread( new WriteThread(this) );
        Controller.getInstance().startThread();

        addRouteButton.setOnAction( event -> handleAddRouteAction() );
        editRouteButton.setOnAction( event -> handleEditRouteAction() );
        deleteRouteButton.setOnAction( event -> handleDeleteRouteAction() );
        updateRouteButton.setOnAction( event -> handleUpdateRouteAction() );

        addFlightButton.setOnAction( event -> handleAddFlightAction() );
        editFlightButton.setOnAction( event -> handleEditFlightAction() );
        deleteFlightButton.setOnAction( event -> handleDeleteFlightAction() );
        updateFlightButton.setOnAction( event -> handleUpdateFlightAction() );
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
                Stage popUp = new Stage();
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

        Alert alert = new Alert( Alert.AlertType.CONFIRMATION );
        alert.setTitle( " Delete a route " );
        alert.setHeaderText( " Are you sure that you want to delete this route? " );
        alert.setContentText( routeTable.getSelectionModel().getSelectedItem().toString() );

        Optional<ButtonType> option = alert.showAndWait();

        if ( option.get() == ButtonType.OK ){

            Optional.ofNullable( routeTable.getSelectionModel().getSelectedItem() ).ifPresent( selectedRoute -> {
            try{
                DataOutputStream outClient =
                        new DataOutputStream ( Controller.getInstance().getClientSocket().getOutputStream() );
                ObjectMapper mapper = new ObjectMapper();

                Controller.getInstance()
                          .getUserInformation()
                          .setChanges( Collections.singletonList( ListChangeAdapter.removeRoute( Collections.singletonList(
                                  selectedRoute ) ) ) );

                outClient.writeUTF( mapper.writeValueAsString( Controller.getInstance().getUserInformation() ) );
                RoutesFlightsOverviewController.getChanges().add ( ListChangeAdapter.removeRoute( Collections.singletonList( selectedRoute ) ) );

//                DataInputStream inClient =
//                       new DataInputStream ( Controller.getInstance().getClientSocket().getInputStream() );
//
//                // get Data
//                Data data = mapper.readerFor( Data.class ).readValue( inClient.readUTF() );
//                changes.forEach( listChangeAdapter -> {
//                            for ( ListChangeAdapter listChangeAdapter1 : data.getChanges() ) {
//                               if ( listChangeAdapter.equals(listChangeAdapter1) ) {
//                                    if ( data.hasNotException() ) {
//
//                                        Alert alert1 = new Alert( Alert.AlertType.INFORMATION );
//                                        alert1.setTitle( " Delete a route" );
//                                        alert1.setHeaderText( " Deleting a route was successful! " );
//                                        alert1.setContentText( listChangeAdapter.getUpdate() );
//                                        changes.remove( listChangeAdapter );
//                                    } else{
//                                        Alert alert1 = new Alert( Alert.AlertType.ERROR );
//                                        alert1.setTitle( " Delete a route" );
//                                        alert1.setHeaderText( " Error while deleting a route on a server! " );
//                                        alert1.setContentText( listChangeAdapter.getUpdate() );
//                                    }
//                               }
//                            }
//                        }
//                );

                Controller.getInstance().getUserInformation().setChanges( null );
            }catch( IOException e ){
                System.out.println( "Connection problem" );
                System.out.println( e.getMessage() );
            }
        } );
        }
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
            Stage popUp = new Stage();
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
                Stage popUp = new Stage();
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
        Alert alert = new Alert( Alert.AlertType.CONFIRMATION );
        alert.setTitle( " Delete a flight " );
        alert.setHeaderText( " Are you sure that you want to delete this flight? " );
        alert.setContentText( flightTable.getSelectionModel().getSelectedItem().toString() );

        Optional<ButtonType> option = alert.showAndWait();

        if ( option.get() == ButtonType.OK ){
        Optional.ofNullable( flightTable.getSelectionModel().getSelectedItem() ).ifPresent( selectedFlight -> {
            try{
                DataOutputStream outClient =
                        new DataOutputStream ( Controller.getInstance().getClientSocket().getOutputStream() );
                ObjectMapper mapper = new ObjectMapper();

                Controller.getInstance()
                          .getUserInformation()
                          .setChanges( Collections.singletonList( ListChangeAdapter.removeFlight( Collections.singletonList(
                                  selectedFlight ) ) ) );

                outClient.writeUTF( mapper.writeValueAsString( Controller.getInstance().getUserInformation() ) );

                RoutesFlightsOverviewController.getChanges().add ( ListChangeAdapter.removeFlight( Collections.singletonList( selectedFlight ) ) );

//                               DataInputStream inClient =
//                        new DataInputStream ( Controller.getInstance().getClientSocket().getInputStream() );

                // get Data
                //Data data = mapper.readerFor( Data.class ).readValue( inClient.readUTF() );
//                changes.forEach( listChangeAdapter -> {
//                    for ( ListChangeAdapter listChangeAdapter1 : data.getChanges() ) {
//                        if ( listChangeAdapter.equals(listChangeAdapter1) ) {
//                            if ( data.hasNotException() ) {
//                                Alert alert1 = new Alert( Alert.AlertType.INFORMATION );
//                                alert1.setTitle( " Delete a flight" );
//                                alert1.setHeaderText( " Deleting a flight was successful! " );
//                                alert1.setContentText( listChangeAdapter.getUpdate() );
//                                changes.remove( listChangeAdapter );
//                            } else{
//                                Alert alert1 = new Alert( Alert.AlertType.ERROR );
//                                alert1.setTitle( " Delete a flight" );
//                                alert1.setHeaderText( " Error while deleting a flight on a server! " );
//                                alert1.setContentText( listChangeAdapter.getUpdate() );
//                                changes.remove( listChangeAdapter );
//                            }
//                        }
//                    }
//                }
//                );

                Controller.getInstance().getUserInformation().setChanges( null );
            }catch( IOException e ){
                System.out.println( "Connection problem" );
                System.out.println( e.getMessage() );
            }
        } );}
        /*
          TODO: add message to server to delete flight
         */
    }

}

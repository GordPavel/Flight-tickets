package sample;

import exceptions.FlightAndRouteException;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.DataModelInstanceSaver;

import java.io.IOException;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 Controller for routes and flights view, client write application.
 disables and hides all buttons/menus, that write client must not see
 */
class RoutesFlightsWriteOverviewController extends RoutesFlightsOverviewController{

    @FXML Button updateFlightButton;
    @FXML Button searchFlightButton;

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
    }

    /**
     deleting route from DB
     */
    @Override
    @FXML
    void handleDeleteRouteButton(){
        Optional.ofNullable( routeTable.getSelectionModel().getSelectedItem() ).ifPresent( selectedRoute -> {
            try{
                DataModelInstanceSaver.getInstance().removeRoute( selectedRoute );
                // TODO: put here request to server to delete route
            }catch( FlightAndRouteException e ){
                showModelAlert( e );
            }
        } );
    }

    /**
     deleting flight from DB
     */
    @Override
    @FXML
    void handleDeleteFlightButton(){
        Optional.ofNullable( flightTable.getSelectionModel().getSelectedItem() ).ifPresent( selectedFlight -> {
            try{
                DataModelInstanceSaver.getInstance().removeFlight( selectedFlight.getNumber() );
                ClientMain.changed = true;
                // TODO: put here request to server to delete flight
            }catch( FlightAndRouteException e ){
                showModelAlert( e );
            }
        } );
    }

    /**
     Update flight list
     */
    @FXML
    public void handleUpdateFlightButton(){
        // TODO: put here request to server to update DB about routes
    }

    /**
     Update route list
     */
    @FXML
    public void handleUpdateRouteButton(){
        // TODO: put here request to server to update DB about flights
    }

    /**
     Search for routes
     */
    @FXML
    public void handleSearchRouteButton(){
        // TODO: put here request to server to update DB about flights

    }


}

package sample;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.DataModelInstanceSaver;
import model.Flight;
import model.Route;

import java.io.IOException;
import java.util.regex.Pattern;


/**
 Controller for routes and flights view, client read-only application.
 disables and hides all buttons/menus, that read-only client must not see. Allows to search in big data base
 Shows the information about all routes and flights
 */
class RoutesFlightsReadOnlyOverviewController extends RoutesFlightsOverviewController{

    @FXML Menu                        fileMenu;
    @FXML Button                      addRouteButton;
    @FXML Button                      addFlightButton;
    @FXML Button                      updateFlightButton;
    @FXML Button                      updateRouteButton;
    @FXML Button                      searchRouteButton;

    public RoutesFlightsReadOnlyOverviewController( Stage thisStage ){
        super( thisStage );
    }

    /**
     initialization of view
     hide, what client doesn't must see, set listeners, start thread
     */
    @Override
    @FXML
    void initialize(){
//        main settings are in super class
        super.initialize();
        fileMenu.setVisible( false );
        addRouteButton.setVisible( false );
        editRoute.setVisible( false );
        deleteRoute.setVisible( false );
        addFlightButton.setVisible( false );
        editFlight.setVisible( false );
        deleteFlight.setVisible( false );
        updateFlightButton.setVisible( false );
        updateRouteButton.setVisible( false );
        searchRouteButton.setVisible( false );


        Controller.getInstance().setThread( new ReadOnlyThread() );
        thisStage.setOnCloseRequest( event -> Controller.getInstance().stopThread() );
        Controller.getInstance().startThread();
    }

    /**
     * About menu handler, shows information about app to user.
     */
    @Override
    @FXML
    void handleAboutAction(){
        Alert alert = new Alert( Alert.AlertType.INFORMATION );
        alert.setTitle( "About" );
        alert.setHeaderText( "This program is designed as reference system for flights and routes.\n" +
                             "You can use it to search for routes and flights in data base." );
        alert.setContentText( " - Use * and ? in search field instead of many or one unknown symbol;\n" );
        alert.showAndWait();
    }
}







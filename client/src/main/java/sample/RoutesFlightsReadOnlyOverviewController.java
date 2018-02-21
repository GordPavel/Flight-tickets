package sample;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.util.Timer;
import java.util.TimerTask;


/**
 Controller for routes and flights view, client read-only application.
 disables and hides all buttons/menus, that read-only client must not see. Allows to search in big data base
 Shows the information about all routes and flights
 */
class RoutesFlightsReadOnlyOverviewController extends RoutesFlightsOverviewController{

//    todo: Поток запросов к серверу через равные промежутки времени

    final Timer timer = new Timer();
    TimerTask task = new TimerTask(){
        @Override
        public void run(){
            System.out.println( "test" );
        }
    };

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
        editRouteButton.setVisible( false );
        deleteRouteButton.setVisible( false );
        addFlightButton.setVisible( false );
        editFlightButton.setVisible( false );
        deleteFlightButton.setVisible( false );
        updateFlightButton.setVisible( false );
        updateRouteButton.setVisible( false );
        searchRouteButton.setVisible( false );
        routeTable.setPrefWidth( 600 );
        departureColumn.setPrefWidth( 300 );
        destinationColumn.setPrefWidth( 300 );
    }

    public void restartTask( TimerTask timerTask ){
        task.cancel();
        task = timerTask;
        timer.schedule( task , 5000 );
    }

    /**
     About menu handler, shows information about app to user.
     */
    private void handleAboutAction(){
        Alert alert = new Alert( Alert.AlertType.INFORMATION );
        alert.setTitle( "About" );
        alert.setHeaderText( "This program is designed as reference system for flights and routes.\n" +
                             "You can use it to search for routes and flights in data base." );
        alert.setContentText( " - Use * and ? in search field instead of many or one unknown symbol;\n" );
        alert.showAndWait();
    }


}







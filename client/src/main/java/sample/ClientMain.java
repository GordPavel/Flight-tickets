package sample;

import exceptions.FlightAndRouteException;
import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.stage.Stage;


public class ClientMain extends Application{

    public static void showWarningByError( FlightAndRouteException e ){
        showWarning( "Error" , "Server error" , e.getMessage() );
    }

    static void showWarning( String title , String header , String content ){
        Alert alert = new Alert( Alert.AlertType.WARNING );
        alert.setTitle( title );
        alert.setHeaderText( header );
        alert.setContentText( content );
        alert.showAndWait();
    }

    public static void main( String[] args ){
        launch( args );
    }

    @Override
    public void start( Stage primaryStage ){
        LoginOverviewController.openLoginScreen( primaryStage );
    }

}

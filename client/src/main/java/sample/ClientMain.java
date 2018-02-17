package sample;

import exceptions.FlightAndRouteException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;


public class ClientMain extends Application{

    @Override
    public void start( Stage primaryStage ) throws Exception{
        Stage loginStage = new Stage();
        FXMLLoader loader = new FXMLLoader( getClass().getResource( "/fxml/LoginOverview.fxml" ) );
        LoginOverviewController controller = new LoginOverviewController( loginStage );
        loader.setController( controller );
        loginStage.setTitle( "Login" );
        Scene scene = new Scene( loader.load() );
        loginStage.setScene( scene );
        loginStage.setResizable( false );
        loginStage.show();
    }

    static void showWarningServerError( FlightAndRouteException e ){
        Alert alert = new Alert( Alert.AlertType.WARNING );
        alert.setTitle( "Error" );
        alert.setHeaderText( "Server error" );
        alert.setContentText( e.getMessage() );
        alert.showAndWait();
    }

    public static void main( String[] args ){
        launch( args );
    }

}

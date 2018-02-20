package sample;

import exceptions.FlightAndRouteException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
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

    static void showError ( String header , String content ){

        Alert alert = new Alert( Alert.AlertType.ERROR);
        alert.setTitle( "Error" );
        alert.setHeaderText( header );
        alert.setContentText( content );
        alert.showAndWait();
    }

    static void showInformation ( String title , String header , String content ){

        Alert alert = new Alert( Alert.AlertType.INFORMATION );
        alert.setTitle( title );
        alert.setHeaderText( header );
        alert.setContentText( content );
        alert.showAndWait();
    }

    public static void main( String[] args ){
        launch( args );
    }

    @Override
    public void start( Stage primaryStage ) throws Exception{
        FXMLLoader loader = new FXMLLoader( getClass().getResource( "/fxml/LoginOverview.fxml" ) );
        LoginOverviewController controller = new LoginOverviewController( primaryStage );
        loader.setController( controller );
        primaryStage.setTitle( "Login" );
        Scene scene = new Scene( loader.load() );
        primaryStage.setScene( scene );
        primaryStage.setResizable( false );
        primaryStage.show();
    }

}

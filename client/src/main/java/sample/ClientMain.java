package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.net.Socket;

public class ClientMain extends Application{

    static Boolean changed = false;
    static File savingFile;

    private static Socket clientSocket;

    public static Socket getClientSocket(){
        return clientSocket;
    }

    public static void setClientSocket( Socket clientSocket ){
        ClientMain.clientSocket = clientSocket;
    }

    @Override
    public void start( Stage primaryStage ) throws Exception{

        FXMLLoader loader = new FXMLLoader( getClass().getResource( "/fxml/ConnectionOverview.fxml" ) );
        ConnectionOverviewController controller = new ConnectionOverviewController( primaryStage );
        loader.setController( controller );
        primaryStage.setTitle( "Connection to server" );
        Scene scene = new Scene( loader.load() );
        primaryStage.setScene( scene );
        primaryStage.setResizable( false );
        primaryStage.show();

    }

    public static void main( String[] args ){
        launch( args );
    }

}

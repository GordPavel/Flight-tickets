package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import transport.UserInformation;

import java.io.File;
import java.net.Socket;


public class ClientMain extends Application{

    static Boolean changed = false;
    static File savingFile;

    private static Socket clientSocket;
    private static UserInformation userInformation;

    public static Socket getClientSocket(){
        return clientSocket;
    }

    public static void setClientSocket( Socket clientSocket ){
        ClientMain.clientSocket = clientSocket;
    }

    public static void setUserInformation(UserInformation userInformation) {
        ClientMain.userInformation = userInformation;
    }

    public static UserInformation getUserInformation() {
        return userInformation;
    }

    @Override
    public void start( Stage primaryStage ) throws Exception{

        Stage                   loginStage = new Stage();
        FXMLLoader              loader     = new FXMLLoader( getClass().getResource( "/fxml/LoginOverview.fxml" ) );
        LoginOverviewController controller = new LoginOverviewController( loginStage );
        loader.setController( controller );
        loginStage.setTitle( "Login" );
        Scene scene = new Scene( loader.load() );
        loginStage.setScene( scene );
        loginStage.setResizable( false );
        loginStage.show();

    }

    public static void main( String[] args ){
        launch( args );
    }

}

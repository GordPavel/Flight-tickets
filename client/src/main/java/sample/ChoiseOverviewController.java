package sample;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.regex.Pattern;

public class ChoiseOverviewController {

    private Stage thisStage;

    ChoiseOverviewController(Stage thisStage ){
        this.thisStage = thisStage;
    }

    @FXML
    Button connectButton;
    @FXML
    Button selectButton;


    /**
     * initializing of view
     */
    @FXML
    public void initialize(){

        System.out.println("test");
    }


    /**
     * Cancel button action handler. Closes window, when button pushed
     * @param actionEvent
     */
    @FXML
    public void handleCancelAction( ActionEvent actionEvent ){
        closeWindow( actionEvent );
    }



    @FXML
    public void handleSelectAction( ActionEvent actionEvent ){



        /**
         * TODO: Select database
         *
         * Send your login and password to server. true? go below : retry message
         * Add view with table of available DB...
         * Load db to datamodel and execute code below
         */


        /**
         * if write
         */
        try {
            Stage primaryStage = new Stage();
            FXMLLoader                      loader     =
                    new FXMLLoader( getClass().getResource( "/fxml/RoutesFlightsOverview.fxml" ) );
            RoutesFlightsWriteOverviewController controller = new RoutesFlightsWriteOverviewController( primaryStage );
            loader.setController( controller );
            primaryStage.setTitle( "Information system about flights and routes" );
            Scene scene = new Scene( loader.load() , 700 , 500 );
            primaryStage.setScene( scene );
            primaryStage.setResizable( false );
            primaryStage.show();
            closeWindow( actionEvent );
        } catch (IOException e)
        {
            System.out.println("load problem");
            System.out.println(e.getMessage());
        }



    }

    private void closeWindow( Event event ){
        Stage stage = ( Stage ) ( (Parent) event.getSource() ).getScene().getWindow();
        stage.close();
    }




}

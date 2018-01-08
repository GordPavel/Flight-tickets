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
import java.net.Socket;
import java.util.regex.Pattern;

public class LoginOverviewController {

    private Stage thisStage;

    LoginOverviewController(Stage thisStage ){
        this.thisStage = thisStage;
    }

    @FXML
    Button connectButton;
    @FXML
    Button logInButton;
    @FXML
    TextField loginTextField;
    @FXML
    PasswordField passwordField;


    /**
     * initializing of view
     */
    @FXML
    public void initialize(){


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
    public void handleLogInAction( ActionEvent actionEvent ){

        Pattern pattern = Pattern.compile("^[\\w\\d]+$");
        Boolean userCanWrite = false;

        if (!(pattern.matcher(loginTextField.getText()).matches()&&pattern.matcher(passwordField.getText()).matches())){
            Alert alert = new Alert( Alert.AlertType.WARNING );
            alert.setTitle( "Error while log in " );
            alert.setHeaderText( "Login or pasword incorrect" );
            alert.setContentText( "Please check them and try again." );
            alert.showAndWait();
        }

        /**
         * TODO: Receiving list of DB
         *
         * Send your login and password to server. true? go below : retry message
         * Add view with table of available DB...
         * Load db to datamodel and execute code below
         */


        /**
         * if ok
         */
        try {
            Stage primaryStage = new Stage();
            FXMLLoader                      loader     =
                    new FXMLLoader( getClass().getResource( "/fxml/ChoiseOverview.fxml" ) );
            ChoiseOverviewController controller = new ChoiseOverviewController( primaryStage );
            loader.setController( controller );
            primaryStage.setTitle( "Select DB" );
            Scene scene = new Scene( loader.load() );
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

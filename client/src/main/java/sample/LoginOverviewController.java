package sample;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import transport.Data;
import transport.UserInformation;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

@SuppressWarnings( "WeakerAccess" )
// Class only connects with server and gets list of available bases for specified user
class LoginOverviewController{

    @FXML   Button        cancelButton;
    @FXML   Button        logInButton;
    @FXML   TextField     loginTextField;
    @FXML   PasswordField passwordField;
    @FXML   TextField     ipTextField;
    @FXML   TextField     portTextField;
    private Stage         thisStage;
    private SimpleBooleanProperty portCorrect = new SimpleBooleanProperty( true );

    LoginOverviewController( Stage thisStage ){
        this.thisStage = thisStage;
    }

    /**
     initializing of view
     */
    @FXML
    private void initialize(){
        ipTextField.setText( "85.113.55.203" );
        portTextField.setText( "5555" );
        portTextField.textProperty().addListener( ( observable , oldValue , newValue ) -> {
            portCorrect.setValue( newValue.matches( "^\\d+$" ) );
        } );
        ipTextField.setOnKeyReleased( this::logIn );
        loginTextField.setOnKeyReleased( this::logIn );
        portTextField.setOnKeyReleased( this::logIn );
        passwordField.setOnKeyReleased( this::logIn );
        logInButton.disableProperty().bind( portCorrect.not() );
    }

    private void logIn( KeyEvent event ){
        if( event.getCode() == KeyCode.ENTER && portCorrect.get() ){
            handleLogInAction();
        }
    }

    @FXML
    void handleLogInAction(){
        int port = Integer.parseInt( portTextField.getText() );
        try( Socket socket = new Socket( ipTextField.getText() , port ) ;
             DataOutputStream outputStream = new DataOutputStream( socket.getOutputStream() ) ;
             DataInputStream inputStream = new DataInputStream( socket.getInputStream() ) ){
            UserInformation request = new UserInformation( loginTextField.getText() , passwordField.getText() );
            ObjectMapper    mapper  = new ObjectMapper();
            outputStream.writeUTF( mapper.writeValueAsString( request ) );
            Data response = mapper.readerFor( Data.class ).readValue( inputStream.readUTF() );

//            Setting global context only after server response
            Controller.getInstance().login = loginTextField.getText();
            Controller.getInstance().password = passwordField.getText();
            Controller.getInstance().host = ipTextField.getText();
            Controller.getInstance().port = port;

            response.withoutExceptionOrWith( data -> {
                ChoiceOverviewController.openChoiceDBScreen( data );
                closeWindow();
            } , ClientMain::showWarningByError );
        }catch( EOFException e ){
            ClientMain.showWarning( "Error adminConnection" , "НЕизвестная хуйня" , "Server has closed adminConnection" );
        }catch( IOException e ){
            e.printStackTrace();
        }
    }


    /**
     Cancel button action handler. Closes window, when button pushed
     */
    @FXML
    private void handleCancelAction(){
        closeWindow();
    }

    private void closeWindow(){
        thisStage.close();
    }

    static void openLoginScreen( Stage primaryStage ){
        try{
            FXMLLoader loader =
                    new FXMLLoader( LoginOverviewController.class.getResource( "/fxml/LoginOverview.fxml" ) );
            LoginOverviewController controller = new LoginOverviewController( primaryStage );
            loader.setController( controller );
            primaryStage.setTitle( "Login" );
            Scene scene = new Scene( loader.load() );
            primaryStage.setScene( scene );
            primaryStage.setResizable( false );
            primaryStage.show();
        }catch( IOException e ){
            System.err.println( "load problem" );
            System.err.println( e.getMessage() );
        }
    }

}

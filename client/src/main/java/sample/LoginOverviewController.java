package sample;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings( "WeakerAccess" )
class LoginOverviewController{

    private Stage thisStage;

    LoginOverviewController( Stage thisStage ){
        this.thisStage = thisStage;
    }

    @FXML Button        cancelButton;
    @FXML Button        logInButton;
    @FXML TextField     loginTextField;
    @FXML PasswordField passwordField;


    /**
     initializing of view
     */
    @FXML
    private void initialize(){
        loginTextField.textProperty().addListener( ( observable ) -> {
            Pattern textPattern = Pattern.compile( "[\\w\\d\\-_]*" );
            Matcher matcher     = textPattern.matcher( loginTextField.getText() );
            if( !matcher.matches() ){
                loginTextField.setStyle( "-fx-text-inner-color: red;" );
                loginTextField.setTooltip( new Tooltip( "Acceptable symbols: 0-9, a-z, -, _" ) );
                logInButton.setDisable( true );
            }else{
                loginTextField.setStyle( "-fx-text-inner-color: black;" );
                loginTextField.setTooltip( null );
                logInButton.setDisable( false );
            }
        } );
        logInButton.setOnAction( event -> handleLogInAction() );
        cancelButton.setOnAction( event -> closeWindow() );
    }

    private void handleLogInAction(){

        Pattern pattern      = Pattern.compile( "^[\\w\\d]+$" );
        Boolean userCanWrite = false;

        if( !( pattern.matcher( loginTextField.getText() ).matches() &&
               pattern.matcher( passwordField.getText() ).matches() ) ){
            Alert alert = new Alert( Alert.AlertType.WARNING );
            alert.setTitle( "Error while log in " );
            alert.setHeaderText( "Login or pasword incorrect" );
            alert.setContentText( "Please check them and try again." );
            alert.showAndWait();
        }

        /*
          TODO: Receiving list of DB

          Send your login and password to server. true? go below : retry message
          Add view with table of available DB...
          Load db to datamodel and execute code below
         */


        // if ok
        try{
            Stage                    primaryStage = new Stage();
            FXMLLoader               loader       =
                    new FXMLLoader( getClass().getResource( "/fxml/ChoiseOverview.fxml" ) );
            ChoiceOverviewController controller   = new ChoiceOverviewController( primaryStage );
            loader.setController( controller );
            primaryStage.setTitle( "Select DB" );
            Scene scene = new Scene( loader.load() );
            primaryStage.setScene( scene );
            primaryStage.setResizable( false );
            primaryStage.show();
            closeWindow();
        }catch( IOException e ){
            System.out.println( "load problem" );
            System.out.println( e.getMessage() );
        }
    }

    private void closeWindow(){
        thisStage.close();
    }

}

package sample;

import com.fasterxml.jackson.databind.ObjectMapper;
import exceptions.FlightAndRouteException;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.DataModel;
import model.DataModelInstanceSaver;
import transport.Data;
import transport.UserInformation;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Controller for login view. Let user choose IP, Port, Login, password, check symbols, if everything correct - send request to server to get list of DM
 */
@SuppressWarnings( "WeakerAccess" )
class LoginOverviewController{

    @FXML   Button        cancelButton;
    @FXML   Button        logInButton;
    @FXML   TextField     loginTextField;
    @FXML   PasswordField passwordField;
    @FXML   TextField     ipTextField;
    @FXML   TextField     portTextField;
    private Stage         thisStage;

    LoginOverviewController( Stage thisStage ){
        this.thisStage = thisStage;
    }

    /**
     initializing of view
     */
    @FXML
    private void initialize(){
        loginTextField.textProperty().addListener( ( observable , oldValue , newValue ) -> fieldCheck() );
        ipTextField.setText( "85.113.55.203" );
        ipTextField.textProperty().addListener( ( observable , oldValue , newValue ) -> fieldCheck() );
        portTextField.setText( "5555" );
        portTextField.textProperty().addListener( ( observable , oldValue , newValue ) -> fieldCheck() );
        ipTextField.setOnKeyPressed( enterHandler );
        loginTextField.setOnKeyPressed( enterHandler );
        portTextField.setOnKeyPressed( enterHandler );
        passwordField.setOnKeyPressed( enterHandler );
    }

    EventHandler<KeyEvent> enterHandler = event -> {
        if( event.getCode() == KeyCode.ENTER ) handleLogInAction();
    };


    /**
     Cancel button action handler. Closes window, when button pushed
     */
    @FXML
    private void handleCancelAction(){
        closeWindow();
    }


    @FXML
    private void handleLogInAction(){

        Data data = new Data();
        Controller.getInstance().connectToServer( ipTextField.getText() , Integer.parseInt( portTextField.getText() ) );

        if( Controller.getInstance().getClientSocket() != null &&
            Controller.getInstance().getClientSocket().isConnected() ){

            Pattern pattern      = Pattern.compile( "^[.\\w\\d\\-_]+$" );
            Boolean userCanWrite = false;

            if( !( pattern.matcher( loginTextField.getText() ).matches() &&
                   pattern.matcher( passwordField.getText() ).matches() ) ){
                ClientMain.showWarning( "Error while log in " ,
                                        "Unacceptable symbols" ,
                                        "Please check login and try again." );
            }

            /*
             */
            if( pattern.matcher( loginTextField.getText() ).matches() &&
                pattern.matcher( passwordField.getText() ).matches() ){
                Controller.getInstance().setUserInformation( new UserInformation() );
                Controller.getInstance().getUserInformation().setLogin( loginTextField.getText() );
                Controller.getInstance().getUserInformation().setPassword( passwordField.getText() );
                ObjectMapper mapper = new ObjectMapper();

                try{
                    DataOutputStream
                            dataOutputStream =
                            new DataOutputStream( Controller.getInstance().getClientSocket().getOutputStream() );
                    DataInputStream
                            inputStream =
                            new DataInputStream( Controller.getInstance().getClientSocket().getInputStream() );
                    System.out.println( mapper.writeValueAsString( Controller.getInstance().getUserInformation() ) );
                    System.out.println( "Connected: " + Controller.getInstance().getClientSocket().isConnected() );
                    dataOutputStream.writeUTF( mapper.writeValueAsString( Controller.getInstance()
                                                                                    .getUserInformation() ) );
                    System.out.println( "Ушло" );
                    data = mapper.readerFor( Data.class ).readValue( inputStream.readUTF() );
                    System.out.println(mapper.writeValueAsString(data));
                    Controller.getInstance().getClientSocket().close();
                }catch( IOException | NullPointerException ex ){
                    System.out.println( ex.getMessage() );
                }

                if (!(data.getBases().isEmpty())) {
                    try{
                        Stage                    primaryStage = new Stage();
                        FXMLLoader
                                                 loader       =
                                new FXMLLoader( getClass().getResource( "/fxml/ChoiceOverview.fxml" ) );
                        ChoiceOverviewController controller   = new ChoiceOverviewController( primaryStage , data );
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
                }else {
                    ClientMain.showWarning( "Error" , "Unacceptable symbols" , "Check your login, password" );
                }
            }else{
                ClientMain.showWarning( "Error" , "Unacceptable symbols" , "Check your login, password" );
            }
        }else{
            ClientMain.showWarning( "Error" , "Network error" , "Can`t connect to server" );
        }
    }

    private void fieldCheck(){
        Pattern
                ipPattern =
                Pattern.compile( "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                                 "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$" );
        Pattern portPattern = Pattern.compile( "[0-9]{1,5}" );

        Pattern textPattern = Pattern.compile( "[.\\w\\d\\-_]*" );
        Matcher matcher     = textPattern.matcher( loginTextField.getText() );
        if( !matcher.matches() ){
            loginTextField.setStyle( "-fx-text-inner-color: red;" );
            loginTextField.setTooltip( new Tooltip( "Acceptable symbols: 0-9, a-z, -, _" ) );
            logInButton.setDisable( true );
        }else{
            loginTextField.setStyle( "-fx-text-inner-color: black;" );
            loginTextField.setTooltip( null );
        }

        if( !ipPattern.matcher( ipTextField.getText() ).matches() ){
            ipTextField.setStyle( "-fx-text-inner-color: red;" );
            logInButton.setDisable( true );
        }else{
            ipTextField.setStyle( "-fx-text-inner-color: black;" );
        }

        if( !portPattern.matcher( portTextField.getText() ).matches() ){
            portTextField.setStyle( "-fx-text-inner-color: red;" );
            logInButton.setDisable( true );
        }else{
            portTextField.setStyle( "-fx-text-inner-color: black;" );
        }

        if( ipPattern.matcher( ipTextField.getText() ).matches() &&
            portPattern.matcher( portTextField.getText() ).matches() &&
            matcher.matches() ){
            logInButton.setDisable( false );
        }
    }

    @FXML
    void openFile(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add( new FileChooser.ExtensionFilter( "Flights and routes" , "*.far" ) );
        Optional.ofNullable( fileChooser.showOpenDialog( new Stage() ) ).ifPresent( file -> {
            try( InputStream inputStream = Files.newInputStream( file.toPath() ) ){
                final DataModel dataModel = DataModelInstanceSaver.getInstance();
                dataModel.clear();
                dataModel.importFrom( inputStream );
                Controller.savingFile = file;
                Controller.changed = false;
                try{
                    Stage primaryStage = new Stage();
                    FXMLLoader loader = new FXMLLoader( getClass().getResource( "/fxml/RoutesFlightsOverview.fxml" ) );
                    RoutesFlightsOverviewController
                            controller =
                            new RoutesFlightsLocalFileOverviewController( primaryStage );
                    loader.setController( controller );
                    primaryStage.setTitle( file.getName() );
                    Scene scene = new Scene( loader.load() );
                    primaryStage.setScene( scene );
                    primaryStage.setResizable( false );
                    primaryStage.show();
                    closeWindow();
                }catch( IOException e ){
//                todo : Если око не открылось
                }
            }catch( FlightAndRouteException e ){
//                todo : Показать ошибку в базе
            }catch( IOException e ){
//                todo : Если файл не открылся
            }
        } );
    }

    private void closeWindow(){
        thisStage.close();
    }

}

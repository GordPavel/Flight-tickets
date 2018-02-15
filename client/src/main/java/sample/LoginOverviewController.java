package sample;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.codehaus.jackson.map.ObjectMapper;
import transport.Data;
import transport.UserInformation;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
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
    @FXML TextField     ipTextField;
    @FXML TextField     portTextField;

    /**
     initializing of view
     */
    @FXML
    private void initialize(){
        loginTextField.textProperty().addListener( ( observable , oldValue , newValue ) -> fieldCheck() );
        ipTextField.setText( "127.0.0.1" );
        ipTextField.textProperty().addListener( ( observable , oldValue , newValue ) -> fieldCheck() );
        portTextField.setText( "1" );
        portTextField.textProperty().addListener( ( observable , oldValue , newValue ) -> fieldCheck() );
    }


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

        if( Controller.getInstance().getClientSocket() != null && Controller.getInstance().getClientSocket().isConnected() ){

            Pattern pattern      = Pattern.compile( "^[\\.\\w\\d\\-_]+$" );
            Boolean userCanWrite = false;

            if( !( pattern.matcher( loginTextField.getText() ).matches() &&
                   pattern.matcher( passwordField.getText() ).matches() ) ){
                Alert alert = new Alert( Alert.AlertType.WARNING );
                alert.setTitle( "Error while log in " );
                alert.setHeaderText( "Unacceptable symbols" );
                alert.setContentText( "Please check login and try again." );
                alert.showAndWait();
            }

        /*
         */
            if( pattern.matcher( loginTextField.getText() ).matches() &&
                pattern.matcher( passwordField.getText() ).matches() ){
                Controller.getInstance().setUserInformation(new UserInformation());
                Controller.getInstance().getUserInformation().setLogin( loginTextField.getText() );
                Controller.getInstance().getUserInformation().setPassword( passwordField.getText() );
                ObjectMapper mapper = new ObjectMapper();

                try(DataOutputStream dataOutputStream = new DataOutputStream(Controller.getInstance().getClientSocket().getOutputStream());
                    DataInputStream inputStream = new DataInputStream(Controller.getInstance().getClientSocket().getInputStream())){
                    System.out.println(mapper.writeValueAsString(Controller.getInstance().getUserInformation()));
                    dataOutputStream.writeUTF(mapper.writeValueAsString(Controller.getInstance().getUserInformation()));
                    System.out.println("Yshlo");
                    data = mapper.reader(Data.class).readValue(inputStream.readUTF());
                }catch( IOException | NullPointerException ex ){
                    System.out.println( ex.getMessage() );
                }


                if( data.notHasException() ){
                    try{
                        Stage                    primaryStage = new Stage();
                        FXMLLoader               loader       =
                                new FXMLLoader( getClass().getResource( "/fxml/ChoiseOverview.fxml" ) );
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
                } else {
                    Alert alert = new Alert( Alert.AlertType.WARNING );
                    alert.setTitle( "Error" );
                    alert.setHeaderText( "Server error" );
                    alert.setContentText( data.getException().getMessage() );
                    alert.showAndWait();
                }
            } else {
                Alert alert = new Alert( Alert.AlertType.WARNING );
                alert.setTitle( "Error" );
                alert.setHeaderText( "Unacceptable symbols" );
                alert.setContentText( "Check your login, password" );
                alert.showAndWait();
            }
        } else {
            Alert alert = new Alert( Alert.AlertType.WARNING );
            alert.setTitle( "Error" );
            alert.setHeaderText( "Network error" );
            alert.setContentText( "Can`t connect to server" );
            alert.showAndWait();
//            try {
//                Map<String,String> map = new HashMap();
////                      map.put("1","2");
////                      map.put("2","3");
//                Controller.getInstance().setClientSocket(new Socket());
//                Data data1 = new Data();
//                data1.setBases(map);
//                Stage primaryStage = new Stage();
//                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ChoiseOverview.fxml"));
//                ChoiceOverviewController controller = new ChoiceOverviewController(primaryStage, data1);
//                loader.setController(controller);
//                primaryStage.setTitle("Select DB");
//                Scene scene = new Scene(loader.load());
//                primaryStage.setScene(scene);
//                primaryStage.setResizable(false);
//                primaryStage.show();
//                closeWindow();
//            } catch (IOException e) {
//                System.out.println("load problem");
//                System.out.println(e.getMessage());
//            }
        }
    }

    private void fieldCheck(){
        Pattern ipPattern = Pattern.compile(
                "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$" );
        Pattern portPattern = Pattern.compile( "[0-9]{1,5}" );

        Pattern textPattern = Pattern.compile( "[\\.\\w\\d\\-_]*" );
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
            portPattern.matcher( portTextField.getText() ).matches() && matcher.matches() ){
            logInButton.setDisable( false );
        }
    }

    private void closeWindow(){
        thisStage.close();
    }

}

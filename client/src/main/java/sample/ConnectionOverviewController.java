package sample;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;
import java.util.regex.Pattern;

public class ConnectionOverviewController {

    private Stage thisStage;

    ConnectionOverviewController( Stage thisStage ){
        this.thisStage = thisStage;
    }

    @FXML
    Button connectButton;
    @FXML
    Button cancelButton;
    @FXML
    TextField ipTextField;
    @FXML
    TextField portTextField;


    /**
     * initializing of view
     */
    @FXML
    public void initialize(){
        ipTextField.setText("127.0.0.1");
        ipTextField.textProperty().addListener((observable, oldValue, newValue) -> fieldCheck());
        portTextField.setText("1");
        portTextField.textProperty().addListener((observable, oldValue, newValue) -> fieldCheck());

    }

    /**
     * Connect button action handler. Establish connection to server, put socket to ClientMain.clientSocket and open login window,
     * if connection successfully established. IP address and port - from TextFields on view.
     * Button disabled, if IP or Port TextField has incorrect information
     * @param actionEvent
     */
    @FXML
    public void handleConnectAction(ActionEvent actionEvent ){

        try {
            Socket socket = new Socket(ipTextField.getText(), Integer.parseInt(portTextField.getText()));
            ClientMain.setClientSocket(socket);
        } catch (IOException ioex){
                    System.out.println("Connection failed");
        }

        /**
         * TODO: after connection
         * Put this part of code in try
         */
        try {
            Stage loginStage = new Stage();
            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/fxml/LoginOverview.fxml"));
            LoginOverviewController controller = new LoginOverviewController(loginStage);
            loader.setController(controller);
            loginStage.setTitle("Login");
            Scene scene = new Scene(loader.load());
            loginStage.setScene(scene);
            loginStage.setResizable(false);
            loginStage.show();
            closeWindow( actionEvent );
        } catch (IOException e)
        {
            System.out.println("load problem");
            System.out.println(e.getMessage());
        }


    }

    /**
     * Cancel button action handler. Closes window, when button pushed
     * @param actionEvent
     */
    @FXML
    public void handleCancelAction( ActionEvent actionEvent ){
        closeWindow( actionEvent );
    }


    private void closeWindow( Event event ){
        Stage stage = ( Stage ) ( (Parent) event.getSource() ).getScene().getWindow();
        stage.close();
    }

    /**
     * Checking fields. If content is not IP address and port - blocking connection button
     */
    private void fieldCheck(){
        Pattern ipPattern = Pattern.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
        Pattern portPattern = Pattern.compile("[0-9]{1,5}");

        if (!ipPattern.matcher(ipTextField.getText()).matches()) {
            ipTextField.setStyle("-fx-text-inner-color: red;");
            connectButton.setDisable(true);
        }
        else {
            ipTextField.setStyle("-fx-text-inner-color: black;");
        }

        if (!portPattern.matcher(portTextField.getText()).matches()) {
            portTextField.setStyle("-fx-text-inner-color: red;");
            connectButton.setDisable(true);
        }
        else {
            portTextField.setStyle("-fx-text-inner-color: black;");
        }

        if (ipPattern.matcher(ipTextField.getText()).matches()&&portPattern.matcher(portTextField.getText()).matches())
            connectButton.setDisable(false);
    }


}

package sample;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Parent;
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

    @FXML
    public void initialize(){
        ipTextField.setText("127.0.0.1");
        ipTextField.textProperty().addListener((observable, oldValue, newValue) -> fieldCheck());
        portTextField.setText("1");
        portTextField.textProperty().addListener((observable, oldValue, newValue) -> fieldCheck());

    }


    @FXML
    public void handleConnectAction(ActionEvent actionEvent ){

        try {
            Socket socket = new Socket(ipTextField.getText(), Integer.parseInt(portTextField.getText()));
            ClientMain.setClientSocket(socket);
        } catch (IOException ioex){
                    System.out.println("Connection failed");
        }

    }


    @FXML
    public void handleCancelAction( ActionEvent actionEvent ){
        closeWindow( actionEvent );
    }

    private void closeWindow( Event event ){
        Stage stage = ( Stage ) ( (Parent) event.getSource() ).getScene().getWindow();
        stage.close();
    }

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

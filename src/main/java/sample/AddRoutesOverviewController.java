package sample;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Route;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Controller for adding route view
 * Allows to enter data for adding a new route
 */

public class AddRoutesOverviewController {

    private Controller controller = Controller.getInstance();

    @FXML
    TextField departureTextField;
    @FXML
    TextField destinationTextField;
    @FXML
    Button addAddRouteOverview;


    /**
     * @param actionEvent
     * Add Button. Add a new route to the DataModel
     */

    @FXML
    private void handleAddAction(ActionEvent actionEvent) {

        if (departureTextField.getText().equals("") || destinationTextField.getText().equals("")) {

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Empty fields ");
            alert.setHeaderText("No parameters");
            alert.setContentText("Please enter all parameters for adding a new route.");

            alert.showAndWait();

        } else if((controller.getRoutes().stream().anyMatch(route -> route.getFrom().toUpperCase().equals( departureTextField.getText().toUpperCase())))
                &&(controller.getRoutes().stream().anyMatch(route -> route.getTo().toUpperCase().equals( destinationTextField.getText().toUpperCase())))){

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Route already exist ");
            alert.setHeaderText("Route already exist");
            alert.setContentText("Please enter other parameters for adding a new route.");

            alert.showAndWait();
        }
        else {


            controller.model.addRoute(new Route(departureTextField.getText(), destinationTextField.getText()));
            controller.updateRoutes();
            controller.getRoutes().forEach(System.out::println);
            closeWindow(actionEvent);
        }


    }

    /**
     * @param event
     * Clear Button. Clear all data in the window for adding a new route
     */

    @FXML
    private void clearData(ActionEvent event) {
        departureTextField.clear();
        destinationTextField.clear();
    }

    /**
     * @param actionEvent
     * Cancel Button.
     * Close a window for adding a new route
     */

    @FXML
    public void handleCancelAction(ActionEvent actionEvent) {

        closeWindow(actionEvent);
    }
    private void closeWindow(Event event) {
        Stage stage = (Stage) ((Parent) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    private void initialize(){

        departureTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            Pattern pattern = Pattern.compile("[0-9\\-_\\w]*");
            Matcher matcher = pattern.matcher(departureTextField.getText());
            if (!matcher.matches())
            {
                departureTextField.setStyle("-fx-text-inner-color: red;");
            }
            else {
                departureTextField.setStyle("-fx-text-inner-color: black;");
            }
            checkTimeTextFields();
        });

        destinationTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            Pattern pattern = Pattern.compile("[0-9\\-_\\w]*");
            Matcher matcher = pattern.matcher(destinationTextField.getText());
            if (!matcher.matches())
            {
                destinationTextField.setStyle("-fx-text-inner-color: red;");
            }
            else {
                destinationTextField.setStyle("-fx-text-inner-color: black;");
            }
            checkTimeTextFields();
        });


    }

    private void checkTimeTextFields(){

        Pattern pattern = Pattern.compile("[0-9\\-_\\w]*");

        if (pattern.matcher(departureTextField.getText()).matches()
                &&pattern.matcher(destinationTextField.getText()).matches()) {
            addAddRouteOverview.setDisable(false);
        }
        else {
            addAddRouteOverview.setDisable(true);
        }
    }
}

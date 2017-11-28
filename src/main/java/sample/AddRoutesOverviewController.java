package sample;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Route;

/**
 * Controller for adding route view
 * Allows to enter data for adding a new route
 */

public class AddRoutesOverviewController {

    private Controller controller = new Controller();

    @FXML
    TextField departureTextField;
    @FXML
    TextField destinationTextField;


    /**
     * @param actionEvent
     * Add Button. Add a new route to the DataModel
     */

    @FXML
    private void handleAddAction(ActionEvent actionEvent) {

        if (!departureTextField.getText().equals("") && !destinationTextField.getText().equals("")) {

            controller.model.addRoute(new Route(departureTextField.getText(), destinationTextField.getText()));
            controller.updateRoutes();

        } else {

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Empty fields ");
            alert.setHeaderText("No parameters");
            alert.setContentText("Please enter all parameters for adding a new route.");

            alert.showAndWait();

        }

        closeWindow(actionEvent);
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
}

package sample;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller for editing a route view
 * Allows to edit data about chosen route
 */

public class EditRoutesOverviewController {

    private Controller controller = new Controller();

    @FXML
    TextField departureTextField;
    @FXML
    TextField destinationTextField;

    /**
     * @param actionEvent
     * Accept Button. Edit necessary data about chosen route
     */

    @FXML
    private void handleEditAction(ActionEvent actionEvent) {

        controller.model.editRoute(Controller.routeForEdit, departureTextField.getText(), destinationTextField.getText());
        controller.updateRoutes();
        closeWindow(actionEvent);
    }

    /**
     * @param event
     * Clear Button. Clear fields in the window for editing a route
     */

    @FXML
    private void clearData(ActionEvent event) {
        departureTextField.clear();
        destinationTextField.clear();
    }

    /**
     * @param actionEvent
     * Cancel Button. Close a window
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

package sample;


import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

import javafx.stage.Stage;
import model.Flight;
import model.Route;


import java.time.LocalDate;
import java.util.Date;

/**
 * Controller for adding flight view
 * Allows to enter data for adding a new flight
 */

public class AddFlightsOverviewController {

    private Controller controller = new Controller();

    @FXML
    ChoiceBox<Route> box;
    @FXML
    TextField number;
    @FXML
    TextField planeID;
    @FXML
    DatePicker departureDate;
    @FXML
    DatePicker arrivingDate;

    /**
     * initialization of view
     */
    @FXML
    private void initialize() {


        departureDate.setValue(LocalDate.now());
        arrivingDate.setValue(LocalDate.now());
        box.setItems(controller.getRoutes());
    }

    /**
     * @param actionEvent
     * Add Button. Add a new flight into the DataModel
     */
    @FXML
    private void handleAddAction(ActionEvent actionEvent) {

        Date arrivDate = new Date(arrivingDate.getValue().toEpochDay());
        Date departDate = new Date(departureDate.getValue().toEpochDay());
        if (arrivDate.compareTo(departDate) < 0) {

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Incorrect data about date");
            alert.setHeaderText("Flight has incorrect dates");
            alert.setContentText("Please enter correct parameters for a new flight.");

            alert.showAndWait();
        } else {

            controller.model.addFlight(new Flight(number.getText(), box.getSelectionModel().getSelectedItem(), planeID.getText(), departDate, arrivDate));
            controller.updateFlights();
            closeWindow(actionEvent);
        }
    }

    /**
     * @param event
     * Clear Button. Clear all fields in the window
     */
    @FXML
    private void clearData(ActionEvent event) {

        number.clear();
        planeID.clear();
        departureDate.setValue(LocalDate.now());
        arrivingDate.setValue(LocalDate.now());
    }

    /**
     * @param actionEvent
     * Cancel Button. Close a window for adding a new flight
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

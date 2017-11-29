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

import model.Route;

import java.time.LocalDate;
import java.util.Date;

/**
 * Controller for editing a flight view
 * Allows to enter data for editing the chosen flight
 */

public class EditFlightsOverviewController {

    private Controller controller = Controller.getInstance();

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

        box.setItems(controller.getRoutes());
    }


    /**
     * @param event Clear Button. Clear all the fields in the window
     */
    @FXML
    private void clearData(ActionEvent event) {

        number.clear();
        planeID.clear();
        departureDate.setValue(LocalDate.now());
        arrivingDate.setValue(LocalDate.now());
    }

    /**
     * @param actionEvent Edit Button. Edit data about the chosen flight
     */
    @FXML
    private void handleEditAction(ActionEvent actionEvent) {

        Date arrivDate = new Date(arrivingDate.getValue().toEpochDay());
        Date departDate = new Date(departureDate.getValue().toEpochDay());
        if (arrivDate.getTime() <= departDate.getTime()) {

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Incorrect data about date");
            alert.setHeaderText("Flight has incorrect dates");
            alert.setContentText("Please enter correct parameters for a flight.");

            alert.showAndWait();
        } else {
            controller.model.editFlight(Controller.flightForEdit, box.getSelectionModel().getSelectedItem(), planeID.getText(), departDate, arrivDate);
            controller.updateFlights();
            closeWindow(actionEvent);
        }
    }

    /**
     *
     * @param actionEvent
     * Cancel Button. Close the window.
     */
    public void handleCancelAction(ActionEvent actionEvent)
    {
        closeWindow(actionEvent);
    }

    private void closeWindow(Event event) {
        Stage stage = (Stage) ((Parent) event.getSource()).getScene().getWindow();
        stage.close();
    }


}

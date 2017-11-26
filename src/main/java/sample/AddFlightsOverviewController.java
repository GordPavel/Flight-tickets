package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ChoiceBox;
import javafx.stage.Stage;

public class AddFlightsOverviewController {

    @FXML
    ChoiceBox<String> box;

    ObservableList<String> list = FXCollections.observableArrayList();

    @FXML
    private void initialize() {


        list.add("Москва - Самара");
        list.add("Париж - Лос-Анджелес");
        list.add("Лондон - Санкт-Галлен");

        box.setItems(list);
    }


    private void closeWindow(Event event) {
        Stage stage = (Stage) ((Parent) event.getSource()).getScene().getWindow();
        stage.close();
    }
    public void handleCancelAction(ActionEvent actionEvent) {
        closeWindow(actionEvent);
    }
}

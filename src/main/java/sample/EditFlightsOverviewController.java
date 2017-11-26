package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;

public class EditFlightsOverviewController{
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
}

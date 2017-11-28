package sample;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class AddFlightsOverviewController{

    @FXML public TextField        numberField;
    @FXML public TextField        planeIdField;
    @FXML public ListView<String> routesList;
    @FXML public TextField        searchField;

    @FXML
    private void initialize(){

    }
}

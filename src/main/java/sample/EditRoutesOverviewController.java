package sample;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class EditRoutesOverviewController{

    private void closeWindow(Event event) {
        Stage stage = (Stage) ((Parent) event.getSource()).getScene().getWindow();
        stage.close();
    }
    public void handleCancelAction(ActionEvent actionEvent) {
        closeWindow(actionEvent);
    }
}

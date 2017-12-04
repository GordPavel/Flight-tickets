package sample;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import model.Route;

//import java.awt.*;


/**
 Controller for flight search view
 Allows to search flights in DataModel with params
 */
public class SearchFlightsOverviewController{


    public Label           numberLabel;
    public Label           planeIdLabel;
    public Label           arriveDateLabel;
    public Label           departureDateLabel;
    public Label           flightTimeLabel;
    public ListView<Route> routesListView;
    public TextField       searchFromTextField;
    public TextField       searchToTextField;
    /**
     Connecting to FXML items
     */
    @FXML  TextField       numberTextField;
    @FXML  TextField       planeIdTextField;
    @FXML  TextField       flightTimeFromTextField;
    @FXML  TextField       flightTimeToTextField;

    @FXML DatePicker departureFromDatePicker;
    @FXML DatePicker departureToDatePicker;
    @FXML TextField  departureFromTimeTextField;
    @FXML TextField  departureToTimeTextField;

    @FXML DatePicker arriveFromDatePicker;
    @FXML DatePicker arriveToDatePicker;
    @FXML TextField  arriveFromTimeTextField;
    @FXML TextField  arriveToTimeTextField;

    private Controller controller = Controller.getInstance();

    @FXML
    public void initialize(){
        departureFromTimeTextField.setText( "00:00" );
        departureToTimeTextField.setText( "00:00" );
        arriveFromTimeTextField.setText( "00:00" );
        arriveToTimeTextField.setText( "00:00" );
        setLayouts();


    }


    //        Don't touch this layout settings! too hard to make correctly!
    private void setLayouts(){
        numberTextField.setLayoutX( routesListView.getLayoutX() );
        numberTextField.setLayoutY( numberLabel.getLayoutY() );

        planeIdTextField.setLayoutX( routesListView.getLayoutX() );
        planeIdTextField.setLayoutY( planeIdLabel.getLayoutY() );

        departureFromDatePicker.setLayoutX( routesListView.getLayoutX() );
        departureFromDatePicker.setLayoutY( departureDateLabel.getLayoutY() );
        departureFromTimeTextField
                .setLayoutX( departureFromDatePicker.getLayoutX() + departureFromDatePicker.getWidth() + 150 );
        departureFromTimeTextField.setLayoutY( departureFromDatePicker.getLayoutY() );

        departureToDatePicker.setLayoutX( departureFromDatePicker.getLayoutX() );
        departureToDatePicker
                .setLayoutY( departureFromDatePicker.getLayoutY() + departureFromDatePicker.getHeight() + 30 );
        departureToTimeTextField.setLayoutX( departureFromTimeTextField.getLayoutX() );
        departureToTimeTextField.setLayoutY( departureToDatePicker.getLayoutY() );

        arriveFromDatePicker.setLayoutX( departureFromDatePicker.getLayoutX() );
        arriveFromDatePicker.setLayoutY( arriveDateLabel.getLayoutY() );
        arriveFromTimeTextField.setLayoutX( departureFromTimeTextField.getLayoutX() );
        arriveFromTimeTextField.setLayoutY( arriveFromDatePicker.getLayoutY() );

        arriveToDatePicker.setLayoutX( arriveFromDatePicker.getLayoutX() );
        arriveToDatePicker.setLayoutY( arriveFromDatePicker.getLayoutY() + arriveFromDatePicker.getHeight() + 30 );
        arriveToTimeTextField.setLayoutX( arriveFromTimeTextField.getLayoutX() );
        arriveToTimeTextField.setLayoutY( arriveToDatePicker.getLayoutY() );

        flightTimeFromTextField.setLayoutX( routesListView.getLayoutX() );
        flightTimeFromTextField.setLayoutY( flightTimeLabel.getLayoutY() );
        flightTimeToTextField.setLayoutX( departureFromTimeTextField.getLayoutX() );
        flightTimeToTextField.setLayoutY( flightTimeLabel.getLayoutY() );
    }

    /**
     Clear button. Clears all fields
     */
    @FXML
    public void handleClearAction(){

    }


}

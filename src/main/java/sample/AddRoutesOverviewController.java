package sample;

import exceptions.FlightAndRouteException;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;
import model.DataModel;
import model.Route;
import np.com.ngopal.control.AutoFillTextBox;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 Controller for adding route view
 Allows to enter data for adding a new route
 */

public class AddRoutesOverviewController{

    private Controller controller = Controller.getInstance();

    @FXML AutoFillTextBox<String> departureTextField;
    @FXML AutoFillTextBox<String> destinationTextField;
    @FXML Button                  addAddRouteOverview;

    private DataModel dataModel = DataModel.getInstance();

    /**
     @param actionEvent Add Button. Add a new route to the DataModel
     */

    @FXML
    private void handleAddAction( ActionEvent actionEvent ){
        if( departureTextField.getText().equals( "" ) || destinationTextField.getText().equals( "" ) ){
            Alert alert = new Alert( Alert.AlertType.WARNING );
            alert.setTitle( "Empty fields " );
            alert.setHeaderText( "No parameters" );
            alert.setContentText( "Please enter all parameters for adding a new route." );
            alert.showAndWait();
        }else{
            try{
                Controller.model.addRoute( new Route( departureTextField.getText() , destinationTextField.getText() ) );
                controller.updateRoutes();
                Main.changed = true;
                closeWindow( actionEvent );
            }catch( FlightAndRouteException e ){
                Alert alert = new Alert( Alert.AlertType.WARNING );
                alert.setTitle( "Model`s message" );
                alert.setHeaderText( "Model send message:" );
                alert.setContentText( e.getMessage() );
                alert.showAndWait();
            }
        }
    }

    /**
     Clear Button. Clear all fields in GUI
     */

    @FXML
    private void clearData(){
        departureTextField.getTextbox().clear();
        destinationTextField.getTextbox().clear();
    }

    /**
     @param actionEvent Cancel Button.
     Close a window for adding a new route
     */

    @FXML
    public void handleCancelAction( ActionEvent actionEvent ){

        closeWindow( actionEvent );
    }

    private void closeWindow( Event event ){
        Stage stage = ( Stage ) ( ( Parent ) event.getSource() ).getScene().getWindow();
        stage.close();
    }


    /**
     initialization of view
     */
    @FXML
    private void initialize(){
        departureTextField.setData( dataModel.listAllAirportsWithPredicate( airport -> true ).collect(
                Collectors.collectingAndThen( toList() , FXCollections::observableArrayList ) ) );
        departureTextField.getTextbox().textProperty().addListener( ( observable , oldValue , newValue ) -> {
            Pattern pattern = Pattern.compile( "[0-9\\-_\\w]*" );
            Matcher matcher = pattern.matcher( departureTextField.getText() );
            if( !matcher.matches() ){
                departureTextField.setStyle( "-fx-text-inner-color: red;" );
                departureTextField.setTooltip( new Tooltip( "Acceptable symbols: 0-9, a-z, -, _" ) );
            }else{
                departureTextField.setStyle( "-fx-text-inner-color: black;" );
                departureTextField.setTooltip( null );
            }
            checkTimeTextFields();
        } );

        destinationTextField.setData( dataModel.listAllAirportsWithPredicate( airport -> true ).collect(
                Collectors.collectingAndThen( toList() , FXCollections::observableArrayList ) ) );
        destinationTextField.getTextbox().textProperty().addListener( ( observable , oldValue , newValue ) -> {
            Pattern pattern = Pattern.compile( "[0-9\\-_\\w]*" );
            Matcher matcher = pattern.matcher( destinationTextField.getText() );
            if( !matcher.matches() ){
                destinationTextField.setStyle( "-fx-text-inner-color: red;" );
                destinationTextField.setTooltip( new Tooltip( "Acceptable symbols: 0-9, a-z, -, _" ) );
            }else{
                destinationTextField.setStyle( "-fx-text-inner-color: black;" );
                destinationTextField.setTooltip( null );
            }
            checkTimeTextFields();
        } );

    }

    /**
     Check for enable/disable add button. Switch it off, if user use unacceptable symbols
     */
    private void checkTimeTextFields(){
        Pattern pattern = Pattern.compile( "[0-9\\-_\\w]*" );
        if( pattern.matcher( departureTextField.getText() ).matches() &&
            pattern.matcher( destinationTextField.getText() ).matches() ){
            addAddRouteOverview.setDisable( false );
        }else{
            addAddRouteOverview.setDisable( true );
        }
    }
}

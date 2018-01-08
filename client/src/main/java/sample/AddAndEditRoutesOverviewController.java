package sample;

import com.jfoenix.controls.JFXButton;
import exceptions.FlightAndRouteException;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import model.Route;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 Controller for adding route view
 Allows to enter data for adding a new route
 */

public class AddAndEditRoutesOverviewController{
    private Controller controller = Controller.getInstance();

    @FXML TextField departureTextField;
    @FXML Label     errorDepartureLabel;
    @FXML TextField destinationTextField;
    @FXML Label     errorDestinationLabel;
    @FXML Label     mainLabel;
    @FXML JFXButton addAndEditRouteButton;

    private Route editingRoute;

    AddAndEditRoutesOverviewController( Route editingRoute ){
        this.editingRoute = editingRoute;
    }

    /**
     initialization of view
     */
    @FXML
    private void initialize() throws IOException{
        Font PT_Mono = Font.loadFont( getClass().getResource( "/PT_Mono.ttf" ).openStream() , 15 );
        departureTextField.setFont( PT_Mono );
        errorDepartureLabel.setVisible( false );
        errorDepartureLabel.setLayoutX( departureTextField.getLayoutX() + 8.5 );
        errorDepartureLabel.setLayoutY( departureTextField.getLayoutY() + 16 );
        departureTextField.textProperty().addListener(
                ( observable , oldValue , newValue ) -> textFieldErrorHandler( newValue , oldValue ,
                                                                               errorDepartureLabel ,
                                                                               departureTextField ) );

        destinationTextField.setFont( PT_Mono );
        errorDestinationLabel.setVisible( false );
        errorDestinationLabel.setLayoutX( destinationTextField.getLayoutX() + 8.5 );
        errorDestinationLabel.setLayoutY( destinationTextField.getLayoutY() + 16 );
        destinationTextField.textProperty().addListener(
                ( observable , oldValue , newValue ) -> textFieldErrorHandler( newValue , oldValue ,
                                                                               errorDepartureLabel ,
                                                                               departureTextField ) );
        addAndEditRouteButton.setOnAction( event -> {
            if( departureTextField.getText().equals( "" ) || destinationTextField.getText().equals( "" ) ){
                Alert alert = new Alert( Alert.AlertType.WARNING );
                alert.setTitle( "Empty fields " );
                alert.setHeaderText( "No parameters" );
                alert.setContentText( "Please enter all parameters for adding a new route." );
                alert.showAndWait();
            }else{
                try{
                    Controller.model
                            .addRoute( new Route( departureTextField.getText() , destinationTextField.getText() ) );
                    controller.updateRoutes();
                    /**
                     * TODO: put here request to server to add route
                     */
                    Main.changed = true;
                    closeWindow( event );
                }catch( FlightAndRouteException e ){
                    Alert alert = new Alert( Alert.AlertType.WARNING );
                    alert.setTitle( "Model`s message" );
                    alert.setHeaderText( "Model send message:" );
                    alert.setContentText( e.getMessage() );
                    alert.showAndWait();
                }
            }
        } );

        if( editingRoute != null ){
            departureTextField.textProperty().setValue( editingRoute.getFrom() );
            destinationTextField.textProperty().setValue( editingRoute.getTo() );
            addAndEditRouteButton.setText( "Edit" );
            mainLabel.setText( "Enter new data." );
//            Edit action
            addAndEditRouteButton.setOnAction( event -> {
                if( departureTextField.getText().equals( "" ) || destinationTextField.getText().equals( "" ) ){
                    Alert alert = new Alert( Alert.AlertType.WARNING );
                    alert.setTitle( "Empty fields " );
                    alert.setHeaderText( "No parameters" );
                    alert.setContentText( "Please enter all parameters for adding a new route." );
                    alert.showAndWait();
                }else{
                    try{
                        Controller.model.editRoute( Controller.routeForEdit , departureTextField.getText() ,
                                                    destinationTextField.getText() );
                        controller.updateRoutes();
                        Main.changed = true;
                        /**
                         * TODO: put here request to server to add route
                         */
                        closeWindow( event );
                    }catch( FlightAndRouteException e ){
                        Alert alert = new Alert( Alert.AlertType.WARNING );
                        alert.setTitle( "Model`s message" );
                        alert.setHeaderText( "Model send message:" );
                        alert.setContentText( e.getMessage() );
                        alert.showAndWait();
                    }
                }
            } );
        }
    }

    /**
     Clear Button. Clear all fields in GUI
     */

    @FXML
    private void clearData(){
        departureTextField.clear();
        destinationTextField.clear();
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

    private void textFieldErrorHandler( String newValue , String oldValue , Label errorLabel ,
                                        TextField handlingTextField ){
        if( newValue.length() > 20 ){
            handlingTextField.textProperty().setValue( oldValue );
        }
        Matcher matcher = Pattern.compile( "[^0-9\\-_\\w]+" ).matcher( newValue );
        if( matcher.find() ){
            handlingTextField.setStyle( "-fx-text-inner-color: red;" );
            handlingTextField.setTooltip( new Tooltip( "Acceptable symbols: 0-9, a-z, -, _" ) );
            errorLabel.setVisible( true );
            errorLabel.setLayoutX( handlingTextField.getLayoutX() + 8.5 +
                                   matcher.start() * ( errorLabel.getFont().getSize() / 1.43 ) );
        }else{
            errorLabel.setVisible( false );
            handlingTextField.setStyle( "-fx-text-inner-color: black;" );
            handlingTextField.setTooltip( null );
        }
        checkErrors();
    }

    /**
     Check for enable/disable add button. Switch it off, if user use unacceptable symbols
     */
    private void checkErrors(){
        Pattern pattern = Pattern.compile( "[0-9\\-_\\w]*" );
        if( pattern.matcher( departureTextField.getText() ).matches() &&
            pattern.matcher( destinationTextField.getText() ).matches() ){
            addAndEditRouteButton.setDisable( false );
        }else{
            addAndEditRouteButton.setDisable( true );
        }
    }
}

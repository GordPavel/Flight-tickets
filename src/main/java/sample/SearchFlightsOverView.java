package sample;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.regex.Pattern;

public class SearchFlightsOverView{
    public TextField  numberTextField;
    public TextField  planeIdTextField;
    public TextField  departureAirportTextField;
    public TextField  arrivalAirportTextField;
    public DatePicker departureDatePicker;
    public TextField  departureTimeTextField;
    public DatePicker arrivalDatePicker;
    public TextField  arrivalTimeTextField;
    public TextField  startFlightTimeRangeTextField;
    public TextField  endFlightTimeRangeTextField;

    public SearchFlightsOverView(){
    }

    SearchFlightsOverView( RoutesFlightsOverviewController flightsController ){
        Parent searchFlightWindow;
        Stage  oldStage = ( Stage ) flightsController.mainPane.getScene().getWindow();
        try{
            searchFlightWindow = FXMLLoader.load( getClass().getResource( "/fxml/SearchFlightsOverview.fxml" ) );

            Scene scene = new Scene( searchFlightWindow );
            Stage popUp = new Stage();
            scene.getStylesheets().add( getClass().getResource( "/fxml/text-field-time-error.css" ).toExternalForm() );

            popUp.initModality( Modality.WINDOW_MODAL );
            popUp.initOwner( oldStage );

            popUp.setTitle( "Search flight" );
            popUp.setScene( scene );
            popUp.setResizable( false );

            oldStage.setOpacity( 0.9 );
            popUp.showAndWait();
            oldStage.setOpacity( 1 );
        }catch( IOException e ){
            e.printStackTrace();
        }
    }

    @FXML
    private void initialize(){
        departureTimeTextField.textProperty().addListener(
                ( observable , oldValue , newValue ) -> checkTimePattern( newValue , departureAirportTextField ) );
        arrivalTimeTextField.textProperty().addListener(
                ( observable , oldValue , newValue ) -> checkTimePattern( newValue , arrivalTimeTextField ) );
    }

    private void checkTimePattern( String newValue , TextField textField ){
        if( newValue.isEmpty() ) return;
        if( !Pattern.matches( "[0-9]*:[0-5][0-9]" , newValue ) ){
            textField.getStyleClass().add( "error" );
        }else{
            textField.getStyleClass().removeAll( "error" );
        }
    }

    private void reSearch(){

    }
}

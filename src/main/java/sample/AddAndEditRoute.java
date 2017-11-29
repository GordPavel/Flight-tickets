package sample;

import exceptions.FlightAndRouteException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import model.DataModel;
import model.Route;
import np.com.ngopal.control.AutoFillTextBox;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AddAndEditRoute implements Initializable{
    public Label                   label;
    public AutoFillTextBox<String> departureTextField;
    public AutoFillTextBox<String> destinationTextField;
    public Button                  add;
    public Button                  cancel;

    private DataModel dataModel = DataModel.getInstance();
    private MainWindowController mainWindowController;
    private Stage                thisWindow;
    private Route                editingRoute;

    AddAndEditRoute( MainWindowController mainWindowController , Stage thisWindow , Route editingRoute ){
        this.mainWindowController = mainWindowController;
        this.thisWindow = thisWindow;
        this.editingRoute = editingRoute;
    }

    @Override
    public void initialize( URL location , ResourceBundle resources ){
        ObservableList<String> data = dataModel.listAllAirportsWithPredicate( airport -> true ).collect(
                Collectors.collectingAndThen( Collectors.toList() , FXCollections::observableArrayList ) );
        departureTextField.setData( data );
        departureTextField.setLayoutX( destinationTextField.getLayoutX() );
        departureTextField.setLayoutY( add.getLayoutY() );
        destinationTextField.setData( data );
        if( editingRoute != null ){
            label.setText( "Edit route" );
            add.setText( "Edit" );
            departureTextField.getTextbox().setText( editingRoute.getFrom() );
            destinationTextField.getTextbox().setText( editingRoute.getTo() );
            add.setOnAction( event -> {
                try{
                    mainWindowController.setRoute( editingRoute , new Route( departureTextField.getText() ,
                                                                             destinationTextField.getText() ) );
                }catch( FlightAndRouteException e ){
                    Optional<ButtonType> answer =
                            new Alert( Alert.AlertType.ERROR , e.getMessage() , ButtonType.CLOSE , ButtonType.FINISH )
                                    .showAndWait();
                    if( answer.get().equals( ButtonType.CLOSE ) ){
                        return;
                    }
                }
                thisWindow.close();
            } );
        }else{
            add.setOnAction( event -> {
                try{
                    mainWindowController
                            .addRoute( new Route( departureTextField.getText() , destinationTextField.getText() ) );
                }catch( FlightAndRouteException e ){
                    Optional<ButtonType> answer =
                            new Alert( Alert.AlertType.ERROR , e.getMessage() , ButtonType.CLOSE , ButtonType.FINISH )
                                    .showAndWait();
                    if( answer.get().equals( ButtonType.CLOSE ) ){
                        return;
                    }
                }
                thisWindow.close();
            } );
        }
        cancel.setOnAction( event -> thisWindow.close() );
    }
}

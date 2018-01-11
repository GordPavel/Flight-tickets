package sample;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.DataModelInstanceSaver;
import model.Flight;

@SuppressWarnings( "WeakerAccess" )
class MergeOverviewController{

    private Stage thisStage;

    MergeOverviewController( Stage thisStage ){
        this.thisStage = thisStage;
    }

    @FXML TableView<Flight>           flightTable;
    @FXML TableColumn<Flight, String> numberColumn;
    @FXML TextArea                    newTextArea;
    @FXML TextArea                    oldTextArea;


    private Controller controller = Controller.getInstance();

    /**
     initialization of view
     */
    public void initialize(){
        numberColumn.setCellValueFactory( new PropertyValueFactory<>( "number" ) );
        flightTable.setItems( controller.getMergeFlights() );
        flightTable.getSelectionModel().selectedItemProperty()
                   .addListener( ( observable , oldValue , newValue ) -> showFlightDetails( newValue ) );
        flightTable.refresh();
    }

    /**
     Show details of selected flight and it`s previous version
     */
    private void showFlightDetails( Flight flight ){
        if( flight != null ){
            newTextArea.setWrapText( true );
            newTextArea.setText( flight.toString() );
            oldTextArea.setWrapText( true );
            oldTextArea.setText(
                    controller.getFlights().filtered( flight1 -> flight1.getNumber().equals( flight.getNumber() ) )
                              .get( 0 ).toString() );
        }else{
            newTextArea.setText( "" );
            oldTextArea.setText( "" );
        }
    }


    /**
     Accept New button. Selected flight from model changes for new one
     */
    @FXML
    public void handleAcceptNew(){
        DataModelInstanceSaver.getInstance().editFlight( flightTable.getSelectionModel().getSelectedItem() ,
                                                         flightTable.getSelectionModel().getSelectedItem().getRoute() ,
                                                         flightTable.getSelectionModel().getSelectedItem()
                                                                    .getPlaneID() ,
                                                         flightTable.getSelectionModel().getSelectedItem()
                                                                    .getDepartureDateTime() ,
                                                         flightTable.getSelectionModel().getSelectedItem()
                                                                    .getArriveDateTime() );
        flightTable.getItems().remove( flightTable.getSelectionModel().getSelectedItem() );
        flightTable.refresh();
        if( flightTable.getItems().isEmpty() ){
            Stage stage = ( Stage ) flightTable.getScene().getWindow();
            stage.close();
        }

    }

    /**
     Accept New button. Selected flight from model don`t changes for new one
     */
    @FXML
    public void handleAcceptOld(){
        flightTable.getItems().remove( flightTable.getSelectionModel().getSelectedItem() );
        flightTable.refresh();
        if( flightTable.getItems().isEmpty() ){
            Stage stage = ( Stage ) flightTable.getScene().getWindow();
            stage.close();
        }
    }


    /**
     Accept New button. All flights from model changes for new one
     */
    @FXML
    public void handleAcceptAllNew(){
        for( Flight flight : flightTable.getItems() ){
            DataModelInstanceSaver.getInstance().editFlight( flight , flight.getRoute() , flight.getPlaneID() ,
                                                             flight.getDepartureDateTime() ,
                                                             flight.getArriveDateTime() );
        }
        flightTable.setItems( null );
        flightTable.refresh();
        if( flightTable.getItems().isEmpty() ){
            Stage stage = ( Stage ) flightTable.getScene().getWindow();
            stage.close();
        }

    }

    /**
     Accept New button. None flight from model changes for new one
     */
    @FXML
    public void handleAcceptAllOld(){
        flightTable.setItems( null );
        flightTable.refresh();
        if( flightTable.getItems().isEmpty() ){
            thisStage.close();
        }
    }
}

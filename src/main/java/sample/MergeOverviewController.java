package sample;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Flight;

public class MergeOverviewController{

    public MergeOverviewController(){

    }

    @FXML TableView<Flight>           flightTable;
    @FXML TableColumn<Flight, String> numberColumn;
    @FXML TextArea                    newTextArea;
    @FXML TextArea                    oldTextArea;


    private Controller controller = Controller.getInstance();

    public void initialize(){
        numberColumn.setCellValueFactory( new PropertyValueFactory<>( "number" ) );
        flightTable.setItems( controller.getMergeFlights() );
        flightTable.getSelectionModel().selectedItemProperty()
                   .addListener( ( observable , oldValue , newValue ) -> showFlightDetails( newValue ) );
        flightTable.refresh();
    }


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

    @FXML
    public void handleAcceptNew(){
        Controller.model.editFlight( flightTable.getSelectionModel().getSelectedItem() ,
                                     flightTable.getSelectionModel().getSelectedItem().getRoute() ,
                                     flightTable.getSelectionModel().getSelectedItem().getPlaneID() ,
                                     flightTable.getSelectionModel().getSelectedItem().getDepartureDate() ,
                                     flightTable.getSelectionModel().getSelectedItem().getArriveDate() );
        flightTable.getItems().remove( flightTable.getSelectionModel().getSelectedItem() );
        flightTable.refresh();
    }

    @FXML
    public void handleAcceptOld(){
        flightTable.getItems().remove( flightTable.getSelectionModel().getSelectedItem() );
        flightTable.refresh();
    }

    @FXML
    public void handleAcceptAllNew(){
        for( Flight flight : flightTable.getItems() ){
            Controller.model.editFlight( flight , flight.getRoute() , flight.getPlaneID() , flight.getDepartureDate() ,
                                         flight.getArriveDate() );
        }
        flightTable.setItems( null );
        flightTable.refresh();

    }

    @FXML
    public void handleAcceptAllOld(){
        flightTable.setItems( null );
        flightTable.refresh();
    }
}

package sample;

import exceptions.FlightAndRouteException;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import model.DataModelInstanceSaver;
import model.Flight;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

class AddAndEditFlightsLocalOverviewController extends AddAndEditFlightsOverviewController{
    AddAndEditFlightsLocalOverviewController( Flight editingFlight , Stage thisStage ){
        super( editingFlight , thisStage );
    }

    @Override
    void initialize() throws IOException{
        super.initialize();
        addAndEditFlightButton.setOnAction( event -> addOrEdit( editingFlight == null ) );
    }

    @Override
    void addOrEdit( Boolean isAdd ){
        ZonedDateTime
                departureDateTime =
                LocalDateTime.of( departureDate.getValue() , departureTime.getValue() )
                             .atZone( routesBox.getSelectionModel().getSelectedItem().getFrom() ),
                arriveDateTime =
                        LocalDateTime.of( arrivingDate.getValue() , arrivingTime.getValue() )
                                     .atZone( routesBox.getSelectionModel().getSelectedItem().getTo() );
        if( routesBox.getValue() == null ){
            Alert alert = new Alert( Alert.AlertType.WARNING );
            alert.setTitle( "Route isn`t chosen" );
            alert.setHeaderText( "Flight must have route" );
            alert.setContentText( "Choose route" );
            alert.showAndWait();
        }else if( planeID.getText().equals( "" ) ){
            Alert alert = new Alert( Alert.AlertType.WARNING );
            alert.setTitle( "You have no plain" );
            alert.setHeaderText( "Flight must have plain" );
            alert.setContentText( "Write plain data" );
            alert.showAndWait();
        }else{
            try{
                if( isAdd ){
                    DataModelInstanceSaver.getInstance()
                                          .addFlight( new Flight( number.getText() ,
                                                                  routesBox.getSelectionModel().getSelectedItem() ,
                                                                  planeID.getText() ,
                                                                  departureDateTime ,
                                                                  arriveDateTime ) );
                }else{
                    DataModelInstanceSaver.getInstance()
                                          .editFlight( editingFlight ,
                                                       routesBox.getSelectionModel().getSelectedItem() ,
                                                       planeID.getText() ,
                                                       departureDateTime ,
                                                       arriveDateTime );
                }
                Controller.changed = true;
                closeWindow();
            }catch( FlightAndRouteException e ){
                RoutesFlightsOverviewController.showModelAlert( e );
            }
        }
    }
}

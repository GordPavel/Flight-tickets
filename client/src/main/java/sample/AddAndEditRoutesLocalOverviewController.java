package sample;

import exceptions.FlightAndRouteException;
import javafx.stage.Stage;
import model.DataModelInstanceSaver;
import model.Route;

import java.util.Optional;

class AddAndEditRoutesLocalOverviewController extends AddAndEditRoutesOverviewController{
    AddAndEditRoutesLocalOverviewController( Route editingRoute , Stage thisStage ){
        super( editingRoute , thisStage );
    }

    @Override
    void initialize(){
        super.initialize();
        addAndEditRouteButton.setOnAction( event -> addOrEdit( editingRoute == null ) );
    }

    @Override
    void addOrEdit( Boolean isAdd ){
        try{
            if( isAdd ){
                DataModelInstanceSaver.getInstance()
                                      .addRoute( new Route( Optional.ofNullable( departureCityChoice.getSelectionModel()
                                                                                                    .getSelectedItem() )
                                                                    .orElseThrow( IllegalStateException::new ) ,
                                                            Optional.ofNullable( destinationCityChoice.getSelectionModel()
                                                                                                      .getSelectedItem() )
                                                                    .orElseThrow( IllegalStateException::new ) ) ,
                                                 true );
            }else{
                DataModelInstanceSaver.getInstance()
                                      .editRoute( editingRoute ,
                                                  Optional.ofNullable( departureCityChoice.getSelectionModel()
                                                                                          .getSelectedItem() )
                                                          .orElseThrow( IllegalStateException::new ) ,
                                                  Optional.ofNullable( destinationCityChoice.getSelectionModel()
                                                                                            .getSelectedItem() )
                                                          .orElseThrow( IllegalStateException::new ) );
            }
            Controller.changed = true;
            closeWindow();
        }catch( FlightAndRouteException e ){
            RoutesFlightsOverviewController.showModelAlert( e );
        }
    }
}

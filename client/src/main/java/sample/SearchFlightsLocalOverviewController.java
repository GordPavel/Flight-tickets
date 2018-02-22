package sample;

import javafx.stage.Stage;

class SearchFlightsLocalOverviewController extends SearchFlightsOverviewController{
    SearchFlightsLocalOverviewController( RoutesFlightsOverviewController mainController , Stage thisStage ){
        super( mainController , thisStage );
    }

    @Override
    void initialize(){
        super.initialize();
        searchButton.setVisible( false );
    }

}

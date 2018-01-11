package sample;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

class ChoiceOverviewController{

    private Stage thisStage;

    ChoiceOverviewController( Stage thisStage ){
        this.thisStage = thisStage;
    }

    @FXML Button connectButton;
    @FXML Button selectButton;


    /**
     initializing of view
     */
    @FXML
    public void initialize(){
        System.out.println( "test" );
    }


    /**
     Cancel button action handler. Closes window, when button pushed

     */
    @FXML
    public void handleCancelAction(){
        closeWindow();
    }


    @FXML
    public void handleSelectAction(){

        /*
          TODO: Select database

          Send your login and password to server. true? go below : retry message
          Add view with table of available DB...
          Load db to dataModel and execute code below
         */

        // if write
        try{
            Stage                                primaryStage = new Stage();
            FXMLLoader                           loader       =
                    new FXMLLoader( getClass().getResource( "/fxml/RoutesFlightsOverview.fxml" ) );
            RoutesFlightsWriteOverviewController controller   =
                    new RoutesFlightsWriteOverviewController( primaryStage );
            loader.setController( controller );
            primaryStage.setTitle( "Information system about flights and routes" );
            Scene scene = new Scene( loader.load() , 700 , 500 );
            primaryStage.setScene( scene );
            primaryStage.setResizable( false );
            primaryStage.show();
            closeWindow();
        }catch( IOException e ){
            System.out.println( "load problem" );
            System.out.println( e.getMessage() );
        }


    }

    private void closeWindow(){
        thisStage.close();
    }
}

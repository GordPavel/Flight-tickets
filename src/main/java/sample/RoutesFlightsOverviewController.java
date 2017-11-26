package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class RoutesFlightsOverviewController{

    private static final String EDIT_ROUTE_WINDOW    = "Edit a route";
    private static final String ADD_ROUTE_WINDOW     = "Add a route";
    private static final String EDIT_FLIGHT_WINDOW   = "Edit a flight";
    private static final String ADD_FLIGHT_WINDOW    = "Add a flight";
    private static final String SEARCH_FLIGHT_WINDOW = "Search a flight";

    public Button addRouteButton;
    public Button editRouteButton;
    public Button searchRouteButton;
    public Button deleteRouteButton;
    public Button addFlightButton;
    public Button editFlightButton;
    public Button searchFlightButton;
    public Button deleteFlightButton;


    public void handleAddRouteButton( ActionEvent actionEvent ){
        Parent addRouteWindow;
        Stage  oldStage = ( Stage ) ( ( Parent ) actionEvent.getSource() ).getScene().getWindow();
        try{
            addRouteWindow = FXMLLoader.load( getClass().getResource( "/fxml/AddRoutesOverview.fxml" ) );

            Scene scene = new Scene( addRouteWindow );
            Stage popUp = new Stage();

            popUp.initModality( Modality.APPLICATION_MODAL );
            popUp.initOwner( oldStage );

            popUp.setTitle( ADD_ROUTE_WINDOW );
            popUp.setScene( scene );
            popUp.setResizable( false );

            oldStage.setOpacity( 0.9 );
            popUp.showAndWait();
            oldStage.setOpacity( 1 );

        }catch( IOException e ){
            e.printStackTrace();
        }
    }

    public void handleEditRouteButton( ActionEvent actionEvent ){
        Parent editRouteWindow;
        Stage  oldStage = ( Stage ) ( ( Parent ) actionEvent.getSource() ).getScene().getWindow();
        try{
            editRouteWindow = FXMLLoader.load( getClass().getResource( "/fxml/EditRoutesOverview.fxml" ) );

            Scene scene = new Scene( editRouteWindow );
            Stage popUp = new Stage();

            popUp.initModality( Modality.APPLICATION_MODAL );
            popUp.initOwner( oldStage );

            popUp.setTitle( EDIT_ROUTE_WINDOW );
            popUp.setScene( scene );
            popUp.setResizable( false );

            oldStage.setOpacity( 0.9 );
            popUp.showAndWait();
            oldStage.setOpacity( 1 );

        }catch( IOException e ){
            e.printStackTrace();
        }
    }

    public void handleAddFlightButton( ActionEvent actionEvent ){
        Parent addFlightWindow;
        Stage  oldStage = ( Stage ) ( ( Parent ) actionEvent.getSource() ).getScene().getWindow();
        try{
            addFlightWindow = FXMLLoader.load( getClass().getResource( "/fxml/AddFlightsOverview.fxml" ) );

            Scene scene = new Scene( addFlightWindow );
            Stage popUp = new Stage();

            popUp.initModality( Modality.APPLICATION_MODAL );
            popUp.initOwner( oldStage );

            popUp.setTitle( ADD_FLIGHT_WINDOW );
            popUp.setScene( scene );
            popUp.setResizable( false );

            oldStage.setOpacity( 0.9 );
            popUp.showAndWait();
            oldStage.setOpacity( 1 );

        }catch( IOException e ){
            e.printStackTrace();
        }
    }

    public void handleEditFlightButton( ActionEvent actionEvent ){
        Parent editFlightWindow;
        Stage  oldStage = ( Stage ) ( ( Parent ) actionEvent.getSource() ).getScene().getWindow();
        try{
            editFlightWindow = FXMLLoader.load( getClass().getResource( "/fxml/EditFlightsOverview.fxml" ) );

            Scene scene = new Scene( editFlightWindow );
            Stage popUp = new Stage();

            popUp.initModality( Modality.APPLICATION_MODAL );
            popUp.initOwner( oldStage );

            popUp.setTitle( EDIT_FLIGHT_WINDOW );
            popUp.setScene( scene );
            popUp.setResizable( false );

            oldStage.setOpacity( 0.9 );
            popUp.showAndWait();
            oldStage.setOpacity( 1 );

        }catch( IOException e ){
            e.printStackTrace();
        }
    }


    public void handleSearchFlightButton( ActionEvent actionEvent ){
        Parent editFlightWindow;
        Stage  oldStage = ( Stage ) ( ( Parent ) actionEvent.getSource() ).getScene().getWindow();
        try{
            editFlightWindow = FXMLLoader.load( getClass().getResource( "/fxml/SearchFlightsOverview.fxml" ) );

            Scene scene = new Scene( editFlightWindow );
            Stage popUp = new Stage();

            popUp.initModality( Modality.APPLICATION_MODAL );
            popUp.initOwner( oldStage );

            popUp.setTitle( SEARCH_FLIGHT_WINDOW );
            popUp.setScene( scene );
            popUp.setResizable( false );

            oldStage.setOpacity( 0.9 );
            popUp.showAndWait();
            oldStage.setOpacity( 1 );

        }catch( IOException e ){
            e.printStackTrace();
        }
    }
}

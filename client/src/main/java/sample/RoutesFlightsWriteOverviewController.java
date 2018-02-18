package sample;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.DataModelInstanceSaver;
import model.FlightOrRoute;
import org.danekja.java.util.function.serializable.SerializablePredicate;
import transport.Data;
import transport.ListChangeAdapter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

/**
 Controller for routes and flights view, client write application.
 disables and hides all buttons/menus, that write client must not see
 */
class RoutesFlightsWriteOverviewController extends RoutesFlightsOverviewController{

    RoutesFlightsWriteOverviewController( Stage thisStage ){
        super( thisStage );
    }

    /**
     initialization of view
     Hiding menus, add listeners, start thread
     */
    @Override
    @FXML
    void initialize(){
        super.initialize();
        updateFlightButton.setLayoutX( updateFlightButton.getLayoutX() - 37 );
        searchFlightButton.setLayoutX( searchFlightButton.getLayoutX() - 37 );

        routeConnectLabel.setVisible( false );
        flightConnectLabel.setVisible( false );

        Controller.getInstance().setThread( new WriteThread() );
        Controller.getInstance().startThread();

        addRouteButton.setOnAction( event -> handleAddRouteAction() );
        editRouteButton.setOnAction( event -> handleEditRouteAction() );
        deleteRouteButton.setOnAction( event -> handleDeleteRouteAction() );
        updateRouteButton.setOnAction( event -> handleUpdateRouteAction() );

        addFlightButton.setOnAction( event -> handleAddFlightAction() );
        editFlightButton.setOnAction( event -> handleEditFlightAction() );
        deleteFlightButton.setOnAction( event -> handleDeleteFlightAction() );
        updateFlightButton.setOnAction( event -> handleUpdateFlightAction() );
    }

    /**
     Open add route view
     */
    private void handleAddRouteAction(){
        try{
            Stage popUp = new Stage();
            FXMLLoader loader = new FXMLLoader( getClass().getResource( "/fxml/AddRoutesOverview.fxml" ) );
            AddAndEditRoutesOverviewController controller = new AddAndEditRoutesOverviewController( null , popUp );
            loader.setController( controller );
            popUp.initModality( Modality.APPLICATION_MODAL );
            popUp.initOwner( thisStage );

            popUp.setTitle( ADD_ROUTE_WINDOW );
            popUp.setScene( new Scene( loader.load() ) );
            popUp.setResizable( false );

            thisStage.setOpacity( 0.9 );
            popUp.showAndWait();
            thisStage.setOpacity( 1 );
        }catch( IOException e ){
            e.printStackTrace();
        }
    }

    /**
     Open edit route view
     */
    private void handleEditRouteAction(){
        Optional.ofNullable( routeTable.getSelectionModel().getSelectedItem() ).ifPresent( selectedRoute -> {
            try{
                FXMLLoader loader = new FXMLLoader( getClass().getResource( "/fxml/AddRoutesOverview.fxml" ) );
                Stage popUp = new Stage();
                AddAndEditRoutesOverviewController controller =
                        new AddAndEditRoutesOverviewController( selectedRoute , popUp );
                loader.setController( controller );

                popUp.initModality( Modality.APPLICATION_MODAL );
                popUp.initOwner( thisStage );

                popUp.setTitle( EDIT_ROUTE_WINDOW );
                popUp.setScene( new Scene( loader.load() ) );
                popUp.setResizable( false );

                thisStage.setOpacity( 0.9 );
                popUp.showAndWait();
                thisStage.setOpacity( 1 );
            }catch( IOException e ){
                e.printStackTrace();
            }
        } );
    }

    /**
     Delete route from local DB
     */
    private void handleDeleteRouteAction(){
        Optional.ofNullable( routeTable.getSelectionModel().getSelectedItem() ).ifPresent( selectedRoute -> {
            try{
                DataOutputStream outClient =
                        ( DataOutputStream ) Controller.getInstance().getClientSocket().getOutputStream();
                ObjectMapper mapper = new ObjectMapper();

                Controller.getInstance()
                          .getUserInformation()
                          .setChanges( Collections.singletonList( ListChangeAdapter.removeRoute( Collections.singletonList(
                                  selectedRoute ) ) ) );

                outClient.writeUTF( mapper.writeValueAsString( Controller.getInstance().getUserInformation() ) );
                // get Data
                Controller.getInstance().getUserInformation().setChanges( null );
            }catch( IOException e ){
                System.out.println( "Connection problem" );
                System.out.println( e.getMessage() );
            }
        } );
        /*
          TODO: set message to delete route to server
         */
    }

    /**
     Open add flight view
     */
    private void handleAddFlightAction(){
        try{
            FXMLLoader loader = new FXMLLoader( getClass().getResource( "/fxml/AddFlightsOverview.fxml" ) );
            Stage popUp = new Stage();
            AddAndEditFlightsOverviewController controller = new AddAndEditFlightsOverviewController( null , popUp );
            loader.setController( controller );

            popUp.initModality( Modality.APPLICATION_MODAL );
            popUp.initOwner( thisStage );

            popUp.setTitle( ADD_FLIGHT_WINDOW );
            popUp.setScene( new Scene( loader.load() ) );
            popUp.setResizable( false );

            thisStage.setOpacity( 0.9 );
            popUp.showAndWait();
            thisStage.setOpacity( 1 );
        }catch( IOException e ){
            e.printStackTrace();
        }
    }

    /**
     Open edit flight view
     */
    private void handleEditFlightAction(){
        Optional.ofNullable( flightTable.getSelectionModel().getSelectedItem() ).ifPresent( selectedFlight -> {
            try{
                FXMLLoader loader = new FXMLLoader( getClass().getResource( "/fxml/AddFlightsOverview.fxml" ) );
                Stage popUp = new Stage();
                AddAndEditFlightsOverviewController controller =
                        new AddAndEditFlightsOverviewController( selectedFlight , popUp );
                loader.setController( controller );

                popUp.initModality( Modality.APPLICATION_MODAL );
                popUp.initOwner( thisStage );

                popUp.setTitle( EDIT_FLIGHT_WINDOW );
                popUp.setScene( new Scene( loader.load() ) );
                popUp.setResizable( false );

                thisStage.setOpacity( 0.9 );
                popUp.showAndWait();
                thisStage.setOpacity( 1 );
            }catch( IOException e ){
                e.printStackTrace();
            }
        } );
    }


    private void handleDeleteFlightAction(){
        Optional.ofNullable( flightTable.getSelectionModel().getSelectedItem() ).ifPresent( selectedFlight -> {
            try{
                DataOutputStream outClient =
                        ( DataOutputStream ) Controller.getInstance().getClientSocket().getOutputStream();
                ObjectMapper mapper = new ObjectMapper();

                Controller.getInstance()
                          .getUserInformation()
                          .setChanges( Collections.singletonList( ListChangeAdapter.removeFlight( Collections.singletonList(
                                  selectedFlight ) ) ) );

                outClient.writeUTF( mapper.writeValueAsString( Controller.getInstance().getUserInformation() ) );
                // get Data

                Controller.getInstance().getUserInformation().setChanges( null );
            }catch( IOException e ){
                System.out.println( "Connection problem" );
                System.out.println( e.getMessage() );
            }
        } );
        /*
          TODO: add message to server to delete flight
         */
    }

    public void handleUpdateRouteAction(){

        if( Controller.getInstance().getClientSocket().isClosed() ){
            Controller.getInstance().reconnect();
        }
        if( !Controller.getInstance().getClientSocket().isConnected() ){
            routeConnectLabel.setText( "Offline" );
            flightConnectLabel.setText( "Offline" );
            Controller.getInstance().reconnect();
        }
        if( Controller.getInstance().getClientSocket().isConnected() ){
            routeConnectLabel.setText( "Online" );
            flightConnectLabel.setText( "Online" );
            Data data;
            ObjectMapper mapper = new ObjectMapper();
            Controller.getInstance()
                      .getUserInformation()
                      .setPredicate( ( SerializablePredicate<? extends FlightOrRoute> ) route -> true );
            try( DataOutputStream dataOutputStream = new DataOutputStream( Controller.getInstance()
                                                                                     .getClientSocket()
                                                                                     .getOutputStream() ) ;
                 DataInputStream inputStream = new DataInputStream( Controller.getInstance()
                                                                              .getClientSocket()
                                                                              .getInputStream() ) ){
                dataOutputStream.writeUTF( mapper.writeValueAsString( Controller.getInstance().getUserInformation() ) );
                data = mapper.readerFor( Data.class ).readValue( inputStream.readUTF() );
                //noinspection CodeBlock2Expr
                data.withoutExceptionOrWith( data1 -> {
                    data1.getChanges().forEach( update -> update.apply( DataModelInstanceSaver.getInstance() ) );
                } , ClientMain::showWarningByError );
            }catch( IOException | NullPointerException ex ){
                System.out.println( ex.getMessage() );
                ex.printStackTrace();
                System.out.println( "Yep" );
            }
            Controller.getInstance().getUserInformation().setPredicate( null );
        }
    }

    public void handleUpdateFlightAction(){

        if( Controller.getInstance().getClientSocket().isClosed() ){
            Controller.getInstance().reconnect();
        }
        if( !Controller.getInstance().getClientSocket().isConnected() ){
            routeConnectLabel.setText( "Offline" );
            flightConnectLabel.setText( "Offline" );
            Controller.getInstance().reconnect();
        }
        if( Controller.getInstance().getClientSocket().isConnected() ){
            routeConnectLabel.setText( "Online" );
            flightConnectLabel.setText( "Online" );
            Data data;
            ObjectMapper mapper = new ObjectMapper();
            Controller.getInstance()
                      .getUserInformation()
                      .setPredicate( ( SerializablePredicate<? extends FlightOrRoute> ) flight -> true );
            try( DataOutputStream dataOutputStream = new DataOutputStream( Controller.getInstance()
                                                                                     .getClientSocket()
                                                                                     .getOutputStream() ) ;
                 DataInputStream inputStream = new DataInputStream( Controller.getInstance()
                                                                              .getClientSocket()
                                                                              .getInputStream() ) ){
                dataOutputStream.writeUTF( mapper.writeValueAsString( Controller.getInstance().getUserInformation() ) );
                data = mapper.readerFor( Data.class ).readValue( inputStream.readUTF() );
                //noinspection CodeBlock2Expr
                data.withoutExceptionOrWith( data1 -> {
                    data1.getChanges().forEach( update -> update.apply( DataModelInstanceSaver.getInstance() ) );
                } , ClientMain::showWarningByError );
            }catch( IOException | NullPointerException ex ){
                System.out.println( ex.getMessage() );
                ex.printStackTrace();
                System.out.println( "Yep" );
            }
            Controller.getInstance().getUserInformation().setPredicate( null );
        }
    }
}

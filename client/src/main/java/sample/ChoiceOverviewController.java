package sample;

import com.fasterxml.jackson.databind.ObjectMapper;
import exceptions.FlightAndRouteException;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.DataModel;
import model.DataModelInstanceSaver;
import transport.Data;

import java.io.*;
import java.nio.file.Files;
import java.util.Map;
import java.util.Optional;

/**
 * Choice overview controller. Allows user to select DM
 */

class ChoiceOverviewController{

    ObservableMap<String, String>             map;
    ObservableList<Map.Entry<String, String>> observableList;
    @FXML   Button                                         cancelButton;
    @FXML   Button                                         selectButton;
    @FXML   TableView                                      baseTable;
    @FXML   TableColumn<Map.Entry<String, String>, String> nameColumn;
    @FXML   TableColumn<Map.Entry<String, String>, String> rightsColumn;
    private Stage                                          thisStage;
    private Data                                           data;

    ChoiceOverviewController( Stage thisStage , Data data ){
        this.thisStage = thisStage;
        this.data = data;
    }

    /**
     initializing of view
     */
    @FXML
    private void initialize(){
        selectButton.setOnAction( event -> handleSelectAction() );
        cancelButton.setOnAction( event -> handleCancelAction() );
        baseTable.getSelectionModel().setSelectionMode( SelectionMode.SINGLE );
        nameColumn.setCellValueFactory( ( TableColumn.CellDataFeatures<Map.Entry<String, String>, String> p ) -> new SimpleStringProperty(
                p.getValue().getKey() ) );
        rightsColumn.setCellValueFactory( ( TableColumn.CellDataFeatures<Map.Entry<String, String>, String> p ) -> new SimpleStringProperty(
                p.getValue().getValue() ) );
        map = FXCollections.observableMap( data.getBases() );
        observableList = FXCollections.observableArrayList( map.entrySet() );
        baseTable.setItems( observableList );
        baseTable.getColumns().setAll( nameColumn , rightsColumn );
        baseTable.getSelectionModel().select( 0 );
        baseTable.setOnKeyPressed( enterHandler );
    }

    EventHandler<KeyEvent> enterHandler = event -> {
        if( event.getCode() == KeyCode.ENTER ) handleSelectAction();
    };


    /**
     * Select button handler. Allows to select DM, create view related to this type of DM, receive data
     */
    private void handleSelectAction(){

        Optional.ofNullable( baseTable.getSelectionModel().getSelectedItem() ).ifPresent( selectedBase -> {
            Controller.getInstance()
                      .getUserInformation()
                      .setDataBase( ( ( Map.Entry<String, String> ) selectedBase ).getKey() );
            Stage primaryStage = new Stage();
            RoutesFlightsOverviewController
                    controller =
                    ( ( Map.Entry<String, String> ) selectedBase ).getValue().toUpperCase().equals( "READWRITE" ) ?
                    new RoutesFlightsWriteOverviewController( primaryStage ) :
                    new RoutesFlightsReadOnlyOverviewController( primaryStage );
            if( Controller.getInstance().getClientSocket().isClosed() ){
                Controller.getInstance().reconnect();
            }
            ObjectMapper mapper = new ObjectMapper();

            try{
                DataOutputStream
                        dataOutputStream =
                        new DataOutputStream( Controller.getInstance().getClientSocket().getOutputStream() );
                DataInputStream
                        inputStream =
                        new DataInputStream( Controller.getInstance().getClientSocket().getInputStream() );
                dataOutputStream.writeUTF( mapper.writeValueAsString( Controller.getInstance().getUserInformation() ) );
                System.out.println( "Ушло" );
                String testString = inputStream.readUTF();
                System.out.println( testString );
                data = mapper.readerFor( Data.class ).readValue( testString );
            }catch( IOException e ){
                throw new IllegalStateException( "load problem " + e.getMessage() , e );
            }
            data.withoutExceptionOrWith( data1 -> {
                try{
                    if( ( controller instanceof RoutesFlightsReadOnlyOverviewController ) &&
                        ( data1.getRoutes() == null ) &&
                        ( new File( Controller.getInstance().getUserInformation().getDataBase() + ".dm" ) ).exists() ){
                        DataModelInstanceSaver.getInstance()
                                              .importFrom( new FileInputStream( Controller.getInstance()
                                                                                          .getUserInformation()
                                                                                          .getDataBase() + ".dm" ) );
                        data1.getChanges()
                             .forEach( update -> update.apply( DataModelInstanceSaver.getInstance() , false ) );
                    }else{
                        System.out.println( ( new File( Controller.getInstance().getUserInformation().getDataBase() +
                                                        ".dm" ) ).exists() );
                        data1.getRoutes()
                             .forEach( route -> DataModelInstanceSaver.getInstance().addRoute( route , false ) );
                        data1.getFlights().forEach( DataModelInstanceSaver.getInstance()::addFlight );
                    }
                    FXMLLoader loader = new FXMLLoader( getClass().getResource( "/fxml/RoutesFlightsOverview.fxml" ) );
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
                }catch( NullPointerException ex ){
                    System.out.println( ex.getMessage() );
                }
            } , ClientMain::showWarningByError );
        } );
    }

    /**
     * Cancel button handler. Allow to go back to LogIn view
     */
    private void handleCancelAction(){
        try{
            Stage                   loginStage = new Stage();
            FXMLLoader              loader     = new FXMLLoader( getClass().getResource( "/fxml/LoginOverview.fxml" ) );
            LoginOverviewController controller = new LoginOverviewController( loginStage );
            loader.setController( controller );
            loginStage.setTitle( "Login" );
            Scene scene = new Scene( loader.load() );
            loginStage.setScene( scene );
            loginStage.setResizable( false );
            loginStage.show();
        }catch( IOException e ){
            System.out.println( e.getMessage() );
        }
        closeWindow();
    }

    private void closeWindow(){
        thisStage.close();
    }
}

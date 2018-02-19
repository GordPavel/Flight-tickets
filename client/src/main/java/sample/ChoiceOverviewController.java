package sample;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import model.DataModelInstanceSaver;
import transport.Data;
import transport.UserInformation;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.Optional;

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
        selectButton.setOnAction( this::handleSelectAction );
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
        baseTable.setOnKeyReleased( event -> {
            if( event.getCode() == KeyCode.ENTER ) handleSelectAction( event );
        } );
    }

    private void handleSelectAction( Event event ){
        Optional.ofNullable( baseTable.getSelectionModel().getSelectedItem() )
                .map( selectedItem -> ( Map.Entry<String, String> ) selectedItem )
                .ifPresent( selectedItem -> {
                    try{
                        Socket socket = new Socket( Controller.getInstance().host , Controller.getInstance().port );
                        Controller.getInstance().connection = socket;
                        ObjectMapper mapper = new ObjectMapper();
                        Controller.getInstance().base = selectedItem.getKey();
                        UserInformation request = new UserInformation( Controller.getInstance().login ,
                                                                       Controller.getInstance().password ,
                                                                       Controller.getInstance().base );
                        DataOutputStream outputStream = new DataOutputStream( socket.getOutputStream() );
                        DataInputStream  inputStream  = new DataInputStream( socket.getInputStream() );
                        outputStream.writeUTF( mapper.writeValueAsString( request ) );
                        Data response = mapper.readerFor( Data.class ).readValue( inputStream.readUTF() );
                        response.withoutExceptionOrWith( data -> {
                            DataModelInstanceSaver.getInstance()
                                                  .getRouteObservableList()
                                                  .setAll( response.getRoutes() );
                            DataModelInstanceSaver.getInstance()
                                                  .getFlightObservableList()
                                                  .setAll( response.getFlights() );
                            try{
                                Stage primaryStage = new Stage();
                                RoutesFlightsOverviewController controller =
                                        selectedItem.getValue().equalsIgnoreCase( "READWRITE" ) ?
                                        new RoutesFlightsWriteOverviewController( primaryStage ) :
                                        new RoutesFlightsReadOnlyOverviewController( primaryStage );
                                FXMLLoader loader =
                                        new FXMLLoader( getClass().getResource( "/fxml/RoutesFlightsOverview.fxml" ) );
                                loader.setController( controller );
                                primaryStage.setTitle( "Information system about flights and routes" );
                                Scene scene = new Scene( loader.load() , 700 , 500 );
                                primaryStage.setScene( scene );
                                primaryStage.setResizable( false );
                                closeWindow();
                                primaryStage.show();
                            }catch( IOException e ){
                                System.err.println( "Loading main screen error" );
                                e.printStackTrace();
                            }
                        } , ClientMain::showWarningByError );
                    }catch( IOException e ){
                        System.err.println( "Connection error" );
                        e.printStackTrace();
                    }
                } );
    }

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
            closeWindow();
            loginStage.show();
        }catch( IOException e ){
            System.out.println( e.getMessage() );
        }
    }

    private void closeWindow(){
        thisStage.close();
    }

    static void openChoiceDBScreen( Data data ){
        try{
            Stage primaryStage = new Stage();
            FXMLLoader loader =
                    new FXMLLoader( ChoiceOverviewController.class.getResource( "/fxml/ChoiceOverview.fxml" ) );
            ChoiceOverviewController controller = new ChoiceOverviewController( primaryStage , data );
            loader.setController( controller );
            primaryStage.setTitle( "Select DB" );
            Scene scene = new Scene( loader.load() );
            primaryStage.setScene( scene );
            primaryStage.setResizable( false );
            primaryStage.show();
        }catch( IOException e ){
            System.err.println( "load problem" );
            System.err.println( e.getMessage() );
        }
    }
}

package sample;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.DataModelInstanceSaver;
import model.Flight;
import model.Route;
import org.codehaus.jackson.map.ObjectMapper;
import transport.Data;
import transport.UserInformation;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

class ChoiceOverviewController{

    private Stage thisStage;
    private Data data;
    ObservableMap<String, String> map;
    ObservableList<Map.Entry<String, String>> observableList;

    ChoiceOverviewController( Stage thisStage, Data data ){
        this.thisStage = thisStage;
        this.data = data;
    }

    @FXML Button cancelButton;
    @FXML Button selectButton;
    @FXML TableView baseTable;
    @FXML TableColumn<Map.Entry<String, String>, String> nameColumn;
    @FXML TableColumn<Map.Entry<String, String>, String> rightsColumn;

    /**
     initializing of view
     */
    @FXML
    private void initialize(){
        selectButton.setOnAction( event -> handleSelectAction() );
        cancelButton.setOnAction( event -> closeWindow() );
        baseTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        nameColumn.setCellValueFactory(
                (TableColumn.CellDataFeatures<Map.Entry<String,String>, String> p) ->
                        new SimpleStringProperty(p.getValue().getKey()));
        rightsColumn.setCellValueFactory(
                (TableColumn.CellDataFeatures<Map.Entry<String,String>, String> p) ->
                        new SimpleStringProperty(p.getValue().getValue()));
        map = FXCollections.observableMap(data.getBases());
        observableList = FXCollections.observableArrayList(map.entrySet());
        baseTable.setItems(observableList);
        baseTable.getColumns().setAll(nameColumn, rightsColumn);
        baseTable.getSelectionModel().select(0);
    }

    private void handleSelectAction(){

        /*
          TODO: Select database

          Send your login and password to server. true? go below : retry message
          Add view with table of available DB...
          Load db to dataModel and execute code below
         */
        Optional.ofNullable( baseTable.getSelectionModel().getSelectedItem() ).ifPresent(selectedBase -> {
            Controller.getInstance().getUserInformation().setDataBase(((Map.Entry<String,String>)selectedBase).getKey());
            Stage primaryStage = new Stage();
            RoutesFlightsOverviewController controller = new RoutesFlightsReadOnlyOverviewController(primaryStage);
            if (((Map.Entry<String,String>)selectedBase).getValue().toUpperCase().equals("READWRITE")) {
                controller = new RoutesFlightsWriteOverviewController(primaryStage);
            }
            if (Controller.getInstance().getClientSocket().isClosed())
            {
                Controller.getInstance().reconnect();
            }
            ObjectMapper mapper = new ObjectMapper();
            try (DataOutputStream dataOutputStream = new DataOutputStream(Controller.getInstance().getClientSocket().getOutputStream());
                 DataInputStream inputStream = new DataInputStream(Controller.getInstance().getClientSocket().getInputStream())){
                dataOutputStream.writeUTF(mapper.writeValueAsString(Controller.getInstance().getUserInformation()));
                System.out.println("Yshlo2");
                String testString = inputStream.readUTF();
                System.out.println(testString);
                data = mapper.reader(Data.class).readValue(testString);
                if (data.notHasException()) {
                    for (Route route : data.getRoutes()) {
                        DataModelInstanceSaver.getInstance().addRoute(route);
                    }
                    for (Flight flight : data.getFlights()) {
                        DataModelInstanceSaver.getInstance().addFlight(flight);
                    }
                    FXMLLoader loader = new FXMLLoader( getClass().getResource( "/fxml/RoutesFlightsOverview.fxml" ) );
                    loader.setController( controller );
                    primaryStage.setTitle( "Information system about flights and routes" );
                    Scene scene = new Scene( loader.load() , 700 , 500 );
                    primaryStage.setScene( scene );
                    primaryStage.setResizable( false );
                    primaryStage.show();
                    closeWindow();
                }
                else {
                    Alert alert = new Alert( Alert.AlertType.WARNING );
                    alert.setTitle( "Error" );
                    alert.setHeaderText( "Server error" );
                    alert.setContentText( data.getException().getMessage() );
                    alert.showAndWait();
                }
            }catch( IOException e ){
                System.out.println( "load problem" );
                System.out.println( e.getMessage() );
            } catch ( NullPointerException ex) {
            System.out.println(ex.getMessage());
        }
    } );


//        // if write
//        try{
//
//            Controller.getInstance().setUserInformation(new UserInformation());
//            Stage primaryStage = new Stage();
//            FXMLLoader loader = new FXMLLoader( getClass().getResource( "/fxml/RoutesFlightsOverview.fxml" ) );
//            RoutesFlightsOverviewController controller = new RoutesFlightsReadOnlyOverviewController( primaryStage );
//            loader.setController( controller );
//            primaryStage.setTitle( "Information system about flights and routes" );
//            Scene scene = new Scene( loader.load() , 700 , 500 );
//            primaryStage.setScene( scene );
//            primaryStage.setResizable( false );
//            primaryStage.show();
//            closeWindow();
//        }catch( IOException e ){
//            System.out.println( "load problem" );
//            System.out.println( e.getMessage() );
//        }


    }

    private void closeWindow(){
        thisStage.close();
    }
}

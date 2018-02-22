package sample;

import exceptions.FlightAndRouteException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.DataModelInstanceSaver;
import model.Flight;
import model.Route;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

class RoutesFlightsLocalFileOverviewController extends RoutesFlightsOverviewController{
    RoutesFlightsLocalFileOverviewController( Stage thisStage ){
        super( thisStage );
    }

    @Override
    void initialize(){
        super.initialize();
        deleteRouteButton.setOnAction( event -> handleDeleteRouteButton() );
        deleteFlightButton.setOnAction( event -> handleDeleteFlightButton() );
        mergeMenuButton.setOnAction( event -> handleMergeAction() );
        searchRouteButton.setVisible( false );
        searchFlightButton.setOnAction( event -> handleSearchFlightButton() );
        addFlightButton.setOnAction( event -> handleAddFlightButton() );
        addRouteButton.setOnAction(event -> handleAddRouteButton());
        editFlightButton.setOnAction( event -> handleEditFlightButton() );
        editRouteButton.setOnAction(event -> handleEditRouteButton());

        editFlightButton.disableProperty().bind( flightTable.getSelectionModel().selectedItemProperty().isNull() );
        deleteFlightButton.disableProperty().bind( flightTable.getSelectionModel().selectedItemProperty().isNull() );
        editRouteButton.disableProperty().bind( routeTable.getSelectionModel().selectedItemProperty().isNull() );
        deleteRouteButton.disableProperty().bind( routeTable.getSelectionModel().selectedItemProperty().isNull() );

        updateRouteButton.setVisible( false );
        updateFlightButton.setVisible( false );

        thisStage.setOnCloseRequest( event -> closeWindow() );
        logoutMenuButton.setOnAction( event -> handleLogOutAction() );
    }

    @FXML
    void handleAddRouteButton(){
        try{
            Stage popUp = new Stage();
            FXMLLoader loader = new FXMLLoader( getClass().getResource( "/fxml/AddRoutesOverview.fxml" ) );
            AddAndEditRoutesOverviewController controller = new AddAndEditRoutesLocalOverviewController( null , popUp );
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

    @FXML
    void handleEditRouteButton(){
        Optional.ofNullable( routeTable.getSelectionModel().getSelectedItem() ).ifPresent( selectedRoute -> {
            try{
                FXMLLoader loader = new FXMLLoader( getClass().getResource( "/fxml/AddRoutesOverview.fxml" ) );
                Stage      popUp  = new Stage();
                AddAndEditRoutesOverviewController
                        controller =
                        new AddAndEditRoutesLocalOverviewController( selectedRoute , popUp );
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

    private void handleDeleteRouteButton(){
        Optional.ofNullable( routeTable.getSelectionModel().getSelectedItem() ).ifPresent( selectedRoute -> {
            try{
                DataModelInstanceSaver.getInstance().removeRoute( selectedRoute );
                Controller.changed = true;
            }catch( FlightAndRouteException e ){
                showModelAlert( e );
            }
        } );
    }

    @FXML
    void handleAddFlightButton(){
        try{
            FXMLLoader loader = new FXMLLoader( getClass().getResource( "/fxml/AddFlightsOverview.fxml" ) );
            Stage      popUp  = new Stage();
            AddAndEditFlightsOverviewController
                    controller =
                    new AddAndEditFlightsLocalOverviewController( null , popUp );
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

    @FXML
    void handleEditFlightButton(){
        Optional.ofNullable( flightTable.getSelectionModel().getSelectedItem() ).ifPresent( selectedFlight -> {
            try{
                FXMLLoader loader = new FXMLLoader( getClass().getResource( "/fxml/AddFlightsOverview.fxml" ) );
                Stage      popUp  = new Stage();
                AddAndEditFlightsOverviewController
                        controller =
                        new AddAndEditFlightsLocalOverviewController( selectedFlight , popUp );
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

    private void handleDeleteFlightButton(){
        Optional.ofNullable( flightTable.getSelectionModel().getSelectedItem() ).ifPresent( selectedFlight -> {
            try{
                DataModelInstanceSaver.getInstance().removeFlight( selectedFlight.getNumber() );
                Controller.changed = true;
            }catch( FlightAndRouteException e ){
                showModelAlert( e );
            }
        } );
    }

    /**
     Merging data from file with local DB
     */
    private void handleMergeAction(){
        Optional.ofNullable( fileChooser.showOpenDialog( new Stage() ) ).ifPresent( file -> {
            try{
                Controller.changed = true;
                List<Serializable>
                        failedInMerge =
                        DataModelInstanceSaver.getInstance()
                                              .mergeData( Files.newInputStream( file.toPath() ) )
                                              .collect( toList() );

                ObservableList<Flight>
                        mergeFlights =
                        failedInMerge.parallelStream()
                                     .filter( element -> element.getClass().equals( Flight.class ) )
                                     .map( Flight.class::cast )
                                     .collect( Collectors.collectingAndThen( toList() ,
                                                                             FXCollections::observableArrayList ) );
                ObservableList<Route>
                        mergeRoutes =
                        failedInMerge.parallelStream()
                                     .filter( element -> element.getClass().equals( Route.class ) )
                                     .map( Route.class::cast )
                                     .collect( Collectors.collectingAndThen( toList() ,
                                                                             FXCollections::observableArrayList ) );
                String
                        errors =
                        failedInMerge.stream()
                                     .map( Serializable::toString )
                                     .collect( Collectors.joining( "\n-" , "-" , "\n" ) );
                Controller.getInstance().setMergeFlights( FXCollections.observableArrayList( mergeFlights ) );
                Controller.getInstance().setMergeRoutes( FXCollections.observableArrayList( mergeRoutes ) );

                if( !failedInMerge.isEmpty() ){
                    ClientMain.showWarning( "Merge results" , "Model have this problems with merge:" , errors );
                }

                if( !mergeFlights.isEmpty() ){
                    Stage                   popUp                   = new Stage();
                    FXMLLoader
                                            loader                  =
                            new FXMLLoader( getClass().getResource( "/fxml/mergeOverview.fxml" ) );
                    MergeOverviewController mergeOverviewController = new MergeOverviewController( popUp );
                    loader.setController( mergeOverviewController );
                    Scene scene = new Scene( loader.load() );
                    popUp.initModality( Modality.APPLICATION_MODAL );
                    popUp.setTitle( ADD_ROUTE_WINDOW );
                    popUp.setScene( scene );
                    popUp.setResizable( false );
                    popUp.showAndWait();
                }
            }catch( IOException | FlightAndRouteException e ){
                new Alert( Alert.AlertType.ERROR , e.getMessage() ).show();
            }
        } );
    }

    @FXML
    void handleSearchFlightButton(){
        if( !Controller.getInstance().isFlightSearchActive() ){
            Controller.getInstance().setFlightSearchActive( true );
            try{
                Stage      popUp  = new Stage();
                FXMLLoader loader = new FXMLLoader( getClass().getResource( "/fxml/SearchFlightsOverview.fxml" ) );
                SearchFlightsOverviewController
                        searchFlights =
                        new SearchFlightsLocalOverviewController( this , popUp );
                loader.setController( searchFlights );
                Scene scene = new Scene( loader.load() );

                popUp.initModality( Modality.NONE );
                popUp.initOwner( thisStage.getOwner() );
                popUp.setX( thisStage.getX() + thisStage.getWidth() );
                popUp.setY( thisStage.getY() );

                popUp.setTitle( SEARCH_FLIGHT_WINDOW );
                popUp.setScene( scene );
                popUp.setResizable( false );

                thisStage.setOpacity( 0.9 );
                popUp.show();
                thisStage.setOpacity( 1 );
            }catch( IOException e ){
                e.printStackTrace();
            }
        }
    }

    @Override
    void handleLogOutAction(){
        closeWindow();
        DataModelInstanceSaver.getInstance().clear();
        try{
            Stage loginStage = new Stage();
            FXMLLoader loader = new FXMLLoader( getClass().getResource( "/fxml/LoginOverview.fxml" ) );
            LoginOverviewController logInController = new LoginOverviewController( loginStage );
            loader.setController( logInController );
            loginStage.setTitle( "Login" );
            Scene scene = new Scene( loader.load() );
            loginStage.setScene( scene );
            loginStage.setResizable( false );
            loginStage.show();
            closeWindow();
        }catch( IOException e ){
            throw new IllegalStateException( "File problem " + e.getMessage() , e );
        }
    }

    private void closeWindow(){
        if( Controller.changed ){
            final Optional<ButtonType>
                    buttonType =
                    new Alert( Alert.AlertType.CONFIRMATION ,
                               "Do you want to save changes?" ,
                               ButtonType.YES ,
                               ButtonType.NO ,
                               ButtonType.CANCEL ).showAndWait();
            if( buttonType.isPresent() ){
                if( buttonType.get() == ButtonType.YES ){
                    try( OutputStream save = Files.newOutputStream( Controller.savingFile.toPath() ) ){
                        DataModelInstanceSaver.getInstance().saveTo( save );
                    }catch( IOException e ){
//                          todo : Не удалось открыть файл
                    }
                }else if( buttonType.get() == ButtonType.CANCEL ){
                    return;
                }
            }
        }

        thisStage.close();
    }
}

package sample;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.DataModel;
import model.Flight;
import model.Route;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MainWindowController implements Initializable{
    public TableView<Route>  routesTableView;
    public TableView<Flight> flightsTableView;
    public Button            searchButton;
    public TextField         searchDepartureAirport;
    public TextField         searchArrivalAirport;
    public TextArea          detailsArea;

    public MenuItem addRoute;
    public MenuItem editRoute;
    public MenuItem removeRoute;
    public MenuItem addFlight;
    public MenuItem editFlight;
    public MenuItem removeFlight;

    public MenuItem newDatabase;
    public MenuItem openDatabase;
    public MenuItem mergeCurrentWithExternal;
    public MenuItem saveInCurrent;
    public MenuItem saveInFile;

    private DataModel dataModel = DataModel.getInstance();
    private Stage thisStage;

    MainWindowController( Stage thisStage ){
        this.thisStage = thisStage;
    }

    @FXML
    @Override
    public void initialize( URL location , ResourceBundle resources ){
        newDatabase.setOnAction( event -> {
            dataModel.clear();
            updateFlightsList();
            updateRoutesList();
        } );
        openDatabase.setOnAction( event -> {
            Stage stage = new Stage();
            stage.setTitle( "Choose file" );
            FileChooser    fileChooser  = new FileChooser();
            Optional<File> fileOptional = Optional.ofNullable( fileChooser.showOpenDialog( stage ) );
            fileOptional.ifPresent( file -> {
                try{
                    dataModel.importFromFile( file );
                }catch( IOException e ){
                    e.printStackTrace();
                }
            } );
        } );

//        Routes view
        routesTableView.setEditable( true );
        routesTableView.getSelectionModel().setSelectionMode( SelectionMode.SINGLE );
        ObservableList<TableColumn<Route, ?>> routesColumns = routesTableView.getColumns();
        routesColumns.get( 0 ).setCellValueFactory( new PropertyValueFactory<>( "from" ) );
        routesColumns.get( 1 ).setCellValueFactory( new PropertyValueFactory<>( "to" ) );
        updateRoutesList();

//        Flights view
        flightsTableView.setEditable( false );
        flightsTableView.getSelectionModel().setSelectionMode( SelectionMode.SINGLE );
        ObservableList<TableColumn<Flight, ?>> flightsColumns = flightsTableView.getColumns();
        flightsColumns.get( 0 ).setCellValueFactory( new PropertyValueFactory<>( "number" ) );
        flightsColumns.get( 1 ).setCellValueFactory( new PropertyValueFactory<>( "routeString" ) );
        flightsColumns.get( 2 ).setCellValueFactory( new PropertyValueFactory<>( "departureDateString" ) );
        flightsColumns.get( 3 ).setCellValueFactory( new PropertyValueFactory<>( "arriveDateString" ) );
        updateFlightsList();

//        Search button
        ImageView imageView = new ImageView( new Image( "searchPic.png" ) );
        imageView.setFitWidth( 60 );
        imageView.setFitHeight( 60 );
        searchButton.setGraphic( imageView );
        searchButton.setContentDisplay( ContentDisplay.CENTER );
        searchButton.setOnAction( event -> initSearchFlightWindow() );

//        Menu items
        addRoute.setOnAction( event -> initAddRouteWindow() );
        editRoute.setOnAction( event -> initEditRouteWindow() );
        removeRoute.setOnAction( event -> {
            Optional<Route> removingRouteOptional =
                    Optional.ofNullable( routesTableView.getSelectionModel().getSelectedItem() );
            if( !removingRouteOptional.isPresent() ){
                new Alert( Alert.AlertType.WARNING , "Select any route" , ButtonType.OK ).showAndWait();
                return;
            }
            dataModel.removeRoute( removingRouteOptional.get() );
            updateRoutesList();
            updateFlightsList();
        } );
        addFlight.setOnAction( event -> initAddFlightWindow() );
        editFlight.setOnAction( event -> initEditFlightWindow() );
        removeFlight.setOnAction( event -> {
            Optional<Flight> removingFlightOptional =
                    Optional.ofNullable( flightsTableView.getSelectionModel().getSelectedItem() );
            if( !removingFlightOptional.isPresent() ){
                new Alert( Alert.AlertType.WARNING , "Select any route" , ButtonType.OK ).showAndWait();
                return;
            }
            dataModel.removeFlight( removingFlightOptional.get().getNumber() );
            updateFlightsList();
        } );

//        Search route text fields
        searchDepartureAirport.textProperty().addListener( ( observable , oldValue , newValue ) -> {
            fromAirport = newValue.isEmpty() ? null : newValue;
            updateRoutesList();
        } );
        searchArrivalAirport.textProperty().addListener( ( observable , oldValue , newValue ) -> {
            toAirPort = newValue.isEmpty() ? null : newValue;
            updateRoutesList();
        } );

        flightsTableView.setOnMouseClicked(
                event -> detailsArea.setText( flightsTableView.getSelectionModel().getSelectedItem().toString() ) );
    }

    private void initSearchFlightWindow(){
        try{
            Stage                   stage                   = new Stage();
            SearchFlightsController searchFlightsController = new SearchFlightsController( this , stage );
            FXMLLoader loader = new FXMLLoader( getClass().getResource( "/fxml/SearchFlightWindow.fxml" ) );
            loader.setController( searchFlightsController );
            stage.setX( thisStage.getX() + thisStage.getWidth() );
            stage.setY( thisStage.getY() );
            Scene sce = new Scene( loader.load() );
            sce.getStylesheets().add( getClass().getResource( "/fxml/text-field-time-error.css" ).toExternalForm() );
            stage.setScene( sce );
            stage.setTitle( "Search flight" );
            stage.setResizable( false );
            stage.show();
        }catch( IOException e ){
            e.printStackTrace();
        }
    }

    private void initAddFlightWindow(){
        try{
            Stage stage = new Stage();
            AddAndEditFlightWindowController addAndEditFlightWindowController =
                    new AddAndEditFlightWindowController( this , stage , null );
            FXMLLoader loader = new FXMLLoader( getClass().getResource( "/fxml/AddAndEditFlightWindow.fxml" ) );
            loader.setController( addAndEditFlightWindowController );
            Scene scene = new Scene( loader.load() );
            scene.getStylesheets().add( getClass().getResource( "/fxml/auto-fill-text-field.css" ).toExternalForm() );
            stage.setScene( scene );

            stage.initModality( Modality.WINDOW_MODAL );
            stage.initOwner( thisStage.getScene().getWindow() );
            stage.setTitle( "Add flight" );
            stage.setResizable( false );
            stage.show();
        }catch( IOException e ){
            e.printStackTrace();
        }
    }

    private void initEditFlightWindow(){
        try{
            Stage stage = new Stage();
            AddAndEditFlightWindowController addAndEditFlightWindowController =
                    new AddAndEditFlightWindowController( this , stage , Optional.ofNullable(
                            flightsTableView.getSelectionModel().getSelectedItem() ).orElseThrow(
                            IllegalArgumentException::new ) );
            FXMLLoader loader = new FXMLLoader( getClass().getResource( "/fxml/AddAndEditFlightWindow.fxml" ) );
            loader.setController( addAndEditFlightWindowController );
            Scene scene = new Scene( loader.load() );
            scene.getStylesheets().add( getClass().getResource( "/fxml/auto-fill-text-field.css" ).toExternalForm() );
            stage.setScene( scene );

            stage.initModality( Modality.WINDOW_MODAL );
            stage.initOwner( thisStage.getScene().getWindow() );
            stage.setTitle( "Add flight" );
            stage.setResizable( false );
            stage.show();
        }catch( IllegalArgumentException e ){
            new Alert( Alert.AlertType.WARNING , "Select any flight" , ButtonType.OK ).showAndWait();
        }catch( IOException e ){
            e.printStackTrace();
        }
    }

    private void initAddRouteWindow(){
        try{
            Stage stage = new Stage();
            AddAndEditRouteWindowController addAndEditRouteWindowController =
                    new AddAndEditRouteWindowController( this , stage , null );
            FXMLLoader loader = new FXMLLoader( getClass().getResource( "/fxml/AddAndEditRouteWindow.fxml" ) );
            loader.setController( addAndEditRouteWindowController );
            Scene scene = new Scene( loader.load() );
            scene.getStylesheets().add( getClass().getResource( "/fxml/auto-fill-text-field.css" ).toExternalForm() );
            stage.setScene( scene );

            stage.initModality( Modality.WINDOW_MODAL );
            stage.initOwner( thisStage.getScene().getWindow() );
            stage.setTitle( "Add route" );
            stage.setResizable( false );
            stage.show();
        }catch( IOException e ){
            e.printStackTrace();
        }
    }

    private void initEditRouteWindow(){
        try{
            Stage stage = new Stage();
            AddAndEditRouteWindowController addAndEditRouteWindowController =
                    new AddAndEditRouteWindowController( this , stage , Optional.ofNullable(
                            routesTableView.getSelectionModel().getSelectedItem() ).orElseThrow(
                            IllegalArgumentException::new ) );
            FXMLLoader loader = new FXMLLoader( getClass().getResource( "/fxml/AddAndEditRouteWindow.fxml" ) );
            loader.setController( addAndEditRouteWindowController );
            Scene scene = new Scene( loader.load() );
            scene.getStylesheets().add( getClass().getResource( "/fxml/auto-fill-text-field.css" ).toExternalForm() );
            stage.setScene( scene );

            stage.initModality( Modality.WINDOW_MODAL );
            stage.initOwner( thisStage.getScene().getWindow() );
            stage.setTitle( "Edit route" );
            stage.setResizable( false );
            stage.show();
        }catch( IllegalArgumentException e ){
            new Alert( Alert.AlertType.WARNING , "Select any route" , ButtonType.OK ).showAndWait();
        }catch( IOException e ){
            e.printStackTrace();
        }
    }

    //    Flights filters
    String    number;
    String    plane;
    String    from;
    String    to;
    LocalDate departureDate;
    LocalTime startDepartureTimeRange;
    LocalTime endDepartureTimeRange;
    LocalDate arrivalDate;
    LocalTime startArrivalTimeRange;
    LocalTime endArrivalTimeRange;
    Long      startFLightTimeRange;
    Long      endFLightTimeRange;

    void updateFlightsList(){
        flightsTableView.getItems().setAll( dataModel.listFlightsWithPredicate(
                flight -> makePredicateForString( this.number ).test( flight.getNumber() ) &&
                          makePredicateForString( this.plane ).test( flight.getPlaneID() ) &&
                          makePredicateForString( this.from ).test( flight.getRoute().getFrom() ) &&
                          makePredicateForString( this.to ).test( flight.getRoute().getTo() ) &&
                          makePredicateForDate( this.departureDate , this.startDepartureTimeRange ,
                                                this.endDepartureTimeRange ).test( flight.getDepartureDate() ) &&
                          makePredicateForDate( this.arrivalDate , this.startArrivalTimeRange ,
                                                this.endArrivalTimeRange ).test( flight.getArriveDate() ) &&
                          ( ( startFLightTimeRange != null ? startFLightTimeRange : 0 ) <= flight.getTravelTime() &&
                            flight.getTravelTime() <
                            ( endFLightTimeRange != null ? endFLightTimeRange : Long.MAX_VALUE ) ) )
                                                     .collect( Collectors.toList() ) );
        detailsArea.setText( "" );
    }

    //    Routes filters
    private String fromAirport;
    private String toAirPort;

    private void updateRoutesList(){
        routesTableView.getItems().setAll( dataModel.listRoutesWithPredicate(
                route1 -> makePredicateForString( this.fromAirport ).test( route1.getFrom() ) &&
                          makePredicateForString( this.toAirPort ).test( route1.getTo() ) )
                                                    .collect( Collectors.toList() ) );
        detailsArea.setText( "" );
    }

    private Predicate<String> makePredicateForString( String string ){
        if( string != null && string.isEmpty() ) string = null;
        return Optional.ofNullable( string ).map( s -> Pattern.compile( ".*" + s + ".*" ).asPredicate() )
                       .orElse( s -> true );
    }

    private Predicate<Date> makePredicateForDate( LocalDate date , LocalTime startTime , LocalTime endTime ){
        return date1 -> {
            LocalDateTime localDateTime = LocalDateTime
                    .parse( new SimpleDateFormat( "dd.MM.yyyy HH:mm" ).format( date1 ) ,
                            DateTimeFormatter.ofPattern( "dd.MM.yyyy HH:mm" ) );
            if( date != null ){
                LocalDateTime start = LocalDateTime.of( date , startTime != null ? startTime : LocalTime.MIN );
                LocalDateTime end   = LocalDateTime.of( date , endTime != null ? endTime : LocalTime.MAX );
                return !localDateTime.isBefore( start ) && localDateTime.isBefore( end );
            }else{
                return !localDateTime.toLocalTime().isBefore( startTime != null ? startTime : LocalTime.MIN ) &&
                       localDateTime.toLocalTime().isBefore( endTime != null ? endTime : LocalTime.MAX );
            }
        };
    }

    void addRoute( Route route ){
        dataModel.addRoute( route );
        updateRoutesList();
    }

    void setRoute( Route oldRoute , Route newRoute ){
        dataModel.editRoute( oldRoute , newRoute.getFrom() , newRoute.getTo() );
        updateRoutesList();
    }
}

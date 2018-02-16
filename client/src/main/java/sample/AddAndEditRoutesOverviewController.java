package sample;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jfoenix.controls.JFXButton;
import exceptions.FlightAndRouteException;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.DataModelInstanceSaver;
import model.Route;
import transport.Data;
import transport.ListChangeAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 Controller for adding route view
 Allows to enter data for adding a new route
 */

class AddAndEditRoutesOverviewController{

    @FXML Label             mainLabel;
    @FXML ChoiceBox<String> departureCountryChoice;
    @FXML ChoiceBox<String> destinationCountryChoice;
    @FXML ChoiceBox<ZoneId> departureCityChoice;
    @FXML ChoiceBox<ZoneId> destinationCityChoice;
    @FXML JFXButton         addAndEditRouteButton;
    @FXML JFXButton         clearButton;
    @FXML JFXButton         cancelButton;

    private Route editingRoute;

    private Stage thisStage;
    private Pattern                   pattern = Pattern.compile( "^([\\w/]+)/(\\w+)$" );
    private Map<String, List<ZoneId>> zones   = ZoneId.getAvailableZoneIds()
                                                      .stream()
                                                      .sorted()
                                                      .filter( Pattern.compile( "(Etc|SystemV)/.+" )
                                                                      .asPredicate()
                                                                      .negate()
                                                                      .and( pattern.asPredicate() ) )
                                                      .collect( Collector.of( HashMap::new ,
                                                                              ( Map<String, List<ZoneId>> map , String zone ) -> {
                                                                                  Matcher matcher =
                                                                                          pattern.matcher( zone );
                                                                                  //noinspection ResultOfMethodCallIgnored
                                                                                  matcher.find();
                                                                                  String key = matcher.group( 1 );
                                                                                  final ZoneId zoneId =
                                                                                          ZoneId.of( zone );
                                                                                  if( map.containsKey( key ) ){
                                                                                      map.get( key ).add( zoneId );
                                                                                  }else{
                                                                                      map.put( key ,
                                                                                               new ArrayList<ZoneId>(){{
                                                                                                   add( zoneId );
                                                                                               }} );
                                                                                  }
                                                                              } , ( map1 , map2 ) -> {
                                                                  map1.forEach( ( key1 , value1 ) -> map2.merge( key1 ,
                                                                                                                 value1 ,
                                                                                                                 ( key2 , value2 ) -> key2 )
                                                                                                         .addAll(
                                                                                                                 value1 ) );
                                                                  return map2;
                                                              } ) );

    AddAndEditRoutesOverviewController( Route editingRoute , Stage thisStage ){
        this.editingRoute = editingRoute;
        this.thisStage = thisStage;
    }

    /**
     initialization of view
     */
    @SuppressWarnings( "ResultOfMethodCallIgnored" )
    @FXML
    private void initialize(){
        departureCountryChoice.setItems( zones.keySet()
                                              .stream()
                                              .collect( Collectors.collectingAndThen( toList() ,
                                                                                      FXCollections::observableList ) ) );
        destinationCountryChoice.setItems( zones.keySet()
                                                .stream()
                                                .collect( Collectors.collectingAndThen( toList() ,
                                                                                        FXCollections::observableList ) ) );
        StringConverter<ZoneId> zoneIdStringConverter = new StringConverter<ZoneId>(){
            @Override
            public String toString( ZoneId zone ){
                Matcher matcher = pattern.matcher( zone.getId() );
                matcher.find();
                return matcher.group( 2 );
            }

            @Override
            public ZoneId fromString( String string ){
                return ZoneId.of( string );
            }
        };
        departureCityChoice.setConverter( zoneIdStringConverter );
        destinationCityChoice.setConverter( zoneIdStringConverter );
        departureCountryChoice.getSelectionModel()
                              .selectedItemProperty()
                              .addListener( ( observable , oldValue , newValue ) -> countrySelected( newValue ,
                                                                                                     departureCityChoice ) );
        destinationCountryChoice.getSelectionModel()
                                .selectedItemProperty()
                                .addListener( ( observable , oldValue , newValue ) -> countrySelected( newValue ,
                                                                                                       destinationCityChoice ) );

        BooleanProperty addAvailable = new SimpleBooleanProperty(
                Optional.ofNullable( departureCityChoice.getSelectionModel().getSelectedItem() ).isPresent() &&
                Optional.ofNullable( destinationCityChoice.getSelectionModel().getSelectedItem() ).isPresent() );
        addAvailable.bind( departureCityChoice.getSelectionModel()
                                              .selectedItemProperty()
                                              .isNotNull()
                                              .and( destinationCityChoice.getSelectionModel()
                                                                         .selectedItemProperty()
                                                                         .isNotNull() ) );
        addAndEditRouteButton.setDisable( !addAvailable.getValue() );
        addAndEditRouteButton.disableProperty().bind( addAvailable.not() );
        addAndEditRouteButton.setOnAction( event -> addOrEdit( editingRoute == null ) );

        if( editingRoute != null ){
            Matcher departureCountryMatcher = pattern.matcher( editingRoute.getFrom().getId() );
            departureCountryMatcher.find();
            departureCountryChoice.getSelectionModel().select( departureCountryMatcher.group( 1 ) );
            departureCityChoice.getSelectionModel().select( ZoneId.of( editingRoute.getFrom().getId() ) );

            Matcher destinationCountryMatcher = pattern.matcher( editingRoute.getTo().getId() );
            destinationCountryMatcher.find();
            destinationCountryChoice.getSelectionModel().select( destinationCountryMatcher.group( 1 ) );
            destinationCityChoice.getSelectionModel().select( ZoneId.of( editingRoute.getTo().getId() ) );

            addAndEditRouteButton.setText( "Edit" );
            mainLabel.setText( "Enter new data." );
        }
        clearButton.setOnAction( event -> handleClearData() );
        cancelButton.setOnAction( event -> closeWindow() );
    }

    private void addOrEdit( Boolean isAdd ){
        try{
            if( isAdd ){
                DataModelInstanceSaver.getInstance()
                                      .addRoute( new Route( Optional.ofNullable(
                                              departureCityChoice.getSelectionModel().getSelectedItem() )
                                                                    .orElseThrow( IllegalStateException::new ) ,
                                                            Optional.ofNullable(
                                                                    destinationCityChoice.getSelectionModel()
                                                                                         .getSelectedItem() )
                                                                    .orElseThrow( IllegalStateException::new ) ) );
            }else{
                DataModelInstanceSaver.getInstance()
                                      .editRoute( editingRoute , Optional.ofNullable(
                                              departureCityChoice.getSelectionModel().getSelectedItem() )
                                                                         .orElseThrow( IllegalStateException::new ) ,
                                                  Optional.ofNullable(
                                                          destinationCityChoice.getSelectionModel().getSelectedItem() )
                                                          .orElseThrow( IllegalStateException::new ) );
            }
//            TODO: put here request to server to add route
            try {
                OutputStream outClient = Controller.getInstance().getClientSocket().getOutputStream();
                InputStream inClient = Controller.getInstance().getClientSocket().getInputStream();
                Data data = new Data();
                ObjectMapper mapper = new ObjectMapper();
                ArrayList<ListChangeAdapter> changes = new ArrayList<>();

                if (isAdd){
                    ArrayList<Route> routes = new ArrayList<>();
                    routes.add( new Route( departureCityChoice.getSelectionModel().getSelectedItem(), destinationCityChoice.getSelectionModel().getSelectedItem()));
                    changes.add( ListChangeAdapter.addRoute( routes ) );
                }else{
                    ArrayList<Route> oldRoutes = new ArrayList<>(), newRoutes = new ArrayList<>();
                    oldRoutes.add( editingRoute );
                    newRoutes.add( new Route( departureCityChoice.getSelectionModel().getSelectedItem(), destinationCityChoice.getSelectionModel().getSelectedItem()));
                    changes.add( ListChangeAdapter.editRoute( oldRoutes, newRoutes ) );
                }

                Controller.getInstance().getUserInformation().setChanges( changes ) ;

                mapper.writeValue( outClient, Controller.getInstance().getUserInformation() );
                // get Data
                data = mapper.readValue( Controller.getInstance().getClientSocket().getInputStream() , Data.class );
                Controller.getInstance().getUserInformation().setChanges(null);
            }catch( IOException e ){
                System.out.println("Connection problem");
                System.out.println( e.getMessage() );
            }
            Controller.changed = true;
            closeWindow();
        }catch( FlightAndRouteException e ){
            RoutesFlightsOverviewController.showModelAlert( e );
        }
    }

    private void countrySelected( String newValue , ChoiceBox<ZoneId> cityBox ){
        Optional<String> chosenCorty = Optional.ofNullable( newValue );
        if( chosenCorty.isPresent() ){
            cityBox.setItems( zones.get( chosenCorty.get() )
                                   .stream()
                                   .collect(
                                           Collectors.collectingAndThen( toList() , FXCollections::observableList ) ) );
        }else{
            cityBox.getItems().clear();
        }
    }

    /**
     Clear Button. Clear all fields in GUI
     */
    private void handleClearData(){
        departureCountryChoice.getSelectionModel().clearSelection();
        destinationCountryChoice.getSelectionModel().clearSelection();
        departureCityChoice.getSelectionModel().clearSelection();
        destinationCityChoice.getSelectionModel().clearSelection();
    }

    private void closeWindow(){
        thisStage.close();
    }
}

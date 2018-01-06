package sample;

import com.jfoenix.controls.JFXButton;
import exceptions.FlightAndRouteException;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.DataModelInstanceSaver;
import model.Route;

import java.io.IOException;
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

public class AddAndEditRoutesOverviewController{

    @FXML ChoiceBox<String> departureCountryChoice;
    @FXML ChoiceBox<String> destinationCountryChoice;
    @FXML ChoiceBox<ZoneId> departureCityChoice;
    @FXML ChoiceBox<ZoneId> destinationCityChoice;
    @FXML JFXButton         addAndEditRouteButton;

    private Stage thisStage;
    private Route editingRoute;
    private Pattern                   pattern = Pattern.compile( "^([\\w/]+)/(\\w+)$" );
    private Map<String, List<ZoneId>> zones   = ZoneId.getAvailableZoneIds().stream().sorted().filter(
            Pattern.compile( "(Etc|SystemV)/.+" ).asPredicate().negate().and( pattern.asPredicate() ) ).collect(
            Collector.of( HashMap::new , ( Map<String, List<ZoneId>> map , String zone ) -> {
                Matcher matcher = pattern.matcher( zone );
                //noinspection ResultOfMethodCallIgnored
                matcher.find();
                String       key    = matcher.group( 1 );
                final ZoneId zoneId = ZoneId.of( zone );
                if( map.containsKey( key ) ){
                    map.get( key ).add( zoneId );
                }else{
                    map.put( key , new ArrayList<ZoneId>(){{
                        add( zoneId );
                    }} );
                }
            } , ( map1 , map2 ) -> {
                map1.forEach(
                        ( key1 , value1 ) -> map2.merge( key1 , value1 , ( key2 , value2 ) -> key2 ).addAll( value1 ) );
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
    private void initialize() throws IOException{
        departureCountryChoice.setItems( zones.keySet().stream().collect(
                Collectors.collectingAndThen( toList() , FXCollections::observableList ) ) );
        destinationCountryChoice.setItems( zones.keySet().stream().collect(
                Collectors.collectingAndThen( toList() , FXCollections::observableList ) ) );
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
        if( editingRoute != null ){
            Matcher departureCountryMatcher = pattern.matcher( editingRoute.getFrom().getId() );
            departureCountryMatcher.find();
            Matcher destinationCountryMatcher = pattern.matcher( editingRoute.getTo().getId() );
            destinationCountryMatcher.find();
            departureCountryChoice.getSelectionModel().select( departureCountryMatcher.group( 1 ) );
            departureCountryChoice.getSelectionModel().select( destinationCountryMatcher.group( 1 ) );
            departureCityChoice.getSelectionModel().select( ZoneId.of( editingRoute.getFrom().getId() ) );
            departureCityChoice.getSelectionModel().select( ZoneId.of( editingRoute.getTo().getId() ) );
            departureCityChoice.setDisable( false );
            destinationCityChoice.setDisable( false );
        }else{
            departureCityChoice.setDisable( true );
            destinationCityChoice.setDisable( true );
        }


        departureCountryChoice.getSelectionModel().selectedItemProperty().addListener(
                ( observable , oldValue , newValue ) -> countrySelected( newValue , departureCityChoice ) );
        destinationCountryChoice.getSelectionModel().selectedItemProperty().addListener(
                ( observable , oldValue , newValue ) -> countrySelected( newValue , destinationCityChoice ) );

        BooleanProperty addAvailable = new SimpleBooleanProperty(
                Optional.ofNullable( departureCityChoice.getSelectionModel().getSelectedItem() ).isPresent() &&
                Optional.ofNullable( destinationCityChoice.getSelectionModel().getSelectedItem() ).isPresent() );
        addAvailable.bind( departureCityChoice.getSelectionModel().selectedItemProperty().isNotNull()
                                              .and( destinationCityChoice.getSelectionModel().selectedItemProperty()
                                                                         .isNotNull() ) );
        addAvailable.bindBidirectional( addAndEditRouteButton.disableProperty() );
    }

    private void countrySelected( String newValue , ChoiceBox<ZoneId> choiceBox ){
        Optional<String> optional = Optional.ofNullable( newValue );
        if( optional.isPresent() ){
            choiceBox.setDisable( false );
            choiceBox.setItems( zones.get( optional.get() ).stream().collect(
                    Collectors.collectingAndThen( toList() , FXCollections::observableList ) ) );
        }else{
            choiceBox.getSelectionModel().clearSelection();
            choiceBox.getItems().clear();
            choiceBox.setDisable( true );
        }
    }

    @FXML
    private void handleAddAction(){
        try{
            DataModelInstanceSaver.getInstance().addRoute(
                    new Route( departureCityChoice.getSelectionModel().getSelectedItem() ,
                               destinationCityChoice.getSelectionModel().getSelectedItem() ) );
            Controller.getInstance().updateRoutes();
            Main.changed = true;
            closeWindow();
        }catch( FlightAndRouteException e ){
            Alert alert = new Alert( Alert.AlertType.WARNING );
            alert.setTitle( "Model`s message" );
            alert.setHeaderText( "Model send message:" );
            alert.setContentText( e.getMessage() );
            alert.showAndWait();
        }
    }

    /**
     Clear Button. Clear all fields in GUI
     */
    @FXML
    private void handleClearData(){
        departureCountryChoice.getSelectionModel().clearSelection();
        destinationCountryChoice.getSelectionModel().clearSelection();
        departureCityChoice.getSelectionModel().clearSelection();
        destinationCityChoice.getSelectionModel().clearSelection();
    }

    /**
     */
    @FXML
    public void handleCancelAction(){
        closeWindow();
    }

    private void closeWindow(){
        thisStage.close();
    }
}

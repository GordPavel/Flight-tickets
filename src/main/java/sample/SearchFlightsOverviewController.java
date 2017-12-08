package sample;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.DataModel;
import model.Flight;
import model.Route;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 Controller for flight search view
 Allows to search flights in DataModel with params
 */
public class SearchFlightsOverviewController{


    @FXML Label           numberLabel;
    @FXML Label           planeIdLabel;
    @FXML Label           arriveDateLabel;
    @FXML Label           departureDateLabel;
    @FXML Label           flightTimeLabel;
    @FXML ListView<Route> routesListView;
    @FXML TextField       searchFromTextField;
    @FXML TextField       searchToTextField;
    /**
     Connecting to FXML items
     */
    @FXML TextField       numberTextField;
    @FXML TextField       planeIdTextField;
    @FXML TextField       flightTimeFromTextField;
    @FXML TextField       flightTimeToTextField;

    @FXML DatePicker departureFromDatePicker;
    @FXML DatePicker departureToDatePicker;

    @FXML DatePicker arriveFromDatePicker;
    @FXML DatePicker arriveToDatePicker;

    private Controller controller = Controller.getInstance();
    private DataModel  dataModel  = DataModel.getInstance();
    private RoutesFlightsOverviewController mainController;
    private Stage                           thisStage;

    public SearchFlightsOverviewController( RoutesFlightsOverviewController mainController , Stage thisStage ){
        this.mainController = mainController;
        this.thisStage = thisStage;
    }

    @FXML
    public void initialize(){
        setLayouts();
        routesListView.getSelectionModel().setSelectionMode( SelectionMode.MULTIPLE );
        numberTextField.textProperty().addListener( ( observable , oldValue , newValue ) -> changed() );
        planeIdTextField.textProperty().addListener( ( observable , oldValue , newValue ) -> changed() );
        departureFromDatePicker.getEditor().textProperty()
                               .addListener( ( observable , oldValue , newValue ) -> changed() );
        departureFromDatePicker.getEditor().setDisable( true );
        departureToDatePicker.getEditor().textProperty()
                             .addListener( ( observable , oldValue , newValue ) -> changed() );
        departureToDatePicker.getEditor().setDisable( true );
        arriveFromDatePicker.getEditor().textProperty()
                            .addListener( ( observable , oldValue , newValue ) -> changed() );
        arriveFromDatePicker.getEditor().setDisable( true );
        arriveToDatePicker.getEditor().textProperty().addListener( ( observable , oldValue , newValue ) -> changed() );
        arriveToDatePicker.getEditor().setDisable( true );
        flightTimeFromTextField.textProperty().addListener( ( observable , oldValue , newValue ) -> changed() );
        flightTimeToTextField.textProperty().addListener( ( observable , oldValue , newValue ) -> changed() );
        routesListView.setOnMouseClicked( event -> changed() );
        routesListView.setItems( dataModel.listRoutesWithPredicate( route -> true ).collect(
                Collectors.collectingAndThen( toList() , FXCollections::observableArrayList ) ) );
        ChangeListener<String> routeSearchListener = ( observable , oldValue , newValue ) -> {
            Predicate<Route> fromPredicate = route -> searchFromTextField.getText().isEmpty() || Pattern.compile(
                    "^" + ".*" + searchFromTextField.getText().replaceAll( "\\*" , ".*" ).replaceAll( "\\?" , "." ) +
                    ".*" + "$" , Pattern.CASE_INSENSITIVE ).matcher( route.getFrom() ).matches();
            Predicate<Route> toPredicate = route -> searchToTextField.getText().isEmpty() || Pattern.compile(
                    "^" + ".*" + searchToTextField.getText().replaceAll( "\\*" , ".*" ).replaceAll( "\\?" , "." ) +
                    ".*" + "$" , Pattern.CASE_INSENSITIVE ).matcher( route.getTo() ).matches();
            routesListView.getItems().setAll(
                    dataModel.listRoutesWithPredicate( fromPredicate.and( toPredicate ) ).collect( toList() ) );
        };
        searchFromTextField.textProperty().addListener( routeSearchListener );
        searchToTextField.textProperty().addListener( routeSearchListener );
        thisStage.setOnCloseRequest( event -> {
            mainController.flightTable.setItems( controller.getFlights() );
            controller.setFlightSearchActiv( false );
        } );
    }

    private void changed(){
        Predicate<Flight> numberPredicate = flight -> numberTextField.getText().isEmpty() || Pattern.compile(
                "^" + ".*" + numberTextField.getText().replaceAll( "\\*" , ".*" ).replaceAll( "\\?" , "." ) + ".*" +
                "$" , Pattern.CASE_INSENSITIVE ).matcher( flight.getNumber() ).matches();
        Predicate<Flight> planePredicate = flight -> planeIdTextField.getText().isEmpty() || Pattern.compile(
                "^" + ".*" + planeIdTextField.getText().replaceAll( "\\*" , ".*" ).replaceAll( "\\?" , "." ) + ".*" +
                "$" , Pattern.CASE_INSENSITIVE ).matcher( flight.getPlaneID() ).matches();
        Predicate<Flight> routePredicate = flight -> routesListView.getSelectionModel().getSelectedItems().isEmpty() ||
                                                     routesListView.getSelectionModel().getSelectedItems()
                                                                   .contains( flight.getRoute() );
        Pattern           datePattern   = Pattern.compile( "^([0-2]\\d|3[0-1]).[0-1]\\d.\\d{4}$" );
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern( "dd.MM.yyyy" );
        SimpleDateFormat  dateFormat    = new SimpleDateFormat( "dd.MM.yyyy" );
        Predicate<Flight> departureDatePredicate = flight ->
                getDatePredicate( departureFromDatePicker.getEditor().getText() , datePattern , dateFormatter ,
                                  dateFormat , true ).test( flight.getDepartureDate() ) &&
                getDatePredicate( departureToDatePicker.getEditor().getText() , datePattern , dateFormatter ,
                                  dateFormat , false ).test( flight.getDepartureDate() );
        Predicate<Flight> arriveDatePredicate = flight ->
                getDatePredicate( arriveFromDatePicker.getEditor().getText() , datePattern , dateFormatter ,
                                  dateFormat , true ).test( flight.getDepartureDate() ) &&
                getDatePredicate( arriveToDatePicker.getEditor().getText() , datePattern , dateFormatter , dateFormat ,
                                  false ).test( flight.getDepartureDate() );
        Predicate<Flight> flightTime = flight -> {
            Pattern timePattern = Pattern.compile( "^(0|[1-9]\\d*):[0-5]\\d$" );
            String  fromTime    = flightTimeFromTextField.getText();
            String  toTime      = flightTimeToTextField.getText();
            Predicate<Long> startTime = aLong -> fromTime.isEmpty() || !timePattern.matcher( fromTime ).matches() ||
                                                 stringToMillis( fromTime ) <= flight.getTravelTime();
            Predicate<Long> endTime = aLong -> toTime.isEmpty() || !timePattern.matcher( toTime ).matches() ||
                                               stringToMillis( toTime ) >= flight.getTravelTime();
            return startTime.test( flight.getTravelTime() ) && endTime.test( flight.getTravelTime() );
        };
        mainController.flightTable.setItems( dataModel.listFlightsWithPredicate(
                numberPredicate.and( planePredicate ).and( routePredicate ).and( departureDatePredicate )
                               .and( arriveDatePredicate ).and( flightTime ) ).collect(
                Collectors.collectingAndThen( toList() , FXCollections::observableArrayList ) ) );
        mainController.flightTable.refresh();
        routesListView.setItems( controller.getRoutes() );
        routesListView.refresh();
    }

    private long stringToMillis( String fromTime ){
        return ( Long.parseLong( fromTime.split( ":" )[ 0 ] ) * 60 + Long.parseLong( fromTime.split( ":" )[ 1 ] ) ) *
               60 * 1000;
    }

    private Predicate<Date> getDatePredicate( String inputDate , Pattern datePattern , DateTimeFormatter dateFormatter ,
                                              SimpleDateFormat dateFormat , Boolean before ){
        Predicate<Date> datePredicate;
        if( datePattern.matcher( inputDate ).matches() ){
            datePredicate = date -> {
                LocalDate inputLocalDate  = LocalDate.parse( inputDate , dateFormatter );
                LocalDate flightLocalDate = LocalDate.parse( dateFormat.format( date ) , dateFormatter );
                return before ? !inputLocalDate.isAfter( flightLocalDate ) :
                       !inputLocalDate.isBefore( flightLocalDate );
            };
        }else{
            datePredicate = date -> true;
        }
        return datePredicate;
    }

    //        Don't touch this layout settings! too hard to make correctly!
    private void setLayouts(){
        numberTextField.setLayoutX( routesListView.getLayoutX() );
        numberTextField.setLayoutY( numberLabel.getLayoutY() );

        planeIdTextField.setLayoutX( routesListView.getLayoutX() );
        planeIdTextField.setLayoutY( planeIdLabel.getLayoutY() );

        departureFromDatePicker.setLayoutX( routesListView.getLayoutX() );
        departureFromDatePicker.setLayoutY( departureDateLabel.getLayoutY() );

        departureToDatePicker.setLayoutX( departureFromDatePicker.getLayoutX() );
        departureToDatePicker
                .setLayoutY( departureFromDatePicker.getLayoutY() + departureFromDatePicker.getHeight() + 30 );

        arriveFromDatePicker.setLayoutX( departureFromDatePicker.getLayoutX() );
        arriveFromDatePicker.setLayoutY( arriveDateLabel.getLayoutY() );

        arriveToDatePicker.setLayoutX( arriveFromDatePicker.getLayoutX() );
        arriveToDatePicker.setLayoutY( arriveFromDatePicker.getLayoutY() + arriveFromDatePicker.getHeight() + 30 );

        flightTimeFromTextField.setLayoutX( routesListView.getLayoutX() );
        flightTimeFromTextField.setLayoutY( flightTimeLabel.getLayoutY() );
        flightTimeToTextField
                .setLayoutX( flightTimeFromTextField.getLayoutX() + flightTimeFromTextField.getWidth() + 100 );
        flightTimeToTextField.setLayoutY( flightTimeLabel.getLayoutY() );
    }

    /**
     Clear button. Clears all fields
     */
    @FXML
    public void handleClearAction(){
        numberTextField.clear();
        planeIdTextField.clear();
        searchFromTextField.clear();
        searchToTextField.clear();
        departureFromDatePicker.getEditor().clear();
        departureToDatePicker.getEditor().clear();
        arriveFromDatePicker.getEditor().clear();
        arriveToDatePicker.getEditor().clear();
        routesListView.getSelectionModel().clearSelection();
        mainController.flightTable.getItems()
                                  .setAll( dataModel.listFlightsWithPredicate( flight -> true ).collect( toList() ) );
    }

}

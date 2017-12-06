package sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.DataModel;
import model.Flight;
import model.Route;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
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
    @FXML TextField  departureFromTimeTextField;
    @FXML TextField  departureToTimeTextField;

    @FXML DatePicker arriveFromDatePicker;
    @FXML DatePicker arriveToDatePicker;
    @FXML TextField  arriveFromTimeTextField;
    @FXML TextField  arriveToTimeTextField;

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
        numberTextField.textProperty().addListener( this::changed );
        planeIdTextField.textProperty().addListener( this::changed );
        departureFromDatePicker.getEditor().textProperty().addListener( this::changed );
        departureFromDatePicker.getEditor().setDisable( true );
        departureToDatePicker.getEditor().textProperty().addListener( this::changed );
        departureToDatePicker.getEditor().setDisable( true );
        departureFromTimeTextField.textProperty().addListener( this::changed );
        departureToTimeTextField.textProperty().addListener( this::changed );
        arriveFromDatePicker.getEditor().textProperty().addListener( this::changed );
        arriveFromDatePicker.getEditor().setDisable( true );
        arriveToDatePicker.getEditor().textProperty().addListener( this::changed );
        arriveToDatePicker.getEditor().setDisable( true );
        arriveFromTimeTextField.textProperty().addListener( this::changed );
        arriveToTimeTextField.textProperty().addListener( this::changed );
        flightTimeFromTextField.textProperty().addListener( this::changed );
        flightTimeToTextField.textProperty().addListener( this::changed );
        routesListView.setOnMouseClicked( event -> changed( null , null , null ) );
        routesListView.setItems( dataModel.listRoutesWithPredicate( route -> true ).collect(
                Collectors.collectingAndThen( toList() , FXCollections::observableArrayList ) ) );
        ChangeListener<String> routeSearchListener = ( observable , oldValue , newValue ) -> {
            Predicate<Route> fromPredicate =
                    route -> Pattern.compile( ".*" + searchFromTextField.getText() + ".*" , Pattern.CASE_INSENSITIVE )
                                    .matcher( route.getFrom() ).matches();
            Predicate<Route> toPredicate =
                    route -> Pattern.compile( ".*" + searchToTextField.getText() + ".*" , Pattern.CASE_INSENSITIVE )
                                    .matcher( route.getTo() ).matches();
            routesListView.getItems().setAll(
                    dataModel.listRoutesWithPredicate( fromPredicate.and( toPredicate ) ).collect( toList() ) );
        };
        searchFromTextField.textProperty().addListener( routeSearchListener );
        searchToTextField.textProperty().addListener( routeSearchListener );
        thisStage.setOnCloseRequest( event -> mainController.flightTable.setItems( controller.getFlights() ) );
    }

    private void changed( ObservableValue<? extends String> observable , String oldValue , String newValue ){
        Predicate<Flight> numberPredicate = flight -> numberTextField.getText().isEmpty() ||
                                                      flight.getNumber().matches( numberTextField.getText() );
        Predicate<Flight> planePredicate = flight -> planeIdTextField.getText().isEmpty() ||
                                                     flight.getNumber().matches( planeIdTextField.getText() );
        Predicate<Flight> routePredicate =
                flight -> Optional.ofNullable( routesListView.getSelectionModel().getSelectedItem() )
                                  .map( route -> Objects.equals( route , flight.getRoute() ) ).orElse( true );
        Predicate<Flight> departureDatePredicate = flight -> {
            Pattern           datePattern   = Pattern.compile( "^([0-2]\\d|3[0-1]).[0-1]\\d.\\d{4}$" );
            Pattern           timePattern   = Pattern.compile( "^(0|[1-9]\\d*):[0-5]\\d$" );
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern( "dd.MM.yyyy" );
            SimpleDateFormat  dateFormat    = new SimpleDateFormat( "dd.MM.yyyy" );
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern( "HH:mm" );
            SimpleDateFormat  timeFormat    = new SimpleDateFormat( "HH:mm" );
            Predicate<Date> startDate = date -> {
                String str = departureFromDatePicker.getEditor().getText();
                return str.isEmpty() || !datePattern.matcher( str ).matches() || !LocalDate.parse( str , dateFormatter )
                                                                                           .isAfter( LocalDate
                                                                                                             .parse( dateFormat
                                                                                                                             .format(
                                                                                                                                     date ) ,
                                                                                                                     dateFormatter ) );
            };
            Predicate<Date> endDate = date -> {
                String str = departureToDatePicker.getEditor().getText();
                return str.isEmpty() || !datePattern.matcher( str ).matches() || !LocalDate.parse( str , dateFormatter )
                                                                                           .isBefore( LocalDate
                                                                                                              .parse( dateFormat
                                                                                                                              .format(
                                                                                                                                      date ) ,
                                                                                                                      dateFormatter ) );
            };
            Predicate<Date> startTime = date -> {
                String str = departureFromTimeTextField.getText();
                return str.isEmpty() || !timePattern.matcher( str ).matches() || !LocalTime.parse( str , timeFormatter )
                                                                                           .isAfter( LocalTime
                                                                                                             .parse( timeFormat
                                                                                                                             .format(
                                                                                                                                     date ) ,
                                                                                                                     timeFormatter ) );
            };
            Predicate<Date> endTime = date -> {
                String str = departureToTimeTextField.getText();
                return str.isEmpty() || !timePattern.matcher( str ).matches() || !LocalTime.parse( str , timeFormatter )
                                                                                           .isBefore( LocalTime
                                                                                                              .parse( timeFormat
                                                                                                                              .format(
                                                                                                                                      date ) ,
                                                                                                                      timeFormatter ) );
            };
            return startDate.test( flight.getDepartureDate() ) && endDate.test( flight.getDepartureDate() ) &&
                   startTime.test( flight.getDepartureDate() ) && endTime.test( flight.getDepartureDate() );
        };
        Predicate<Flight> arriveDatePredicate = flight -> {
            Pattern           datePattern   = Pattern.compile( "^([0-2]\\d|3[0-1]).[0-1]\\d.\\d{4}$" );
            Pattern           timePattern   = Pattern.compile( "^(0|[1-9]\\d*):[0-5]\\d$" );
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern( "HH:mm" );
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern( "dd.MM.yyyy" );
            SimpleDateFormat  dateFormat    = new SimpleDateFormat( "dd.MM.yyyy" );
            SimpleDateFormat  timeFormat    = new SimpleDateFormat( "HH:mm" );
            Predicate<Date> startDate = date -> {
                String str = arriveFromDatePicker.getEditor().getText();
                return str.isEmpty() || !datePattern.matcher( str ).matches() || !LocalDate.parse( str , dateFormatter )
                                                                                           .isAfter( LocalDate
                                                                                                             .parse( dateFormat
                                                                                                                             .format(
                                                                                                                                     date ) ,
                                                                                                                     dateFormatter ) );
            };
            Predicate<Date> endDate = date -> {
                String str = arriveToDatePicker.getEditor().getText();
                return str.isEmpty() || !datePattern.matcher( str ).matches() || !LocalDate.parse( str , dateFormatter )
                                                                                           .isBefore( LocalDate
                                                                                                              .parse( dateFormat
                                                                                                                              .format(
                                                                                                                                      date ) ,
                                                                                                                      dateFormatter ) );
            };
            Predicate<Date> startTime = date -> {
                String str = arriveFromTimeTextField.getText();
                return str.isEmpty() || !timePattern.matcher( str ).matches() || !LocalTime.parse( str , timeFormatter )
                                                                                           .isAfter( LocalTime
                                                                                                             .parse( timeFormat
                                                                                                                             .format(
                                                                                                                                     date ) ,
                                                                                                                     timeFormatter ) );
            };
            Predicate<Date> endTime = date -> {
                String str = arriveToTimeTextField.getText();
                return str.isEmpty() || !timePattern.matcher( str ).matches() || !LocalTime.parse( str , timeFormatter )
                                                                                           .isBefore( LocalTime
                                                                                                              .parse( timeFormat
                                                                                                                              .format(
                                                                                                                                      date ) ,
                                                                                                                      timeFormatter ) );
            };
            return startDate.test( flight.getArriveDate() ) && endDate.test( flight.getArriveDate() ) &&
                   startTime.test( flight.getArriveDate() ) && endTime.test( flight.getArriveDate() );
        };
        Predicate<Flight> flightTime = flight -> {
            Pattern timePattern = Pattern.compile( "^(0|[1-9]\\d*):[0-5]\\d$" );
            String  fromTime    = flightTimeFromTextField.getText();
            String  toTime      = flightTimeToTextField.getText();
            Predicate<Long> startTime = aLong -> fromTime.isEmpty() || !timePattern.matcher( fromTime ).matches() ||
                                                 ( Long.parseLong( fromTime.split( ":" )[ 0 ] ) * 60 +
                                                   Long.parseLong( fromTime.split( ":" )[ 1 ] ) ) * 60 * 1000 <=
                                                 flight.getTravelTime();
            Predicate<Long> endTime = aLong -> toTime.isEmpty() || !timePattern.matcher( toTime ).matches() ||
                                               ( Long.parseLong( toTime.split( ":" )[ 0 ] ) * 60 +
                                                 Long.parseLong( toTime.split( ":" )[ 1 ] ) ) * 60 * 1000 >=
                                               flight.getTravelTime();
            return startTime.test( flight.getTravelTime() ) && endTime.test( flight.getTravelTime() );
        };
        mainController.flightTable.setItems( dataModel.listFlightsWithPredicate(
                numberPredicate.and( planePredicate ).and( routePredicate ).and( departureDatePredicate )
                               .and( arriveDatePredicate ).and( flightTime ) ).collect(
                Collectors.collectingAndThen( toList() , FXCollections::observableArrayList ) ) );
        mainController.flightTable.refresh();
    }

    //        Don't touch this layout settings! too hard to make correctly!
    private void setLayouts(){
        numberTextField.setLayoutX( routesListView.getLayoutX() );
        numberTextField.setLayoutY( numberLabel.getLayoutY() );

        planeIdTextField.setLayoutX( routesListView.getLayoutX() );
        planeIdTextField.setLayoutY( planeIdLabel.getLayoutY() );

        departureFromDatePicker.setLayoutX( routesListView.getLayoutX() );
        departureFromDatePicker.setLayoutY( departureDateLabel.getLayoutY() );
        departureFromTimeTextField
                .setLayoutX( departureFromDatePicker.getLayoutX() + departureFromDatePicker.getWidth() + 150 );
        departureFromTimeTextField.setLayoutY( departureFromDatePicker.getLayoutY() );

        departureToDatePicker.setLayoutX( departureFromDatePicker.getLayoutX() );
        departureToDatePicker
                .setLayoutY( departureFromDatePicker.getLayoutY() + departureFromDatePicker.getHeight() + 30 );
        departureToTimeTextField.setLayoutX( departureFromTimeTextField.getLayoutX() );
        departureToTimeTextField.setLayoutY( departureToDatePicker.getLayoutY() );

        arriveFromDatePicker.setLayoutX( departureFromDatePicker.getLayoutX() );
        arriveFromDatePicker.setLayoutY( arriveDateLabel.getLayoutY() );
        arriveFromTimeTextField.setLayoutX( departureFromTimeTextField.getLayoutX() );
        arriveFromTimeTextField.setLayoutY( arriveFromDatePicker.getLayoutY() );

        arriveToDatePicker.setLayoutX( arriveFromDatePicker.getLayoutX() );
        arriveToDatePicker.setLayoutY( arriveFromDatePicker.getLayoutY() + arriveFromDatePicker.getHeight() + 30 );
        arriveToTimeTextField.setLayoutX( arriveFromTimeTextField.getLayoutX() );
        arriveToTimeTextField.setLayoutY( arriveToDatePicker.getLayoutY() );

        flightTimeFromTextField.setLayoutX( routesListView.getLayoutX() );
        flightTimeFromTextField.setLayoutY( flightTimeLabel.getLayoutY() );
        flightTimeToTextField.setLayoutX( departureFromTimeTextField.getLayoutX() );
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
        routesListView.getSelectionModel().clearSelection();
        mainController.flightTable.getItems()
                                  .setAll( dataModel.listFlightsWithPredicate( flight -> true ).collect( toList() ) );
    }

}

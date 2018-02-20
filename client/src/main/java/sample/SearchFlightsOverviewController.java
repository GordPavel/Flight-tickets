package sample;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTimePicker;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.DataModelInstanceSaver;
import model.Flight;
import model.Route;
import org.danekja.java.util.function.serializable.SerializablePredicate;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.TimerTask;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 Controller for flight search view
 Allows to search flights in DataModel with params
 */
class SearchFlightsOverviewController{

    @FXML Label           numberLabel;
    @FXML Label           planeIdLabel;
    @FXML Label           arriveDateLabel;
    @FXML Label           departureDateLabel;
    @FXML Label           flightTimeLabel;
    @FXML ListView<Route> routesListView;
    @FXML TextField       searchFromTextField;
    @FXML TextField       searchToTextField;

    @FXML TextField numberTextField;
    @FXML TextField planeIdTextField;

    @FXML JFXDatePicker departureFromDatePicker;
    @FXML JFXDatePicker departureToDatePicker;

    @FXML JFXDatePicker arriveFromDatePicker;
    @FXML JFXDatePicker arriveToDatePicker;

    @FXML JFXTimePicker flightTimeFrom;
    @FXML JFXTimePicker flightTimeTo;

    @FXML JFXButton searchButton;

    private RoutesFlightsOverviewController mainController;
    private Stage                           thisStage;
    private boolean                         correctSymbols;
    private ObjectProperty<Predicate<Flight>> flightsPredicate  = new SimpleObjectProperty<>( flight -> true );
    private FilteredList<Route>
                                              routeFilteredList =
            DataModelInstanceSaver.getInstance().getRouteObservableList().filtered( route -> true );
    private ObjectProperty<Predicate<Route>>  routesPredicate   = new SimpleObjectProperty<>( route -> true );

    SearchFlightsOverviewController( RoutesFlightsOverviewController mainController , Stage thisStage ){
        this.mainController = mainController;
        this.thisStage = thisStage;
        FilteredList<Flight> flightFilteredList = mainController.flightTable.getItems().filtered( flight -> true );
        mainController.flightTable.setItems( flightFilteredList );
        flightFilteredList.predicateProperty().bind( flightsPredicate );
        flightsPredicate.addListener( ( observable , oldValue , newValue ) -> {
            Controller.getInstance().flightPredicate = newValue;
        } );

        routeFilteredList.predicateProperty().bind( routesPredicate );
    }

    /**
     initialization of view
     */
    @FXML
    private void initialize(){
        correctSymbols = true;
        setLayouts();
        routesListView.setItems( routeFilteredList );
        routesListView.getSelectionModel().setSelectionMode( SelectionMode.MULTIPLE );
        numberTextField.textProperty().addListener( this::changed );
        numberTextField.textProperty()
                       .addListener( ( observable , oldValue , newValue ) -> formatCheck( numberTextField ) );
        planeIdTextField.textProperty().addListener( this::changed );
        planeIdTextField.textProperty()
                        .addListener( ( observable , oldValue , newValue ) -> formatCheck( planeIdTextField ) );
        departureFromDatePicker.getEditor().textProperty().addListener( this::changed );
        departureFromDatePicker.getEditor().setDisable( true );
        departureToDatePicker.getEditor().textProperty().addListener( this::changed );
        departureToDatePicker.getEditor().setDisable( true );
        arriveFromDatePicker.getEditor().textProperty().addListener( this::changed );
        arriveFromDatePicker.getEditor().setDisable( true );
        arriveToDatePicker.getEditor().textProperty().addListener( this::changed );
        arriveToDatePicker.getEditor().setDisable( true );
        flightTimeFrom.setValue( LocalTime.MIN );
        flightTimeTo.setValue( LocalTime.MAX );
        StringConverter<LocalTime> localTimeStringConverter = new StringConverter<LocalTime>(){
            final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern( "HH:mm" );

            @Override
            public String toString( LocalTime time ){
                return timeFormatter.format( time );
            }

            @Override
            public LocalTime fromString( String string ){
                return LocalTime.parse( string , timeFormatter );
            }
        };
        flightTimeFrom.setConverter( localTimeStringConverter );
        flightTimeTo.setConverter( localTimeStringConverter );
        flightTimeFrom.getEditor().textProperty().addListener( this::changed );
        flightTimeTo.getEditor().textProperty().addListener( this::changed );
        routesListView.getSelectionModel().selectedItemProperty().addListener( this::changed );
        ChangeListener<String> routeSearchListener = ( observable , oldValue , newValue ) -> {
            SerializablePredicate<Route>
                    fromPredicate =
                    route -> searchFromTextField.getText().isEmpty() ||
                             makeStringPredicate( searchFromTextField.getText() ).matcher( route.getFrom().getId() )
                                                                                 .matches();
            SerializablePredicate<Route>
                    toPredicate =
                    route -> searchToTextField.getText().isEmpty() ||
                             makeStringPredicate( searchToTextField.getText() ).matcher( route.getTo().getId() )
                                                                               .matches();
            routesPredicate.setValue( fromPredicate.and( toPredicate ) );
        };
        searchFromTextField.textProperty().addListener( routeSearchListener );
        searchToTextField.textProperty().addListener( routeSearchListener );
        searchFromTextField.textProperty()
                           .addListener( ( observable , oldValue , newValue ) -> formatCheck( searchFromTextField ) );
        searchToTextField.textProperty()
                         .addListener( ( observable , oldValue , newValue ) -> formatCheck( searchToTextField ) );
        thisStage.setOnCloseRequest( event -> {
            mainController.flightTable.setItems( DataModelInstanceSaver.getInstance().getFlightObservableList() );
            Controller.getInstance().flightPredicate = flight -> true;
        } );
        if( mainController instanceof RoutesFlightsReadOnlyOverviewController ){
            searchButton.setVisible( false );
        }
    }

    /**
     Flight search method, used in listeners
     */
    private void changed( ObservableValue<?> value , Object oldValue , Object newValue ){
        SerializablePredicate<Flight>
                numberPredicate =
                flight -> numberTextField.getText().isEmpty() ||
                          makeStringPredicate( numberTextField.getText() ).matcher( flight.getNumber() ).matches();
        SerializablePredicate<Flight>
                planePredicate =
                flight -> planeIdTextField.getText().isEmpty() ||
                          makeStringPredicate( planeIdTextField.getText() ).matcher( flight.getPlaneID() ).matches();
        SerializablePredicate<Flight>
                routePredicate =
                flight -> routesListView.getSelectionModel().getSelectedItems().isEmpty() ||
                          routesListView.getSelectionModel().getSelectedItems().contains( flight.getRoute() );
        Pattern           datePattern   = Pattern.compile( "^([0-2]\\d|3[0-1]).[0-1]\\d.\\d{4}$" );
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern( "dd.MM.yyyy" );
        SimpleDateFormat  dateFormat    = new SimpleDateFormat( "dd.MM.yyyy" );
        SerializablePredicate<Flight>
                departureDatePredicate =
                flight -> getDateTimePredicate( departureFromDatePicker.getEditor().getText() ,
                                                datePattern ,
                                                dateFormatter ,
                                                dateFormat ,
                                                true ).test( flight.getDepartureDateTime() ) &&
                          getDateTimePredicate( departureToDatePicker.getEditor().getText() ,
                                                datePattern ,
                                                dateFormatter ,
                                                dateFormat ,
                                                false ).test( flight.getDepartureDateTime() );
        SerializablePredicate<Flight>
                arriveDatePredicate =
                flight -> getDateTimePredicate( arriveFromDatePicker.getEditor().getText() ,
                                                datePattern ,
                                                dateFormatter ,
                                                dateFormat ,
                                                true ).test( flight.getDepartureDateTime() ) &&
                          getDateTimePredicate( arriveToDatePicker.getEditor().getText() ,
                                                datePattern ,
                                                dateFormatter ,
                                                dateFormat ,
                                                false ).test( flight.getDepartureDateTime() );
        SerializablePredicate<Flight> flightTimePredicate = flight -> {
            SerializablePredicate<Long>
                    startTime =
                    aLong -> flightTimeFrom.getEditor().getText().isEmpty() ||
                             flightTimeFrom.getValue().get( ChronoField.MILLI_OF_DAY ) <= flight.getTravelTime();
            SerializablePredicate<Long>
                    endTime =
                    aLong -> flightTimeTo.getEditor().getText().isEmpty() ||
                             flightTimeTo.getValue().get( ChronoField.MILLI_OF_DAY ) >= flight.getTravelTime();
            return startTime.test( flight.getTravelTime() ) && endTime.test( flight.getTravelTime() );
        };
        if( correctSymbols ){
            SerializablePredicate<Flight>
                    v =
                    numberPredicate.and( planePredicate )
                                   .and( routePredicate )
                                   .and( departureDatePredicate )
                                   .and( arriveDatePredicate )
                                   .and( flightTimePredicate );
            flightsPredicate.setValue( v );
            if( ( mainController instanceof RoutesFlightsReadOnlyOverviewController ) ){
                ( ( RoutesFlightsReadOnlyOverviewController ) mainController ).restartTask( new TimerTask(){
                    @Override
                    public void run(){
                        handleSearchAction();
                    }
                } );
            }
        }
    }

    private Pattern makeStringPredicate( String text ){
        return Pattern.compile( "^.*" + text.replaceAll( "\\*" , ".*" ).replaceAll( "\\?" , "." ) + ".*$" ,
                                Pattern.CASE_INSENSITIVE );
    }


    /**
     @param field - text field to check for acceptable symbols
     method used to not allow user to use unacceptable symbols for search
     */
    private void formatCheck( TextField field ){
        Pattern textPattern = Pattern.compile( "[\\w\\d?*\\-_]*" );
        Matcher matcher     = textPattern.matcher( field.getText() );
        if( !matcher.matches() ){
            field.setStyle( "-fx-text-inner-color: red;" );
            field.setTooltip( new Tooltip( "Acceptable symbols: 0-9, a-z, -, _, *, ?" ) );
            correctSymbols = false;
        }else{
            field.setStyle( "-fx-text-inner-color: black;" );
            field.setTooltip( null );
        }
        if( textPattern.matcher( numberTextField.getText() ).matches() &&
            textPattern.matcher( planeIdTextField.getText() ).matches() &&
            textPattern.matcher( searchFromTextField.getText() ).matches() &&
            textPattern.matcher( searchToTextField.getText() ).matches() ){
            correctSymbols = true;
        }

    }

    private SerializablePredicate<ZonedDateTime> getDateTimePredicate( String inputDate ,
                                                                       Pattern datePattern ,
                                                                       DateTimeFormatter dateFormatter ,
                                                                       SimpleDateFormat dateFormat ,
                                                                       Boolean before ){
        SerializablePredicate<ZonedDateTime> datePredicate;
        if( datePattern.matcher( inputDate ).matches() ){
            datePredicate = date -> {
                LocalDate inputLocalDate  = LocalDate.parse( inputDate , dateFormatter );
                LocalDate flightLocalDate = LocalDate.parse( dateFormat.format( date ) , dateFormatter );
                return before ?
                       !inputLocalDate.isAfter( flightLocalDate ) :
                       !inputLocalDate.isBefore( flightLocalDate );
            };
        }else{
            datePredicate = date -> true;
        }
        return datePredicate;
    }

    //        Don't touch this layout settings! too hard to make correctly!
    private void setLayouts(){
        numberTextField.setLayoutY( numberLabel.getLayoutY() );

        planeIdTextField.setLayoutX( numberTextField.getLayoutX() );
        planeIdTextField.setLayoutY( planeIdLabel.getLayoutY() );

        departureFromDatePicker.setLayoutY( departureDateLabel.getLayoutY() );

        departureToDatePicker.setLayoutX( departureFromDatePicker.getLayoutX() );
        departureToDatePicker.setLayoutY( departureFromDatePicker.getLayoutY() +
                                          departureFromDatePicker.getHeight() +
                                          30 );

        arriveFromDatePicker.setLayoutX( departureFromDatePicker.getLayoutX() );
        arriveFromDatePicker.setLayoutY( arriveDateLabel.getLayoutY() );

        arriveToDatePicker.setLayoutX( arriveFromDatePicker.getLayoutX() );
        arriveToDatePicker.setLayoutY( arriveFromDatePicker.getLayoutY() + arriveFromDatePicker.getHeight() + 30 );

        flightTimeFrom.setLayoutY( flightTimeLabel.getLayoutY() - 10 );
        flightTimeTo.setLayoutX( flightTimeFrom.getLayoutX() + flightTimeFrom.getWidth() + 150 );
        flightTimeTo.setLayoutY( flightTimeLabel.getLayoutY() - 10 );
    }

    /**
     Clear button. Clears all fields
     */
    @FXML
    private void handleClearAction(){
        numberTextField.clear();
        planeIdTextField.clear();
        searchFromTextField.clear();
        searchToTextField.clear();
        departureFromDatePicker.getEditor().clear();
        departureToDatePicker.getEditor().clear();
        arriveFromDatePicker.getEditor().clear();
        arriveToDatePicker.getEditor().clear();
        routesListView.getSelectionModel().clearSelection();
        flightTimeFrom.getEditor().clear();
        flightTimeTo.getEditor().clear();
        searchFromTextField.clear();
        searchToTextField.clear();
    }


    /**
     *
     */
    @FXML
    private void handleSearchAction(){
//        todo : Догрузка сущностей
    }

}

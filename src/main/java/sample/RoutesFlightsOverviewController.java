package sample;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Flight;
import model.Route;

import java.io.IOException;
import java.time.ZoneId;


/**
 * Controller for routes and flights view
 * Shows the information about all routes and flights
 */
public class RoutesFlightsOverviewController {


    private Controller controller = new Controller();

    private static final String EDIT_ROUTE_WINDOW = "Edit a route";
    private static final String ADD_ROUTE_WINDOW = "Add a route";
    private static final String EDIT_FLIGHT_WINDOW = "Edit a flight";
    private static final String ADD_FLIGHT_WINDOW = "Add a flight";
    private static final String SEARCH_FLIGHT_WINDOW = "Search a flight";

    @FXML
    Button addRouteButton;
    @FXML
    Button editRouteButton;
    @FXML
    Button searchRouteButton;
    @FXML
    Button deleteRouteButton;
    @FXML
    Button addFlightButton;
    @FXML
    Button editFlightButton;
    @FXML
    Button searchFlightButton;
    @FXML
    Button deleteFlightButton;
    @FXML
    TextField departure;
    @FXML
    TextField destination;
    @FXML
    TableView<Route> routeTable;
    @FXML
    TableColumn<Route, String> departureColumn;
    @FXML
    TableColumn<Route, String> destinationColumn;
    @FXML
    TableView<Flight> flightTable;
    @FXML
    TableColumn<Flight, String> number;
    @FXML
    TableColumn<Flight, Route> routeColumnFlight;
    @FXML
    TextArea detailsTextArea;


    public RoutesFlightsOverviewController() {

    }


    /**
     * initialization of view
     */
    public void initialize() {

        departureColumn.setCellValueFactory(new PropertyValueFactory<Route, String>("from"));
        destinationColumn.setCellValueFactory(new PropertyValueFactory<Route, String>("to"));
        routeTable.setItems(controller.getRoutes());
        number.setCellValueFactory(new PropertyValueFactory<Flight, String>("number"));
        routeColumnFlight.setCellValueFactory(new PropertyValueFactory<Flight, Route>("route"));
        flightTable.setItems(controller.getFlights());
        flightTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showFlightDetails(newValue));
    }

    /**
     * @param flight show detail information about chosen flight
     */

    private void showFlightDetails(Flight flight) {

        if (flight != null) {

            detailsTextArea.setWrapText(true);
            detailsTextArea.setText(flight.toString());

        } else {
            detailsTextArea.setText("");
        }
    }

    /**
     * @param event Delete Button (for routes). Delete the chosen route from model and TableView
     */
    @FXML
    public void handleDeleteRouteButton(ActionEvent event) throws ClassNotFoundException {

        Route selectedRoute = routeTable.getSelectionModel().getSelectedItem();
        if (selectedRoute != null) {

            routeTable.getItems().remove(selectedRoute);
            controller.model.removeRoute(selectedRoute);
            controller.updateRoutes();
            routeTable.refresh();
        } else {

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Route Selected");
            alert.setContentText("Please select a route in the table.");

            alert.showAndWait();
        }
    }

    /**
     * @param event Delete Button (for flights). Delete the chosen flight from model and TableView
     */
    @FXML
    public void handleDeleteFlightButton(ActionEvent event) throws ClassNotFoundException {

        Flight selectedFlight = flightTable.getSelectionModel().getSelectedItem();
        if (selectedFlight != null) {

            flightTable.getItems().remove(selectedFlight);
            controller.model.removeFlight(selectedFlight.getNumber());
            controller.updateFlights();
            flightTable.refresh();
        } else {

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Flight Selected");
            alert.setContentText("Please select a flight in the table.");

            alert.showAndWait();
        }
    }

    /**
     * @param actionEvent Add Button (for routes). Create a new window for adding a new route
     */
    @FXML
    public void handleAddRouteButton(ActionEvent actionEvent) {

        Parent addRouteWindow;
        Stage oldStage = (Stage) ((Parent) actionEvent.getSource()).getScene().getWindow();

        routeTable.refresh();
        try {
            addRouteWindow = FXMLLoader.load(getClass().getResource("/fxml/AddRoutesOverview.fxml"));

            Scene scene = new Scene(addRouteWindow);
            Stage popUp = new Stage();

            popUp.initModality(Modality.APPLICATION_MODAL);
            popUp.initOwner(oldStage);

            popUp.setTitle(ADD_ROUTE_WINDOW);
            popUp.setScene(scene);
            popUp.setResizable(false);

            oldStage.setOpacity(0.9);
            popUp.showAndWait();
            oldStage.setOpacity(1);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param actionEvent Edit Button (for routes). Create a new window for editing the information about chosen route
     */
    @FXML
    public void handleEditRouteButton(ActionEvent actionEvent) {
        Parent editRouteWindow;
        Stage oldStage = (Stage) ((Parent) actionEvent.getSource()).getScene().getWindow();
        Route selectedRoute = routeTable.getSelectionModel().getSelectedItem();

        if (selectedRoute == null) {

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Route Selected");
            alert.setContentText("Please select a route to edit in the table.");

            alert.showAndWait();

        } else {
            controller.setRouteForEdit(selectedRoute);

            routeTable.refresh();

            try {
                editRouteWindow = FXMLLoader.load(getClass().getResource("/fxml/EditRoutesOverview.fxml"));

                Scene scene = new Scene(editRouteWindow);
                Stage popUp = new Stage();

                popUp.initModality(Modality.APPLICATION_MODAL);
                popUp.initOwner(oldStage);

                popUp.setTitle(EDIT_ROUTE_WINDOW);
                popUp.setScene(scene);
                popUp.setResizable(false);

                ((TextField) (editRouteWindow.getChildrenUnmodifiable().get(0))).setText(selectedRoute.getFrom());
                ((TextField) (editRouteWindow.getChildrenUnmodifiable().get(4))).setText(selectedRoute.getTo());

                oldStage.setOpacity(0.9);
                popUp.showAndWait();
                oldStage.setOpacity(1);


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param actionEvent Add Button (for flights). Create a new window for adding a new flight
     */
    @FXML
    public void handleAddFlightButton(ActionEvent actionEvent) {
        Parent addFlightWindow;
        Stage oldStage = (Stage) ((Parent) actionEvent.getSource()).getScene().getWindow();

        flightTable.refresh();

        try {
            addFlightWindow = FXMLLoader.load(getClass().getResource("/fxml/AddFlightsOverview.fxml"));

            Scene scene = new Scene(addFlightWindow);
            Stage popUp = new Stage();

            popUp.initModality(Modality.APPLICATION_MODAL);
            popUp.initOwner(oldStage);

            popUp.setTitle(ADD_FLIGHT_WINDOW);
            popUp.setScene(scene);
            popUp.setResizable(false);

            oldStage.setOpacity(0.9);
            popUp.showAndWait();
            oldStage.setOpacity(1);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param actionEvent Edit Button (for flight). Create a new window for editing the information about chosen flight.
     */
    @FXML
    public void handleEditFlightButton(ActionEvent actionEvent) {
        Parent editFlightWindow;
        Stage oldStage = (Stage) ((Parent) actionEvent.getSource()).getScene().getWindow();

        Flight selectedFlight = flightTable.getSelectionModel().getSelectedItem();

        if (selectedFlight == null) {

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Flight Selected");
            alert.setContentText("Please select a flight to edit in the table.");

            alert.showAndWait();
        } else {
            controller.setFlightForEdit(selectedFlight);

            flightTable.refresh();
            try {
                editFlightWindow = FXMLLoader.load(getClass().getResource("/fxml/EditFlightsOverview.fxml"));

                Scene scene = new Scene(editFlightWindow);
                Stage popUp = new Stage();

                popUp.initModality(Modality.APPLICATION_MODAL);
                popUp.initOwner(oldStage);

                popUp.setTitle(EDIT_FLIGHT_WINDOW);
                popUp.setScene(scene);
                popUp.setResizable(false);

                ((TextField) (editFlightWindow.getChildrenUnmodifiable().get(2))).setText(selectedFlight.getNumber());
                ((TextField) (editFlightWindow.getChildrenUnmodifiable().get(3))).setText(selectedFlight.getPlaneID());
                ((DatePicker) (editFlightWindow.getChildrenUnmodifiable().get(9))).setValue(selectedFlight.getDepartureDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                ((DatePicker) (editFlightWindow.getChildrenUnmodifiable().get(10))).setValue(selectedFlight.getArrivalDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                ((ChoiceBox<Route>) (editFlightWindow.getChildrenUnmodifiable().get(11))).setValue(selectedFlight.getRoute());

                oldStage.setOpacity(0.9);
                popUp.showAndWait();
                oldStage.setOpacity(1);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param actionEvent Edit Button (for flight). Create a new window for searching the necessary flight.
     */
    @FXML
    public void handleSearchFlightButton(ActionEvent actionEvent) {
        Parent editFlightWindow;
        Stage oldStage = (Stage) ((Parent) actionEvent.getSource()).getScene().getWindow();
        try {
            editFlightWindow = FXMLLoader.load(getClass().getResource("/fxml/SearchFlightsOverview.fxml"));

            Scene scene = new Scene(editFlightWindow);
            Stage popUp = new Stage();

            popUp.initModality(Modality.APPLICATION_MODAL);
            popUp.initOwner(oldStage);

            popUp.setTitle(SEARCH_FLIGHT_WINDOW);
            popUp.setScene(scene);
            popUp.setResizable(false);

            oldStage.setOpacity(0.9);
            popUp.showAndWait();
            oldStage.setOpacity(1);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * @param actionEvent Search Button (for routes). Search necessary route in TableView.
     */
    @FXML
    public void handleSearchRouteButton(ActionEvent actionEvent) {


        if (departure.getText().equals("") && destination.getText().equals("")) {

            controller.updateRoutes();
            routeTable.setItems(controller.getRoutes());
            routeTable.refresh();
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Empty Search ");
            alert.setHeaderText("No parameters");
            alert.setContentText("Please enter one of parameters for searching.");

            alert.showAndWait();

        } else if (!departure.getText().equals("") && destination.getText().equals("")) {

            controller.setRoutes(FXCollections.observableArrayList(Controller.searchEngine.findRoutesByDepartureAirport(departure.getText())));
            routeTable.setItems(controller.getRoutes());
            routeTable.refresh();
        } else if (departure.getText().equals("") && !destination.getText().equals("")) {

            controller.setRoutes(FXCollections.observableArrayList(Controller.searchEngine.findRoutesByArrivalAirport(destination.getText())));
            routeTable.setItems(controller.getRoutes());
            routeTable.refresh();
        } else {

            controller.setRoutes(FXCollections.observableArrayList(Controller.searchEngine.searchRoute(departure.getText(), destination.getText())));
            routeTable.setItems(controller.getRoutes());
            routeTable.refresh();
        }
    }

    @FXML
    private void handleImportAction(ActionEvent actionEvent) {


       /* FileChooser fileChooser = new FileChooser();

        try {

            controller.model.importFromFile(file);
        } catch (IOException e) {

            e.printStackTrace();
        } catch (FlightAndRouteException e) {

            e.printStackTrace();
        }*/
    }

    @FXML
    private void handleExportAction(ActionEvent actionEvent) {


    }

    @FXML
    private void handleMergeAction(ActionEvent actionEvent) {


    }
}

//Stage stage = null;

//FileChooser fileChooser;

//File file = fileChooser.showOpenDialog(stage.getOwner());





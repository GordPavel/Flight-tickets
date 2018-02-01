package sample;

import javafx.collections.ObservableList;
import model.Flight;
import model.Route;
import transport.UserInformation;

import java.io.File;
import java.net.Socket;

/**
 Support class for controllers
 */
class Controller{

    private Controller(){}

    private static class InstanceHolder{
        private static final Controller instance = new Controller();
    }

    static synchronized Controller getInstance(){
        return Controller.InstanceHolder.instance;
    }

    private static boolean flightSearchActive = false;
    private ObservableList<Flight> mergeFlights;
    private ObservableList<Route>  mergeRoutes;
    private FaRThread              thread;
    static Boolean changed = false;
    static File savingFile;

    private static Socket clientSocket;
    private static UserInformation userInformation;

    public Socket getClientSocket(){
        return clientSocket;
    }

    public void setClientSocket( Socket clientSocket ){
        this.clientSocket = clientSocket;
    }

    public void setUserInformation(UserInformation userInformation) {
        this.userInformation = userInformation;
    }

    public UserInformation getUserInformation() {
        return userInformation;
    }

    void setThread( FaRThread thread ){
        this.thread = thread;
    }

    void startThread(){
        thread.start();
    }

    void stopThread(){
        thread.setStop();
    }

    void setMergeFlights( ObservableList<Flight> mergeFlights ){
        this.mergeFlights = mergeFlights;
    }

    ObservableList<Flight> getMergeFlights(){
        return mergeFlights;
    }

    void setMergeRoutes( ObservableList<Route> mergeRoutes ){
        this.mergeRoutes = mergeRoutes;
    }

    ObservableList<Route> getMergeRoutes(){
        return mergeRoutes;
    }

    void setFlightSearchActive( boolean flightSearchActive ){
        Controller.flightSearchActive = flightSearchActive;
    }

    boolean isFlightSearchActive(){
        return flightSearchActive;
    }
}


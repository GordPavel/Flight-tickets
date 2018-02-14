package sample;

import javafx.collections.ObservableList;
import model.Flight;
import model.Route;
import transport.UserInformation;

import java.io.File;
import java.io.IOException;
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
    static String ip;
    static int port;

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

    public void setIp(String ip) {
        Controller.ip = ip;
    }

    public String getIp() {
        return ip;
    }

    public void setPort(int port) {
        Controller.port = port;
    }

    public int getPort() {
        return port;
    }

    public void connectToServer(String ip, int port) {
        try{
            System.out.println("Connection...");
            Socket socket = new Socket( ip , port );
            System.out.println("socket.isClosed = "+socket.isClosed());
            setIp(ip);
            setPort(port);
            Controller.getInstance().setClientSocket( socket );
        }catch( IOException ioex ){
            System.out.println( "Connection failed" );
            System.out.println( ioex.getMessage() );
        }
    }

    public void reconnect(){
        connectToServer(ip,port);
    }
}


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
public class Controller{

    static Boolean changed = false;
    static File   savingFile;
    static String ip;
    static int    port;
    private static boolean flightSearchActive = false;
    private static Socket          clientSocket;
    private static UserInformation userInformation;
    private ObservableList<Flight> mergeFlights;
    private ObservableList<Route>  mergeRoutes;
    private FaRThread              thread;
    private Controller(){}

    public static synchronized Controller getInstance(){
        return Controller.InstanceHolder.instance;
    }

    public Socket getClientSocket(){
        return clientSocket;
    }

    public void setClientSocket( Socket clientSocket ){
        this.clientSocket = clientSocket;
    }

    public UserInformation getUserInformation(){
        return userInformation;
    }

    public void setUserInformation( UserInformation userInformation ){
        this.userInformation = userInformation;
    }

    void setThread( FaRThread thread ){
        this.thread = thread;
        this.thread.setDaemon( true );
    }

    void startThread(){
        thread.start();
    }

    void stopThread(){
        thread.setStop();
    }

    ObservableList<Flight> getMergeFlights(){
        return mergeFlights;
    }

    void setMergeFlights( ObservableList<Flight> mergeFlights ){
        this.mergeFlights = mergeFlights;
    }

    ObservableList<Route> getMergeRoutes(){
        return mergeRoutes;
    }

    void setMergeRoutes( ObservableList<Route> mergeRoutes ){
        this.mergeRoutes = mergeRoutes;
    }

    boolean isFlightSearchActive(){
        return flightSearchActive;
    }

    void setFlightSearchActive( boolean flightSearchActive ){
        Controller.flightSearchActive = flightSearchActive;
    }

    public String getIp(){
        return ip;
    }

    public void setIp( String ip ){
        Controller.ip = ip;
    }

    public int getPort(){
        return port;
    }

    public void setPort( int port ){
        Controller.port = port;
    }

    public void connectToServer( String ip , int port ){
        try{
            System.out.println( "Connection..." );
            Socket socket = new Socket( ip , port );
            System.out.println( "socket.isClosed = " + socket.isClosed() );
            setIp( ip );
            setPort( port );
            Controller.getInstance().setClientSocket( socket );
        }catch( IOException e ){
            System.out.println( "Connection failed" );
            System.out.println( e.getMessage() );
        }
    }

    public void reconnect(){
        connectToServer( ip , port );
    }

    private static class InstanceHolder{
        private static final Controller instance = new Controller();
    }
}


package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.DataModel;
import model.Flight;
import model.Route;
import searchengine.SearchEngine;

import java.util.Date;

public class Main extends Application{

    static DataModel    model  = DataModel.getInstance();
    static SearchEngine engine = new SearchEngine( model );

    @Override
    public void start( Stage primaryStage ) throws Exception{


        Parent root = FXMLLoader.load( getClass().getResource( "/fxml/RoutesFlightsOverview.fxml" ) );
        primaryStage.setTitle( "Routes and flights" );
        primaryStage.setScene( new Scene( root , 700 , 500 ) );
        primaryStage.setX( 5 );
        primaryStage.setY( 5 );
        primaryStage.setResizable( false );
        setUserAgentStylesheet( STYLESHEET_CASPIAN );
        primaryStage.show();
    }


    public static void main( String[] args ){

        Route route1 = new Route( "QqQ" , "WwW" );
        Route route2 = new Route( "QqQ1" , "WqW" );
        Route route3 = new Route( "WwW" , "QqQ" );

        Flight flight1 = new Flight( "qwer1" , route1 , "Q1" , new Date( 117 , 10 , 8 ) , new Date( 2017 , 10 , 10 ) );
        Flight flight2 = new Flight( "qer1" , route1 , "q2" , new Date( 117 , 10 , 9 ) , new Date( 117 , 10 , 13 ) );
        Flight flight3 = new Flight( "asqwer1d" , route2 , "a2" , new Date( 117 , 10 , 5 ) , new Date( 117 , 10 , 7 ) );
        Flight flight4 = new Flight( "awed1" , route3 , "A132" , new Date( 117 , 10 , 2 ) , new Date( 117 , 10 , 3 ) );

        model.addRoute( route1 );
        model.addRoute( route2 );
        model.addRoute( route3 );

        model.addFlight( flight1 );
        model.addFlight( flight2 );
        model.addFlight( flight3 );
        model.addFlight( flight4 );

        launch( args );
    }

    public static SearchEngine getEngine(){
        return engine;
    }

    public static DataModel getModel(){return model;}
}

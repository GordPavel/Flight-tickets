package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.DataModel;
import model.Flight;
import model.Route;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Main extends Application{

    @Override
    public void start( Stage primaryStage ) throws Exception{
        DataModel dataModel = DataModel.getInstance();
        List<Route> routes = Stream.of( new Route( "port1" , "port2" ) , new Route( "port1" , "port3" ) ,
                                        new Route( "port2" , "port3" ) , new Route( "port3" , "port1" ) ,
                                        new Route( "port3" , "port2" ) , new Route( "port2" , "port1" ) )
                                   .collect( Collectors.toList() );
        routes.forEach( dataModel::addRoute );
        IntStream.rangeClosed( 1 , 10 ).mapToObj(
                i -> new Flight( String.format( "number%d" , i ) , routes.get( ( i - 1 ) % routes.size() ) ,
                                 String.format( "planeId%d" , i + 1 ) , Date.from(
                        LocalDateTime.of( 2009 + i , 12 , 15 , 10 , 30 ).atZone( ZoneId.of( "Europe/Samara" ) )
                                     .toInstant() ) , Date.from(
                        LocalDateTime.of( 2009 + i , 12 , 15 , 11 + i , 30 ).atZone( ZoneId.of( "Europe/Samara" ) )
                                     .toInstant() ) ) ).forEach( dataModel::addFlight );

        FXMLLoader loader = new FXMLLoader( getClass().getResource( "/fxml/MainWindow.fxml" ) );
        loader.setController( new MainWindowController( primaryStage ) );
        primaryStage.setX( 5 );
        primaryStage.setY( 5 );
        primaryStage.setTitle( "Routes and flights" );
        primaryStage.setScene( new Scene( loader.load() ) );
        primaryStage.setResizable( false );
        primaryStage.show();
    }

    public static void main( String[] args ){
        launch( args );
    }
}

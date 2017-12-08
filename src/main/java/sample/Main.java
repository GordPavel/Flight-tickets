package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.DataModel;


import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class Main extends Application{

    private static DataModel model   = DataModel.getInstance();
    static         Boolean   changed = false;
    static File savingFile;

    @Override
    public void start( Stage primaryStage ) throws Exception{
        FXMLLoader                      loader     =
                new FXMLLoader( getClass().getResource( "/fxml/RoutesFlightsOverview.fxml" ) );
        RoutesFlightsOverviewController controller = new RoutesFlightsOverviewController( primaryStage );
        loader.setController( controller );
        primaryStage.setTitle( "Routes and flights" );
        primaryStage.setScene( new Scene( loader.load() , 700 , 500 ) );
        primaryStage.setX( 5 );
        primaryStage.setY( 5 );
        primaryStage.setOnCloseRequest( event -> {
            if( changed ){
                Optional<ButtonType> answer =
                        new Alert( Alert.AlertType.CONFIRMATION , "Your data is unsaved, close anyway?" ,
                                   ButtonType.YES , new ButtonType( savingFile != null ? "Save" : "Save as..." ,
                                                                    ButtonBar.ButtonData.APPLY ) , ButtonType.CANCEL )
                                .showAndWait();
                answer.map( ButtonType::getButtonData ).ifPresent( buttonData -> {
                    switch( buttonData ){
                        case YES:
                            break;
                        case APPLY:
                            if( savingFile == null ){
                                FileChooser fileChooser = new FileChooser();
                                fileChooser.getExtensionFilters()
                                           .add( new FileChooser.ExtensionFilter( "Flights and routes" , "*.far" ) );
                                Optional<File> savingFileOptional =
                                        Optional.ofNullable( fileChooser.showSaveDialog( new Stage() ) );
                                if( savingFileOptional.isPresent() ){
                                    savingFile = savingFileOptional.get();
                                }else{
                                    event.consume();
                                    return;
                                }
                            }
                            try{
                                DataModel.getInstance().exportToFile( savingFile );
                            }catch( IOException e ){
                                e.printStackTrace();
                            }
                            break;
                        case CANCEL_CLOSE:
                            event.consume();
                            break;
                    }
                } );
            }
        } );
        primaryStage.setResizable( false );
        setUserAgentStylesheet( STYLESHEET_CASPIAN );
        primaryStage.show();
    }

    public static void main( String[] args ){
//        Route route1 = new Route( "QqQ" , "WwW" );
//        Route route2 = new Route( "QqQ1" , "WqW" );
//        Route route3 = new Route( "WwW" , "QqQ" );
//
//        Flight flight1 = new Flight( "qwer1" , route1 , "Q1" , new Date( 117 , 10 , 8 ) , new Date( 2017 , 10 , 10 ) );
//        Flight flight2 = new Flight( "qer1" , route1 , "q2" , new Date( 117 , 10 , 9 ) , new Date( 117 , 10 , 13 ) );
//        Flight flight3 = new Flight( "asqwer1d" , route2 , "a2" , new Date( 117 , 10 , 5 ) , new Date( 117 , 10 , 7 ) );
//        Flight flight4 = new Flight( "awed1" , route3 , "A132" , new Date( 117 , 10 , 2 ) , new Date( 117 , 10 , 3 ) );
//
//        model.addRoute( route1 );
//        model.addRoute( route2 );
//        model.addRoute( route3 );
//
//        model.addFlight( flight1 );
//        model.addFlight( flight2 );
//        model.addFlight( flight3 );
//        model.addFlight( flight4 );

        launch( args );
    }

}

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

    static Boolean changed = false;
    static File savingFile;

    @Override
    public void start( Stage primaryStage ) throws Exception{
        FXMLLoader loader = new FXMLLoader( getClass().getResource( "/fxml/RoutesFlightsOverview.fxml" ) );
        RoutesFlightsOverviewController controller = new RoutesFlightsOverviewController( primaryStage );
        loader.setController( controller );
        primaryStage.setTitle( "Information system about flights and routes" );
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
        primaryStage.show();
    }

    public static void main( String[] args ){
        launch( args );
    }

}

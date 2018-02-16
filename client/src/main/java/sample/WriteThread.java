package sample;

import javafx.scene.control.Alert;
import model.DataModelInstanceSaver;
import org.codehaus.jackson.map.ObjectMapper;
import transport.Data;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;


/**
 TODO: change parent thread
 <p>
 create new parent tread, that will leave until stop command... Also, change in Controller "Thread thread" to new parent
 */
public class WriteThread extends FaRThread{

    Socket clientSocket;
    private boolean stop = false;

    public void setStop(){
        this.stop = true;
    }

    public WriteThread(){
        super();
    }

    public void start(){
        clientSocket = Controller.getInstance().getClientSocket();
        super.start();
    }

    public void run(){
        while( !stop ){
            System.out.println( 123 );
            if( Controller.getInstance().getClientSocket().isClosed() ){
                Controller.getInstance().reconnect();
            }
            if( Controller.getInstance().getClientSocket().isConnected() ){
                Data data;
                ObjectMapper mapper = new ObjectMapper();
                try( DataInputStream inputStream = new DataInputStream(
                        Controller.getInstance().getClientSocket().getInputStream() ) ){
                    data = mapper.reader( Data.class ).readValue( inputStream.readUTF() );
                    data.withoutExceptionOrWith( data1 -> data1.getChanges()
                                                               .forEach( update -> update.apply(
                                                                       DataModelInstanceSaver.getInstance() ) ) ,
                                                 error -> {
                                                     Alert alert = new Alert( Alert.AlertType.WARNING );
                                                     alert.setTitle( "Error" );
                                                     alert.setHeaderText( "Server error" );
                                                     alert.setContentText( error.getMessage() );
                                                     alert.showAndWait();
                                                 } );
                }catch( IOException | NullPointerException ex ){
                    System.out.println( ex.getMessage() );
                }
            }
            try{
                Thread.sleep( 5000 );
            }catch( InterruptedException ex ){

            }
        }

    }

}

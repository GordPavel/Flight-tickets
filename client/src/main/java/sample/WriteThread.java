package sample;

import javafx.scene.control.Alert;
import model.DataModelInstanceSaver;
import org.codehaus.jackson.map.ObjectMapper;
import transport.Data;
import transport.ListChangeAdapter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;


/**
 TODO: change parent thread
 <p>
 create new parent tread, that will leave until stop command... Also, change in Controller "Thread thread" to new parrent
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
            System.out.println(123);
            if (Controller.getInstance().getClientSocket().isClosed())
            {
                Controller.getInstance().reconnect();
            }
            if( Controller.getInstance().getClientSocket().isConnected() ){
                Data data = new Data();
                ObjectMapper mapper = new ObjectMapper();
                try(DataInputStream inputStream = new DataInputStream(Controller.getInstance().getClientSocket().getInputStream())){
                    data = mapper.reader(Data.class).readValue(inputStream.readUTF());
                    if( data.notHasException() ){
                        for( ListChangeAdapter update : data.getChanges() ){
                            update.apply( DataModelInstanceSaver.getInstance() );
                        }
                    }else{
                        Alert alert = new Alert( Alert.AlertType.WARNING );
                        alert.setTitle( "Error" );
                        alert.setHeaderText( "Server error" );
                        alert.setContentText( data.getException().getMessage() );
                        alert.showAndWait();
                    }
                }catch( IOException | NullPointerException ex ){
                    System.out.println( ex.getMessage() );
                }
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex)
            {

            }
        }

    }

}

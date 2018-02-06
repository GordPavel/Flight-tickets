package sample;

import javafx.scene.control.Alert;
import model.DataModelInstanceSaver;
import org.codehaus.jackson.map.ObjectMapper;
import transport.Data;
import transport.ListChangeAdapter;

import java.io.IOException;
import java.net.Socket;


/**
 TODO: change parent thread
 <p>
 create new parent tread, that will leave until stop command... Also, change in Controller "Thread thread" to new parrent
 */
class ReadOnlyThread extends FaRThread{

    private Socket clientSocket;
    public  int     test = 0;
    private boolean stop = false;

    public void setStop(){
        this.stop = true;
    }

    public ReadOnlyThread(){
        super();
    }

    public void start(){
        clientSocket = Controller.getInstance().getClientSocket();
        super.start();
    }

    public void run(){
        while( !stop ){
            updateData();
            System.out.println( test++ );
            try{
                Thread.sleep( 1000 );
            }catch( InterruptedException e ){

            }

        }

    }

    synchronized void updateData(){
        Data data = new Data();
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(Controller.getInstance().getClientSocket().getOutputStream(), Controller.getInstance().getUserInformation());
            data = (Data) mapper.readValue(Controller.getInstance().getClientSocket().getInputStream(), Data.class);
            if (data.notHasException()) {
                for (ListChangeAdapter update : data.getListChangeAdapters()) {
                    update.apply(DataModelInstanceSaver.getInstance());
                }
            } else {
                Alert alert = new Alert( Alert.AlertType.WARNING );
                alert.setTitle( "Error" );
                alert.setHeaderText( "Server error" );
                alert.setContentText( data.getException().getMessage() );
                alert.showAndWait();
            }
        } catch (IOException | NullPointerException ex) {
            System.out.println(ex.getMessage());
        }
    }
}

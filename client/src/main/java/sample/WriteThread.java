package sample;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.DataModelInstanceSaver;
import transport.Data;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.SocketException;


/**
 TODO: change parent thread
 <p>
 create new parent tread, that will leave until stop command... Also, change in Controller "Thread thread" to new parent
 */
public class WriteThread extends FaRThread{

    private boolean stop = false;

    RoutesFlightsOverviewController parentController;

    public WriteThread(RoutesFlightsOverviewController parentController){
        super();
        this.parentController = parentController;
    }

    public void setStop(){
        this.stop = true;
    }

    public void start(){
        super.start();
    }

    public void run(){
        while( !stop ){
            System.out.println( 123 );
            Data data = parentController.receiveUpdate( );
            if (!(RoutesFlightsOverviewController.getChanges().isEmpty())){
                FaRExchanger.exchange(data);
            }
            try{
                Thread.sleep( 100 );
            }
            catch( InterruptedException ex ){

            }
        }

    }

}

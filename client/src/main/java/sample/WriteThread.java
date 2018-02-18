package sample;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.DataModelInstanceSaver;
import transport.Data;

import java.io.DataInputStream;
import java.io.IOException;


/**
 TODO: change parent thread
 <p>
 create new parent tread, that will leave until stop command... Also, change in Controller "Thread thread" to new parent
 */
public class WriteThread extends FaRThread{

    private boolean stop = false;

    public WriteThread(){
        super();
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
            if( Controller.getInstance().getClientSocket().isClosed() ){
                Controller.getInstance().reconnect();
            }
            if( Controller.getInstance().getClientSocket().isConnected() ){
                Data data;
                ObjectMapper mapper = new ObjectMapper();
                try( DataInputStream inputStream = new DataInputStream( Controller.getInstance()
                                                                                  .getClientSocket()
                                                                                  .getInputStream() ) ){
                    data = mapper.readerFor( Data.class ).readValue( inputStream.readUTF() );
                    data.withoutExceptionOrWith( data1 -> data1.getChanges()
                                                               .forEach( update -> update.apply( DataModelInstanceSaver.getInstance() ) ) ,
                                                 ClientMain::showWarningByError );
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

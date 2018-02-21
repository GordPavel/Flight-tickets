package sample;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.DataModelInstanceSaver;
import transport.Data;

import java.io.*;
import java.net.Socket;


/**
 TODO: change parent thread
 <p>
 create new parent tread, that will leave until stop command... Also, change in Controller "Thread thread" to new parent
 */
class ReadOnlyThread extends FaRThread{

    RoutesFlightsOverviewController parentController;
    private Socket clientSocket;
    private boolean stop = false;

    public ReadOnlyThread(){
        super();
    }

    public ReadOnlyThread( RoutesFlightsOverviewController parentController ){
        super();
        this.parentController = parentController;
    }

    public void setStop(){
        this.stop = true;
    }

    public void start(){
        clientSocket = Controller.getInstance().getClientSocket();
        super.start();
    }

    public void run(){
        while( !stop ){
            saveDM();
            Controller.getInstance().reconnect();
            parentController.requestUpdate();
            parentController.receiveUpdate();
            try{
                Thread.sleep( 10000 );
            }catch( InterruptedException e ){

            }

        }

    }

    synchronized void updateData(){

        if( Controller.getInstance().getClientSocket().isClosed() ){
            Controller.getInstance().reconnect();
        }
        if( !Controller.getInstance().getClientSocket().isConnected() ){
            parentController.routeConnectLabel.setText( "Offline" );
            parentController.flightConnectLabel.setText( "Offline" );
            Controller.getInstance().reconnect();
        }
        if( Controller.getInstance().getClientSocket().isConnected() ){
            parentController.routeConnectLabel.setText( "Online" );
            parentController.flightConnectLabel.setText( "Online" );
            Data data;
            ObjectMapper mapper = new ObjectMapper();
            Controller.getInstance().getUserInformation().setPredicate( null );
            try( DataOutputStream dataOutputStream = new DataOutputStream( Controller.getInstance()
                                                                                     .getClientSocket()
                                                                                     .getOutputStream() ) ;
                 DataInputStream inputStream = new DataInputStream( Controller.getInstance()
                                                                              .getClientSocket()
                                                                              .getInputStream() ) ){
                dataOutputStream.writeUTF( mapper.writeValueAsString( Controller.getInstance().getUserInformation() ) );
                data = mapper.readerFor( Data.class ).readValue( inputStream.readUTF() );
                //noinspection CodeBlock2Expr
                data.withoutExceptionOrWith( data1 -> {
                    data1.getChanges().forEach( update -> update.apply( DataModelInstanceSaver.getInstance() ) );
                } , ClientMain::showWarningByError );
            }catch( IOException | NullPointerException ex ){
                System.out.println( ex.getMessage() );
                ex.printStackTrace();
                System.out.println( "Yep" );
            }
            Controller.getInstance().getUserInformation().setPredicate( null );
        }
    }

    private void saveDM(){
        File file = new File( Controller.getInstance().getUserInformation().getDataBase() + ".dm" );
        try{
            if( !file.exists() ){
                BufferedWriter writer =
                        new BufferedWriter( new OutputStreamWriter( new FileOutputStream( file ) , "utf-8" ) );
                writer.write( "temp" );
            }
            DataModelInstanceSaver.getInstance().saveTo( new FileOutputStream( file ) );
        }catch( IOException ex ){
            System.out.println( ex.getMessage() );
        }
    }
}

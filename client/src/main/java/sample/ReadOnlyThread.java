package sample;

import java.net.Socket;


/**
 TODO: change parent thread
 <p>
 create new parent tread, that will leave until stop command... Also, change in Controller "Thread thread" to new parent
 */
class ReadOnlyThread extends FaRThread{

    private Socket clientSocket;
    private boolean stop = false;
    RoutesFlightsOverviewController parentController;

    public void setStop(){
        this.stop = true;
    }

    public ReadOnlyThread(){
        super();
    }

    public ReadOnlyThread(RoutesFlightsOverviewController parentController){
        super();
        this.parentController = parentController;
    }

    public void start(){
        clientSocket = Controller.getInstance().getClientSocket();
        super.start();
    }

    public void run(){
        while( !stop ){
            updateData();
            try{
                Thread.sleep( 1000 );
            }catch( InterruptedException e ){

            }

        }

    }

    synchronized void updateData() {

        parentController.requestUpdate(null);
    }
}

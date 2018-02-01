package sample;

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
            // TODO: updating data from server
            updateData();
            System.out.println( test++ );
            try{
                Thread.sleep( 1000 );
            }catch( InterruptedException e ){

            }

        }

    }

    synchronized void updateData(){
//        todo : Закончить
    }
}

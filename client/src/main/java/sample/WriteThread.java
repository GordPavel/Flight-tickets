package sample;

import java.net.Socket;


/**
 *
 * TODO: change parent thread
 *
 * create new parent tread, that will leave until stop command... Also, change in Controller "Thread thread" to new parrent
 *
 */
public class WriteThread extends FaRThread {

    Socket clientSocket;
    public int test=0;
    private boolean stop=false;

    public void setStop() {
        this.stop = true;
    }

    public WriteThread() {
        super();
    }

    public void start() {
        clientSocket=ClientMain.getClientSocket();
        super.start();
    }

    public void run() {
        while (!stop) {
            /**
             * TODO: updating data from server
             */
            updateData();
            System.out.println(test++);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e)
            {

            }

        }

    }

    synchronized void updateData(){

    }
}
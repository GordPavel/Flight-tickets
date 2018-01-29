package sample;


/**
 Abstract patent thread for client application
 */
abstract class FaRThread extends Thread{

    FaRThread(){
        super();
    }

    abstract void setStop();
}

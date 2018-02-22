package sample;

import transport.Data;

public class FaRExchanger {

    static boolean haveSome = false;
    static Data toExchange=null;

    static Data exchange(){
        while (!haveSome)
        {
            try{
                Thread.sleep( 500 );
            }
            catch( InterruptedException ex ){

            }
        }
        haveSome=false;
        return toExchange;
    }
    static void exchange(Data data){
        toExchange=data;
        haveSome=true;
    }
}

package sample;

import transport.Data;

/**
 * Class for exchanging objects between threads
 */
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
        if (data.getChanges().get(0).equalsEntities(RoutesFlightsOverviewController.getChanges().get(0))) {
            toExchange = data;
            haveSome = true;
        }
    }
}

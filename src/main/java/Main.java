import model.DataModel;
import model.Flight;
import model.Route;
import searchengine.SearchEngine;

import java.util.Date;

public class Main{

    public static void main( String[] args ){
        DataModel model = DataModel.getInstance();

        Route route1 = new Route( "QqQ" , "WwW" );
        Route route2 = new Route( "QqQ1" , "WqW" );
        Route route3 = new Route( "WwW" , "QqQ" );

        Flight flight1 = new Flight( "qwer1" , route1 , "Q1" , new Date( 100 ) , new Date( 120 ) );
        Flight flight2 = new Flight( "qer1" , route1 , "q2" , new Date( 130 ) , new Date( 140 ) );
        Flight flight3 = new Flight( "asqwer1d" , route2 , "a2" , new Date( 150 ) , new Date( 160 ) );
        Flight flight4 = new Flight( "awed1" , route3 , "A132" , new Date( 170 ) , new Date( 180 ) );

        SearchEngine searchEngine = new SearchEngine( model );

        model.addRoute( route1 );
        model.addRoute( route2 );
        model.addRoute( route3 );

        model.addFlight( flight1 );
        model.addFlight( flight2 );
        model.addFlight( flight3 );
        model.addFlight( flight4 );


        //System.out.println(route1);
        /*List<Route> routes = searchEngine.findRoutesByDepartureAirport("???");

        for (Route route:routes)
        {
            System.out.println(route);
        }*/


    }
}

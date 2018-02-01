package transport;

import model.Flight;
import model.Route;

import java.util.function.Predicate;

/**
 Information about user ( name, password ) and database, which is required
 */

public class UserInformation{

    private String name;
    private String password;
    private String dataBase;
    private Predicate<Flight> flightPredicate;
    private Predicate<Route> routePredicate;
    private ListChangeAdapter listChangeAdapter;

    UserInformation(){

    }

    public String getName(){
        return name;
    }

    public void setName( String name ){
        this.name = name;
    }

    public String getPassword(){
        return password;
    }

    public void setPassword( String password ){
        this.password = password;
    }

    public String getDataBase(){
        return dataBase;
    }

    public void setDataBase( String dataBase ){
        this.dataBase = dataBase;
    }

    public Predicate<Flight> getFlightPredicate() {
        return flightPredicate;
    }

    public void setFlightPredicate(Predicate<Flight> flightPredicate) {
        this.flightPredicate = flightPredicate;
    }

    public Predicate<Route> getRoutePredicate() {
        return routePredicate;
    }

    public void setRoutePredicate(Predicate<Route> routePredicate) {
        this.routePredicate = routePredicate;
    }

    public ListChangeAdapter getListChangeAdapter() {
        return listChangeAdapter;
    }

    public void setListChangeAdapter(ListChangeAdapter listChangeAdapter) {
        this.listChangeAdapter = listChangeAdapter;
    }
}

package model;

import javafx.collections.ListChangeListener;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DataModelWithLockAndListener{
    DataModel                  model;
    ReentrantReadWriteLock     lock;
    ListChangeListener<Route>  routeListener;
    ListChangeListener<Flight> flightListener;

    DataModelWithLockAndListener( DataModel model , ReentrantReadWriteLock lock ,
                                  ListChangeListener<Route> routeListener , ListChangeListener<Flight> flightListener ){
        this.model = model;
        this.lock = lock;
        this.routeListener = routeListener;
        this.flightListener = flightListener;
    }

    public DataModel getModel(){
        return model;
    }

    public void setModel( DataModel model ){
        this.model = model;
    }

    public ReentrantReadWriteLock getLock(){
        return lock;
    }

    public void setLock( ReentrantReadWriteLock lock ){
        this.lock = lock;
    }

    public ListChangeListener<Route> getRouteListener(){
        return routeListener;
    }

    public void setRouteListener( ListChangeListener<Route> routeListener ){
        this.routeListener = routeListener;
    }

    public ListChangeListener<Flight> getFlightListener(){
        return flightListener;
    }

    public void setFlightListener( ListChangeListener<Flight> flightListener ){
        this.flightListener = flightListener;
    }
}

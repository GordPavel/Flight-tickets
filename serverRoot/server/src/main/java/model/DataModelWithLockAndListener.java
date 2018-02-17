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

}

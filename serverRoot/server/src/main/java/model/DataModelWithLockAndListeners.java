package model;

import javafx.collections.ListChangeListener;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DataModelWithLockAndListeners{
    DataModel                  model;
    ReentrantReadWriteLock     lock;
    ListChangeListener<Route>  routeReadListener;
    ListChangeListener<Flight> flightReadListener;
    ListChangeListener<Route>  routeWriteListener;
    ListChangeListener<Flight> flightWriteListener;

    public DataModelWithLockAndListeners( DataModel model ,
                                          ReentrantReadWriteLock lock ,
                                          ListChangeListener<Route> routeReadListener ,
                                          ListChangeListener<Flight> flightReadListener ,
                                          ListChangeListener<Route> routeWriteListener ,
                                          ListChangeListener<Flight> flightWriteListener ){
        this.model = model;
        this.lock = lock;
        this.routeReadListener = routeReadListener;
        this.flightReadListener = flightReadListener;
        this.routeWriteListener = routeWriteListener;
        this.flightWriteListener = flightWriteListener;
    }

    public DataModel getModel(){
        return model;
    }

}

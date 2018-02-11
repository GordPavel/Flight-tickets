package model;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DataModelWithLock{
    DataModel              model;
    ReentrantReadWriteLock lock;

    public DataModelWithLock( DataModel model , ReentrantReadWriteLock lock ){
        this.model = model;
        this.lock = lock;
    }

    public DataModel getModel(){
        return model;
    }

    public ReentrantReadWriteLock getLock(){
        return lock;
    }
}

package model;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DataModelWithLock{
    DataModel              model;
    ReentrantReadWriteLock lock;

    public DataModelWithLock( DataModel model , ReentrantReadWriteLock lock ){
        this.model = model;
        this.lock = lock;
    }

    public ReentrantReadWriteLock.ReadLock getReadLock(){
        return lock.readLock();
    }
}

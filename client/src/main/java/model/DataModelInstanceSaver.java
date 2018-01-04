package model;

public class DataModelInstanceSaver{
    private static class InstanceHolder{
        private static final DataModel instance = new DataModel();
    }

    public static synchronized DataModel getInstance(){
        return InstanceHolder.instance;
    }
}

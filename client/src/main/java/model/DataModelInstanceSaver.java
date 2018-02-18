package model;

public class DataModelInstanceSaver{
    public static synchronized DataModel getInstance(){
        return InstanceHolder.instance;
    }

    private static class InstanceHolder{
        private static final DataModel instance = new DataModel();
    }
}

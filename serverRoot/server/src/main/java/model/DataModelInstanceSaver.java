package model;

public class DataModelInstanceSaver{
    public static synchronized DataModel getInstance(){
        return new DataModel();
    }
}

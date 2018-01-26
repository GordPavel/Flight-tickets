package transport;

/**
 * Information about user ( name, password ) and database, which is required
 */

public class UserInformation {

    private String name;
    private String password;
    private String dataBase;


    public String getName() {

        return name;
    }

    public void setName(String name ) {

        this.name = name;
    }

    public String getPassword() {

        return password;
    }

    public void setPassword(String password ) {

        this.password = password;
    }

    public String getDataBase() {

        return dataBase;
    }

    public void setDataBase(String dataBase ) {

        this.dataBase = dataBase;
    }


}

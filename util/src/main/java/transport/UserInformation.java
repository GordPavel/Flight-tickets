package transport;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.List;

/**
 Information about user ( login, password ) and database, which is required
 */

public class UserInformation{
    private String          login;
    private String          password;
    private String          dataBase;
    private PredicateParser predicateParser;

    private List<ListChangeAdapter> changes;

    public UserInformation(){}

    public UserInformation( UserInformation user ){
        this.login = user.getLogin();
        this.password = user.getPassword();
        this.dataBase = user.getDataBase();
    }

    public UserInformation( String login , String password ){
        this.login = login;
        this.password = password;
    }

    public UserInformation( String login , String password , String dataBase ){
        this.login = login;
        this.password = password;
        this.dataBase = dataBase;
    }

    @JsonGetter( "login" )
    public String getLogin(){
        return login;
    }

    @JsonSetter( "login" )
    public void setLogin( String login ){this.login = login;}

    @JsonGetter( "password" )
    public String getPassword(){
        return password;
    }

    @JsonSetter( "password" )
    public void setPassword( String password ){
        this.password = password;
    }

    @JsonGetter( "dataBase" )
    public String getDataBase(){
        return dataBase;
    }

    @JsonSetter( "dataBase" )
    public void setDataBase( String dataBase ){
        this.dataBase = dataBase;
    }

    @JsonGetter( "predicateParser" )
    public PredicateParser getPredicate(){
        return predicateParser;
    }

    @JsonSetter( "predicateParser" )
    public void setPredicate( PredicateParser predicateParser ){
        this.predicateParser = predicateParser;
    }

    @JsonGetter( "changes" )
    public List<ListChangeAdapter> getChanges(){
        return changes;
    }

    @JsonSetter( "changes" )
    public void setChanges( List<ListChangeAdapter> changes ){
        this.changes = changes;
    }


    @Override
    public boolean equals( Object obj ){
        if( !( obj instanceof UserInformation ) ) return false;
        UserInformation information = ( UserInformation ) obj;
        return login.equals( information.login ) && password.equals( information.password ) && dataBase == null ?
               information.dataBase == null :
               dataBase.equals( information.dataBase ) &&
               ( predicateParser == null && information.predicateParser == null ) ||
               ( predicateParser != null && information.predicateParser != null );
    }
}

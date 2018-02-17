package transport;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import model.FlightOrRoute;
import org.danekja.java.util.function.serializable.SerializablePredicate;

import java.util.List;
import java.util.function.Predicate;

/**
 Information about user ( login, password ) and database, which is required
 */

public class UserInformation{

    private String                                         login;
    private String                                         password;
    private String                                         dataBase;
    @JsonSerialize( using = PredicateSerializer.class )
    @JsonDeserialize( using = PredicateDeserializer.class )
    private SerializablePredicate<? extends FlightOrRoute> predicate;
    private List<ListChangeAdapter>                        changes;

    public UserInformation(){}

    public UserInformation( String login , String password ){
        this.login = login;
        this.password = password;
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

    @JsonGetter( "predicate" )
    public Predicate<? extends FlightOrRoute> getPredicate(){
        return predicate;
    }

    @JsonSetter( "predicate" )
    public void setPredicate( SerializablePredicate<? extends FlightOrRoute> predicate ){
        this.predicate = predicate;
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
               dataBase.equals( information.dataBase ) && ( predicate == null && information.predicate == null ) ||
               ( predicate != null && information.predicate != null );
    }
}

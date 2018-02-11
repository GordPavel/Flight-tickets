package transport;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import model.FlightOrRoute;
import org.danekja.java.util.function.serializable.SerializablePredicate;

import java.io.Serializable;
import java.util.List;
import java.util.List;
import java.util.function.Predicate;

/**
 Information about user ( name, password ) and database, which is required
 */

public class UserInformation{

    private String                                       name;
    private String                                       password;
    private String                                       dataBase;
    @JsonSerialize( using = PredicateSerializer.class )
    @JsonDeserialize( using = PredicateDeserializer.class )
    private SerializablePredicate<? super FlightOrRoute> predicate;
    private List<ListChangeAdapter>                      changes;

    public UserInformation(){}

    //    @JsonCreator
    UserInformation(
            @JsonProperty( "name" )
                    String login ,
            @JsonProperty( "password" )
                    String password ,
            @JsonProperty( "dataBase" )
                    String dataBase ,
            @JsonProperty( "predicate" )
                    SerializablePredicate<? super FlightOrRoute> predicate ,
            @JsonProperty( "changes" )
                    List<ListChangeAdapter> changes ){
        this.name = login;
        this.password = password;
        this.dataBase = dataBase;
        this.predicate = predicate;
        this.changes = changes;
    }

    @JsonGetter( "name" )
    public String getLogin(){
        return name;
    }

    @JsonSetter( "name" )

    public void setName(String name) {this.name=name;}

    @JsonGetter( "password" )
    public String getPassword(){
        return password;
    }

    @JsonSetter( "password" )
    public void setPassword(String password) {
        this.password = password;
    }

    @JsonGetter( "dataBase" )
    public String getDataBase(){
        return dataBase;
    }

    @JsonSetter( "dataBase" )
    public void setDataBase(String dataBase) {
        this.dataBase = dataBase;
    }

    @JsonGetter( "predicate" )
    public Predicate<? super FlightOrRoute> getPredicate(){
        return predicate;
    }

    @JsonSetter( "predicate" )
    public void setPredicate(SerializablePredicate<? super FlightOrRoute> predicate) {
        this.predicate = predicate;
    }

    @JsonGetter( "changes" )
    public List<ListChangeAdapter> getChanges(){
        return changes;
    }

    @JsonSetter( "changes" )
    public void setChanges(List<ListChangeAdapter> changes) {
        this.changes = changes;
    }


    @Override
    public int hashCode(){
        return super.hashCode();
    }

    @Override
    public boolean equals( Object obj ){
        if( !( obj instanceof UserInformation ) ) return false;
        UserInformation information = ( UserInformation ) obj;
        return name.equals( information.name ) && password.equals( information.password ) && dataBase == null ?
               information.dataBase == null :
               dataBase.equals( information.dataBase ) && ( predicate == null && information.predicate == null ) ||
               ( predicate != null && information.predicate != null );
    }
}

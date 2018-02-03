package transport;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    public String getName(){
        return name;
    }

    @JsonGetter( "password" )
    public String getPassword(){
        return password;
    }

    @JsonProperty( "dataBase" )
    public String getDataBase(){
        return dataBase;
    }

    @JsonProperty( "predicate" )
    public Predicate<? super FlightOrRoute> getPredicate(){
        return predicate;
    }

    @JsonProperty( "changes" )
    public List<ListChangeAdapter> getChanges(){
        return changes;
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

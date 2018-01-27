package transport;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import model.FlightOrRoute;

import java.util.Optional;
import java.util.function.Predicate;

//Class for sending object for change and an action
//If you what to specify exact classes, that allow in any generic, use interfaces
@JsonPropertyOrder( { "action" , "typeOfObject" , "objectForAction" , "predicate" } )
public class Actions{

    // If you have any set of arguments to use, write an enum
    public enum ActionsType{
        DELETE,
        ADD,
        EDIT,
        UPDATE
    }

    @JsonDeserialize( using = ActionsDeserializer.class )
    @JsonSerialize( using = ActionsSerializer.class )
    private FlightOrRoute     objectForAction;
    private ActionsType       action;
    private String            typeOfObject;
    private Predicate<String> predicate;

    @JsonCreator
    public Actions(
            @JsonProperty( "objectForAction" )
                    FlightOrRoute objectForAction ,
            @JsonProperty( "action" )
                    ActionsType action ,
            @JsonProperty( "predicate" )
                    Predicate<String> predicate ){
        this.objectForAction = objectForAction;
        this.action = action;
        this.typeOfObject = objectForAction.getClass().getTypeName();
        this.predicate = predicate;
    }

    public FlightOrRoute getObjectForAction(){
        return objectForAction;
    }

    public String getTypeOfObject(){
        return typeOfObject;
    }

    public ActionsType getAction(){
        return action;
    }

    public Predicate<String> getPredicate(){
        return predicate;
    }

    @Override
    public int hashCode(){
        return objectForAction.hashCode() ^ ( action.hashCode() + 31 ) ^ typeOfObject.hashCode() ^
               Optional.ofNullable( predicate ).map( Predicate::hashCode ).orElse( 50 );
    }

    @Override
    public boolean equals( Object obj ){
        if( !( obj instanceof Actions ) ) return false;
        Actions actions = ( Actions ) obj;
        return objectForAction.equals( actions.objectForAction ) && action.equals( actions.action ) &&
               typeOfObject.equals( actions.typeOfObject ) && predicate != null ?
               predicate.equals( actions.predicate ) : actions.predicate == null;
    }
}


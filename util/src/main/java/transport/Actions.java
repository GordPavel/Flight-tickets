package transport;

import exceptions.FaRWrongClassException;
import model.Flight;
import model.FlightOrRoute;
import model.Route;

//Class for sending object for change and an action
//If you what to specify exact classes, that allow in any generic, use interfaces
public class Actions<T extends FlightOrRoute>{

    // If you have any set of arguments to use, write an enum
    public enum ActionsType{
        DELETE,
        ADD,
        EDIT,
        UPDATE
    }

    private T           objectForAction;
    private ActionsType action;
    private String      typeOfObject;

    public Actions( T objectForAction , ActionsType action ){
        this.objectForAction = objectForAction;
        this.action = action;
        this.typeOfObject = objectForAction.getClass().getTypeName();
    }

//    Use switch construction before unwrapping data object
    public Route tryGetRoute(){
        if( !typeOfObject.equals( Route.class.getTypeName() ) )
            throw new FaRWrongClassException( "This POJO contains flight" );
        return Route.class.cast( objectForAction );
    }

    public Flight tryGetFlight(){
        if( !typeOfObject.equals( Flight.class.getTypeName() ) )
            throw new FaRWrongClassException( "This POJO contains route" );
        return Flight.class.cast( objectForAction );
    }

    public void setObjectForAction( T objectForAction ){
        this.objectForAction = objectForAction;
    }

    public void setAction( ActionsType action ){
        this.action = action;
    }

    public ActionsType getAction(){
        return action;
    }
}


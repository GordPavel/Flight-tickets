package exceptions;

/**
 Exception for wrong defined class of object for change
 */

public class FaRWrongClassException extends FlightAndRouteException{

    public FaRWrongClassException(){

        super();
    }

    public FaRWrongClassException( String s ){

        super( s );
    }
}

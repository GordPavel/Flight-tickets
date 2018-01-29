package exceptions;

/**
 Exception for wrong defined action sent to server
 */


public class FaRWrongActionException extends FlightAndRouteException{

    public FaRWrongActionException(){

        super();
    }

    public FaRWrongActionException( String s ){

        super( s );
    }
}

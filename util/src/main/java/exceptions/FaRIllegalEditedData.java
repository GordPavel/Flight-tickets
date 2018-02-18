package exceptions;

/**
 When edited data hasn't previous version in database
 */
public class FaRIllegalEditedData extends FlightAndRouteException{
    private static final long serialVersionUID = -5365630128856068164L;

    /**
     Constructs an <code>FaRIllegalEditedData</code> with no
     detail message.
     */
    public FaRIllegalEditedData(){
        super();
    }

    /**
     Constructs an <code>FaRIllegalEditedData</code> with the
     specified detail message.

     @param s the detail message.
     */
    public FaRIllegalEditedData( String s ){
        super( s );
    }

    /**
     Constructs a new exception with the specified detail message and
     cause.
     <p>
     <p>Note that the detail message associated with <code>cause</code> is
     <i>not</i> automatically incorporated in this exception's detail
     message.

     @param message the detail message (which is saved for later retrieval
     by the {@link Throwable#getMessage()} method).
     @param cause   the cause (which is saved for later retrieval by the
     {@link Throwable#getCause()} method).  (A {@code null} value
     is permitted, and indicates that the cause is nonexistent or
     unknown.)

     @since 1.5
     */
    public FaRIllegalEditedData( String message , Throwable cause ){
        super( message , cause );
    }

    /**
     Constructs a new exception with the specified cause and a detail
     message of {@code (cause==null ? null : cause.toString())} (which
     typically contains the class and detail message of {@code cause}).
     This constructor is useful for exceptions that are little more than
     wrappers for other throwables (for example, {@link
    java.security.PrivilegedActionException}).

     @param cause the cause (which is saved for later retrieval by the
     {@link Throwable#getCause()} method).  (A {@code null} value is
     permitted, and indicates that the cause is nonexistent or
     unknown.)

     @since 1.5
     */
    public FaRIllegalEditedData( Throwable cause ){
        super( cause );
    }
}

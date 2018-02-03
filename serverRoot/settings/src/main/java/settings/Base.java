package settings;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@XmlRootElement
public class Base implements Cloneable{
    private String     name;
    private Boolean    isRunning;
    private List<User> user;

    public Base(){
    }

    Base( String name ){
        this.name = name;
        isRunning = false;
        user = new ArrayList<>();
    }

    Base( String path , Boolean isRunning , List<User> user ){
        this.name = path;
        this.isRunning = isRunning;
        this.user = user;
    }

    public String getName(){
        return name;
    }

    @XmlAttribute
    void setName( String name ){
        this.name = name;
    }

    public Boolean isRunning(){
        return isRunning;
    }

    @XmlElement
    public void setRunning( Boolean running ){
        isRunning = running;
    }

    public List<User> getUser(){
        return user;
    }

    public Stream<String> listAllUsers(){
        return user.stream()
                   .map( user1 -> String.format( "%-2s %10s password:%s" ,
                                                 user1.getPrivilege() == UserPrivileges.ReadWrite ? "rw" : "r" ,
                                                 user1.getName() , user1.getPassword() ) );
    }

    @XmlElement
    void setUser( List<User> user ){
        this.user = user;
    }

    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder( String.format( "   %s\n" , name ) );
        for( User user : user ){
            stringBuilder.append( user.toString() );
        }
        return stringBuilder.toString();
    }

    @Override
    public int hashCode(){
        Integer integer = name.hashCode();
        for( User user : this.user ){
            integer ^= user.hashCode();
        }
        return integer;
    }

    @Override
    public boolean equals( Object obj ){
        if( !( obj instanceof Base ) ) return false;
        Base base = ( Base ) obj;
        if( this.user.size() != base.user.size() ) return false;
        Iterator<User> userIterator = base.user.iterator();
        for( User user : this.user ){
            if( !user.equals( userIterator.next() ) ) return false;
        }
        return this.name.equals( base.name );
    }

    @Override
    protected Object clone() throws CloneNotSupportedException{
        Base clone = ( Base ) super.clone();
        clone.user = user.stream().map( user1 -> {
            try{
                return ( User ) user1.clone();
            }catch( CloneNotSupportedException e ){
                throw new IllegalStateException( e );
            }
        } ).collect( Collectors.toList() );
        return clone;
    }
}

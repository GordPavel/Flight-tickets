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
    private List<User> users;

    public Base(){
    }

    Base( String name ){
        this.name = name;
        isRunning = false;
        users = new ArrayList<>();
    }

    public Base( String name , Boolean isRunning , List<User> users ){
        this.name = name;
        this.isRunning = isRunning;
        this.users = users;
    }

    public String getName(){
        return name;
    }

    @SuppressWarnings( "unused" )
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

    public List<User> getUsers(){
        return users;
    }

    public Stream<String> listAllUsers(){
        return users.stream()
                    .map( user1 -> String.format( "%-2s %10s password:%s" ,
                                                 user1.getPrivilege() == UserPrivileges.ReadWrite ? "rw" : "r" ,
                                                 user1.getLogin() , user1.getPassword() ) );
    }

    @SuppressWarnings( "unused" )
    @XmlElement
    void setUsers( List<User> users ){
        this.users = users;
    }

    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder( String.format( "   %s\n" , name ) );
        for( User user : users ){
            stringBuilder.append( user.toString() );
        }
        return stringBuilder.toString();
    }

    @Override
    public int hashCode(){
        Integer integer = name.hashCode();
        for( User user : this.users ){
            integer ^= user.hashCode();
        }
        return integer;
    }

    @Override
    public boolean equals( Object obj ){
        if( !( obj instanceof Base ) ) return false;
        Base base = ( Base ) obj;
        if( this.users.size() != base.users.size() ) return false;
        Iterator<User> userIterator = base.users.iterator();
        for( User user : this.users ){
            if( !user.equals( userIterator.next() ) ) return false;
        }
        return this.name.equals( base.name );
    }

    @Override
    protected Object clone() throws CloneNotSupportedException{
        Base clone = ( Base ) super.clone();
        clone.users = users.stream().map( user1 -> {
            try{
                return ( User ) user1.clone();
            }catch( CloneNotSupportedException e ){
                throw new IllegalStateException( e );
            }
        } ).collect( Collectors.toList() );
        return clone;
    }
}

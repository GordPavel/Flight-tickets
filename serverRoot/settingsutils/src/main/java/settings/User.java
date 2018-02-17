package settings;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@SuppressWarnings( "WeakerAccess" )
@XmlRootElement
public class User implements Cloneable{
    private String         login;
    private String         password;
    private UserPrivileges privilege;

    public User(){
    }

    public User( String login , String password , UserPrivileges privilege ){
        this.login = login;
        this.password = password;
        this.privilege = privilege;
    }

    public String getLogin(){
        return login;
    }

    @XmlAttribute
    void setLogin( String login ){
        this.login = login;
    }

    public String getPassword(){
        return password;
    }

    @XmlElement
    void setPassword( String password ){
        this.password = password;
    }

    public UserPrivileges getPrivilege(){
        return privilege;
    }

    @XmlElement
    void setPrivilege( UserPrivileges privilege ){
        this.privilege = privilege;
    }

    @Override
    public String toString(){
        return String.format( "         %s\n" + "         %s\n" + "         %s\n\n" , login , password , privilege );
    }

    @Override
    public int hashCode(){
        return login.hashCode() ^ password.hashCode() ^ privilege.hashCode();
    }

    @Override
    public boolean equals( Object obj ){
        if( !( obj instanceof User ) ) return false;
        User user = ( User ) obj;
        return this.login.equals( user.login ) && this.password.equals( user.password ) &&
               this.privilege.equals( user.privilege );
    }

    @SuppressWarnings( "EmptyMethod" )
    @Override
    protected Object clone() throws CloneNotSupportedException{
        return super.clone();
    }
}
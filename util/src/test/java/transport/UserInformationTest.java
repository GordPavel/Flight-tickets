package transport;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class UserInformationTest{

    ObjectMapper mapper = new ObjectMapper();

    @Test
    void json(){
        UserInformation information = new UserInformation();
        information.setLogin( "testLogin" );
    }
}
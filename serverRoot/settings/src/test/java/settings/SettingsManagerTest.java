package settings;

import org.junit.jupiter.api.Test;
import settings.serverexceptions.SettingsException;

class SettingsManagerTest{

    @Test
    void setCacheTimeout(){
        SettingsManager.setCacheTimeout( 1000L );
    }

    @Test
    void setDatabase(){
        String baseName = "test";
        try{
            SettingsManager.addNewBase( baseName );
            SettingsManager.startStopBase( baseName , true );
        }catch( SettingsException ignored ){
        }
    }
}
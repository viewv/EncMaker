import org.junit.jupiter.api.Test;
import top.viewv.encmaker.model.location.LocationEntity;
import top.viewv.encmaker.model.location.LocationSerialize;

import java.io.File;
import java.io.FileNotFoundException;

public class LocationTest {
    @Test
    public void addLocation() throws FileNotFoundException {
        File currentDir = new File("vault.ser");
        if (!currentDir.exists()){
            LocationEntity locationEntity = new LocationEntity();
            locationEntity.location.put("Main","~/Develop/encfs");
            locationEntity.location.put("Valut","~/Develop/encbox");
            LocationSerialize.serialize(locationEntity,"vault.ser");
        }else {
            LocationEntity locationEntity = LocationSerialize.deserialize("vault.ser");
            locationEntity.location.put("Main","~/Develop/encfs");
            locationEntity.location.put("Valut","~/Develop/encbox");
            LocationSerialize.serialize(locationEntity,"vault.ser");
        }
    }
}

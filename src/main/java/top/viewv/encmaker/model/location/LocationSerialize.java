package top.viewv.encmaker.model.location;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class LocationSerialize {
    public static void serialize(LocationEntity locationEntity, String destfilename) throws FileNotFoundException {
        Kryo kryo = new Kryo();
        kryo.register(LocationEntity.class);
        kryo.register(java.util.HashMap.class);

        Output output = new Output(new FileOutputStream(destfilename));
        kryo.writeObject(output, locationEntity);
        output.close();
    }

    public static LocationEntity deserialize(String sourcefilename) throws FileNotFoundException {
        Kryo kryo = new Kryo();
        kryo.register(LocationEntity.class);
        kryo.register(java.util.HashMap.class);

        Input input = new Input(new FileInputStream(sourcefilename));
        LocationEntity locationEntity = kryo.readObject(input, LocationEntity.class);
        input.close();
        return locationEntity;
    }
}

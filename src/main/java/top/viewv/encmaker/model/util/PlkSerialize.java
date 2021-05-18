package top.viewv.encmaker.model.util;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class PlkSerialize {
    public static void serialize(PlkEntity plkEntity, String destfilename) throws FileNotFoundException {
        Kryo kryo = new Kryo();
        kryo.register(PlkEntity.class);
        kryo.register(String.class);

        Output output = new Output(new FileOutputStream(destfilename));
        kryo.writeObject(output, plkEntity);
        output.close();
    }

    public static PlkEntity deserialize(String sourcefilename) throws FileNotFoundException {
        Kryo kryo = new Kryo();
        kryo.register(PlkEntity.class);
        kryo.register(String.class);

        Input input = new Input(new FileInputStream(sourcefilename));
        PlkEntity plkEntity = kryo.readObject(input, PlkEntity.class);
        input.close();
        return plkEntity;
    }
}

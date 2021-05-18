package top.viewv.encmaker.model.util;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class Serialize {
    public static void serialize(Entity entity, String destfilename) throws FileNotFoundException {
        Kryo kryo = new Kryo();
        kryo.register(Entity.class);
        kryo.register(String.class);

        Output output = new Output(new FileOutputStream(destfilename));
        kryo.writeObject(output, entity);
        output.close();
    }

    public static Entity deserialize(String sourcefilename) throws FileNotFoundException {
        Kryo kryo = new Kryo();
        kryo.register(Entity.class);
        kryo.register(String.class);

        Input input = new Input(new FileInputStream(sourcefilename));
        Entity entity = kryo.readObject(input, Entity.class);
        input.close();
        return entity;
    }
}

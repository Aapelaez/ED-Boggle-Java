package utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Convert {
    public static byte[] toBytes (Object o) throws IOException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);

        return baos.toByteArray();
    }

    public static Object toObject (byte[] bytes) throws ClassNotFoundException, IOException{

        return new ObjectInputStream(new ByteArrayInputStream(bytes)).readObject();
    }
}
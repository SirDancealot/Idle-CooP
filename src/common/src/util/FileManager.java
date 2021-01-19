package common.src.util;

import java.io.*;

public class FileManager {
	public static <E> E loadObject(String path) {
		try {
			File f = new File(path);
			E obj;
			if (!f.exists())
				return null;
			FileInputStream fis = new FileInputStream(path);
			ObjectInputStream ois = new ObjectInputStream(fis);
			obj = (E)ois.readObject();
			ois.close();
			fis.close();
			return obj;
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static <E> void saveObject(String path, E obj) {
		try {
			File f = new File(path);
			FileOutputStream fos = new FileOutputStream(path);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(obj);
			oos.close();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

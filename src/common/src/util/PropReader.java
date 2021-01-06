package common.src.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropReader {
	private static PropReader INSTANCE;
	private static Properties props;
	private static boolean init = false;

	private PropReader() throws IOException {
		INSTANCE = this;
		props = new Properties();
		InputStream propIS = getClass().getClassLoader().getResourceAsStream("config.properties");

		if (propIS != null) {
			props.load(propIS);
		} else {
			throw new FileNotFoundException("Property file not found");
		}
	}

	public static String getProperty(String key) {
		return props.getProperty(key);
	}

	public static PropReader getINSTANCE() throws IOException {
		if (INSTANCE == null)
			return new PropReader();
		return INSTANCE;
	}

	public static void init() throws IOException {
		if (!init) {
			init = true;
			new PropReader();
		}
	}
}

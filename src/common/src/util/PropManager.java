package common.src.util;

import java.io.*;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URL;
import java.util.Properties;

public class PropManager {
	private static PropManager INSTANCE;
	private static Properties props;
	private static boolean init = false;

	private PropManager() throws IOException {
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

	public static void setProperty(String key, String value) {
		props.setProperty(key, value);
	}

	public static PropManager getINSTANCE() throws IOException {
		if (INSTANCE == null)
			return new PropManager();
		return INSTANCE;
	}

	public static void init() throws IOException {
		if (!init) {
			init = true;
			new PropManager();
		}
		/*
		Solution to find internal IP taken from this website
		https://stackoverflow.com/questions/9481865/getting-the-ip-address-of-the-current-machine-using-java
		 */
		try (final DatagramSocket socket = new DatagramSocket()) {
			socket.connect(InetAddress.getByName("8.8.8.8"),10002);
			PropManager.setProperty("localIP", socket.getLocalAddress().getHostAddress());
		}

		/*
		Solution to find external IP taken from this website
		https://stackoverflow.com/questions/2939218/getting-the-external-ip-address-in-java
		 */
		URL ipPage = new URL("http://checkip.amazonaws.com");
		BufferedReader br = new BufferedReader(new InputStreamReader(ipPage.openStream()));
		PropManager.setProperty("externalIP", br.readLine());
	}

	public static void initData(boolean host, String hostIP, String hostPort, String localPort) throws IOException {
		init();
		if (host)
			PropManager.setProperty("host", "true");
		PropManager.setProperty("hostIP", hostIP);
		PropManager.setProperty("hostPort", hostPort);
		PropManager.setProperty("localPort", localPort);


	}
}

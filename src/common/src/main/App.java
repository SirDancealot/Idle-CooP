package common.src.main;

import common.src.main.client.Client;
import common.src.main.host.Host;
import common.src.util.PropManager;
import common.src.util.SpaceManager;

import java.io.IOException;


public class App implements Runnable {
	private boolean host;
	private String hostIP, hostPort, localPort, username;

	public App(boolean host, String hostIP, String hostPort, String localPort, String username) {
		this.host = host;
		this.hostIP = hostIP;
		this.hostPort = hostPort;
		this.localPort = localPort;
		this.username = username;
	}

	public static void main(String[] argv) {
		new Thread(new App(false, "188.178.206.128", "33333", "33333", "Eraisuithon")).start();
	}

	@Override
	public void run() {
		try {
			PropManager.initData(host, hostIP, hostPort, localPort);
			SpaceManager.init();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if(host) {
			PropManager.setProperty("hostIP",PropManager.getProperty("externalIP"));
			new Thread(new Host()).start();
		}
		new Thread(new Client(username)).start();
	}
}

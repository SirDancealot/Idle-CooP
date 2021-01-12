package common.src.main;

import common.src.main.client.Client;
import common.src.main.host.Host;
import common.src.util.PropManager;
import java.io.IOException;


public class App implements Runnable {
	private boolean host;
	private String hostIP, hostPort, localPort;

	public App(boolean host, String hostIP, String hostPort, String localPort) {
		this.host = host;
		this.hostIP = hostIP;
		this.hostPort = hostPort;
		this.localPort = localPort;
	}

	public static void main(String[] argv) {
		new Thread(new App(true, "", "", "")).start();
	}

	@Override
	public void run() {
		try {
			PropManager.initData(host, hostIP, hostPort, localPort);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if(host) {
			new Thread(new Host(PropManager.getProperty("internalIP"), PropManager.getProperty("localPort"))).start();

		}

		new Thread(new Client(PropManager.getProperty("hostIP"), PropManager.getProperty("hostPort"))).start();
	}
}

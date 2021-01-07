package common.src.main;

import common.src.util.PropManager;
import java.io.IOException;


public class App implements Runnable {
	private String state;

	public App(String state, String hostIP, String hostPort, String localPort) {
		this.state = state;
	}

	public static void main(String[] argv) {
		new Thread(new App("host", "", "", "")).start();
	}

	@Override
	public void run() {
		try {
			PropManager.init();
		} catch (IOException e) {
			e.printStackTrace();
		}

		switch (state) {
			case "host":
				new Thread(new Host(PropManager.getProperty("internalIP"), PropManager.getProperty("internalPort"), new String[] { "inbox" })).start();
			case "client":
			default:
				new Thread(new Client(PropManager.getProperty("hostIP"), PropManager.getProperty("hostPort"), "inbox")).start();
		}
	}
}

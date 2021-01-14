package common.src.main.host;

import common.src.util.SpaceManager;
import org.jspace.*;

import java.io.IOException;

public class Host implements Runnable {

	private Space lobby;
	private Space clients;

	public Host() {
		clients = new SequentialSpace();
		SpaceManager.addLocalSpace(clients, "clients");
		new Thread(new Chat()).start();
	}

	@Override
	public void run() {
		init();
		loop();
	}

	private void init() {
		lobby = new SequentialSpace();
		SpaceManager.exposeHostSpace(lobby, "lobby");
	}

	private void loop() {
		while (true) {
			try {
				Object[] tup = lobby.get(new FormalField(String.class), new FormalField(String.class), new FormalField(String.class), new FormalField(String.class));
				String req, ip, port, user;
				req = tup[0].toString();
				ip = tup[1].toString();
				port = tup[2].toString();
				user = tup[3].toString();
				switch (req) {
					case "joinReq":
						clients.put(ip, port, user);
						System.out.println("--- Connected ---");
						System.out.println(ip + ":" + port);
						break;
					case "exitReq":
						clients.getp(new ActualField(ip), new ActualField(port), new ActualField(user));
						System.out.println("--- Goodbye ---");
						System.out.println(ip + ":" + port);
						break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}

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
				Object[] tup = lobby.get(new ActualField("joinReq"), new FormalField(String.class), new FormalField(String.class));
				clients.put(tup[1].toString(), tup[2].toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}

package common.src.main;

import org.jspace.*;

import java.io.IOException;

public class Host implements Runnable {

	private String[] spaces;
	private String ip;
	private String port;
	private SpaceRepository repo;
	private SpaceRepository clients;

	public Host(String ip, String port, String[] spaces) {
		this.ip = ip;
		this.port = port;
		this.spaces = spaces;
		clients = new SpaceRepository();
	}

	@Override
	public void run() {
		init();
		loop();
	}

	private void init() {
		repo = new SpaceRepository();
		repo.addGate("tcp://" + ip + ":" + port + "/?keep");
		repo.add("lobby", new SequentialSpace());
	}

	private void loop() {

		Space lobby = repo.get("lobby");
		while (true) {
			try {
				Object[] tup = lobby.get(new ActualField("joinReq"), new FormalField(String.class), new FormalField(String.class));
				Space client = new RemoteSpace("tcp://" + tup[1].toString() + ":" + tup[2].toString() + "/local?keep");
				clients.add(tup[1].toString(), client);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}

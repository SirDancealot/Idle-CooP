package common.src.main;

import org.jspace.FormalField;
import org.jspace.SequentialSpace;
import org.jspace.Space;
import org.jspace.SpaceRepository;

public class Host implements Runnable {

	private String[] spaces;
	private String ip;
	private int port;
	private SpaceRepository repo;

	public Host(String ip, int port, String[] spaces) {
		this.ip = ip;
		this.port = port;
		this.spaces = spaces;
	}

	@Override
	public void run() {
		init();
		loop();
	}

	private void init() {
		repo = new SpaceRepository();
		repo.addGate("tcp://" + ip + ":" + port + "/?keep");

		for (String s : spaces) {
			repo.add(s, new SequentialSpace());
		}
	}

	private void loop() {
		Space inbox = repo.get("inbox");
		while (true) {
			try {
				System.out.println(inbox.get(new FormalField(String.class))[0]);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}

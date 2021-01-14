package common.src.main.host;

import common.src.util.SpaceManager;
import org.jspace.*;

import java.io.IOException;
import java.util.List;

public class Chat implements Runnable {
	private Space chatSpace;
	private Space clients;
	private SpaceRepository clientSpaces;

	@Override
	public void run() {
		init();
		loop();
	}

	private void init() {
		chatSpace = new SequentialSpace();
		clientSpaces = new SpaceRepository();
		SpaceManager.exposeHostSpace(chatSpace, "chat");
		try {
			clients = SpaceManager.getLocalSpace("clients");
		} catch (IOException e) {
			e.printStackTrace();
		}

		SpaceManager.addHostExitEvent(() -> {
			try {
				chatSpace.put("exit", "", "");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
	}

	private void loop() {
		Object[] data;
		String req, msg, uname;
		boolean stop = false;
		while (!stop) {
			try {
				data = chatSpace.get(new FormalField(String.class), new FormalField(String.class), new FormalField(String.class));
				req = data[0].toString();
				msg = data[1].toString();
				uname = data[2].toString();
				switch (req) {
					case "joined":
						updateClientSpaces(msg);
						System.out.println("Welcome: " + uname);
						break;
					case "msg":
						writeClients(msg, false, uname);
						break;
					case "disconnect":
						removeClient(uname);
						break;
					case "exit":
						stop = true;
						writeClients("exit", true, "HOST");
						break;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void writeClients(String msg, boolean hostMsg, String uname) {
		try {
			List<Object[]> clientData = clients.queryAll(new FormalField(String.class), new FormalField(String.class));
			for (Object[] o : clientData) {
				Space space = clientSpaces.get(o[0].toString());
				if (space != null) {
					space.put(msg, hostMsg, uname);
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void updateClientSpaces(String clientIP) {
		try {
			Object[] data = clients.queryp(new ActualField(clientIP), new FormalField(String.class), new FormalField(String.class));
			if (clientSpaces.get(data[0].toString()) == null)
				clientSpaces.add(data[0].toString(), SpaceManager.getRemoteSpace(data[0].toString(), data[1].toString(), "localChat"));
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}
	}

	private void removeClient(String clientIp) {
		try {
			clientSpaces.remove(clientIp).put("disconnected", true);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

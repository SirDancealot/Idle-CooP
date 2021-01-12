package common.src.main.client;

import common.src.util.PropManager;
import common.src.util.SpaceManager;
import org.jspace.FormalField;
import org.jspace.SequentialSpace;
import org.jspace.Space;

import java.io.IOException;
import java.util.Scanner;

public class Chat implements Runnable {
	private Space remoteChatSpace;
	private Space localChatSpace;
	private boolean reading;

	public Chat(boolean reading) {
		this.reading = reading;
	}

	@Override
	public void run() {
		if (reading) {
			initReader();
			loopReader();
		} else {
			initWriter();
			loopWriter();
		}
	}

	private void initWriter() {
		new Thread(new Chat(true));
		try {
			remoteChatSpace = SpaceManager.getHostSpace("chat");
			remoteChatSpace.put("joined", PropManager.getProperty("externalIP"));
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void loopWriter() {
		Scanner sc = new Scanner(System.in);
		boolean stop = false;
		while (!stop) {
			String line = sc.nextLine();
			switch (line) {
				case "stop":
					exit();
					stop = true;
					break;
			}
		}
	}

	private void initReader() {
		localChatSpace = new SequentialSpace();
		SpaceManager.exposePublicSpace(localChatSpace, "localChat");
	}

	private void loopReader() {
		boolean stop = false;
		Object[] data;
		while (!stop) {
			try {
				data = localChatSpace.get(new FormalField(String.class), new FormalField(Boolean.class));
				String msg = data[0].toString();
				boolean hostMsg = (boolean)data[1];

				if (hostMsg) {
					switch (msg) {
						case "exit":

						case "disconnected":
							stop = true;
							break;
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}

	private void exit() {
		try {
			remoteChatSpace.put("disconnect", PropManager.getProperty("externalIP"));

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void writeChat(String msg) {

	}
}

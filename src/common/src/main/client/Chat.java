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
	private static Thread _writer;
	private String username;

	public Chat(String username, boolean reading) {
		this.username = username;
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
		new Thread(new Chat(username, true)).start();
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
			if ("stop".equals(line)) {
				exit();
				stop = true;
			} else {
				writeChat(line);
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
				data = localChatSpace.get(new FormalField(String.class), new FormalField(Boolean.class), new FormalField(String.class));
				String msg = data[0].toString();
				boolean hostMsg = (boolean)data[1];
				String user = data[2].toString();

				if (hostMsg) {
					switch (msg) {
						case "exit":
							_writer.stop();
						case "disconnected":
							stop = true;
							break;
					}
				} else {
					System.out.println(user + ": " + msg);
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
		try {
			remoteChatSpace.put("msg", msg);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void setWriter(Thread writer) {
		_writer = writer;
	}
}

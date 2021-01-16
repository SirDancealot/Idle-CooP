package common.src.main.client;

import common.src.main.Data.PlayerState;
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
			remoteChatSpace.put("joined", PropManager.getProperty("externalIP"), username);
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
					try {
						remoteChatSpace.put("disconnect", PropManager.getProperty("externalIP"), username);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					stop = true;
					break;
				case "ser":
					try {
						remoteChatSpace.put(new PlayerState());
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					break;
				case "d":
				case "debug":
					String debug = PropManager.getProperty("debug");
					if (debug != null) {
						if (debug.equals("true"))
							PropManager.setProperty("debug", "false");
						else
							PropManager.setProperty("debug", "true");
					} else
						PropManager.setProperty("debug", "true");
					break;
				default:
					writeChat(line);
			}
		}
	}

	private void initReader() {
		localChatSpace = new SequentialSpace();
		SpaceManager.exposePublicSpace(localChatSpace, "localChat");
		SpaceManager.addClientExitEvent(() -> {
			try {
				localChatSpace.put("stop", true, "CLIENT");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
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
						case "stop":
							try {
								SpaceManager.getHostSpace("chat").put("disconnect", "", username);
							} catch (IOException e) {
								e.printStackTrace();
							}
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


	private void writeChat(String msg) {
		try {
			remoteChatSpace.put("msg", msg, username);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void setWriter(Thread writer) {
		_writer = writer;
	}
}

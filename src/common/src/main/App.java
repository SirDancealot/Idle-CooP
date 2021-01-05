package common.src.main;

import org.jspace.*;

import java.io.IOException;


public class App {

	public static void main(String[] argv) throws InterruptedException, IOException {
		Space inbox = new SequentialSpace();
		SpaceRepository inboxR = new SpaceRepository();
		inboxR.add("inbox", inbox);
		inboxR.addGate("tcp://192.168.1.62:33333/?keep");

		Object[] tup = inboxR.get("inbox").get(new FormalField(String.class));
		System.out.println(tup[0]);

		/*
		RemoteSpace outbox = new RemoteSpace("tcp://188.178.206.128:33333/inbox?keep");
		outbox.put("goodbye");
		 */

	}

}

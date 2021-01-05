package common.src.main;

import org.jspace.*;

import java.io.IOException;


public class App {

	public static void main(String[] argv) throws InterruptedException, IOException {
		new Thread(new Host("192.168.1.62", 33333, new String[] { "inbox" })).start();
		new Thread(new Client("188.178.206.128", 33333, "inbox")).start();
	}

}

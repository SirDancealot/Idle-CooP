package common.src.main;

import common.src.util.PropReader;
import org.jspace.*;

import java.io.IOException;


public class App {

	public static void main(String[] argv) throws InterruptedException, IOException {
		PropReader.init();

		new Thread(new Host(PropReader.getProperty("internalIP"), PropReader.getProperty("internalPort"), new String[] { "inbox" })).start();
		new Thread(new Client(PropReader.getProperty("hostIP"), PropReader.getProperty("hostPort"), "inbox")).start();
	}

}

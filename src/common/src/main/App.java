package common.src.main;

import org.jspace.*;

import javax.swing.*;
import java.io.IOException;


public class App {

	public static void main(String[] argv) throws InterruptedException, IOException {
		new Thread(new Host("192.168.0.185", 33333, new String[] { "inbox" })).start();
		new Thread(new Client("80.210.68.189", 33333, "inbox")).start();

		//SwingUtilities.invokeLater();

	}
}

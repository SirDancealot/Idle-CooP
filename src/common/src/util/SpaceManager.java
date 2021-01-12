package common.src.util;

import org.jspace.RemoteSpace;
import org.jspace.Space;
import org.jspace.SpaceRepository;

import java.io.IOException;

public class SpaceManager {
	private static boolean init = false;
	private static boolean host;
	private static SpaceManager INSTANCE;
	private static SpaceRepository publicSpaceRepo;
	private static SpaceRepository localSpaceRepo;
	private static SpaceRepository hostSpaceRepo;

	private SpaceManager() {
		INSTANCE = this;
		host = PropManager.getProperty("host") != null;

		String IP = PropManager.getProperty("localIP");
		String port = PropManager.getProperty("localPort");
		publicSpaceRepo = new SpaceRepository();
		publicSpaceRepo.addGate("tcp://" + IP + ":" + port + "/?keep");

		localSpaceRepo = new SpaceRepository();
		localSpaceRepo.addGate("tcp://localhost:33335/?keep");

		if (host) {
			String hostPort = PropManager.getProperty("hostPort");
			hostSpaceRepo = new SpaceRepository();
			hostSpaceRepo.addGate("tcp://" + IP + ":" + hostPort + "/?keep");
		}
	}

	public static void addLocalSpace(Space space, String name) {
		localSpaceRepo.add(name, space);
	}

	public static void exposePublicSpace(Space space, String name) {
		publicSpaceRepo.add(name, space);
	}

	public static void exposeHostSpace(Space space, String name) {
		if (!host)
			return;
		hostSpaceRepo.add(name, space);
	}

	public static Space getLocalSpace(String sName) throws IOException {
		return new RemoteSpace("tcp://localhost:33335/" + sName + "?keep");
	}

	public static Space getHostSpace(String name) throws IOException {
		String rsName;
		String ip = ((PropManager.getProperty("host") != null ? PropManager.getProperty("localIP") : PropManager.getProperty("hostIP")));
		rsName = "tcp://" + ip + ":" + PropManager.getProperty("hostPort") + "/" + name + "?keep";
		return new RemoteSpace(rsName);
	}

	public static Space getRemoteSpace(String ip, String port, String name) throws IOException {
		String rsProps;
		if(ip.equals(PropManager.getProperty("externalIP"))){
			ip = PropManager.getProperty("localIP");
		}
		rsProps = "tcp://" + ip + ":" + port + "/" + name + "?keep";
		return new RemoteSpace(rsProps);
	}

	public SpaceManager getINSTANCE() {
		init();
		return INSTANCE;
	}

	public static void init() {
		if (!init) {
			init = true;
			new SpaceManager();
		}
	}
}

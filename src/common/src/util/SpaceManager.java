package common.src.util;

import org.jspace.Space;
import org.jspace.SpaceRepository;

public class SpaceManager {
	private static boolean init = false;
	private static SpaceManager INSTANCE;
	private static SpaceRepository publicSpaceRepo;
	private static SpaceRepository localSpaceRepo;

	private SpaceManager() {
		INSTANCE = this;

		String IP = PropManager.getProperty("internalIP");
		String port = PropManager.getProperty("internalPort");
		publicSpaceRepo = new SpaceRepository();
		publicSpaceRepo.addGate("tcp://" + IP + ":" + port + "/?keep");

		localSpaceRepo = new SpaceRepository();
		localSpaceRepo.addGate("tcp://localhost:33334/?keep");
	}

	public Space getLocalSpace(String sName) {
		return localSpaceRepo.get(sName);
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

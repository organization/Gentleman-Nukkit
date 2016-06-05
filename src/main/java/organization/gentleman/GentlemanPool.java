package organization.gentleman;

import java.util.ArrayList;

import cn.nukkit.utils.Config;

@SuppressWarnings("rawtypes")
public class GentlemanPool {
	private static ArrayList<ArrayList> badQueue = new ArrayList<>();
	private static Config dictionary;

	public static void putBadQueue(ArrayList<ArrayList> mem) {
		badQueue = mem;
	}

	public static void putDictionary(Config mem) {
		dictionary = mem;
	}

	public static ArrayList<ArrayList> getBadQueue() {
		return badQueue;
	}

	public static Config getDictionary() {
		return dictionary;
	}
}

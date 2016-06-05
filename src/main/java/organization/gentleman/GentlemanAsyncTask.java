package organization.gentleman;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import cn.nukkit.Server;
import cn.nukkit.scheduler.AsyncTask;

public class GentlemanAsyncTask extends AsyncTask {
	public static final int COMMAND = 0;
	public static final int SIGN = 1;
	public static final int NAME = 2;
	public static final int CHAT = 3;
	public static final int FULL_CHAT = 4;

	private int eventType;
	private boolean needDictionaryCheck = false;

	private String name = null;
	private String format = null;
	private String message = null;
	private String find = null;
	private String blockData = null;

	public GentlemanAsyncTask(int eventType) {
		this.eventType = eventType;
	}

	public GentlemanAsyncTask(int eventType, boolean needDictionaryCheck) {
		this.eventType = eventType;
		this.needDictionaryCheck = needDictionaryCheck;
	}

	public GentlemanAsyncTask setName(String name) {
		this.name = name;
		return this;
	}

	public GentlemanAsyncTask setFormat(String format) {
		this.format = format;
		return this;
	}

	public GentlemanAsyncTask setMessage(String message) {
		this.message = message;
		return this;
	}

	public GentlemanAsyncTask setBlockData(int x, int y, int z, int id, int dmg) {
		this.blockData = x + ":" + y + ":" + z + ":" + id + ":" + dmg;
		return this;
	}

	@Override
	public void onRun() {
		if (eventType == FULL_CHAT) {
			String[] cut = this.message.split(">");
			String chat = "";

			for (int ch = 1; ch < cut.length; ch++)
				chat += cut[ch];

			if (chat.isEmpty()) {
				this.find = this.checkBadWord(this.message);
			} else {
				this.find = this.checkBadWord(chat);
			}
		} else {
			this.find = this.checkBadWord(this.message);
		}
	}

	public ArrayList<String> cutWords(String str) {
		ArrayList<String> list = new ArrayList<String>();
		for (int ch = 0; ch < str.length(); ch++)
			list.add(String.valueOf(str.charAt(ch)));
		return list;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String checkBadWord(String word) {
		if (needDictionaryCheck)
			word = this.removeDictionaryText(word);

		ArrayList<String> words = this.cutWords(word);
		ArrayList<ArrayList> badQueue = GentlemanPool.getBadQueue();

		for (ArrayList<String> queue : badQueue) {
			LinkedHashMap<String, Boolean> findCount = new LinkedHashMap<String, Boolean>();

			for (String match_alpha : queue) {

				for (int ch = 0; ch < words.size(); ch++) {
					String used_alpha = String.valueOf(words.get(ch));

					match_alpha = match_alpha.toLowerCase();
					used_alpha = used_alpha.toLowerCase();

					if (match_alpha.equals(used_alpha)) {
						findCount.put(match_alpha, true);
						break;
					}
				}
				if (queue.size() == findCount.size()) {
					String badWord = "";
					for (int ch = 0; ch < queue.size(); ch++)
						badWord += queue.get(ch);
					return badWord;
				}
			}
		}
		return null;
	}

	public String removeDictionaryText(String text) {
		for (Entry<String, Object> entry : GentlemanPool.getDictionary().getAll().entrySet())
			text.replaceAll(entry.getKey(), "");
		return text;
	}

	@Override
	public void onCompletion(Server server) {
		EventListener.getInstance().callback(eventType, name, find, message, format, blockData);
	}
}

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

	private String name = "";
	private String format = "";
	private String message = "";
	private String find = "";
	private String posData = "";

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

	public GentlemanAsyncTask setPosData(int x, int y, int z, int id, int dmg) {
		this.posData = x + ":" + y + ":" + z + ":" + id + ":" + dmg;
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

		final int length = str.length();
		for (int offset = 0; offset < length;) {
			final int codepoint = str.codePointAt(offset);
			list.add(String.valueOf((char) codepoint));
			offset += Character.charCount(codepoint);
		}
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
					String used_alpha = words.get(ch);

					match_alpha = match_alpha.toLowerCase();
					used_alpha = used_alpha.toLowerCase();

					if (match_alpha.equals(used_alpha)){
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
		return "";
	}

	public String removeDictionaryText(String text) {
		for (Entry<String, Object> entry : GentlemanPool.getDictionary().getAll().entrySet())
			text.replaceAll(entry.getKey(), "");
		return text;
	}

	@Override
	public void onCompletion(Server server) {
		EventListener.getInstance().callback(eventType, name, find, message, format, posData);
	}
}

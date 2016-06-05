package organization.gentleman;

import java.util.ArrayList;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.utils.Config;

@SuppressWarnings("rawtypes")
public class GentlemanPlugin extends PluginBase {
	private static GentlemanPlugin instance;
	private Config messages, badwords, dictionary;
	private ArrayList<ArrayList> badQueue = new ArrayList<>();
	private ArrayList<String> needCheckCommand = new ArrayList<>();

	@Override
	public void onEnable() {
		this.getDataFolder().mkdirs();
		this.initMessage();
		this.poolRegister();

		instance = this;
		this.getServer().getPluginManager().registerEvents(new EventListener(this), this);
	}

	public void initMessage() {
		this.saveResource("messages.yml", false);
		this.saveResource("dictionary.yml", false);
		this.saveResource("badwords.json", false);

		this.messages = new Config(this.getDataFolder().getAbsolutePath() + "messages.yml", Config.YAML);
		this.dictionary = new Config(this.getDataFolder().getAbsolutePath() + "dictionary.yml", Config.YAML);
		this.badwords = new Config(this.getDataFolder().getAbsolutePath() + "badwords.yml", Config.JSON);

		this.makeQueue();
		this.needToCheckCommand("tell");
		this.needToCheckCommand("me");
	}

	@SuppressWarnings("unchecked")
	public void makeQueue() {
		for (String badword : ((ArrayList<String>) this.badwords.get("badwords")))
			badQueue.add(this.cutWords(badword));
	}

	public ArrayList<String> cutWords(String str) {
		ArrayList<String> list = new ArrayList<String>();
		for (int ch = 0; ch < str.length(); ch++)
			list.add(String.valueOf(str.charAt(ch)));
		return list;
	}

	public void poolRegister() {
		AsyncTask task = new AsyncTask() {
			private ArrayList<ArrayList> badQueue;
			private Config dictionary;

			public AsyncTask setData(ArrayList<ArrayList> badQueue, Config dictionary) {
				this.badQueue = badQueue;
				this.dictionary = dictionary;
				return this;
			}

			@Override
			public void onRun() {
				GentlemanPool.putBadQueue(this.badQueue);
				GentlemanPool.putDictionary(this.dictionary);
			}
		}.setData(this.badQueue, this.dictionary);

		int size = this.getServer().getScheduler().getAsyncTaskPoolSize();
		for (int i = 0; i < size; ++i)
			this.getServer().getScheduler().scheduleAsyncTask(task);
	}

	public String get(String key) {
		return String.valueOf(this.messages.get(String.valueOf(this.messages.get("default-language")) + "-" + key));
	}

	/**
	 * 특정명령어 사용시 비속어 체크가 필요하다면<br>
	 * 이 함수를 사용해서 Gentleman 에 추가
	 * 
	 * @param commandName
	 */
	public void needToCheckCommand(String commandName) {
		this.needCheckCommand.add(commandName);
	}

	/**
	 * 비속어 모니터링 중인 명령어 목록을 반환
	 * 
	 * @return ArrayList<String>
	 */
	public ArrayList<String> getNeedToCheckCommand() {
		return this.needCheckCommand;
	}

	/**
	 * 해당 명령어가 비속어 모니터링 중인지 여부를 반환
	 * 
	 * @param commandName
	 * @return
	 */
	public boolean isMonitor(String commandName) {
		return this.needCheckCommand.contains(commandName);
	}

	public static GentlemanPlugin getInstance() {
		return instance;
	}
}

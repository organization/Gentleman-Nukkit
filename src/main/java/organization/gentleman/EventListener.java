package organization.gentleman;

import java.util.LinkedHashMap;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.SignChangeEvent;
import cn.nukkit.event.player.PlayerCommandPreprocessEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerKickEvent;

public class EventListener implements Listener {
	private static EventListener instance;
	private GentlemanPlugin plugin;

	private LinkedHashMap<String, Player> playerTemp = new LinkedHashMap<String, Player>();
	private LinkedHashMap<String, Boolean> nameCheck = new LinkedHashMap<String, Boolean>();
	private LinkedHashMap<String, Boolean> signCheck = new LinkedHashMap<String, Boolean>();
	private LinkedHashMap<String, Boolean> chatCheck = new LinkedHashMap<String, Boolean>();
	private LinkedHashMap<String, Boolean> commandCheck = new LinkedHashMap<String, Boolean>();

	public EventListener(GentlemanPlugin plugin) {
		instance = this;
		this.plugin = plugin;
	}

	public static EventListener getInstance() {
		return instance;
	}

	@EventHandler
	public void onSignChangeEvent(SignChangeEvent event) {
		if (event.getPlayer().isOp())
			return;

		String message = "";
		for (String line : event.getLines())
			message += line + "\n";

		if (!this.signCheck.containsKey(event.getPlayer().getName() + ">" + message)) {
			this.signCheck.put(event.getPlayer().getName() + ">" + message, false);

			this.plugin.getServer()
					.getScheduler().scheduleAsyncTask(new GentlemanAsyncTask(GentlemanAsyncTask.SIGN, true)
							.setName(event.getPlayer().getName()).setBlockData(((Double) event.getBlock().x).intValue(),
									((Double) event.getBlock().y).intValue(), ((Double) event.getBlock().z).intValue(),
									event.getBlock().getId(), event.getBlock().getDamage())
							.setMessage(message));

			event.setCancelled();
		} else {
			if (!this.signCheck.get(event.getPlayer().getName() + ">" + message)) {
				event.setCancelled();
			} else {
				this.signCheck.remove(event.getPlayer().getName() + ">" + message);
			}
		}
	}

	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
		if (event.getPlayer().isOp())
			return;
		this.playerTemp.put(event.getPlayer().getName(), event.getPlayer());

		if (!this.nameCheck.containsKey(event.getPlayer().getName())) {
			this.nameCheck.put(event.getPlayer().getName(), true);

			this.plugin.getServer().getScheduler()
					.scheduleAsyncTask(new GentlemanAsyncTask(GentlemanAsyncTask.NAME, true)
							.setName(event.getPlayer().getName()).setMessage(event.getJoinMessage().toString()));

			event.setJoinMessage("");
		} else {
			if (!this.nameCheck.get(event.getPlayer().getName()))
				event.setCancelled();
		}
	}

	@EventHandler
	public void onPlayerKickEvent(PlayerKickEvent event) {
		if (event.getReason() == this.plugin.get("bad-words-nickname"))
			event.setQuitMessage("");
	}

	@EventHandler
	public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
		if (!this.playerTemp.containsKey(event.getPlayer().getName()))
			this.playerTemp.put(event.getPlayer().getName(), event.getPlayer());

		if (event.getMessage().charAt(0) != '/') {
			if (!this.chatCheck.containsKey(event.getPlayer().getName() + ">" + event.getMessage())) {
				this.chatCheck.put(event.getPlayer().getName() + ">" + event.getMessage(), false);

				this.plugin.getServer().getScheduler()
						.scheduleAsyncTask(new GentlemanAsyncTask(GentlemanAsyncTask.FULL_CHAT, true)
								.setName(event.getPlayer().getName()).setMessage(event.getMessage())
								.setFormat("chat.type.text"));

				event.setCancelled();
			} else {
				if (!this.chatCheck.get(event.getPlayer().getName() + ">" + event.getMessage())) {
					event.setCancelled();
				} else {
					this.chatCheck.remove(event.getPlayer().getName() + ">" + event.getMessage());
				}
			}
			return;
		}

		String[] commands = event.getMessage().split(" ");
		if (commands.length == 0)
			return;

		switch (commands[0]) {
		case "tell":
		case "me":
			break;
		default:
			return;
		}

		if (!this.commandCheck.containsKey(event.getPlayer().getName() + ">" + event.getMessage())) {
			this.commandCheck.put(event.getPlayer().getName() + ">" + event.getMessage(), false);

			this.plugin.getServer().getScheduler().scheduleAsyncTask(
					new GentlemanAsyncTask(GentlemanAsyncTask.COMMAND).setName(event.getPlayer().getName()));

			event.setCancelled();
		} else {
			if (!this.commandCheck.get(event.getPlayer().getName() + ">" + event.getMessage())) {
				event.setCancelled();
			} else {
				this.commandCheck.remove(event.getPlayer().getName() + ">" + event.getMessage());
			}
		}
	}

	public void callback(int eventType, String name, String find, String message, String format, String blockData) {
		if(!this.playerTemp.containsKey(name))
			return;
		
		Player player = this.playerTemp.get(name);
		
		if(player instanceof Player || player.closed)
			return;
		
		switch(eventType){
		case GentlemanAsyncTask.CHAT:
			break;
		case GentlemanAsyncTask.COMMAND:
			break;
		case GentlemanAsyncTask.FULL_CHAT:
			break;
		case GentlemanAsyncTask.NAME:
			break;
		case GentlemanAsyncTask.SIGN:
			break;
		}
	}
}
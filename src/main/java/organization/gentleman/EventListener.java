package organization.gentleman;

import java.util.LinkedHashMap;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.TranslationContainer;
import cn.nukkit.event.block.SignChangeEvent;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.event.player.PlayerCommandPreprocessEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerKickEvent;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.TextPacket;
import cn.nukkit.scheduler.Task;
import cn.nukkit.utils.TextFormat;

public class EventListener implements Listener {
	private static EventListener instance;
	private GentlemanPlugin plugin;
	private boolean localChat = false;
	private int chattyCount = 0;

	private LinkedHashMap<String, Player> playerTemp = new LinkedHashMap<String, Player>();
	private LinkedHashMap<String, Boolean> nameCheck = new LinkedHashMap<String, Boolean>();
	private LinkedHashMap<String, Boolean> signCheck = new LinkedHashMap<String, Boolean>();
	private LinkedHashMap<String, Boolean> chatCheck = new LinkedHashMap<String, Boolean>();
	private LinkedHashMap<String, Boolean> commandCheck = new LinkedHashMap<String, Boolean>();

	public EventListener(GentlemanPlugin plugin) {
		instance = this;
		this.plugin = plugin;

		this.plugin.getServer().getScheduler().scheduleDelayedRepeatingTask(new Task() {
			@Override
			public void onRun(int currentTick) {
				EventListener.getInstance().chattyClear();
			}
		}, 1200, 1200);
	}

	public static EventListener getInstance() {
		return instance;
	}

	public void chattyUp() {
		if (++this.chattyCount >= 45 && !this.localChat) {
			this.localChat = true;
			this.plugin.getServer().broadcastMessage(this.plugin.get("chatty-on"));
		}
	}

	public void chattyClear() {
		if (this.chattyCount < 45 && this.localChat) {
			this.localChat = false;
			this.plugin.getServer().broadcastMessage(this.plugin.get("chatty-off"));
		}
		this.chattyCount = 0;
	}

	@EventHandler
	public void onPlayerChatEvent(PlayerChatEvent event) {
		this.plugin.getServer().getScheduler().scheduleDelayedTask(new Task() {
			PlayerChatEvent event;

			public Task setEvent(PlayerChatEvent event) {
				this.event = event;
				return this;
			}

			@Override
			public void onRun(int currentTick) {
				if (!this.event.isCancelled())
					EventListener.getInstance().chattyUp();
			}
		}.setEvent(event), 5);
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
							.setName(event.getPlayer().getName()).setPosData(((Double) event.getBlock().x).intValue(),
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
							.setName(event.getPlayer().getName()).setMessage(event.getPlayer().getName()));

			event.setJoinMessage("");
		} else {
			if (!this.nameCheck.get(event.getPlayer().getName()))
				event.setCancelled();
		}
	}

	@EventHandler
	public void onPlayerKickEvent(PlayerKickEvent event) {
		if (event.getReason().equals(this.plugin.get("badwords-nickname")))
			event.setQuitMessage("");
	}

	@EventHandler
	public void onDataPacketReceiveEvent(DataPacketReceiveEvent event) {
		if (event.getPlayer().isOp())
			return;
		
		switch (event.getPacket().pid()) {
		case ProtocolInfo.TEXT_PACKET:
			TextPacket textPacket = (TextPacket) event.getPacket();
			if (textPacket.type != TextPacket.TYPE_CHAT)
				return;

			for (String msg : textPacket.message.split("\n")) {
				if ("".equals(msg.trim()) && msg.length() <= 255)
					continue;
				if (msg.startsWith("/"))
					continue;

				if (!this.chatCheck.containsKey(event.getPlayer().getName() + ">" + msg)) {
					this.chatCheck.put(event.getPlayer().getName() + ">" + msg, false);

					this.plugin.getServer().getScheduler()
							.scheduleAsyncTask(new GentlemanAsyncTask(GentlemanAsyncTask.FULL_CHAT, true)
									.setName(event.getPlayer().getName()).setMessage(msg).setFormat("chat.type.text")
									.setPosData(event.getPlayer().getFloorX(), event.getPlayer().getFloorY(),
											event.getPlayer().getFloorZ(), 0, 0));

					event.setCancelled();
				} else {
					if (!this.chatCheck.get(event.getPlayer().getName() + ">" + msg)) {
						event.setCancelled();
					} else {
						this.chatCheck.remove(event.getPlayer().getName() + ">" + msg);
					}
				}
				return;
			}
			break;
		}
	}

	@EventHandler
	public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
		if (event.getPlayer().isOp())
			return;
		
		if (!this.playerTemp.containsKey(event.getPlayer().getName()))
			this.playerTemp.put(event.getPlayer().getName(), event.getPlayer());
		String[] commands = event.getMessage().split(" ");
		if (commands.length == 0)
			return;

		if (!GentlemanPlugin.getInstance().isMonitor(commands[0]))
			return;

		if (!this.commandCheck.containsKey(event.getPlayer().getName() + ">" + event.getMessage())) {
			this.commandCheck.put(event.getPlayer().getName() + ">" + event.getMessage(), false);

			this.plugin.getServer().getScheduler().scheduleAsyncTask(new GentlemanAsyncTask(GentlemanAsyncTask.COMMAND)
					.setName(event.getPlayer().getName()).setMessage(event.getMessage()));

			event.setCancelled();
		} else {
			if (!this.commandCheck.get(event.getPlayer().getName() + ">" + event.getMessage())) {
				event.setCancelled();
			} else {
				this.commandCheck.remove(event.getPlayer().getName() + ">" + event.getMessage());
			}
		}
	}

	public void cautionAlram(Player player, String word) {
		this.cautionAlram(player, word, player.getFloorX(), player.getFloorY(), player.getFloorZ());
	}

	public void cautionAlram(Player player, String word, int x, int y, int z) {
		this.plugin.getServer().getLogger()
				.alert(this.plugin.get("some-badwords-found") + " [" + x + ":" + y + ":" + z + "]");
		this.plugin.getServer().getLogger().alert(player.getName() + "> " + word);

		player.sendMessage(TextFormat.RED + this.plugin.get("some-badwords-found"));
		player.sendMessage(TextFormat.RED + player.getName() + "> " + word);

		for (Player online : this.plugin.getServer().getOnlinePlayers().values()) {
			if (!online.isOp())
				continue;

			online.sendMessage(
					TextFormat.RED + this.plugin.get("some-badwords-found") + " [" + x + ":" + y + ":" + z + "]");
			online.sendMessage(TextFormat.RED + player.getName() + "> " + word);
		}
	}

	public void callback(int eventType, String name, String find, String message, String format, String posData) {
		if (!this.playerTemp.containsKey(name))
			return;

		Player player = this.playerTemp.get(name);
		if (!(player instanceof Player) || player.closed)
			return;

		switch (eventType) {
		case GentlemanAsyncTask.FULL_CHAT:
		case GentlemanAsyncTask.CHAT:
			if (!find.isEmpty()) {
				this.cautionAlram(player, find);
				this.chatCheck.remove(name + ">" + message);
				return;
			}

			if (!this.chatCheck.containsKey(name + ">" + message))
				return;
			this.chatCheck.put(name + ">" + message, true);

			PlayerChatEvent chatEvent = new PlayerChatEvent(player, message, format, null);
			this.plugin.getServer().getPluginManager().callEvent(chatEvent);

			if (!chatEvent.isCancelled()) {
				for (Player online : this.plugin.getServer().getOnlinePlayers().values()) {
					if (this.localChat) {
						String[] pos = posData.split(":");
						Vector3 senderVec = new Vector3(Double.valueOf(pos[0]), Double.valueOf(pos[1]),
								Double.valueOf(pos[2]));

						if (online.distance(senderVec) >= 40)
							continue;
					}
					online.sendMessage(this.plugin.getServer().getLanguage().translateString(chatEvent.getFormat(),
							new String[] { chatEvent.getPlayer().getDisplayName(), chatEvent.getMessage() }));
				}
			}
			break;
		case GentlemanAsyncTask.COMMAND:
			if (!find.isEmpty()) {
				this.cautionAlram(player, find);
				this.commandCheck.remove(player.getName() + ">" + message);
				return;
			}

			if (!this.commandCheck.containsKey(name + ">" + message))
				return;

			this.commandCheck.put(name + ">" + message, true);

			PlayerCommandPreprocessEvent commandEvent = new PlayerCommandPreprocessEvent(player, message);
			this.plugin.getServer().getPluginManager().callEvent(commandEvent);

			if (!commandEvent.isCancelled())
				this.plugin.getServer().dispatchCommand(commandEvent.getPlayer(),
						commandEvent.getMessage().substring(1));
			break;
		case GentlemanAsyncTask.NAME:
			if (!find.isEmpty()) {
				player.kick(this.plugin.get("badwords-nickname"), false);
				this.nameCheck.remove(player.getName());
				return;
			}
			this.nameCheck.remove(player.getName());
			this.plugin.getServer().broadcastMessage(new TranslationContainer(
					TextFormat.YELLOW + "%multiplayer.player.joined", new String[] { player.getName() }));
			break;
		case GentlemanAsyncTask.SIGN:
			if (!find.isEmpty()) {
				String[] lines = message.split("\\n");
				String signText = "";

				for (int index = 0; index < lines.length; index++)
					signText += lines[index] + " ";

				String[] pos = posData.split(":");
				this.cautionAlram(player, find + "(" + signText + ")", Integer.valueOf(pos[0]), Integer.valueOf(pos[1]),
						Integer.valueOf(pos[2]));
				return;
			}

			if (!this.signCheck.containsKey(name + ">" + message))
				return;

			this.signCheck.put(name + ">" + message, true);

			String[] pos = posData.split(":");
			Block block = Block.get(Integer.valueOf(pos[3]), Integer.valueOf(pos[4]),
					new Position(Double.valueOf(pos[0]), Double.valueOf(pos[1]), Double.valueOf(pos[2]),
							this.plugin.getServer().getDefaultLevel()));

			String[] splitLines = message.split("\\n");
			String[] lines = new String[4];

			for (int index = 0; index <= 3; index++) {
				if (splitLines.length - 1 < index) {
					lines[index] = "";
					continue;
				}
				lines[index] = splitLines[index];
			}

			SignChangeEvent signEvent = new SignChangeEvent(block, player, lines);
			this.plugin.getServer().getPluginManager().callEvent(signEvent);

			BlockEntity tile = block.getLevel().getBlockEntity(block);
			if (!(tile instanceof BlockEntitySign) || tile.equals(null))
				return;

			if (!signEvent.isCancelled())
				((BlockEntitySign) tile).setText(signEvent.getLine(0), signEvent.getLine(1), signEvent.getLine(2),
						signEvent.getLine(3));
			break;
		}
	}
}
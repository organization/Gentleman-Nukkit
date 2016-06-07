package organization.idea;

import java.util.LinkedHashMap;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.level.ChunkPopulateEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.level.Level;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.format.generic.BaseFullChunk;
import cn.nukkit.utils.TextFormat;

public class EventListener implements Listener {
	private IDEAPlugin plugin;
	public LinkedHashMap<String, Boolean> synchroQueue = new LinkedHashMap<String, Boolean>();
	public LinkedHashMap<String, Boolean> shiftQueue = new LinkedHashMap<String, Boolean>();
	public LinkedHashMap<String, String> chunkQueue = new LinkedHashMap<String, String>();

	public EventListener(IDEAPlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerInteractEvent(PlayerInteractEvent event) {
		if (this.synchroQueue.containsKey(event.getPlayer().getName())) {
			this.synchroQueue.remove(event.getPlayer().getName());
			event.setCancelled();

			Player player = event.getPlayer();
			Level level = player.getLevel();

			Level ideaLevel = this.getServer().getLevelByName(level.getFolderName() + "_IDEA");
			if (!(ideaLevel instanceof Level)) {
				player.sendMessage(TextFormat.RED + this.get("idea-doesnt-exist"));
				return;
			}
			
			if (ideaLevel.populateChunk((int) event.getBlock().x >> 4, (int) event.getBlock().z >> 4)) {
				System.out.println("CHUNK-1:" + "" + ideaLevel.getId() + ":" + ((int) event.getBlock().x >> 4) + ":"
						+ ((int) event.getBlock().z >> 4));
				this.chunkQueue.put("" + ideaLevel.getId() + ":" + ((int) event.getBlock().x >> 4) + ":"
						+ ((int) event.getBlock().z >> 4), "" + level.getId() + ":" + player.getName());
				return;
			}

			BaseFullChunk originChunk = ideaLevel.getChunk((int) event.getBlock().x >> 4, (int) event.getBlock().z >> 4,
					true);
			if (!(originChunk instanceof FullChunk)) {
				player.sendMessage(TextFormat.RED + this.get("idea-is-breakdown"));
				return;
			}

			BaseFullChunk clonedChunk = originChunk.clone();
			clonedChunk.setX(originChunk.getX());
			clonedChunk.setZ(originChunk.getZ());

			level.setChunk(originChunk.getX(), originChunk.getZ(), clonedChunk, false);

			player.sendMessage(TextFormat.DARK_AQUA + this.get("idea-instance-synchro-100"));
		} else if (this.shiftQueue.containsKey(event.getPlayer().getName())) {
			this.shiftQueue.remove(event.getPlayer().getName());
			event.setCancelled();

			Player player = event.getPlayer();
			Level level = player.getLevel();

			Level ideaLevel = this.getServer().getLevelByName(level.getFolderName() + "_IDEA");
			if (!(ideaLevel instanceof Level)) {
				player.sendMessage(TextFormat.RED + this.get("idea-doesnt-exist"));
				return;
			}

			if (level.populateChunk((int) event.getBlock().x >> 4, (int) event.getBlock().z >> 4)) {
				System.out.println("CHUNK-2");
				this.chunkQueue.put("" + level.getId() + ":" + ((int) event.getBlock().x >> 4) + ":"
						+ ((int) event.getBlock().z >> 4), "" + ideaLevel.getId() + ":" + player.getName());
				return;
			}

			BaseFullChunk originChunk = level.getChunk((int) event.getBlock().x >> 4, (int) event.getBlock().z >> 4,
					true);
			if (!(originChunk instanceof FullChunk)) {
				player.sendMessage(TextFormat.RED + this.get("idea-is-breakdown"));
				return;
			}

			BaseFullChunk clonedChunk = originChunk.clone();
			clonedChunk.setX(originChunk.getX());
			clonedChunk.setZ(originChunk.getZ());

			ideaLevel.setChunk(originChunk.getX(), originChunk.getZ(), clonedChunk, false);

			player.sendMessage(TextFormat.DARK_AQUA + this.get("idea-instance-synchro-100"));
		}
	}

	@EventHandler
	public void onChunkPopulateEvent(ChunkPopulateEvent event) {
		System.out.println(
				"POP:" + event.getLevel().getId() + ":" + event.getChunk().getX() + ":" + event.getChunk().getZ());
		if (!this.chunkQueue.containsKey(
				"" + event.getLevel().getId() + ":" + event.getChunk().getX() + ":" + event.getChunk().getZ()))
			return;

		System.out.println("C1");
		String[] data = this.chunkQueue
				.get("" + event.getLevel().getId() + ":" + event.getChunk().getX() + ":" + event.getChunk().getZ())
				.split(":");

		this.chunkQueue
				.remove("" + event.getLevel().getId() + ":" + event.getChunk().getX() + ":" + event.getChunk().getZ());

		if (data.length != 2)
			return;

		System.out.println("C2");
		Level targetLevel = this.getServer().getLevel(Integer.valueOf(data[0]));
		if (!(targetLevel instanceof Level))
			return;

		System.out.println("C3");
		Player player = this.getServer().getPlayer(data[1]);
		if (!(player instanceof Player))
			return;

		BaseFullChunk clonedChunk = ((BaseFullChunk) event.getChunk()).clone();
		clonedChunk.setX(event.getChunk().getX());
		clonedChunk.setZ(event.getChunk().getZ());

		targetLevel.setChunk(event.getChunk().getX(), event.getChunk().getZ(), clonedChunk, false);
		player.sendMessage(TextFormat.DARK_AQUA + this.get("idea-instance-synchro-100"));

		System.out.println("C4");
	}

	public Server getServer() {
		return this.plugin.getServer();
	}

	public String get(String str) {
		return this.plugin.get(str);
	}
}

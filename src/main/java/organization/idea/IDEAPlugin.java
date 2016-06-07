package organization.idea;

import java.io.File;
import java.util.LinkedHashMap;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.PluginCommand;
import cn.nukkit.command.SimpleCommandMap;
import cn.nukkit.level.Level;
import cn.nukkit.permission.Permission;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;

public class IDEAPlugin extends PluginBase {
	private Config messages;
	private EventListener listener;

	@Override
	public void onEnable() {
		this.getDataFolder().mkdirs();

		this.initMessage();
		this.loadIDEALevels();

		this.registerPermission("idea.synchro", true, this.get("idea-permission-desc"));
		this.registerCommand(this.get("idea"), this.get("idea-command-desc"), "idea.synchro", "/" + this.get("idea"));

		this.listener = new EventListener(this);
		this.getServer().getPluginManager().registerEvents(this.listener, this);
	}

	public void loadIDEALevels() {
		for (Level level : this.getServer().getLevels().values()) {

			if (level.getFolderName().split("_IDEA").length >= 2)
				continue;
			if (this.getServer().getLevelByName(level.getFolderName() + "_IDEA") instanceof Level)
				continue;

			File ideaFolder = new File(this.getServer().getDataPath() + "/worlds/" + level.getFolderName() + "_IDEA");

			if (!ideaFolder.exists())
				continue;

			this.getServer().loadLevel(level.getFolderName() + "_IDEA");
		}
	}

	public void initMessage() {
		this.saveResource("messages.yml", false);
		this.messages = new Config(this.getDataFolder().getAbsolutePath() + "/messages.yml", Config.YAML);
	}

	public String get(String str) {
		return String.valueOf(this.messages.get(String.valueOf(this.messages.get("default-language")) + "-" + str));
	}

	private boolean registerCommand(String commandName, String commandDescription, String permissionName,
			String commandUsage) {
		SimpleCommandMap commandMap = this.getServer().getCommandMap();
		PluginCommand<Plugin> command = new PluginCommand<>(commandName, this);
		command.setDescription(commandDescription);
		command.setPermission(permissionName);
		command.setUsage(commandUsage);
		return commandMap.register(commandName, command);
	}

	private boolean registerPermission(String permissionName, boolean isOp, String description) {
		LinkedHashMap<String, Object> data = new LinkedHashMap<String, Object>();
		data.put("description", description);

		String DEFAULT = (isOp) ? Permission.DEFAULT_OP : Permission.DEFAULT_TRUE;
		Permission permission = Permission.loadPermission(permissionName, data, DEFAULT);
		return this.getServer().getPluginManager().addPermission(permission);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(TextFormat.DARK_AQUA + this.get("help-synchro"));
			sender.sendMessage(TextFormat.DARK_AQUA + this.get("help-shift"));
			sender.sendMessage(TextFormat.DARK_AQUA + this.get("help-cancel"));
			return true;
		}

		if (args[0].equals(this.get("synchro"))) {
			if (this.listener.shiftQueue.containsKey(sender.getName()))
				this.listener.shiftQueue.remove(sender.getName());
			if (!(this.listener.synchroQueue.containsKey(sender.getName())))
				this.listener.synchroQueue.put(sender.getName(), true);

			sender.sendMessage(TextFormat.DARK_AQUA + this.get("synchro-sequence-start"));
			sender.sendMessage(TextFormat.DARK_AQUA + this.get("please-make-dicision"));
			return true;
		}

		if (args[0].equals(this.get("shift"))) {
			if (this.listener.synchroQueue.containsKey(sender.getName()))
				this.listener.synchroQueue.remove(sender.getName());
			if (!(this.listener.shiftQueue.containsKey(sender.getName())))
				this.listener.shiftQueue.put(sender.getName(), true);

			sender.sendMessage(TextFormat.DARK_AQUA + this.get("shift-sequence-start"));
			sender.sendMessage(TextFormat.DARK_AQUA + this.get("please-make-dicision"));
			return true;
		}

		if (args[0].equals(this.get("cancel"))) {
			if (this.listener.shiftQueue.containsKey(sender.getName()))
				this.listener.shiftQueue.remove(sender.getName());
			if (!(this.listener.shiftQueue.containsKey(sender.getName())))
				this.listener.shiftQueue.put(sender.getName(), true);

			sender.sendMessage(TextFormat.DARK_AQUA + this.get("all-queue-cancelled"));
			return true;
		}

		sender.sendMessage(TextFormat.DARK_AQUA + this.get("help-synchro"));
		sender.sendMessage(TextFormat.DARK_AQUA + this.get("help-shift"));
		sender.sendMessage(TextFormat.DARK_AQUA + this.get("help-cancel"));
		return true;
	}
}

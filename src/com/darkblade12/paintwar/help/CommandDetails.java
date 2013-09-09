package com.darkblade12.paintwar.help;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.darkblade12.paintwar.PaintWar;
import com.darkblade12.paintwar.message.MessageManager;

public class CommandDetails {
	private String command;
	private boolean executableAsConsole;
	private String description;
	private String permission;
	private String hidePermission;

	public CommandDetails(String command, boolean executableAsConsole, String description, String permission) {
		this.command = command;
		this.executableAsConsole = executableAsConsole;
		this.description = description;
		this.permission = permission;
		hidePermission = "";
	}

	public CommandDetails(String command, boolean executableAsConsole, String description, String permission, String hidePermission) {
		this.command = command;
		this.executableAsConsole = executableAsConsole;
		this.description = description;
		this.permission = permission;
		this.hidePermission = hidePermission;
	}

	public boolean isHidden(CommandSender s) {
		return hidePermission.length() == 0 ? !hasPermission(s) : !hasPermission(s) ? true : s.hasPermission(hidePermission);
	}

	public String getInvalidUsageString() {
		return "§cInvalid usage!\n§6/" + command;
	}

	public String getHelpPageString(String label) {
		return label.replace("<random_color>", MessageManager.randomColorCode()).replace("<command>", command).replace("<console_check>", executableAsConsole ? MessageManager.check : MessageManager.missing)
				.replace("<description>", description).replace("<permission>", permission);
	}

	public String getCommand() {
		return this.command;
	}

	public boolean isExecutableAsConsole() {
		return this.executableAsConsole;
	}

	public String getDescription() {
		return this.description;
	}

	public String getPermission() {
		return this.permission;
	}

	public boolean hasPermission(CommandSender s) {
		if (permission.contains("None"))
			return true;
		return s.hasPermission(permission) || s.hasPermission("PaintWar.*");
	}

	public String getHidePermission() {
		return this.hidePermission;
	}

	public boolean checkUsage(PaintWar plugin, CommandSender s, String[] args) {
		if (!(s instanceof Player) && !executableAsConsole) {
			s.sendMessage(plugin.message.command_no_console_executor());
			return false;
		} else if (!hasPermission(s)) {
			s.sendMessage(plugin.message.command_no_permission());
			return false;
		}
		int min = 0;
		int max = 0;
		String[] cs = command.split(" ");
		for (int i = 1; i < cs.length; i++) {
			String arg = cs[i];
			max++;
			if (!arg.startsWith("[") && !arg.endsWith("]"))
				min++;
		}
		if (args.length < min || args.length > max) {
			s.sendMessage(getInvalidUsageString());
			return false;
		}
		return true;
	}
}

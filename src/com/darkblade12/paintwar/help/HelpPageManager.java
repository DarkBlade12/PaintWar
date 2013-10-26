package com.darkblade12.paintwar.help;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.CommandSender;

import com.darkblade12.paintwar.PaintWar;
import com.darkblade12.paintwar.manager.Manager;

public class HelpPageManager extends Manager {
	public final static String MASTER_PERMISSION = "PaintWar.*";
	public final static String BONUS_PERMISSION = "PaintWar.bonus";
	private String pageHeader;
	private String pageFooter;
	private String commandDetailsLabel;
	private int commandsPerPage;
	private List<CommandDetails> commands;

	public HelpPageManager(PaintWar plugin, String pageHeader, String pageFooter, String commandDetailsLabel, int commandsPerPage) {
		super(plugin);
		this.pageHeader = pageHeader;
		this.pageFooter = pageFooter;
		this.commandDetailsLabel = commandDetailsLabel;
		this.commandsPerPage = commandsPerPage;
		initialize();
	}

	@Override
	public boolean initialize() {
		commands = Arrays.asList(new CommandDetails("pw create <name>", true, "Creates a new arena with the specified name", "PaintWar.create"), new CommandDetails("pw remove <name>", true, "Removes an existing arena",
				"PaintWar.remove"), new CommandDetails("pw select <arena>", false, "Selects an existing arena which allows you to leave out the [arena] argument for setup commands", "PaintWar.select"),
				new CommandDetails("pw wand", false, "Gives you the PaintWar region selection wand", "PaintWar.wand"), new CommandDetails("pw set <floor/protection> [arena]", false,
						"Determines the selected region as floor/protection region of an arena", "PaintWar.set"), new CommandDetails("pw spawn <add/del> <name> [arena]", false,
						"Adds/Deletes a spawn to/from an arena", "PaintWar.spawn"), new CommandDetails("pw check [arena]", true, "Shows the setup checklist for an arena", "PaintWar.check"), new CommandDetails(
						"pw edit [arena]", true, "Toggles the edit mode of an arena", "PaintWar.edit"), new CommandDetails("pw arenas", true, "Shows a list of all created arenas", "PaintWar.arenas"),
				new CommandDetails("pw players <arena>", true, "Shows a list of players who are in the specified arena", "PaintWar.players"), new CommandDetails("pw join <arena>", false,
						"Let's you join a valid arena", "PaintWar.join"), new CommandDetails("pw leave", false, "Let's you leave the current arena", "PaintWar.leave"), new CommandDetails("pw start <arena>",
						true, "Starts the game in a valid arena manually", "PaintWar.start"), new CommandDetails("pw stop <arena>", true, "Stops a game in an arena manually", "PaintWar.stop"), new CommandDetails("pw kick <player>",
						true, "Kicks a player out of his joined arena", "PaintWar.kick"), new CommandDetails("pw stats [player]", true, "Shows the statistics of yourself or a specified player",
						"PaintWar.stats"), new CommandDetails("pw top <won/lost/wl>", true, "Shows the top ten list for a specified category", "PaintWar.top"), new CommandDetails("pw signs", true,
						"Shows a list of all arena signs", "PaintWar.signs"), new CommandDetails("pw tp <id>", false, "Teleports you to a specified arena sign", "PaintWar.tp"), new CommandDetails(
						"pw reload [config]", true, "Reloads the whole plugin/config", "PaintWar.reload"), new CommandDetails("pw help [page]", true, "Shows the first/a specified help page", "§4§lNone"));
		return true;
	}

	@Override
	public void disable() {}

	public int getHelpPageAmount(CommandSender s) {
		double p = (double) getVisibleCommands(s).size() / (double) commandsPerPage;
		int pages = (int) p;
		if (p > (double) pages)
			pages++;
		return pages;
	}

	public boolean hasHelpPage(CommandSender s, int desiredPage) {
		return desiredPage > 0 && desiredPage <= getHelpPageAmount(s);
	}

	public void displayHelpPage(CommandSender s, int page) {
		List<CommandDetails> visibleCommands = getVisibleCommands(s);
		String helpPage = "";
		for (int i = (page - 1) * commandsPerPage; i <= page * commandsPerPage - 1; i++) {
			if (i > visibleCommands.size() - 1)
				break;
			helpPage += "\n§r" + visibleCommands.get(i).getHelpPageString(commandDetailsLabel);
		}
		int pages = getHelpPageAmount(s);
		s.sendMessage((pageHeader == null ? "" : pageHeader.replace("<version>", plugin.getDescription().getVersion())) + helpPage
				+ (pageFooter == null ? "" : "\n§r" + pageFooter.replace("<current_page>", (page == pages ? "§6§l" : "§a§l") + page).replace("<page_amount>", pages + "")));
	}

	public List<CommandDetails> getVisibleCommands(CommandSender s) {
		List<CommandDetails> visible = new ArrayList<CommandDetails>();
		for (int i = 0; i < commands.size(); i++) {
			CommandDetails cd = commands.get(i);
			if (!cd.isHidden(s))
				visible.add(cd);
		}
		return visible;
	}

	public String getPageHeader() {
		return this.pageHeader;
	}

	public String getPageFooter() {
		return this.pageFooter;
	}

	public String getCommandDetailsLabel() {
		return this.commandDetailsLabel;
	}

	public List<CommandDetails> getCommands() {
		return this.commands;
	}

	public CommandDetails getCommand(String commandPart) {
		commandPart = commandPart.toLowerCase();
		for (int i = 0; i < commands.size(); i++) {
			CommandDetails cd = commands.get(i);
			if (cd.getCommand().contains(commandPart))
				return cd;
		}
		return null;
	}
}

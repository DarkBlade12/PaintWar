package com.darkblade12.paintwar.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.darkblade12.paintwar.PaintWar;
import com.darkblade12.paintwar.arena.Arena;
import com.darkblade12.paintwar.arena.State;
import com.darkblade12.paintwar.arena.event.PlayerStartCountdownEvent;
import com.darkblade12.paintwar.arena.event.PlayerStopGameEvent;
import com.darkblade12.paintwar.arena.region.Cuboid;
import com.darkblade12.paintwar.help.CommandDetails;
import com.darkblade12.paintwar.sign.ArenaSign;
import com.darkblade12.paintwar.stats.Stat;
import com.darkblade12.paintwar.util.PlayerUtil;

public class PaintWarCE implements CommandExecutor {
	private PaintWar plugin;
	private CommandDetails help;

	public PaintWarCE(PaintWar plugin) {
		this.plugin = plugin;
		plugin.getCommand("pw").setExecutor(this);
		help = plugin.help.getCommand("help");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(help.getInvalidUsageString());
			return true;
		}
		Player p = sender instanceof Player ? (Player) sender : null;
		String sub = args[0].toLowerCase();
		CommandDetails cd = plugin.help.getCommand(sub);
		if (cd == null) {
			sender.sendMessage(help.getInvalidUsageString());
			return true;
		} else if (!cd.checkUsage(plugin, sender, args))
			return true;
		if (sub.equals("create")) {
			String name = args[1];
			if (plugin.arena.getArena(name) != null) {
				sender.sendMessage(plugin.message.arena_x(true));
				return true;
			}
			Arena.create(plugin, name);
			sender.sendMessage(plugin.message.arena_x(true, name));
		} else if (sub.equals("remove")) {
			Arena a = plugin.arena.getArena(args[1]);
			if (a == null) {
				sender.sendMessage(plugin.message.arena_x(false));
				return true;
			}
			a.remove();
			sender.sendMessage(plugin.message.arena_x(false, a.getName()));
		} else if (sub.equals("select")) {
			Arena a = plugin.arena.getArena(args[1]);
			if (a == null) {
				sender.sendMessage(plugin.message.arena_x(false));
				return true;
			}
			String name = a.getName();
			plugin.player.selectArena(p, name);
			sender.sendMessage(plugin.message.arena_selected(name));
		} else if (sub.equals("wand")) {
			if (!PlayerUtil.hasEnoughSpace(p, PaintWar.getWand())) {
				sender.sendMessage(plugin.message.player_not_enough_space());
				return true;
			}
			p.getInventory().addItem(PaintWar.getWand());
			sender.sendMessage(plugin.message.player_got_wand());
		} else if (sub.equals("set")) {
			Arena a;
			if (args.length == 2) {
				a = plugin.player.getSelectedArena(p);
				if (a == null) {
					sender.sendMessage(cd.getInvalidUsageString());
					return true;
				}
			} else {
				a = plugin.arena.getArena(args[2]);
			}
			if (a == null) {
				sender.sendMessage(plugin.message.arena_x(false));
				return true;
			} else if (!a.isInEditMode()) {
				sender.sendMessage(plugin.message.arena_not_in_edit_mode());
				return true;
			} else if (!plugin.player.hasSelectedBothPositions(p)) {
				sender.sendMessage(plugin.message.player_too_few_positions());
				return true;
			}
			Cuboid region;
			try {
				region = new Cuboid(plugin.player.getPosition(p, true), plugin.player.getPosition(p, false));
			} catch (Exception e) {
				// this catch is useless since it's checked before if both positions aren't null
				return true;
			}
			if (args[1].equalsIgnoreCase("floor")) {
				a.setFloor(region);
			} else if (args[1].equalsIgnoreCase("protection")) {
				a.setProtection(region);
			} else {
				sender.sendMessage(cd.getInvalidUsageString());
				return true;
			}
			sender.sendMessage(plugin.message.arena_x_set(args[1].equalsIgnoreCase("floor"), a.getName()));
		} else if (sub.equalsIgnoreCase("spawn")) {
			Arena a;
			if (args.length == 3) {
				a = plugin.player.getSelectedArena(p);
				if (a == null) {
					sender.sendMessage(cd.getInvalidUsageString());
					return true;
				}
			} else {
				a = plugin.arena.getArena(args[3]);
			}
			if (a == null) {
				sender.sendMessage(plugin.message.arena_x(false));
				return true;
			}
			String spawn = args[2];
			if (!a.isInEditMode()) {
				sender.sendMessage(plugin.message.arena_not_in_edit_mode());
				return true;
			}
			String operation = args[1].toLowerCase();
			if (operation.equals("add")) {
				if (a.getSpawnAmount() == 16) {
					sender.sendMessage(plugin.message.arena_too_x_spawns(true));
					return true;
				} else if (a.hasSpawn(spawn)) {
					sender.sendMessage(plugin.message.arena_spawn_x(true));
					return true;
				}
				a.addSpawn(spawn, p.getLocation());
			} else if (operation.equals("del")) {
				if (!a.hasSpawn(spawn)) {
					sender.sendMessage(plugin.message.arena_spawn_x(false));
					return true;
				}
				a.deleteSpawn(spawn);
			} else {
				sender.sendMessage(cd.getInvalidUsageString());
				return true;
			}
			sender.sendMessage(plugin.message.arena_spawn_x(operation.equals("add"), spawn, a.getName()));
		} else if (sub.equals("check")) {
			Arena a;
			if (args.length == 1 && p != null) {
				a = plugin.player.getSelectedArena(p);
				if (a == null) {
					sender.sendMessage(cd.getInvalidUsageString());
					return true;
				}
			} else {
				a = plugin.arena.getArena(args[1]);
			}
			if (a == null) {
				sender.sendMessage(plugin.message.arena_x(false));
				return true;
			} else if (!a.isInEditMode()) {
				sender.sendMessage(plugin.message.arena_not_in_edit_mode());
				return true;
			}
			sender.sendMessage(plugin.message.arena_checklist(a));
		} else if (sub.equals("edit")) {
			Arena a;
			if (args.length == 1 && p != null) {
				a = plugin.player.getSelectedArena(p);
				if (a == null) {
					sender.sendMessage(cd.getInvalidUsageString());
					return true;
				}
			} else {
				a = plugin.arena.getArena(args[1]);
			}
			if (a == null) {
				sender.sendMessage(plugin.message.arena_x(false));
				return true;
			} else if (a.isInEditMode() && !a.isReadyForUse()) {
				sender.sendMessage(plugin.message.arena_x_for_use(false));
				return true;
			}
			a.switchEditMode();
			sender.sendMessage(plugin.message.arena_toggle_edit_mode(a.getName(), a.isInEditMode()));
		} else if (sub.equals("arenas")) {
			sender.sendMessage(plugin.message.arena_list());
		} else if (sub.equals("players")) {
			Arena a = plugin.arena.getArena(args[1]);
			if (a == null) {
				sender.sendMessage(plugin.message.arena_x(false));
				return true;
			}
			sender.sendMessage(plugin.message.player_list(a));
		} else if (sub.equals("join")) {
			Arena a = plugin.arena.getArena(args[1]);
			if (a == null) {
				sender.sendMessage(plugin.message.arena_x(false));
				return true;
			} else if (!a.isSetup()) {
				sender.sendMessage(plugin.message.arena_x_setup(false));
				return true;
			} else if (plugin.arena.hasJoinedArena(p)) {
				sender.sendMessage(plugin.message.arena_x_joined(true));
				return true;
			} else if (a.requiresEmptyInventory() && !PlayerUtil.hasEmptyInventory(p)) {
				sender.sendMessage(plugin.message.player_inventory_not_empty());
				return true;
			} else if (a.isFull()) {
				sender.sendMessage(plugin.message.arena_full());
				return true;
			} else if (a.getState() == State.NOT_JOINABLE) {
				sender.sendMessage(plugin.message.arena_game_x_started(true));
				return true;
			}
			a.handleJoin(p);
			sender.sendMessage(plugin.message.arena_joined(a));
		} else if (sub.equals("leave")) {
			Arena a = plugin.arena.getJoinedArena(p);
			if (a == null) {
				sender.sendMessage(plugin.message.arena_x_joined(false));
				return true;
			}
			a.handleLeave(p);
			sender.sendMessage(plugin.message.arena_left(a));
		} else if (sub.equals("start")) {
			Arena a = plugin.arena.getArena(args[1]);
			if (a == null) {
				sender.sendMessage(plugin.message.arena_x(false));
				return true;
			} else if (!a.isSetup()) {
				sender.sendMessage(plugin.message.arena_x_setup(false));
				return true;
			} else if (a.getState() != State.JOINABLE) {
				sender.sendMessage(plugin.message.arena_game_x_started(true));
				return true;
			} else if (a.getPlayers().size() < 2) {
				sender.sendMessage(plugin.message.arena_can_not_start());
				return true;
			}
			if (p != null) {
				PlayerStartCountdownEvent e = new PlayerStartCountdownEvent(p, a);
				e.call();
				if (e.isCancelled())
					return true;
			}
			a.broadcastMessage(plugin.message.arena_game_started_other(sender.getName()));
			a.startCountdown();
			sender.sendMessage(plugin.message.arena_game_started(a.getName()));
		} else if (sub.equals("stop")) {
			Arena a = plugin.arena.getArena(args[1]);
			if (a == null) {
				sender.sendMessage(plugin.message.arena_x(false));
				return true;
			} else if (a.getState() != State.NOT_JOINABLE) {
				sender.sendMessage(plugin.message.arena_game_x_started(false));
				return true;
			}
			if (p != null) {
				PlayerStopGameEvent e = new PlayerStopGameEvent(p, a);
				e.call();
				if (e.isCancelled())
					return true;
			}
			a.broadcastMessage(plugin.message.arena_game_stopped_other(sender.getName()));
			a.stopGame(true);
			sender.sendMessage(plugin.message.arena_game_stopped(a.getName()));
		} else if (sub.equals("kick")) {
			Player k = Bukkit.getPlayer(args[1]);
			if (k == null) {
				sender.sendMessage(plugin.message.player_not_existent());
				return true;
			}
			String playerName = k.getName();
			Arena a = plugin.arena.getJoinedArena(k);
			if (a == null) {
				sender.sendMessage(plugin.message.arena_x_joined(false));
				return true;
			}
			String name = a.getName();
			a.handleLeave(k);
			a.broadcastMessage(plugin.message.player_kicked_other(name, playerName));
			sender.sendMessage(plugin.message.player_kicked(playerName, name));
		} else if (sub.equals("stats")) {
			boolean own = args.length == 1;
			if (p == null && own) {
				sender.sendMessage(plugin.message.command_no_console_executor());
				return true;
			}
			String name = own ? sender.getName() : plugin.stats.getPlayerName(args[1]);
			if (!plugin.stats.hasStats(name)) {
				sender.sendMessage(plugin.message.stats_not_found(name));
				return true;
			}
			sender.sendMessage(plugin.message.stats(name));
		} else if (sub.equals("top")) {
			if (!plugin.stats.hasStats()) {
				sender.sendMessage(plugin.message.no_top_ten());
				return true;
			}
			Stat s = Stat.fromName(args[1]);
			if (s == null) {
				sender.sendMessage(cd.getInvalidUsageString());
				return true;
			}
			sender.sendMessage(plugin.message.top_ten(s));
		} else if (sub.equals("signs")) {
			sender.sendMessage(plugin.message.sign_list());
		} else if (sub.equals("tp")) {
			ArenaSign as = plugin.sign.getSign(args[1]);
			if (as == null) {
				sender.sendMessage(plugin.message.sign_not_existent());
				return true;
			}
			p.teleport(as.getLocation());
			sender.sendMessage(plugin.message.sign_teleport(as.getId()));
		} else if (sub.equals("reload")) {
			if (args.length == 2) {
				if (args[1].equalsIgnoreCase("config")) {
					plugin.loadConfig();
					plugin.setting.initialize();
				} else {
					sender.sendMessage(cd.getInvalidUsageString());
					return true;
				}
			}
			sender.sendMessage(args.length == 2 ? plugin.message.reload_config() : plugin.message.reload_plugin());
		} else if (sub.equals("help")) {
			int page = 1;
			if (args.length == 2)
				try {
					page = Integer.parseInt(args[1]);
					if (!plugin.help.hasHelpPage(sender, page)) {
						sender.sendMessage(plugin.message.help_page_not_existent());
						return true;
					}
				} catch (Exception e) {
					sender.sendMessage(plugin.message.help_page_invalid_number());
					return true;
				}
			plugin.help.displayHelpPage(sender, page);
		}
		return true;
	}
}

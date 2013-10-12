package com.darkblade12.paintwar.message;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.darkblade12.paintwar.PaintWar;
import com.darkblade12.paintwar.arena.Arena;
import com.darkblade12.paintwar.arena.State;
import com.darkblade12.paintwar.arena.region.Cuboid;
import com.darkblade12.paintwar.loader.TextFileLoader;
import com.darkblade12.paintwar.manager.Manager;
import com.darkblade12.paintwar.sign.ArenaSign;
import com.darkblade12.paintwar.stats.Stat;

public class MessageManager extends Manager implements MessageContainer {
	private final static String[] colorCodeModifiers = new String[] { /* "0", */"1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e"/* , "f" */};
	private final static Map<Integer, String> numberSymbols = new HashMap<Integer, String>();
	public final static String check = "§a\u2714";
	public final static String missing = "§c\u2718";
	private TextFileLoader loader;
	private Map<String, String> messages;

	static {
		numberSymbols.put(1, "§6\u2776");
		numberSymbols.put(2, "§7\u2777");
		numberSymbols.put(3, "§8\u2778");
		numberSymbols.put(4, "§f\u2779");
		numberSymbols.put(5, "§f\u277A");
		numberSymbols.put(6, "§f\u277B");
		numberSymbols.put(7, "§f\u277C");
		numberSymbols.put(8, "§f\u277D");
		numberSymbols.put(9, "§f\u277E");
		numberSymbols.put(10, "§f\u277F");
	}

	public MessageManager(PaintWar plugin) {
		super(plugin);
		this.plugin = plugin;
	}

	@Override
	public boolean initialize() {
		loader = new TextFileLoader(plugin, "lang_" + plugin.setting.LANGUAGE_NAME + ".txt");
		if (!loader.loadFile()) {
			plugin.l.warning("Failed to save " + loader.getOuputFileName() + ". Plugin will disable!");
			return false;
		} else if (!loadMessages()) {
			plugin.l.warning("Failed to load messages. Plugin will disable!");
			return false;
		}
		plugin.l.info(loader.getOuputFileName() + " successfully loaded.");
		return true;
	}

	@Override
	public void disable() {}

	private boolean loadMessages() {
		messages = new HashMap<String, String>();
		try {
			BufferedReader reader = loader.getReader();
			String line = reader.readLine();
			while (line != null) {
				String[] s = line.split("=");
				if (s.length == 2 && !line.startsWith("#"))
					messages.put(s[0], ChatColor.translateAlternateColorCodes('&', s[1]).replace(";", "\n"));
				line = reader.readLine();
			}
			reader.close();
			return true;
		} catch (Exception e) {
			plugin.l.warning("Failed to read " + loader.getOuputFileName() + ". Plugin will disable!");
			return false;
		}
	}

	public String getMessage(String id) {
		if (!messages.containsKey(id))
			return "§cMessage not available, please check your language file! §8(§7Message id: §6" + id + "§8)";
		return messages.get(id);
	}

	public String getMessage(String id, boolean withPrefix) {
		return (withPrefix ? PaintWar.PREFIX : "") + getMessage(id);
	}

	public static String coloredArrow() {
		return randomColorCode() + "\u276D" + randomColorCode() + "\u276F" + randomColorCode() + "\u2771";
	}

	public static String randomColorCode() {
		return "§" + colorCodeModifiers[RANDOM.nextInt(colorCodeModifiers.length)];
	}

	public static String getSymbol(int num) {
		return numberSymbols.get(num);
	}

	private String getChecklist(Arena a) {
		int spawns = a.getSpawnAmount();
		StringBuilder builder = new StringBuilder("\n §e\u2022 §6§lProtection: " + (a.getProtection() != null ? check : missing) + "\n §e\u2022 §6§lFloor: " + (a.getFloor() != null ? check : missing)
				+ "\n §e\u2022 §6§lSpawns: " + (spawns > 1 ? check : missing) + " §8(§b" + spawns + (spawns > 1 ? "" : "§7, " + arena_too_x_spawns(false)) + "§8)");
		if (a.isReadyForUse())
			builder.append("\n" + arena_x_for_use(true));
		return builder.toString();
	}

	private String arenaListToString() {
		List<Arena> arenas = plugin.arena.getArenas();
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < arenas.size(); i++) {
			Arena a = arenas.get(i);
			State s = a.getState();
			builder.append("\n " + randomColorCode() + "\u276D\u276F\u2771 §7§o" + a.getName() + " §r\u268A "
					+ (a.isSetup() ? (s == State.JOINABLE ? "§a" : s == State.COUNTING ? "§6" : "§4") + "§l" + getMessage("state_" + s.getName()) : "§4§lNot setup"));
		}
		return builder.toString();
	}

	private String playerListToString(Arena a) {
		StringBuilder builder = new StringBuilder();
		for (String name : a.getPlayerNames())
			builder.append("\n §6\u25BB §e§o" + name);
		return builder.length() == 0 ? "\n §6\u25BB §4§oNone" : builder.toString();
	}

	private String getStats(String name) {
		return "\n§r §7\u27AB §e§l" + getMessage("category_won_games") + ": §6§l" + plugin.stats.get(name, Stat.WON_GAMES) + "\n§r §7\u27AB §c§l" + getMessage("category_lost_games") + ": §4§l"
				+ plugin.stats.get(name, Stat.LOST_GAMES) + "\n§r §7\u27AB §8§l" + getMessage("category_wl_ratio") + ": §b§l" + plugin.stats.getRatio(name);
	}

	private String getTop(Stat s) {
		StringBuilder builder = new StringBuilder();
		boolean wl = s == Stat.WL_RATIO;
		Map<Integer, String> top = plugin.stats.getTop(s);
		for (int i = 1; i <= top.size(); i++) {
			String name = top.get(i);
			builder.append("\n§r " + getSymbol(i) + " §e§l" + name + ": §a§l");
			if (!wl)
				builder.append(plugin.stats.get(name, s));
			else
				builder.append(plugin.stats.getRatio(name));
		}
		return builder.toString();
	}

	private String signListToString() {
		StringBuilder builder = new StringBuilder();
		List<ArenaSign> signs = plugin.sign.getSigns();
		for (int i = 0; i < signs.size(); i++) {
			ArenaSign as = signs.get(i);
			builder.append("\n §8\u25BB §c§o" + as.getId() + " §r\u268A §e§oArena: §6§o" + as.getArenaName());
		}
		return builder.length() == 0 ? "\n §6\u25BB §4§oNone" : builder.toString();
	}

	@Override
	public String command_no_permission() {
		return getMessage("command_no_permission");
	}

	@Override
	public String command_no_console_executor() {
		return getMessage("command_no_console_executor");
	}

	@Override
	public String arena_x(boolean existent) {
		return getMessage("arena_" + (existent ? "" : "not_") + "existent", true);
	}

	@Override
	public String arena_x(boolean created, String arena) {
		return getMessage("arena_" + (created ? "created" : "removed"), true).replace("<arena>", arena);
	}

	@Override
	public String arena_selected(String arena) {
		return getMessage("arena_selected", true).replace("<arena>", arena);
	}

	@Override
	public String arena_x_set(boolean floor, String arena) {
		return getMessage("arena_" + (floor ? "floor" : "protection") + "_set", true).replace("<arena>", arena);
	}

	@Override
	public String arena_spawn_x(boolean existent) {
		return getMessage("arena_spawn_" + (existent ? "" : "not_") + "existent", true);
	}

	@Override
	public String arena_too_x_spawns(boolean many) {
		return getMessage("arena_too_" + (many ? "many" : "few") + "_spawns", many);
	}

	@Override
	public String arena_spawn_x(boolean added, String spawn, String arena) {
		return getMessage("arena_spawn_" + (added ? "added" : "deleted"), true).replace("<spawn>", spawn).replace("<arena>", arena);
	}

	@Override
	public String arena_checklist(Arena arena) {
		return getMessage("arena_checklist", true).replace("<arena>", arena.getName()).replace("<checklist>", getChecklist(arena));
	}

	@Override
	public String arena_x_for_use(boolean ready) {
		return getMessage("arena_" + (ready ? "" : "not_") + "ready_for_use", !ready);
	}

	@Override
	public String arena_x_setup(boolean already) {
		return getMessage("arena_" + (already ? "already" : "not") + "_setup", true);
	}

	@Override
	public String arena_toggle_edit_mode(String arena, boolean state) {
		return getMessage("arena_toggle_edit_mode", true).replace("<arena>", arena).replace("<state>", state ? "§2" + getMessage("state_on") : "§4" + getMessage("state_off"));
	}

	@Override
	public String arena_not_in_edit_mode() {
		return getMessage("arena_not_in_edit_mode", true);
	}

	@Override
	public String arena_list() {
		return getMessage("arena_list", true).replace("<arenas>", arenaListToString());
	}

	@Override
	public String arena_full() {
		return getMessage("arena_full", true);
	}

	@Override
	public String arena_game_x_started(boolean already) {
		return getMessage("arena_game_" + (already ? "already" : "not") + "_started", true);
	}

	@Override
	public String arena_x_joined(boolean already) {
		return getMessage("arena_" + (already ? "already" : "not") + "_joined", true);
	}

	@Override
	public String arena_joined(Arena arena) {
		return getMessage("arena_joined", true).replace("<arena>", arena.getName()).replace("<player_display>", arena.getPlayerDisplay());
	}

	@Override
	public String arena_joined_other(String player, Arena arena) {
		return getMessage("arena_joined_other", true).replace("<player>", player).replace("<arena>", arena.getName()).replace("<player_display>", arena.getPlayerDisplay());
	}

	@Override
	public String arena_left(Arena arena) {
		return getMessage("arena_left", true).replace("<arena>", arena.getName()).replace("<player_display>", arena.getPlayerDisplay());
	}

	@Override
	public String arena_left_other(String player, Arena arena) {
		return getMessage("arena_left_other", true).replace("<player>", player).replace("<arena>", arena.getName()).replace("<player_display>", arena.getPlayerDisplay());
	}

	@Override
	public String arena_ready() {
		return getMessage("arena_ready", true);
	}

	@Override
	public String arena_ready_other(String player, String arena) {
		return getMessage("arena_ready_other", true).replace("<player>", player).replace("<arena>", arena);
	}

	@Override
	public String arena_already_ready() {
		return getMessage("arena_already_ready", true);
	}

	@Override
	public String arena_countdown(int time) {
		return getMessage("arena_countdown", true).replace("<time>", time + "");
	}

	@Override
	public String arena_countdown_go() {
		return getMessage("arena_countdown_go", true);
	}

	@Override
	public String arena_countdown_stopped() {
		return getMessage("arena_countdown_stopped", true);
	}

	@Override
	public String arena_powerup_x(boolean got, String player, String powerup) {
		return getMessage("arena_powerup_" + (got ? "got" : "end"), true).replace("<player>", player).replace("<powerup>", powerup.replace("_", " "));
	}

	@Override
	public String arena_player_won_game(String player, String arena) {
		return getMessage("arena_player_won_game", true).replace("<player>", player).replace("<arena>", arena);
	}

	@Override
	public String arena_game_overview(String arena, String overview) {
		return getMessage("arena_game_overview", true).replace("<arena>", arena).replace("<overview>", overview);
	}

	@Override
	public String arena_forced_stop(String arena) {
		return getMessage("arena_forced_stop", true).replace("<arena>", arena);
	}

	@Override
	public String arena_can_not_start() {
		return getMessage("arena_can_not_start", true);
	}

	@Override
	public String arena_game_started(String arena) {
		return getMessage("arena_game_started", true).replace("<arena>", arena);
	}

	@Override
	public String arena_game_started_other(String player) {
		return getMessage("arena_game_started_other", true).replace("<player>", player);
	}

	@Override
	public String arena_game_stopped(String arena) {
		return getMessage("arena_game_stopped", true).replace("<arena>", arena);
	}

	@Override
	public String arena_game_stopped_other(String player) {
		return getMessage("arena_game_stopped_other", true).replace("<player>", player);
	}

	@Override
	public String arena_action_not_allowed(String action) {
		return getMessage("arena_action_not_allowed", true).replace("<action>", action);
	}

	@Override
	public String arena_time_remaining(int seconds) {
		return getMessage("arena_time_remaining", true).replace("<seconds>", seconds + "");
	}

	@Override
	public String arena_disabled() {
		return getMessage("arena_disabled", true);
	}

	@Override
	public String stats_not_found(String player) {
		return getMessage("stats_not_found", true).replace("<player>", player);
	}

	@Override
	public String stats(String player) {
		return getMessage("stats", true).replace("<player>", player).replace("<stats>", getStats(player));
	}

	@Override
	public String no_top_ten() {
		return getMessage("no_top_ten", true);
	}

	@Override
	public String top_ten(Stat category) {
		return getMessage("top_ten", true).replace("<category>",
				category == Stat.WON_GAMES ? getMessage("category_won_games") : category == Stat.LOST_GAMES ? getMessage("category_lost_games") : getMessage("category_wl_ratio")).replace("<top>",
				getTop(category));
	}

	@Override
	public String player_not_enough_space() {
		return getMessage("player_not_enough_space", true);
	}

	@Override
	public String player_got_wand() {
		return getMessage("player_got_wand", true);
	}

	@Override
	public String player_position_set(Player player, boolean first, Location location) {
		String player_position_set = getMessage("player_position_set", true).replace("<position>", first ? getMessage("position_first") : getMessage("position_second")).replace("<location>",
				location.getX() + ", " + location.getY() + ", " + location.getZ());
		if (plugin.data.isSelectionComplete(player))
			try {
				player_position_set += " §8(§e" + new Cuboid(plugin.data.getPosition(player, true), plugin.data.getPosition(player, false)).getVolume() + "§8)";
			} catch (Exception e) {
				// failed to initiate the {@Cuboid}, should not happen since it's checked before
			}
		return player_position_set;
	}

	@Override
	public String player_too_few_positions() {
		return getMessage("player_too_few_positions", true);
	}

	@Override
	public String player_list(Arena arena) {
		return getMessage("player_list", true).replace("<arena>", arena.getName()).replace("<players>", playerListToString(arena)).replace("<player_display>", arena.getPlayerDisplay());
	}

	@Override
	public String player_inventory_not_empty() {
		return getMessage("player_inventory_not_empty", true);
	}

	@Override
	public String player_not_existent() {
		return getMessage("player_not_existent", true);
	}

	@Override
	public String player_not_joined_arena() {
		return getMessage("player_not_joined_arena", true);
	}

	@Override
	public String player_kicked(String player, String arena) {
		return getMessage("player_kicked", true).replace("<player>", player).replace("<arena>", arena);
	}

	@Override
	public String player_kicked_other(String arena, String player) {
		return getMessage("player_kicked_other", true).replace("<arena>", arena).replace("<player>", player);
	}

	@Override
	public String help_page_invalid_number() {
		return getMessage("help_page_invalid_number", true);
	}

	@Override
	public String help_page_header(String version) {
		return getMessage("help_page_header");
	}

	@Override
	public String help_page_not_existent() {
		return getMessage("help_page_not_existent", true);
	}

	@Override
	public String sign_no_x_permission(boolean create) {
		return getMessage("sign_no_" + (create ? "create" : "break") + "_permission", true);
	}

	@Override
	public String sign_no_arena_found() {
		return getMessage("sign_no_arena_found", true);
	}

	@Override
	public String sign_x(boolean created, String id) {
		return getMessage("sign_" + (created ? "created" : "removed"), true).replace("<id>", id);
	}

	@Override
	public String sign_list() {
		return getMessage("sign_list", true).replace("<signs>", signListToString());
	}

	@Override
	public String sign_not_existent() {
		return getMessage("sign_not_existent", true);
	}

	@Override
	public String sign_teleport(String id) {
		return getMessage("sign_teleport", true).replace("<id>", id);
	}

	@Override
	public String reload_config() {
		return getMessage("reload_config", true);
	}

	@Override
	public String reload_plugin() {
		return getMessage("reload_plugin", true);
	}
}

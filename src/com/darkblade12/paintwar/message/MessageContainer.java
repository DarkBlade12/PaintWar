package com.darkblade12.paintwar.message;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.darkblade12.paintwar.arena.Arena;
import com.darkblade12.paintwar.stats.Stat;

public interface MessageContainer {

	public String command_no_permission();

	public String command_no_console_executor();

	public String arena_x(boolean existent);

	public String arena_x(boolean created, String arena);

	public String arena_selected(String arena);

	public String arena_x_set(boolean floor, String arena);

	public String arena_spawn_x(boolean existent);

	public String arena_too_x_spawns(boolean many);

	public String arena_spawn_x(boolean added, String spawn, String arena);

	/**
	 * Argument "checklist" is not visible as parameter
	 */
	public String arena_checklist(Arena arena);

	public String arena_x_for_use(boolean ready);

	public String arena_x_setup(boolean already);

	public String arena_toggle_edit_mode(String arena, boolean state);

	public String arena_not_in_edit_mode();

	/**
	 * Argument "arenas" is not visible as parameter
	 */
	public String arena_list();

	public String arena_full();

	public String arena_game_x_started(boolean already);

	public String arena_x_joined(boolean already);

	/**
	 * Argument "player_display" is not visible as parameter
	 */
	public String arena_joined(Arena arena);

	/**
	 * Argument "player_display" is not visible as parameter
	 */
	public String arena_joined_other(String player, Arena arena);

	/**
	 * Argument "player_display" is not visible as parameter
	 */
	public String arena_left(Arena arena);

	/**
	 * Argument "player_display" is not visible as parameter
	 */
	public String arena_left_other(String player, Arena arena);

	public String arena_ready();

	public String arena_ready_other(String player, String arena);

	public String arena_already_ready();

	public String arena_countdown(int time);

	public String arena_countdown_go();
	
	public String arena_countdown_stopped();

	public String arena_powerup_x(boolean got, String player, String powerup);

	public String arena_player_won_game(String player, String arena);

	public String arena_game_overview(String arena, String overview);
	
	public String arena_forced_stop(String arena);

	public String arena_can_not_start();

	public String arena_game_started(String arena);

	public String arena_game_started_other(String player);

	public String arena_game_stopped(String arena);

	public String arena_game_stopped_other(String player);

	public String arena_action_not_allowed(String action);

	public String arena_disabled();

	public String stats_not_found(String player);

	/**
	 * Argument "stats" is not visible as parameter
	 */
	public String stats(String player);

	public String no_top_ten();

	/**
	 * Argument "top" is not visible as parameter
	 */
	public String top_ten(Stat category);

	public String player_not_enough_space();

	public String player_got_wand();

	public String player_position_set(Player player, boolean first, Location location);

	public String player_too_few_positions();

	/**
	 * Arguments "players" and "player_display" are not visible as parameter
	 */
	public String player_list(Arena arena);

	public String player_inventory_not_empty();

	public String player_not_existent();

	public String player_not_joined_arena();

	public String player_kicked(String player, String arena);

	public String player_kicked_other(String arena, String player);

	public String help_page_invalid_number();

	public String help_page_header(String version);

	public String help_page_not_existent();

	public String sign_no_x_permission(boolean create);

	public String sign_no_arena_found();

	public String sign_x(boolean created, String id);

	/**
	 * Argument "signs" is not visible as parameter
	 */
	public String sign_list();
	
	public String sign_not_existent();
	
	public String sign_teleport(String id);

	public String reload_config();

	public String reload_plugin();

}

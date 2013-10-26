package com.darkblade12.paintwar.arena.powerup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.darkblade12.paintwar.PaintWar;
import com.darkblade12.paintwar.arena.Arena;
import com.darkblade12.paintwar.arena.State;
import com.darkblade12.paintwar.arena.powerup.util.TaskHandler;
import com.darkblade12.paintwar.help.HelpPageManager;
import com.darkblade12.paintwar.manager.MultipleTaskManager;

public class PowerupManager extends MultipleTaskManager {
	private Arena arena;
	private List<Integer> schedule;
	private List<Powerup> powerups;
	private Map<Powerup, int[]> settings;
	private Map<String, TaskHandler> handlers;

	public PowerupManager(PaintWar plugin, Arena arena) {
		super(plugin);
		this.arena = arena;
	}

	@Override
	public boolean initialize() {
		schedule = new ArrayList<Integer>();
		YamlConfiguration config = arena.getConfig();
		String timeScheduleString = config.getString("Powerup_Settings.Time_Schedule");
		if (timeScheduleString == null)
			return false;
		for (String num : timeScheduleString.split(", "))
			try {
				schedule.add(Integer.parseInt(num));
			} catch (Exception e) {
				// invalid number is ignored
			}
		powerups = new ArrayList<Powerup>();
		settings = new HashMap<Powerup, int[]>();
		String defaultIndex = "Powerup_Settings." + arena.getMode().getName() + "_Mode_Powerups.";
		String index;
		for (Powerup pow : Powerup.values()) {
			index = defaultIndex + pow.getName();
			if (config.getConfigurationSection(index) == null)
				continue;
			powerups.add(pow);
			switch (pow) {
				case BIG_BRUSH:
				case TINY_BRUSH:
					settings.put(pow, new int[] { config.getInt(index + ".Brush_Size"), config.getInt(index + ".Duration") });
					continue;
				case EMPTY_PAINT:
				case FREEZE:
				case ADVANCED_DARKNESS:
				case IMMORTAL_COLOR:
				case NO_BORDERS:
					settings.put(pow, new int[] { config.getInt(index + ".Duration") });
					continue;
				case SPEED:
				case DRUNKEN:
				case SLOWNESS:
					settings.put(pow, new int[] { config.getInt(index + ".Intensity"), config.getInt(index + ".Duration") });
					continue;
				case BIG_BLOB:
					settings.put(pow, new int[] { config.getInt(index + ".Blob_Size") });
					continue;
				case TINY_BLOBS:
					settings.put(pow, new int[] { config.getInt(index + ".Blob_Size"), config.getInt(index + ".Blob_Amount") });
					continue;
				case JUMPING:
					settings.put(pow, new int[] { config.getInt(index + ".Jump_Height"), config.getInt(index + ".Duration") });
					continue;
				case COLOR_BOMBS:
					settings.put(pow, new int[] { config.getInt(index + ".Bomb_Size"), config.getInt(index + ".Bomb_Amount"), config.getInt(index + ".Disappear_Duration") });
					continue;
				case ERASER:
					settings.put(pow, new int[] { config.getInt(index + ".Eraser_Size"), config.getInt(index + ".Duration") });
					continue;
				case POWERUP_MAGNET:
					settings.put(pow, new int[] { config.getInt(index + ".Magnet_Range"), config.getInt(index + ".Duration") });
					continue;
				case DASH:
					settings.put(pow, new int[] { config.getInt(index + ".Dash_Amount"), config.getInt(index + ".Disappear_Duration") });
					continue;
			}
		}
		handlers = new ConcurrentHashMap<String, TaskHandler>();
		return schedule.size() != 0 && powerups.size() != 0;
	}

	public void startTasks() {
		for (int seconds : schedule) {
			scheduleTask(new Runnable() {
				@Override
				public void run() {
					if (arena.getState() != State.NOT_JOINABLE) // just a temporary solution for the powerups dropping after a game in an arena has stopped problem
						cancelTasks();
					else
						arena.getFloor().dropPowerup(getRandomPowerup());
				}
			}, seconds * 20);
		}
		if (plugin.setting.BONUS_ENABLED && settings.containsKey(plugin.setting.BONUS_POWERUP))
			scheduleTask(new Runnable() {
				@Override
				public void run() {
					for (Player p : arena.getPlayers())
						if (p.hasPermission(HelpPageManager.BONUS_PERMISSION))
							activatePowerup(p, plugin.setting.BONUS_POWERUP);
				}
			}, plugin.setting.BONUS_DELAY * 20);
	}

	public void activatePowerup(Player p, Powerup pow) {
		pow.activate(plugin, p, this, arena);
		arena.update();
	}

	public void addTasks(String name, Powerup p, int... tasks) {
		if (!handlers.containsKey(name))
			handlers.put(name, new TaskHandler(p, tasks));
		else {
			TaskHandler handler = handlers.get(name);
			handler.addTasks(this, p, tasks);
			handlers.put(name, handler);
		}
	}

	public void removeTasks(String name, Powerup p) {
		TaskHandler handler = handlers.get(name);
		if (handler != null) {
			handler.removeTasks(p);
			handlers.put(name, handler);
		}
	}

	public void clearHandlers() {
		handlers = new ConcurrentHashMap<String, TaskHandler>();
	}

	public boolean hasTasks(String name, Powerup pow) {
		return handlers.containsKey(name) && handlers.get(name).hasTasks();
	}

	public Arena getBoundArena() {
		return this.arena;
	}

	public Powerup getRandomPowerup() {
		return powerups.get(RANDOM.nextInt(powerups.size()));
	}

	public boolean isEnabled(Powerup p) {
		return powerups.contains(p);
	}

	public int[] getSettings(Powerup p) {
		return settings.get(p);
	}

	public int getColorBombSize() {
		return settings.containsKey(Powerup.COLOR_BOMBS) ? settings.get(Powerup.COLOR_BOMBS)[0] : 0;
	}
}

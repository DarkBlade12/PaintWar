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
import com.darkblade12.paintwar.help.HelpPageManager;
import com.darkblade12.paintwar.manager.MultipleTaskManager;

public class PowerupManager extends MultipleTaskManager {
	private Arena arena;
	private List<Integer> timeSchedule;
	private List<Powerup> enabledPowerups;
	private Map<Powerup, int[]> powerupSettings;
	private Map<String, Map<Powerup, int[]>> playerTasks;

	public PowerupManager(PaintWar plugin, Arena arena) {
		super(plugin);
		this.arena = arena;
	}

	@Override
	public boolean initialize() {
		timeSchedule = new ArrayList<Integer>();
		YamlConfiguration config = arena.getConfig();
		String timeScheduleString = config.getString("Powerup_Settings.Time_Schedule");
		if (timeScheduleString == null)
			return false;
		for (String num : timeScheduleString.split(", "))
			try {
				timeSchedule.add(Integer.parseInt(num));
			} catch (Exception e) {
				// invalid number is ignored
			}
		enabledPowerups = new ArrayList<Powerup>();
		powerupSettings = new HashMap<Powerup, int[]>();
		String defaultIndex = "Powerup_Settings." + arena.getMode().getName() + "_Mode_Powerups.";
		String index;
		for (Powerup pow : Powerup.values()) {
			index = defaultIndex + pow.getName();
			if (config.getConfigurationSection(index) == null)
				continue;
			enabledPowerups.add(pow);
			switch (pow) {
				case BIG_BRUSH:
				case TINY_BRUSH:
					powerupSettings.put(pow, new int[] { config.getInt(index + ".Brush_Size"), config.getInt(index + ".Duration") });
					continue;
				case EMPTY_PAINT:
				case FREEZE:
				case ADVANCED_DARKNESS:
				case IMMORTAL_COLOR:
				case NO_BORDERS:
					powerupSettings.put(pow, new int[] { config.getInt(index + ".Duration") });
					continue;
				case SPEED:
				case DRUNKEN:
				case SLOWNESS:
					powerupSettings.put(pow, new int[] { config.getInt(index + ".Intensity"), config.getInt(index + ".Duration") });
					continue;
				case BIG_BLOB:
					powerupSettings.put(pow, new int[] { config.getInt(index + ".Blob_Size") });
					continue;
				case TINY_BLOBS:
					powerupSettings.put(pow, new int[] { config.getInt(index + ".Blob_Size"), config.getInt(index + ".Blob_Amount") });
					continue;
				case JUMPING:
					powerupSettings.put(pow, new int[] { config.getInt(index + ".Jump_Height"), config.getInt(index + ".Duration") });
					continue;
				case COLOR_BOMBS:
					powerupSettings.put(pow, new int[] { config.getInt(index + ".Bomb_Size"), config.getInt(index + ".Bomb_Amount"), config.getInt(index + ".Disappear_Duration") });
					continue;
				case ERASER:
					powerupSettings.put(pow, new int[] { config.getInt(index + ".Eraser_Size"), config.getInt(index + ".Duration") });
					continue;
				case POWERUP_MAGNET:
					powerupSettings.put(pow, new int[] { config.getInt(index + ".Magnet_Range"), config.getInt(index + ".Duration") });
					continue;
				case DASH:
					powerupSettings.put(pow, new int[] { config.getInt(index + ".Dash_Amount"), config.getInt(index + ".Disappear_Duration") });
					continue;
			}
		}
		playerTasks = new ConcurrentHashMap<String, Map<Powerup, int[]>>();
		if (timeSchedule.size() == 0 || enabledPowerups.size() == 0)
			return false;
		return true;
	}

	public void startTasks() {
		for (int seconds : timeSchedule) {
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
		if (plugin.setting.BONUS_ENABLED && powerupSettings.containsKey(plugin.setting.BONUS_POWERUP))
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

	public void setTasks(String name, Powerup p, int[] tasks) {
		Map<Powerup, int[]> powerupTasks = playerTasks.containsKey(name) ? playerTasks.get(name) : new HashMap<Powerup, int[]>();
		if (powerupTasks.containsKey(p)) {
			int[] i = powerupTasks.get(p);
			cancelTask(i[0]); // stop the ending task
			tasks[1] = i[1]; // transfer the repeating task
		}
		powerupTasks.put(p, tasks);
		playerTasks.put(name, powerupTasks);
	}

	public void removeTasks(String name, Powerup p) {
		Map<Powerup, int[]> powerupTasks = playerTasks.get(name);
		powerupTasks.remove(p);
		playerTasks.put(name, powerupTasks);
	}

	public void clearTasks() {
		playerTasks = new ConcurrentHashMap<String, Map<Powerup, int[]>>();
	}

	public boolean hasTasks(String name, Powerup pow) {
		return playerTasks.containsKey(name) && playerTasks.get(name).containsKey(pow);
	}

	public Arena getBoundArena() {
		return this.arena;
	}

	public Powerup getRandomPowerup() {
		return enabledPowerups.get(rn.nextInt(enabledPowerups.size()));
	}

	public boolean isEnabled(Powerup p) {
		return enabledPowerups.contains(p);
	}

	public int[] getSettings(Powerup p) {
		return powerupSettings.get(p);
	}

	public int getColorBombSize() {
		return powerupSettings.containsKey(Powerup.COLOR_BOMBS) ? powerupSettings.get(Powerup.COLOR_BOMBS)[0] : 0;
	}
}

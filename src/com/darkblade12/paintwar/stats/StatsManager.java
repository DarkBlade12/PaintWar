package com.darkblade12.paintwar.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.configuration.file.YamlConfiguration;

import com.darkblade12.paintwar.PaintWar;
import com.darkblade12.paintwar.loader.ConfigLoader;
import com.darkblade12.paintwar.manager.Manager;

public class StatsManager extends Manager {
	private ConfigLoader loader;
	public YamlConfiguration config;

	public StatsManager(PaintWar plugin) {
		super(plugin);
	}

	@Override
	public boolean initialize() {
		loader = new ConfigLoader(plugin, "stats.yml");
		if (!loader.loadConfig()) {
			plugin.l.warning("Failed to load stats.yml. Plugin will disable!");
			return false;
		}
		config = loader.getConfig();
		plugin.l.info("stats.yml successfully loaded.");
		return true;
	}

	@Override
	public void disable() {}

	public String getPlayerName(String name) {
		for (String playerName : config.getKeys(false))
			if (playerName.toLowerCase().contains(name.toLowerCase()))
				return playerName;
		return name;
	}

	public boolean hasStats(String name) {
		return config.getConfigurationSection(name) != null;
	}

	public boolean hasStats() {
		return config.getKeys(false).size() > 0;
	}

	public void add(String name, Stat s, int amount) {
		String index = name + "." + s.getName();
		config.set(index, config.getInt(index) + amount);
		loader.saveConfig(config);
	}

	public int get(String name, Stat s) {
		return config.getInt(name + "." + s.getName());
	}

	public double getRatio(String name) {
		int lost = get(name, Stat.LOST_GAMES);
		if (lost == 0)
			return lost;
		return Math.round(((double) get(name, Stat.WON_GAMES) / (double) lost) * 100.0D) / 100.0D;
	}

	public Map<Integer, String> getTop(Stat s) {
		boolean wl = s == Stat.WL_RATIO;
		Map<Integer, String> top = new HashMap<Integer, String>();
		Map<String, Double> all = new HashMap<String, Double>();
		for (String name : config.getKeys(false))
			all.put(name, wl ? getRatio(name) : (double) get(name, s));
		for (int i = 1; i <= 10; i++) {
			double highest = 0;
			String name = "None";
			for (Entry<String, Double> e : all.entrySet()) {
				double amount = e.getValue();
				if (amount > highest) {
					highest = amount;
					name = e.getKey();
				}
			}
			if (!name.equals("None")) {
				top.put(i, name);
				all.remove(name);
			}
		}
		return top;
	}

	public void reset(String name, Stat s) {
		config.set(name + "." + s.getName(), null);
		loader.saveConfig(config);
	}

	public void reset(String name) {
		config.set(name, null);
		loader.saveConfig(config);
	}
}

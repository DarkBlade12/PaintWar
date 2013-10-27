package com.darkblade12.paintwar.sign;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;

import com.darkblade12.paintwar.PaintWar;
import com.darkblade12.paintwar.arena.Arena;
import com.darkblade12.paintwar.arena.State;
import com.darkblade12.paintwar.util.LocationUtil;

public class ArenaSign {
	public final static String HEADER = "§6[§cPaintWar§6]";
	private PaintWar plugin;
	private String id;
	private Location location;
	private String arenaName;
	private int[] placeholderPositions; // 0 = <players>, 1 = <player_amount>, 2 = <state>, 3 = <mode>
	private boolean playersScrolling;
	private int textPosition;

	public ArenaSign(PaintWar plugin, Location location, String arenaName) {
		this.plugin = plugin;
		id = plugin.sign.generateSignId();
		this.location = location;
		this.arenaName = arenaName;
		searchPlaceholders();
		textPosition = 0;
		plugin.sign.config.set(id + ".Location", LocationUtil.parse(location));
		plugin.sign.config.set(id + ".Arena", arenaName);
		plugin.sign.config.set(id + ".Placeholder_Positions", placeholderPositions[0] + ", " + placeholderPositions[1] + ", " + placeholderPositions[2] + ", " + placeholderPositions[3]);
		plugin.sign.saveConfig();
		plugin.sign.addSign(this);
	}

	public ArenaSign(PaintWar plugin, String id, Location location, String arenaName, int[] placeholderPositions) {
		this.plugin = plugin;
		this.id = id;
		this.location = location;
		this.arenaName = arenaName;
		this.placeholderPositions = placeholderPositions;
		textPosition = 0;
	}

	private void searchPlaceholders() {
		placeholderPositions = new int[4];
		Sign s = getSign();
		final String[] lines = s.getLines();
		for (int i = 1; i < lines.length; i++) {
			String line = lines[i];
			if (line.equalsIgnoreCase("<players>") && placeholderPositions[0] == 0)
				placeholderPositions[0] = i;
			else if (line.equalsIgnoreCase("<player_amount>") && placeholderPositions[1] == 0)
				placeholderPositions[1] = i;
			else if (line.equalsIgnoreCase("<state>") && placeholderPositions[2] == 0)
				placeholderPositions[2] = i;
			else if (line.equalsIgnoreCase("<mode>") && placeholderPositions[3] == 0)
				placeholderPositions[3] = i;
		}
	}

	public void updateSign() {
		Sign s = getSign();
		Arena a = getArena();
		if (s == null || a == null) {
			remove(true);
			return;
		}
		if (!s.getLine(0).equals(HEADER))
			s.setLine(0, HEADER);
		for (int i = 0; i < 4; i++) {
			int position = placeholderPositions[i];
			if (position == 0)
				continue;
			String text;
			if (i == 0) {
				List<String> players = a.getPlayerNames();
				if (players.size() == 0) {
					if (playersScrolling) {
						playersScrolling = false;
						textPosition = 0;
					}
					text = scrollFurther(plugin.message.getMessage("no_players"));
				} else {
					if (!playersScrolling) {
						playersScrolling = true;
						textPosition = 0;
					}
					String list = StringUtils.join(players, ", ");
					text = scrollFurther(list.length() < 14 ? list.substring(0, list.length() - 2) : list);
				}
			} else if (i == 1) {
				String d = a.getPlayerDisplay();
				text = d.substring(2, d.length() - 3) + "�r}";
			} else if (i == 2) {
				State st = a.getState();
				text = (st == State.JOINABLE ? "�a" : st == State.COUNTING ? "�6" : "�4") + plugin.message.getMessage("state_" + st.getName());
			} else {
				text = "�8" + a.getMode().getName();
			}
			s.setLine(position, text);
		}
		s.update();
		update();
	}

	private String scrollFurther(String text) {
		if (text.length() < 14) {
			return "�e" + text;
		}
		int e = textPosition + 14;
		String f = "�e" + (e > text.length() ? text.substring(textPosition, text.length()) + text.substring(0, 14 - (text.length() - textPosition)) : text.substring(textPosition, e));
		textPosition++;
		if (textPosition == text.length())
			textPosition = 0;
		return f;
	}

	public void update() {
		plugin.sign.updateSign(this);
	}

	public void remove(boolean automatically) {
		plugin.sign.config.set(id, null);
		plugin.sign.saveConfig();
		plugin.sign.removeSign(this);
		location.getBlock().setType(Material.AIR);
		if (automatically)
			plugin.l.info("Arena sign with id '" + id + "' has been automatically removed! Reason: Sign or Arena is no longer existent");
	}

	public String getId() {
		return this.id;
	}

	public Location getLocation() {
		return this.location;
	}

	public Sign getSign() {
		try {
			return (Sign) location.getBlock().getState();
		} catch (Exception e) {
			return null;
		}
	}

	public int[] getPlaceholderPositions() {
		return this.placeholderPositions;
	}

	public String getArenaName() {
		return this.arenaName;
	}

	public Arena getArena() {
		return plugin.arena.getArena(arenaName);
	}
}

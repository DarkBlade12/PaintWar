package com.darkblade12.paintwar.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public enum Stat {
	WON_GAMES("Won_Games", "won"),
	LOST_GAMES("Lost_Games", "lost"),
	WL_RATIO("W/L_Ratio", "wl");

	private String name;
	private String alternativeName;
	private static final Map<String, Stat> NAME_MAP = new HashMap<String, Stat>();
	private static final Map<String, Stat> ALTERNATIVE_NAME_MAP = new HashMap<String, Stat>();

	static {
		for (Stat stat : values()) {
			if (stat.name != null)
				NAME_MAP.put(stat.getName(), stat);
			if (stat.alternativeName != null)
				NAME_MAP.put(stat.getAlternativeName(), stat);
		}
	}

	private Stat(String name, String alternativeName) {
		this.name = name;
		this.alternativeName = alternativeName;
	}

	public String getName() {
		return this.name;
	}

	public String getAlternativeName() {
		return this.alternativeName;
	}

	public static Stat fromName(String name) {
		if (name == null)
			return null;
		for (Entry<String, Stat> e : NAME_MAP.entrySet())
			if (e.getKey().equalsIgnoreCase(name))
				return e.getValue();
		for (Entry<String, Stat> e : ALTERNATIVE_NAME_MAP.entrySet())
			if (e.getKey().equalsIgnoreCase(name))
				return e.getValue();
		return null;
	}
}

package com.darkblade12.paintwar.arena;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public enum Mode {
	DEFAULT("Default");

	private String name;
	private static final Map<String, Mode> NAME_MAP = new HashMap<String, Mode>();

	static {
		for (Mode mode : values())
			if (mode.name != null)
				NAME_MAP.put(mode.getName(), mode);
	}

	private Mode(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public static Mode fromName(String name) {
		if (name == null)
			return null;
		for (Entry<String, Mode> e : NAME_MAP.entrySet())
			if (e.getKey().equalsIgnoreCase(name))
				return e.getValue();
		return null;
	}
}

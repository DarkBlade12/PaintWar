package com.darkblade12.paintwar.arena;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public enum State {
	JOINABLE("Joinable"),
	COUNTING("Counting..."),
	NOT_JOINABLE("Not joinable");

	private String name;
	private static final Map<String, State> NAME_MAP = new HashMap<String, State>();

	static {
		for (State state : values()) {
			if (state.name != null) {
				NAME_MAP.put(state.getName(), state);
			}
		}
	}

	private State(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public static State fromName(String name) {
		if (name == null)
			return null;
		for (Entry<String, State> e : NAME_MAP.entrySet()) {
			if (e.getKey().equalsIgnoreCase(name))
				return e.getValue();
		}
		return null;
	}
}

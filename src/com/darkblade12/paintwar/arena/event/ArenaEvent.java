package com.darkblade12.paintwar.arena.event;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;

import com.darkblade12.paintwar.arena.Arena;

public abstract class ArenaEvent extends Event {
	protected Arena arena;

	public ArenaEvent(Arena arena) {
		super(false);
		this.arena = arena;
	}

	public Arena getArena() {
		return this.arena;
	}

	public void call() {
		Bukkit.getServer().getPluginManager().callEvent(this);
	}
}

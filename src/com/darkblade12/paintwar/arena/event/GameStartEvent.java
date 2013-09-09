package com.darkblade12.paintwar.arena.event;

import org.bukkit.event.HandlerList;

import com.darkblade12.paintwar.arena.Arena;

public class GameStartEvent extends ArenaEvent {
	private static final HandlerList handlers = new HandlerList();

	public GameStartEvent(Arena arena) {
		super(arena);
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}

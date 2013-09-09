package com.darkblade12.paintwar.arena.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import com.darkblade12.paintwar.arena.Arena;

public class CountdownStartEvent extends ArenaEvent implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled;

	public CountdownStartEvent(Arena arena) {
		super(arena);
	}

	public void setCancelled(boolean arg0) {
		this.cancelled = arg0;
	}

	public boolean isCancelled() {
		return this.cancelled;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}

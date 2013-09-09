package com.darkblade12.paintwar.arena.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import com.darkblade12.paintwar.arena.Arena;

public class PlayerJoinArenaEvent extends PlayerArenaEvent implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled;

	public PlayerJoinArenaEvent(Player player, Arena arena) {
		super(player, arena);
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

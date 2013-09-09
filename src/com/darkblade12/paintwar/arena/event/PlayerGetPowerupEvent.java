package com.darkblade12.paintwar.arena.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import com.darkblade12.paintwar.arena.Arena;
import com.darkblade12.paintwar.arena.powerup.Powerup;

public class PlayerGetPowerupEvent extends PlayerArenaEvent implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	private Powerup powerup;
	private boolean cancelled;

	public PlayerGetPowerupEvent(Player player, Arena arena, Powerup powerup) {
		super(player, arena);
		this.player = player;
		this.powerup = powerup;
	}

	public void setCancelled(boolean arg0) {
		this.cancelled = arg0;
	}

	public boolean isCancelled() {
		return this.cancelled;
	}

	public Powerup getPowerup() {
		return this.powerup;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}

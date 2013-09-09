package com.darkblade12.paintwar.arena.event;

import org.bukkit.entity.Player;

import com.darkblade12.paintwar.arena.Arena;

public abstract class PlayerArenaEvent extends ArenaEvent {
	protected Player player;

	public PlayerArenaEvent(Player player, Arena arena) {
		super(arena);
		this.player = player;
	}

	public Player getPlayer() {
		return this.player;
	}
}

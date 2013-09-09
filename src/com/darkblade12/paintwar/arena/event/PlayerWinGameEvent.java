package com.darkblade12.paintwar.arena.event;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import com.darkblade12.paintwar.arena.Arena;

public class PlayerWinGameEvent extends PlayerArenaEvent {
	private static final HandlerList handlers = new HandlerList();
	private List<ItemStack> itemRewards;
	private double moneyReward;

	public PlayerWinGameEvent(Player player, Arena arena, List<ItemStack> itemRewards, double moneyReward) {
		super(player, arena);
		this.itemRewards = itemRewards;
		this.moneyReward = moneyReward;
	}

	public List<ItemStack> getItemRewards() {
		return this.itemRewards;
	}

	public double getMoneyReward() {
		return this.moneyReward;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}

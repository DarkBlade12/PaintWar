package com.darkblade12.paintwar.data;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class PlayerData {
	private Location location;
	private ItemStack[][] inventory;
	private boolean allowFlight;
	private boolean flying;
	private GameMode gameMode;
	private int level;
	private float exp;

	private PlayerData(Location location, ItemStack[][] inventory, boolean allowFlight, boolean flying, GameMode gameMode, int level, float exp) {
		this.location = location;
		this.inventory = inventory;
		this.allowFlight = allowFlight;
		this.flying = flying;
		this.gameMode = gameMode;
		this.level = level;
		this.exp = exp;
	}

	public static PlayerData get(Player p) {
		PlayerInventory i = p.getInventory();
		return new PlayerData(p.getLocation(), new ItemStack[][] { i.getContents(), i.getArmorContents() }, p.getAllowFlight(), p.isFlying(), p.getGameMode(), p.getLevel(), p.getExp());
	}

	public static void clean(Player p) {
		PlayerInventory i = p.getInventory();
		i.clear();
		i.setArmorContents(new ItemStack[4]);
		p.setFlying(false);
		p.setAllowFlight(false);
		p.setGameMode(GameMode.SURVIVAL);
		p.setLevel(0);
		p.setExp(0);
	}

	public void apply(Player p) {
		p.teleport(location);
		PlayerInventory i = p.getInventory();
		i.setContents(inventory[0]);
		i.setArmorContents(inventory[1]);
		p.setAllowFlight(allowFlight);
		p.setFlying(flying);
		p.setGameMode(gameMode);
		p.setLevel(level);
		p.setExp(exp);
	}
}

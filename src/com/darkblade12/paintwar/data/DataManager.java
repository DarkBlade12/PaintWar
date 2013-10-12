package com.darkblade12.paintwar.data;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.darkblade12.paintwar.PaintWar;
import com.darkblade12.paintwar.arena.Arena;
import com.darkblade12.paintwar.arena.region.Cuboid;
import com.darkblade12.paintwar.arena.util.PaintColor;
import com.darkblade12.paintwar.manager.Manager;

public class DataManager extends Manager {
	private Map<String, PlayerData> playerData;
	private Map<String, ArenaData> arenaData;

	public DataManager(PaintWar plugin) {
		super(plugin);
		initialize();
	}

	@Override
	public boolean initialize() {
		playerData = new HashMap<String, PlayerData>();
		arenaData = new HashMap<String, ArenaData>();
		return true;
	}

	@Override
	public void disable() {}

	public void createDataBackup(Player p) {
		playerData.put(p.getName(), PlayerData.get(p));
		PlayerData.clean(p);
	}

	public void restoreDataBackup(Player p) {
		String n = p.getName();
		if (playerData.containsKey(n)) {
			playerData.get(n).apply(p);
			playerData.remove(n);
		}
		arenaData.remove(n);
	}

	private ArenaData getArenaData(Player p) {
		String n = p.getName();
		return arenaData.containsKey(n) ? arenaData.get(n) : new ArenaData();
	}

	public void setBrushSize(Player p, int brushSize) {
		ArenaData a = getArenaData(p);
		a.setBrushSize(brushSize);
		arenaData.put(p.getName(), a);
	}

	public void setPaintColor(Player p, PaintColor paintColor) {
		ArenaData a = getArenaData(p);
		a.setPaintColor(paintColor);
		arenaData.put(p.getName(), a);
	}

	public void setReservedSpawn(Player p, String reservedSpawn) {
		ArenaData a = getArenaData(p);
		a.setReservedSpawn(reservedSpawn);
		arenaData.put(p.getName(), a);
	}

	public void setReady(Player p, boolean ready) {
		ArenaData a = getArenaData(p);
		a.setReady(ready);
		arenaData.put(p.getName(), a);
	}

	public void setBlockMovement(Player p, boolean blockMovement) {
		ArenaData a = getArenaData(p);
		a.setBlockMovement(blockMovement);
		arenaData.put(p.getName(), a);
	}

	public void setEmptyPaint(Player p, boolean emptyPaint) {
		ArenaData a = getArenaData(p);
		a.setEmptyPaint(emptyPaint);
		arenaData.put(p.getName(), a);
	}

	public void setEraser(Player p, boolean eraser) {
		ArenaData a = getArenaData(p);
		a.setEraser(eraser);
		arenaData.put(p.getName(), a);
	}

	public void setDashes(Player p, int dashes) {
		ArenaData a = getArenaData(p);
		a.setDashes(dashes);
		arenaData.put(p.getName(), a);
		p.setLevel(dashes);
	}

	public void setNoBorders(Player p, boolean noBorders) {
		ArenaData a = getArenaData(p);
		a.setNoBorders(noBorders);
		arenaData.put(p.getName(), a);
	}

	public void setSelectedArena(Player p, Arena selectedArena) {
		ArenaData a = getArenaData(p);
		a.setSelectedArena(selectedArena.getName());
		arenaData.put(p.getName(), a);
	}

	public void setPosition(Player p, Location position, boolean first) {
		ArenaData a = getArenaData(p);
		a.setPosition(position, first);
		arenaData.put(p.getName(), a);
	}

	public int getBrushSize(Player p) {
		return getArenaData(p).getBrushSize();
	}

	public PaintColor getPaintColor(Player p) {
		return getArenaData(p).getPaintColor();
	}

	public String getReservedSpawn(Player p) {
		return getArenaData(p).getReservedSpawn();
	}

	public boolean isReady(Player p) {
		return getArenaData(p).isReady();
	}

	public boolean getBlockMovement(Player p) {
		return getArenaData(p).getBlockMovement();
	}

	public boolean hasEmptyPaint(Player p) {
		return getArenaData(p).hasEmptyPaint();
	}

	public boolean isEraser(Player p) {
		return getArenaData(p).isEraser();
	}

	public int getDashes(Player p) {
		return getArenaData(p).getDashes();
	}

	public boolean hasNoBorders(Player p) {
		return getArenaData(p).hasNoBorders();
	}

	public Arena getSelectedArena(Player p) {
		return plugin.arena.getArena(getArenaData(p).getSelectedArena());
	}

	public Location getPosition(Player p, boolean first) {
		return getArenaData(p).getPosition(first);
	}

	public boolean isSelectionComplete(Player p) {
		return getPosition(p, true) != null && getPosition(p, false) != null;
	}

	public Cuboid getSelection(Player p) throws Exception {
		return new Cuboid(getPosition(p, true), getPosition(p, false));
	}

	public static boolean hasEmptyInventory(Player p) {
		PlayerInventory i = p.getInventory();
		for (ItemStack[] contents : new ItemStack[][] { i.getContents(), i.getArmorContents() })
			for (ItemStack is : contents)
				if (is != null && is.getType() != Material.AIR)
					return false;
		return true;
	}

	public static boolean hasEnoughSpace(Player p, ItemStack i) {
		int s = 0;
		for (ItemStack is : p.getInventory().getContents())
			if (is == null)
				s += 64;
			else if (is.isSimilar(i))
				s += 64 - is.getAmount();
		return s >= i.getAmount();
	}
}

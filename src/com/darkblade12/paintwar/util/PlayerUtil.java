package com.darkblade12.paintwar.util;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;

import com.darkblade12.paintwar.PaintWar;
import com.darkblade12.paintwar.arena.Arena;

public class PlayerUtil extends MetadataUtil {

	public PlayerUtil(Plugin plugin) {
		super(plugin);
	}

	public void saveState(Player p) {
		PlayerInventory i = p.getInventory();
		set(p, "Player_State", new Object[] { new ItemStack[][] { i.getContents(), i.getArmorContents() }, p.getGameMode().name(), p.getLocation() });
		p.setGameMode(GameMode.SURVIVAL);
		i.setArmorContents(new ItemStack[] { new ItemStack(0), new ItemStack(0), new ItemStack(0), new ItemStack(0) });
		i.clear();
	}

	public void restoreState(Player p) {
		if (!hasKey(p, "Player_State"))
			return;
		Object[] state = (Object[]) get(p, "Player_State", true);
		ItemStack[][] contents = (ItemStack[][]) state[0];
		PlayerInventory i = p.getInventory();
		i.setContents(contents[0]);
		i.setArmorContents(contents[1]);
		p.setGameMode(GameMode.valueOf((String) state[1]));
		p.teleport((Location) state[2]);
	}

	public void setBrushSize(Player p, int size) {
		set(p, "Brush_Size", size);
	}

	public int getBrushSize(Player p) {
		if (!hasKey(p, "Brush_Size"))
			return -1;
		return (Integer) get(p, "Brush_Size");
	}

	public void setTrailColor(Player p, byte color) {
		set(p, "Trail_Color", color);
	}

	public byte getTrailColor(Player p) {
		if (!hasKey(p, "Trail_Color"))
			return -1;
		return (Byte) get(p, "Trail_Color");
	}

	public void reserveSpawn(Player p, Location loc, String name) {
		p.teleport(loc);
		set(p, "Reserved_Spawn", name);
	}

	public String getReservedSpawn(Player p) {
		if (!hasKey(p, "Reserved_Spawn"))
			return null;
		return (String) get(p, "Reserved_Spawn");
	}

	public void markAsReady(Player p) {
		set(p, "Ready", true);
	}

	public boolean isReady(Player p) {
		return hasKey(p, "Ready");
	}

	public void blockMovement(Player p, boolean block) {
		if (block)
			set(p, "Block_Movement", true);
		else
			remove(p, "Block_Movement");
	}

	public boolean isMovementBlocked(Player p) {
		return hasKey(p, "Block_Movement");
	}

	public void setEmptyPaint(Player p, boolean empty) {
		if (empty)
			set(p, "Empty_Paint", true);
		else
			remove(p, "Empty_Paint");
	}

	public boolean hasEmptyPaint(Player p) {
		return hasKey(p, "Empty_Paint");
	}

	public void setEraser(Player p, boolean eraser) {
		if (eraser)
			set(p, "Eraser", true);
		else
			remove(p, "Eraser");
	}

	public boolean isEraser(Player p) {
		return hasKey(p, "Eraser");
	}

	public void setDashes(Player p, int dashes) {
		if (dashes != 0)
			set(p, "Dashes", dashes);
		else
			remove(p, "Dashes");
	}

	public int getDashes(Player p) {
		if (!hasKey(p, "Dashes"))
			return 0;
		return (Integer) get(p, "Dashes");
	}

	public void setNoBorders(Player p, boolean noBorders) {
		if (noBorders)
			set(p, "No_Borders", true);
		else
			remove(p, "No_Borders");
	}

	public boolean hasNoBorders(Player p) {
		return hasKey(p, "No_Borders");
	}

	public void clean(Player p) {
		removeAll(p, "Brush_Size", "Trail_Color", "Reserved_Spawn", "Ready", "Block_Movement", "Eraser", "Dashes");
		for (PotionEffect effect : p.getActivePotionEffects())
			p.removePotionEffect(effect.getType());
		p.setWalkSpeed(0.2F);
		restoreState(p);
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
		int space = 0;
		for (ItemStack is : p.getInventory().getContents())
			if (is == null)
				space += 64;
			else if (is.isSimilar(i))
				space += 64 - is.getAmount();
		return space >= i.getAmount();
	}

	public void selectArena(Player p, String name) {
		set(p, "Selected_Arena", name);
	}

	public Arena getSelectedArena(Player p) {
		if (!hasKey(p, "Selected_Arena"))
			return null;
		return ((PaintWar) plugin).arena.getArena((String) get(p, "Selected_Arena"));
	}

	public void selectPosition(Player p, Location position, boolean first) {
		set(p, (first ? "First" : "Second") + "_Position", position);
	}

	public Location getPosition(Player p, boolean first) {
		String index = (first ? "First" : "Second") + "_Position";
		if (!hasKey(p, index))
			return null;
		return (Location) get(p, index);
	}

	public boolean hasSelectedBothPositions(Player p) {
		return getPosition(p, true) != null && getPosition(p, false) != null;
	}
}

package com.darkblade12.paintwar.arena.region;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import com.darkblade12.paintwar.PaintWar;
import com.darkblade12.paintwar.arena.powerup.Powerup;
import com.darkblade12.paintwar.util.FireworkUtil;

public class Floor extends Cuboid {
	private PaintWar plugin;
	private static final Random rn = new Random();
	private List<Integer> ignoredIds;
	private List<Byte> immortalColors;
	private Map<Location, int[]> backup;

	public Floor(Location l1, Location l2, PaintWar plugin, List<Integer> ignoredIds) throws Exception {
		super(l1, l2);
		this.plugin = plugin;
		this.ignoredIds = ignoredIds;
		immortalColors = new ArrayList<Byte>();
	}

	public int getBlockCount() {
		int blocks = 0;
		for (Block b : getBlocks())
			if (!isIgnored(b))
				blocks++;
		return blocks;
	}

	public void createBackup() {
		backup = new HashMap<Location, int[]>();
		for (Block b : getBlocks())
			backup.put(b.getLocation(), new int[] { b.getTypeId(), b.getData() });
	}

	public void restoreBackup() {
		for (Entry<Location, int[]> e : backup.entrySet()) {
			int[] i = e.getValue();
			e.getKey().getBlock().setTypeIdAndData(i[0], (byte) i[1], false);
		}
		backup = null;
		removeItems();
	}

	private void removeItems() {
		List<Chunk> removed = new ArrayList<Chunk>();
		for (Block b : getBlocks()) {
			Location loc = b.getLocation();
			Chunk c = loc.add(0.0D, 1.0D, 0.0D).getChunk();
			if (removed.contains(c))
				continue;
			for (Entity e : c.getEntities())
				if (e instanceof Item)
					e.remove();
			removed.add(c);
		}
	}

	public void dropPowerup(Powerup pow) {
		World world = getWorld();
		Location randomLoc = new Location(world, rn.nextInt(x2 - x1 + 1) + x1, y2 + 2, rn.nextInt(z2 - z1 + 1) + z1);
		for (int e = 1; e <= 6; e++)
			world.playEffect(randomLoc, Effect.ENDER_SIGNAL, 0);
		FireworkUtil.createRandomFirework(randomLoc);
		world.dropItem(randomLoc, pow.getItem());
	}

	private boolean isIgnored(Block b) {
		return ignoredIds.contains(b.getTypeId()) ? true : isInside(b.getLocation()) ? b.getTypeId() == 35 ? immortalColors.contains(b.getData()) : false : true;
	}

	public Map<Byte, Integer> getWoolDataMap() {
		Map<Byte, Integer> map = new HashMap<Byte, Integer>();
		for (Block b : getBlocks()) {
			if (b.getTypeId() != 35)
				continue;
			byte data = b.getData();
			map.put(data, (map.containsKey(data) ? map.get(data) : 0) + 1);
		}
		return map;
	}

	public void createTrail(Player p) {
		if (plugin.player.hasEmptyPaint(p))
			return;
		Location loc = p.getLocation();
		createBlob(p, loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ(), plugin.player.getBrushSize(p));
	}

	public void createBlob(Player p, int cx, int cy, int cz, int radius) {
		if (radius <= 0)
			return;
		byte color = plugin.player.getTrailColor(p);
		boolean eraser = plugin.player.isEraser(p);
		World world = getWorld();
		int squared = radius * radius;
		for (int x = cx - radius; x <= cx + radius; x++) {
			for (int z = cz - radius; z <= cz + radius; z++) {
				if ((cx - x) * (cx - x) + (cz - z) * (cz - z) <= squared) {
					Block b = world.getBlockAt(new Location(world, x, cy, z));
					if (isIgnored(b))
						continue;
					else if (eraser) {
						int[] i = backup.get(b.getLocation());
						if (i != null)
							b.setTypeIdAndData(i[0], (byte) i[1], false);
					} else
						b.setTypeIdAndData(35, color, false);
				}
			}
		}
	}

	public void createTinyBlobs(Player p, int amount, int radius) {
		for (int i = 1; i <= amount; i++)
			createBlob(p, rn.nextInt(x2 - x1 + 1) + x1, y2, rn.nextInt(z2 - z1 + 1) + z1, radius);
	}

	public void setColorImmortal(byte color, boolean immortal) {
		if (immortal)
			immortalColors.add(color);
		else
			immortalColors.remove((Object) color);
	}
}

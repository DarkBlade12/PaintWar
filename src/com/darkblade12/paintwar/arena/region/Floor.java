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
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import com.darkblade12.paintwar.PaintWar;
import com.darkblade12.paintwar.arena.powerup.Powerup;
import com.darkblade12.paintwar.arena.util.PaintColor;
import com.darkblade12.paintwar.util.FireworkUtil;

// TODO look for alternative methods for the deprecated stuff ("setTypeIdAndData" and "getData")

@SuppressWarnings("deprecation")
public class Floor extends Cuboid {
	private PaintWar plugin;
	private static final Random RANDOM = new Random();
	private List<Material> ignoredMaterials;
	private List<PaintColor> immortalColors;
	private Map<Location, int[]> backup;

	public Floor(Location l1, Location l2, PaintWar plugin, List<Material> ignoredMaterials) throws Exception {
		super(l1, l2);
		this.plugin = plugin;
		this.ignoredMaterials = ignoredMaterials;
		immortalColors = new ArrayList<PaintColor>();
	}

	public void createBackup() {
		backup = new HashMap<Location, int[]>();
		for (Block b : this)
			backup.put(b.getLocation(), new int[] { b.getTypeId(), b.getData() });
	}

	public void restoreBackup() {
		if (backup == null || backup.size() == 0)
			throw new UnsupportedOperationException("Cannot restore an empty backup");
		for (Entry<Location, int[]> e : backup.entrySet()) {
			int[] i = e.getValue();
			e.getKey().getBlock().setTypeIdAndData(i[0], (byte) i[1], false);
		}
		backup = null;
		removeItems();
	}

	private void removeItems() {
		List<Chunk> removed = new ArrayList<Chunk>();
		for (Block b : this) {
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
		Location randomLoc = new Location(world, RANDOM.nextInt(x2 - x1 + 1) + x1, y2 + 2, RANDOM.nextInt(z2 - z1 + 1) + z1);
		for (int e = 1; e <= 6; e++)
			world.playEffect(randomLoc, Effect.ENDER_SIGNAL, 0);
		FireworkUtil.generateFirework(randomLoc);
		world.dropItem(randomLoc, pow.getItem());
	}

	private void colorBlock(Block b, PaintColor color) {
		if (!isIgnored(b))
			b.setTypeIdAndData(35, color.getCorrespondingData(), false);
	}

	private void eraseColor(Block b) {
		if (!isIgnored(b)) {
			int[] i = backup.get(b.getLocation());
			if (i != null)
				b.setTypeIdAndData(i[0], (byte) i[1], false);
		}
	}

	public void colorTrace(Player p) {
		if (plugin.data.hasEmptyPaint(p))
			return;
		Location loc = p.getLocation();
		colorCircle(p, loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ(), plugin.data.getBrushSize(p));
	}

	public void colorCircle(Player p, int cX, int cY, int cZ, int r) {
		if (r < 0)
			return;
		PaintColor color = plugin.data.getPaintColor(p);
		boolean eraser = plugin.data.isEraser(p);
		World world = getWorld();
		if (r == 0) {
			Block b = world.getBlockAt(cX, cY, cZ);
			if (eraser)
				eraseColor(b);
			else
				colorBlock(b, color);
			return;
		}
		int sqr = r * r;
		for (int x = cX - r; x <= cX + r; x++)
			for (int z = cZ - r; z <= cZ + r; z++)
				if ((cX - x) * (cX - x) + (cZ - z) * (cZ - z) <= sqr) {
					Block b = world.getBlockAt(x, cY, z);
					if (eraser)
						eraseColor(b);
					else
						colorBlock(b, color);
				}
	}

	public void colorMultipleCircles(Player p, int amount, int radius) {
		for (int i = 1; i <= amount; i++)
			colorCircle(p, RANDOM.nextInt(x2 - x1 + 1) + x1, y2, RANDOM.nextInt(z2 - z1 + 1) + z1, radius);
	}

	public void setImmortal(PaintColor color, boolean immortal) {
		if (immortal)
			immortalColors.add(color);
		else
			immortalColors.remove(color);
	}

	private boolean isIgnored(Block b) {
		return ignoredMaterials.contains(b.getType()) ? true : isInside(b.getLocation()) ? b.getType() == Material.WOOL ? immortalColors.contains(b.getData()) : false : true;
	}

	public Map<PaintColor, Integer> getColorMap() {
		Map<PaintColor, Integer> map = new HashMap<PaintColor, Integer>();
		for (Block b : this)
			if (b.getType() == Material.WOOL) {
				PaintColor color = PaintColor.fromCorrespondingData(b.getData());
				map.put(color, (map.containsKey(color) ? map.get(color) : 0) + 1);
			}
		return map;
	}

	@Override
	public int getVolume() {
		int blocks = 0;
		for (Block b : this)
			if (!isIgnored(b))
				blocks++;
		return blocks;
	}
}

package com.darkblade12.paintwar.arena.region;

import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public class Cuboid implements Iterable<Block> {
	protected int x1, y1, z1, x2, y2, z2;
	protected String worldName;

	public Cuboid(Location l1, Location l2) throws Exception {
		if (l1 == null || l2 == null)
			throw new NullPointerException("Location can't be null");
		else if (l1.getWorld() == null)
			throw new IllegalStateException("Can't create a Cuboid for an unloaded world");
		else if (!l1.getWorld().getName().equals(l2.getWorld().getName()))
			throw new IllegalStateException("Can't create a Cuboid between two different worlds");
		worldName = l1.getWorld().getName();
		x1 = Math.min(l1.getBlockX(), l2.getBlockX());
		y1 = Math.min(l1.getBlockY(), l2.getBlockY());
		z1 = Math.min(l1.getBlockZ(), l2.getBlockZ());
		x2 = Math.max(l1.getBlockX(), l2.getBlockX());
		y2 = Math.max(l1.getBlockY(), l2.getBlockY());
		z2 = Math.max(l1.getBlockZ(), l2.getBlockZ());
	}

	public boolean isInside(Location loc) {
		if (!loc.getWorld().getName().equals(worldName))
			return false;
		int x = loc.getBlockX();
		int y = loc.getBlockY();
		int z = loc.getBlockZ();
		if (x >= x1 && x <= x2)
			if (y >= y1 && y <= y2)
				if (z >= z1 && z <= z2)
					return true;
		return false;
	}

	public void setBlocks(Material mat) {
		for (Block b : this)
			b.setType(mat);
	}
	
	public boolean reachedHorizontalBorder(Location loc) {
		int x = loc.getBlockX();
		int z = loc.getBlockZ();
		return x == x1 || x == x2 || z == z1 || z == z2;
	}

	public Location getHorizontalMirrorLocation(Location loc) {
		int x = loc.getBlockX();
		int z = loc.getBlockZ();
		boolean xBorder = x == x1 || x == x2;
		boolean x1Border = xBorder ? x == x1 : false;
		boolean zBorder = z == z1 || z == z2;
		boolean z1Border = zBorder ? z == z1 : false;
		if (xBorder)
			loc.setX(x1Border ? x2 : x1 + 1);
		if (zBorder)
			loc.setZ(z1Border ? z2 : z1 + 1);
		return loc;

	}

	public int getVolume() {
		return ((x2 - x1) + 1) * ((y2 - y1) + 1) * ((z2 - z1) + 1);
	}

	public Location getLowerNE() {
		return new Location(this.getWorld(), this.x1, this.y1, this.z1);
	}

	public Location getUpperSW() {
		return new Location(this.getWorld(), this.x2, this.y2, this.z2);
	}

	public World getWorld() {
		World world = Bukkit.getWorld(worldName);
		if (world == null)
			throw new IllegalStateException("World '" + this.worldName + "' is not loaded");
		return world;
	}

	@Override
	public Iterator<Block> iterator() {
		return new CuboidIterator(getWorld(), x1, y1, z1, x2, y2, z2);
	}

	private class CuboidIterator implements Iterator<Block> {
		private World w;
		private int baseX, baseY, baseZ;
		private int x, y, z;
		private int sizeX, sizeY, sizeZ;

		public CuboidIterator(World w, int x1, int y1, int z1, int x2, int y2, int z2) {
			this.w = w;
			baseX = x1;
			baseY = y1;
			baseZ = z1;
			sizeX = Math.abs(x2 - x1) + 1;
			sizeY = Math.abs(y2 - y1) + 1;
			sizeZ = Math.abs(z2 - z1) + 1;
			x = this.y = this.z = 0;
		}

		@Override
		public boolean hasNext() {
			return x < sizeX && y < sizeY && z < sizeZ;
		}

		@Override
		public Block next() {
			Block b = this.w.getBlockAt(baseX + x, baseY + y, baseZ + z);
			if (++x >= sizeX) {
				x = 0;
				if (++y >= this.sizeY) {
					y = 0;
					++z;
				}
			}
			return b;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("This operation is not available on the type CuboidIterator");
		}
	}
}

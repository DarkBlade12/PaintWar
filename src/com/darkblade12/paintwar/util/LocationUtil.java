package com.darkblade12.paintwar.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class LocationUtil {

	public static String parse(Location loc) {
		return loc == null ? null : loc.getWorld().getName() + ", " + loc.getX() + ", " + loc.getY() + ", " + loc.getZ();
	}

	public static String parse(Location loc, boolean spawnLocation) {
		return loc == null ? null : parse(loc) + (spawnLocation ? ", " + loc.getYaw() + ", " + loc.getPitch() : "");
	}

	public static Location parse(String s) throws Exception {
		try {
			String[] sp = s.split(", ");
			return new Location(Bukkit.getWorld(sp[0]), Double.parseDouble(sp[1]), Double.parseDouble(sp[2]), Double.parseDouble(sp[3]));
		} catch (Exception e) {
			throw new Exception("Invalid location format");
		}
	}

	public static Location parse(String s, boolean spawnLocation) throws Exception {
		try {
			String[] sp = s.split(", ");
			if (spawnLocation)
				return new Location(Bukkit.getWorld(sp[0]), Double.parseDouble(sp[1]), Double.parseDouble(sp[2]), Double.parseDouble(sp[3]), Float.parseFloat(sp[4]), Float.parseFloat(sp[5]));
			else
				return new Location(Bukkit.getWorld(sp[0]), Double.parseDouble(sp[1]), Double.parseDouble(sp[2]), Double.parseDouble(sp[3]));
		} catch (Exception e) {
			throw new Exception("Invalid location format");
		}
	}

	public static boolean noDistance(Location loc1, Location loc2) {
		if (loc1 == null || loc2 == null || !loc1.getWorld().getName().equals(loc2.getWorld().getName()))
			return false;
		return loc1.distance(loc2) == 0;
	}

	public static void moveTowards(Entity from, Entity to, double multiplier) {
		Location l1 = from.getLocation();
		Location l2 = to.getLocation();
		double dX = l1.getX() - l2.getX();
		double dY = l1.getY() - l2.getY();
		double dZ = l1.getZ() - l2.getZ();
		double yaw = Math.atan2(dZ, dX);
		double pitch = Math.atan2(Math.sqrt(dZ * dZ + dX * dX), dY) + Math.PI;
		double x = Math.sin(pitch) * Math.cos(yaw);
		double y = Math.sin(pitch) * Math.sin(yaw);
		double z = Math.cos(pitch);
		from.setVelocity(new Vector(x, z, y).multiply(multiplier));
	}
}

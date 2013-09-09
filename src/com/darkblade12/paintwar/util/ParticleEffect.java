package com.darkblade12.paintwar.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public enum ParticleEffect {

	HUGE_EXPLOSION("hugeexplosion", 0),
	LARGE_EXPLODE("largeexplode", 1),
	FIREWORKS_SPARK("fireworksSpark", 2),
	BUBBLE("bubble", 3),
	SUSPEND("suspend", 4),
	DEPTH_SUSPEND("depthSuspend", 5),
	TOWN_AURA("townaura", 6),
	CRIT("crit", 7),
	MAGIC_CRIT("magicCrit", 8),
	MOB_SPELL("mobSpell", 9),
	MOB_SPELL_AMBIENT("mobSpellAmbient", 10),
	SPELL("spell", 11),
	INSTANT_SPELL("instantSpell", 12),
	WITCH_MAGIC("witchMagic", 13),
	NOTE("note", 14),
	PORTAL("portal", 15),
	ENCHANTMENT_TABLE("enchantmenttable", 16),
	EXPLODE("explode", 17),
	FLAME("flame", 18),
	LAVA("lava", 19),
	FOOTSTEP("footstep", 20),
	SPLASH("splash", 21),
	LARGE_SMOKE("largesmoke", 22),
	CLOUD("cloud", 23),
	RED_DUST("reddust", 24),
	SNOWBALL_POOF("snowballpoof", 25),
	DRIP_WATER("dripWater", 26),
	DRIP_LAVA("dripLava", 27),
	SNOW_SHOVEL("snowshovel", 28),
	SLIME("slime", 29),
	HEART("heart", 30),
	ANGRY_VILLAGER("angryVillager", 31),
	HAPPY_VILLAGER("happyVillager", 32);

	private String name;
	private int id;

	ParticleEffect(String name, int id) {
		this.name = name;
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public int getId() {
		return id;
	}

	private static final Map<String, ParticleEffect> NAME_MAP = new HashMap<String, ParticleEffect>();
	private static final Map<Integer, ParticleEffect> ID_MAP = new HashMap<Integer, ParticleEffect>();
	static {
		for (ParticleEffect effect : values()) {
			NAME_MAP.put(effect.name, effect);
			ID_MAP.put(effect.id, effect);
		}
	}

	public static ParticleEffect fromName(String name) {
		if (name == null)
			return null;
		for (Entry<String, ParticleEffect> e : NAME_MAP.entrySet())
			if (e.getKey().equalsIgnoreCase(name))
				return e.getValue();
		return null;
	}

	public static ParticleEffect fromId(int id) {
		return ID_MAP.get(id);
	}

	public void play(Player p, Location loc, float offsetX, float offsetY, float offsetZ, float speed, int amount) {
		sendPacket(p, createNormalPacket(this, loc, offsetX, offsetY, offsetZ, speed, amount));
	}

	public void play(Location loc, float offsetX, float offsetY, float offsetZ, float speed, int amount) {
		Object packet = createNormalPacket(this, loc, offsetX, offsetY, offsetZ, speed, amount);
		for (Player p : loc.getWorld().getPlayers())
			sendPacket(p, packet);
	}

	public void play(Location loc, double range, float offsetX, float offsetY, float offsetZ, float speed, int amount) {
		Object packet = createNormalPacket(this, loc, offsetX, offsetY, offsetZ, speed, amount);
		for (Player p : loc.getWorld().getPlayers())
			if (p.getLocation().distance(loc) <= range)
				sendPacket(p, packet);
	}

	public static void playTileCrack(Player p, Location loc, int id, byte data, float offsetX, float offsetY, float offsetZ, int amount) {
		sendPacket(p, createTileCrackPacket(id, data, loc, offsetX, offsetY, offsetZ, amount));
	}

	public static void playTileCrack(Location loc, int id, byte data, float offsetX, float offsetY, float offsetZ, int amount) {
		Object packet = createTileCrackPacket(id, data, loc, offsetX, offsetY, offsetZ, amount);
		for (Player p : loc.getWorld().getPlayers())
			sendPacket(p, packet);
	}

	public static void playTileCrack(Location loc, double range, int id, byte data, float offsetX, float offsetY, float offsetZ, int amount) {
		Object packet = createTileCrackPacket(id, data, loc, offsetX, offsetY, offsetZ, amount);
		for (Player p : loc.getWorld().getPlayers())
			if (p.getLocation().distance(loc) <= range)
				sendPacket(p, packet);
	}

	public static void playIconCrack(Player p, Location loc, int id, float offsetX, float offsetY, float offsetZ, int amount) {
		sendPacket(p, createIconCrackPacket(id, loc, offsetX, offsetY, offsetZ, amount));
	}
	
	public static void playIconCrack(Location loc, int id, float offsetX, float offsetY, float offsetZ, int amount) {
		Object packet = createIconCrackPacket(id, loc, offsetX, offsetY, offsetZ, amount);
		for (Player p : loc.getWorld().getPlayers())
			sendPacket(p, packet);
	}

	public static void playIconCrack(Location loc, double range, int id, float offsetX, float offsetY, float offsetZ, int amount) {
		Object packet = createIconCrackPacket(id, loc, offsetX, offsetY, offsetZ, amount);
		for (Player p : loc.getWorld().getPlayers())
			if (p.getLocation().distance(loc) <= range)
				sendPacket(p, packet);
	}

	private Object createNormalPacket(ParticleEffect effect, Location loc, float offsetX, float offsetY, float offsetZ, float speed, int amount) {
		return createPacket(effect.getName(), loc, offsetX, offsetY, offsetZ, speed, amount);
	}

	private static Object createTileCrackPacket(int id, byte data, Location loc, float offsetX, float offsetY, float offsetZ, int amount) {
		return createPacket("tilecrack_" + id + "_" + data, loc, offsetX, offsetY, offsetZ, 0.1F, amount);
	}

	private static Object createIconCrackPacket(int id, Location loc, float offsetX, float offsetY, float offsetZ, int amount) {
		return createPacket("iconcrack_" + id, loc, offsetX, offsetY, offsetZ, 0.1F, amount);
	}

	private static Object createPacket(String effectName, Location loc, float offsetX, float offsetY, float offsetZ, float speed, int amount) {
		if (amount <= 0)
			throw new IllegalArgumentException("Amount of particles has to be greater than 0!");
		try {
			Object packet = ReflectionUtil.getClass("Packet63WorldParticles");
			ReflectionUtil.setValue(packet, "a", effectName);
			ReflectionUtil.setValue(packet, "b", (float) loc.getX());
			ReflectionUtil.setValue(packet, "c", (float) loc.getY());
			ReflectionUtil.setValue(packet, "d", (float) loc.getZ());
			ReflectionUtil.setValue(packet, "e", offsetX);
			ReflectionUtil.setValue(packet, "f", offsetY);
			ReflectionUtil.setValue(packet, "g", offsetZ);
			ReflectionUtil.setValue(packet, "h", speed);
			ReflectionUtil.setValue(packet, "i", amount);
			return packet;
		} catch (Exception e) {
			Bukkit.getLogger().warning("[ParticleEffect] Failed to create a particle packet!");
			return null;
		}
	}

	private static void sendPacket(Player p, Object packet) {
		if (packet == null)
			return;
		try {
			Object entityPlayer = ReflectionUtil.getMethod("getHandle", p.getClass(), 0).invoke(p);
			Object playerConnection = entityPlayer.getClass().getField("playerConnection").get(entityPlayer);
			ReflectionUtil.getMethod("sendPacket", playerConnection.getClass(), 1).invoke(playerConnection, packet);
		} catch (Exception e) {
			Bukkit.getLogger().warning("[ParticleEffect] Failed to send a particle packet to " + p.getName() + "!");
		}
	}
}

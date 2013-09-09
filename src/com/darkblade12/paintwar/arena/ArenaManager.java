package com.darkblade12.paintwar.arena;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;

import com.darkblade12.paintwar.PaintWar;
import com.darkblade12.paintwar.arena.event.PlayerGetPowerupEvent;
import com.darkblade12.paintwar.arena.powerup.Powerup;
import com.darkblade12.paintwar.arena.region.Cuboid;
import com.darkblade12.paintwar.manager.Manager;
import com.darkblade12.paintwar.util.ParticleEffect;

public class ArenaManager extends Manager implements Listener {
	private List<Arena> arenas;
	AutoKickManager kick;

	public ArenaManager(PaintWar plugin) {
		super(plugin);
		initialize();
	}

	@Override
	public boolean initialize() {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		loadArenas();
		if (plugin.setting.AUTO_KICK_ENABLED)
			kick = new AutoKickManager(plugin);
		return true;
	}

	@Override
	public void disable() {
		HandlerList.unregisterAll(this);
		for (int i = 0; i < arenas.size(); i++)
			arenas.get(i).disable();
		kick.disable();
		plugin.l.info("ArenaManager has been disabled, all games have been stopped!");
	}

	public void loadArenas() {
		arenas = new ArrayList<Arena>();
		File directory = new File("plugins/PaintWar/arenas/");
		if (!directory.exists() && !directory.isDirectory())
			return;
		for (File f : directory.listFiles()) {
			String name = f.getName();
			if (name.contains(".yml")) {
				String arena = name.replace(".yml", "");
				try {
					arenas.add(new Arena(plugin, arena));
				} catch (Exception e) {
					plugin.l.warning("Failed to load arena '" + arena + "'! Reason: " + e.getMessage());
				}
			}
		}
		int amount = arenas.size();
		plugin.l.info(amount + " arena" + (amount == 1 ? "" : "s") + " loaded.");
	}

	public List<Arena> getArenas() {
		return this.arenas;
	}

	public void addArena(Arena a) {
		arenas.add(a);
	}

	public void removeArena(Arena a) {
		String name = a.getName();
		for (int i = 0; i < arenas.size(); i++) {
			if (arenas.get(i).getName().equals(name)) {
				arenas.remove(i);
				return;
			}
		}
	}

	public Arena getArena(String name) {
		for (int i = 0; i < arenas.size(); i++) {
			Arena a = arenas.get(i);
			if (a.getName().equalsIgnoreCase(name))
				return a;
		}
		return null;
	}

	public Arena getArena(Location loc) {
		for (int i = 0; i < arenas.size(); i++) {
			Arena a = arenas.get(i);
			if (!a.isInEditMode() && a.getProtection().isInside(loc))
				return a;
		}
		return null;
	}

	public void updateArena(Arena a) {
		String name = a.getName();
		for (int i = 0; i < arenas.size(); i++) {
			if (arenas.get(i).getName().equals(name)) {
				arenas.set(i, a);
				return;
			}
		}
	}

	public Arena getJoinedArena(Player p) {
		String name = p.getName();
		for (int i = 0; i < arenas.size(); i++) {
			Arena a = arenas.get(i);
			if (a.getPlayerNames().contains(name))
				return a;
		}
		return null;
	}

	public boolean hasJoinedArena(Player p) {
		return getJoinedArena(p) != null;
	}

	private void checkIllegalPlayerAction(Cancellable event, Player p, Location loc, String action) {
		Arena a = getArena(loc);
		if (a == null)
			return;
		if (!p.hasPermission("PaintWar.build." + a.getName()) || !p.hasPermission("PaintWar.*")) {
			event.setCancelled(true);
			p.sendMessage(plugin.message.arena_action_not_allowed(action));
		} else if (!a.isInEditMode()) {
			event.setCancelled(true);
			p.sendMessage(plugin.message.arena_not_in_edit_mode());
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerMove(PlayerMoveEvent event) {
		Player p = event.getPlayer();
		Arena a = getJoinedArena(p);
		Location f = event.getFrom();
		Location t = event.getTo();
		if (f.getX() == t.getX() && f.getY() == t.getY() && f.getZ() == t.getZ() || a == null)
			return;
		if (plugin.player.isMovementBlocked(p)) {
			if (f.getY() <= t.getY())
				p.teleport(f);
			return;
		} else if (a.getState() != State.NOT_JOINABLE)
			return;
		if (plugin.player.hasNoBorders(p)) {
			Cuboid floor = a.getFloor();
			if (floor.isAtHorizontalBorder(t))
				p.teleport(floor.getHorizontalMirrorLocation(t));
		}
		a.getFloor().createTrail(p);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onProjectileHit(ProjectileHitEvent event) {
		Projectile pr = event.getEntity();
		Entity s = pr.getShooter();
		if (!(pr instanceof Snowball) || !(s instanceof Player))
			return;
		Player p = (Player) s;
		Arena a = getJoinedArena(p);
		if (a == null)
			return;
		Location loc = pr.getLocation();
		loc.getWorld().playSound(loc, Sound.SLIME_WALK2, 1.0F, 5.0F);
		a.getFloor().createBlob(p, loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ(), a.getColorBombSize());
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
		Player p = event.getPlayer();
		int dashes = plugin.player.getDashes(p);
		if (dashes == 0)
			return;
		plugin.player.setDashes(p, dashes - 1);
		p.setVelocity(p.getLocation().getDirection().multiply(8.0D).setY(0.25D));
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		Player p = event.getPlayer();
		Arena a = getJoinedArena(p);
		Item i = event.getItem();
		Powerup pow = Powerup.fromIcon(i.getItemStack().getType());
		if (a == null || pow == null)
			return;
		event.setCancelled(true);
		PlayerGetPowerupEvent e = new PlayerGetPowerupEvent(p, a, pow);
		e.call();
		if (!e.isCancelled()) {
			Location loc = i.getLocation();
			ParticleEffect.RED_DUST.play(loc.add(0.0D, 1.5D, 0.0D), 0.3F, 0.3F, 0.3F, 0.0F, 22);
			loc.getWorld().playSound(loc, Sound.ORB_PICKUP, 1.0F, 5.0F);
			a.broadcastMessage(plugin.message.arena_powerup_x(true, p.getName(), pow.getName()));
			a.activatePowerup(p, pow);
			i.remove();
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player p = event.getPlayer();
		Arena a = getJoinedArena(p);
		if (a == null)
			return;
		a.handleLeave(p);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		Player p = event.getPlayer();
		Arena a = getJoinedArena(p);
		if (a == null || !event.getMessage().equalsIgnoreCase("ready"))
			return;
		event.setCancelled(true);
		if (a.getState() != State.JOINABLE) {
			p.sendMessage(plugin.message.arena_game_x_started(true));
		} else if (plugin.player.isReady(p)) {
			p.sendMessage(plugin.message.arena_already_ready());
		} else {
			plugin.player.markAsReady(p);
			p.sendMessage(plugin.message.arena_ready());
			a.broadcastMessage(plugin.message.arena_ready_other(p.getName(), a.getName()));
			a.checkCountdownStart();
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerFoodLevelChange(FoodLevelChangeEvent event) {
		Player p = (Player) event.getEntity();
		Arena a = getJoinedArena(p);
		if (a == null || !a.hasHungerDisabled())
			return;
		event.setCancelled(true);
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		Player p = event.getPlayer();
		if (!hasJoinedArena(p))
			return;
		event.setCancelled(true);
		p.updateInventory();
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDamage(EntityDamageEvent event) {
		Entity e = event.getEntity();
		if (!(e instanceof Player))
			return;
		Player p = (Player) e;
		if (!hasJoinedArena(p))
			return;
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
		Player p = event.getPlayer();
		Arena a = getJoinedArena(p);
		if (a == null)
			return;
		String cmd = event.getMessage().toLowerCase();
		if (a.isCommandAllowed(cmd.replace("/", "")))
			return;
		event.setCancelled(true);
		p.sendMessage(plugin.message.arena_action_not_allowed(plugin.message.getMessage("action_use_command").replace("<command>", cmd.split(" ")[0])));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player p = event.getPlayer();
		ItemStack h = p.getItemInHand();
		Action a = event.getAction();
		if (!h.isSimilar(PaintWar.getWand())) {
			/* Protection part start */
			if (h.getType() == Material.WATER_BUCKET || h.getType() == Material.LAVA_BUCKET)
				if (a == Action.RIGHT_CLICK_BLOCK)
					checkIllegalPlayerAction(event, event.getPlayer(), event.getClickedBlock().getLocation(), plugin.message.getMessage("place_liquids"));
			/* Protection part end */
			return;
		}
		event.setCancelled(true);
		if (a.name().contains("BLOCK")) {
			Location loc = event.getClickedBlock().getLocation();
			boolean first = a == Action.LEFT_CLICK_BLOCK;
			plugin.player.selectPosition(p, loc, first);
			p.sendMessage(plugin.message.player_position_set(p, first, loc));
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onBlockBreak(BlockBreakEvent event) {
		checkIllegalPlayerAction(event, event.getPlayer(), event.getBlock().getLocation(), plugin.message.getMessage("break_blocks"));
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onBlockPlace(BlockPlaceEvent event) {
		checkIllegalPlayerAction(event, event.getPlayer(), event.getBlock().getLocation(), plugin.message.getMessage("place_blocks"));
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onBlockIgnite(BlockIgniteEvent event) {
		checkIllegalPlayerAction(event, event.getPlayer(), event.getBlock().getLocation(), plugin.message.getMessage("ignite_blocks"));
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onEntityExplode(EntityExplodeEvent event) {
		List<Block> blocks = event.blockList();
		for (int i = 0; i < arenas.size(); i++) {
			Cuboid protection = arenas.get(i).getProtection();
			for (Block b : blocks) {
				if (protection.isInside(b.getLocation())) {
					event.setCancelled(true);
					return;
				}
			}
		}
	}
}

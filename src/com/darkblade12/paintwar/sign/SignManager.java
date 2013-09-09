package com.darkblade12.paintwar.sign;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.darkblade12.paintwar.PaintWar;
import com.darkblade12.paintwar.arena.Arena;
import com.darkblade12.paintwar.loader.ConfigLoader;
import com.darkblade12.paintwar.manager.SingleTaskManager;
import com.darkblade12.paintwar.util.LocationUtil;

public class SignManager extends SingleTaskManager implements Listener {
	private List<ArenaSign> signs;
	private ConfigLoader loader;
	public YamlConfiguration config;

	public SignManager(PaintWar plugin) {
		super(plugin);
	}

	@Override
	public boolean initialize() {
		loader = new ConfigLoader(plugin, "signs.yml");
		if (!loader.loadConfig()) {
			plugin.l.warning("Failed to load signs.yml. Plugin will disable!");
			return false;
		}
		config = loader.getConfig();
		plugin.l.info("signs.yml successfully loaded.");
		loadSigns();
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		scheduleRepeatingTask(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < signs.size(); i++)
					signs.get(i).updateSign();
			}
		}, 5, 5);
		return true;
	}

	@Override
	public void disable() {
		super.disable();
		HandlerList.unregisterAll(this);
	}

	public void loadSigns() {
		signs = new ArrayList<ArenaSign>();
		for (String sign : config.getKeys(false)) {
			try {
				String[] s = config.getString(sign + ".Placeholder_Positions").split(", ");
				signs.add(new ArenaSign(plugin, sign, LocationUtil.parse(config.getString(sign + ".Location")), config.getString(sign + ".Arena"), new int[] { Integer.parseInt(s[0]), Integer.parseInt(s[1]),
						Integer.parseInt(s[2]), Integer.parseInt(s[3]) }));
			} catch (Exception e) {
				plugin.l.warning("Failed to load arena sign '" + sign + "'! Reason: " + e.getMessage());
			}
		}
		int amount = signs.size();
		plugin.l.info(amount + " arena sign" + (amount == 1 ? "" : "s") + " loaded.");
	}
	
	public List<ArenaSign> getSigns() {
		return this.signs;
	}

	public void addSign(ArenaSign as) {
		signs.add(as);
	}

	public void removeSign(ArenaSign as) {
		String id = as.getId();
		for (int i = 0; i < signs.size(); i++) {
			if (signs.get(i).getId().equals(id)) {
				signs.remove(i);
				return;
			}
		}
	}

	public void updateSign(ArenaSign as) {
		String id = as.getId();
		for (int i = 0; i < signs.size(); i++) {
			if (signs.get(i).getId().equals(id)) {
				signs.set(i, as);
				return;
			}
		}
	}

	public ArenaSign getSign(Location loc) {
		for (int i = 0; i < signs.size(); i++) {
			ArenaSign as = signs.get(i);
			if (LocationUtil.noDistance(as.getLocation(), loc))
				return as;
		}
		return null;
	}

	public ArenaSign getSign(String id) {
		for (int i = 0; i < signs.size(); i++) {
			ArenaSign as = signs.get(i);
			if (id.equalsIgnoreCase(as.getId()))
				return as;
		}
		return null;
	}

	public String generateSignId() {
		List<Integer> usedNums = new ArrayList<Integer>();
		String part = plugin.setting.DEFAULT_SIGN_ID.replace("<num>", "");
		for (String sign : config.getKeys(false))
			try {
				usedNums.add(Integer.parseInt(sign.replace(part, "")));
			} catch (Exception e) {
				// custom names are ignored
			}
		int num = 1;
		while (usedNums.contains(num))
			num++;
		return plugin.setting.DEFAULT_SIGN_ID.replace("<num>", num + "");
	}

	public void saveConfig() {
		loader.saveConfig(config);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onSignChange(SignChangeEvent event) {
		final Player p = event.getPlayer();
		String[] lines = event.getLines();
		if (!lines[0].equalsIgnoreCase("[PaintWar]") && !lines[0].equalsIgnoreCase("PaintWar"))
			return;
		event.setLine(0, ArenaSign.HEADER);
		final Block b = event.getBlock();
		if (!p.hasPermission("PaintWar.sign.create") && !p.hasPermission("PaintWar.*") && !p.hasPermission("PaintWar.sign.*")) {
			b.breakNaturally();
			p.sendMessage(plugin.message.sign_no_x_permission(true));
			return;
		}
		Arena a = null;
		for (int i = 1; i < 4; i++) {
			String line = lines[i];
			if (line.equalsIgnoreCase("<players>") || line.equalsIgnoreCase("<player_amount>") || line.equalsIgnoreCase("<state>"))
				continue;
			else if (line.startsWith("#")) {
				event.setLine(i, ChatColor.translateAlternateColorCodes('&', line.replace("#", "")));
				continue;
			}
			if (a == null) {
				a = plugin.arena.getArena(line);
				if (a != null) {
					String name = "§6" + a.getName();
					event.setLine(i, name.length() > 16 ? name.substring(0, 16) : name);
				}
			} else {
				event.setLine(i, "");
			}
		}
		if (a == null) {
			b.breakNaturally();
			p.sendMessage(plugin.message.sign_no_arena_found());
			return;
		}
		final String name = a.getName();
		new BukkitRunnable() {
			@Override
			public void run() {
				ArenaSign as = new ArenaSign(plugin, b.getLocation(), name);
				p.sendMessage(plugin.message.sign_x(true, as.getId()));
			}
		}.runTaskLater(plugin, 1L);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player p = event.getPlayer();
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		ArenaSign as = getSign(event.getClickedBlock().getLocation());
		if (as == null)
			return;
		event.setCancelled(true);
		p.performCommand("pw join " + as.getArenaName());
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockBreak(BlockBreakEvent event) {
		Player p = event.getPlayer();
		ArenaSign as = getSign(event.getBlock().getLocation());
		if (as == null)
			return;
		else if (p.hasPermission("PaintWar.sign.break") || p.hasPermission("PaintWar.*") || p.hasPermission("PaintWar.sign.*")) {
			p.sendMessage(plugin.message.sign_x(false, as.getId()));
			return;
		}
		event.setCancelled(true);
		p.sendMessage(plugin.message.sign_no_x_permission(false));
	}
}

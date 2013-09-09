package com.darkblade12.paintwar.arena;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import com.darkblade12.paintwar.PaintWar;
import com.darkblade12.paintwar.arena.event.PlayerJoinArenaEvent;
import com.darkblade12.paintwar.arena.event.PlayerLeaveArenaEvent;
import com.darkblade12.paintwar.manager.SingleTaskManager;
import com.darkblade12.paintwar.util.LocationUtil;

public class AutoKickManager extends SingleTaskManager implements Listener {
	private List<String> players;
	private Map<String, Location> lastLocation;
	private Map<String, Integer> idleSeconds;

	public AutoKickManager(PaintWar plugin) {
		super(plugin);
		initialize();
	}

	@Override
	public boolean initialize() {
		players = new ArrayList<String>();
		lastLocation = new ConcurrentHashMap<String, Location>();
		idleSeconds = new ConcurrentHashMap<String, Integer>();
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		scheduleRepeatingTask(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < players.size(); i++) {
					String name = players.get(i);
					Player p = Bukkit.getPlayerExact(name);
					Location loc = p.getLocation();
					int s = idleSeconds.get(name);
					if (LocationUtil.noDistance(loc, lastLocation.get(name)) && !plugin.player.isMovementBlocked(p)) {
						s++;
						idleSeconds.put(name, s);
					} else {
						lastLocation.put(name, loc);
						idleSeconds.put(name, 0);
					}
					if (s == plugin.setting.AUTO_KICK_TIME) {
						Arena a = plugin.arena.getJoinedArena(p);
						a.handleLeave(p);
						a.broadcastMessage(plugin.message.player_kicked_other(name, "CONSOLE"));
					}
				}
			}
		}, 20, 20);
		return true;
	}

	@Override
	public void disable() {
		super.disable();
		HandlerList.unregisterAll(this);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoinArena(PlayerJoinArenaEvent event) {
		Player p = event.getPlayer();
		String name = p.getName();
		players.add(name);
		lastLocation.put(name, p.getLocation());
		idleSeconds.put(name, 0);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLeaveArena(PlayerLeaveArenaEvent event) {
		String name = event.getPlayer().getName();
		players.remove(name);
		lastLocation.remove(name);
		idleSeconds.remove(name);
	}
}

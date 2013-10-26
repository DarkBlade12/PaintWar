package com.darkblade12.paintwar.arena;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.darkblade12.paintwar.PaintWar;
import com.darkblade12.paintwar.arena.event.CountdownStartEvent;
import com.darkblade12.paintwar.arena.event.GameEndEvent;
import com.darkblade12.paintwar.arena.event.GameStartEvent;
import com.darkblade12.paintwar.arena.event.PlayerJoinArenaEvent;
import com.darkblade12.paintwar.arena.event.PlayerLeaveArenaEvent;
import com.darkblade12.paintwar.arena.event.PlayerWinGameEvent;
import com.darkblade12.paintwar.arena.powerup.Powerup;
import com.darkblade12.paintwar.arena.powerup.PowerupManager;
import com.darkblade12.paintwar.arena.region.Cuboid;
import com.darkblade12.paintwar.arena.region.Floor;
import com.darkblade12.paintwar.arena.util.PaintColor;
import com.darkblade12.paintwar.data.DataManager;
import com.darkblade12.paintwar.loader.ConfigLoader;
import com.darkblade12.paintwar.manager.MultipleTaskManager;
import com.darkblade12.paintwar.message.MessageManager;
import com.darkblade12.paintwar.stats.Stat;
import com.darkblade12.paintwar.util.LocationUtil;

// TODO look for alternative methods for the deprecated stuff ("getMaterial(int)" and "getData")

@SuppressWarnings("deprecation")
public class Arena extends MultipleTaskManager {
	private String name;
	private ConfigLoader loader;
	private YamlConfiguration config;
	private boolean editMode;
	private boolean requireEmptyInventory;
	private boolean hungerDisabled;
	private List<Material> ignoredMaterials;
	private boolean commandsDisabled;
	private String[] allowedCommands;
	private int gameDuration;
	private int countdownAmount;
	private int defaultBrushSize;
	private Mode mode;
	private boolean automaticStartEnabled;
	private int automaticStartPercent;
	private int automaticStartPlayerAmount;
	private boolean timeRemainingMessagesEnabled;
	private List<Integer> timeRemainingMessagesTimeSchedule;
	private boolean powerupsEnabled;
	private PowerupManager powerupManager;
	private boolean itemRewardsEnabled;
	private List<ItemStack> itemRewards;
	private boolean moneyRewardEnabled;
	private double moneyAmount;
	private Map<String, Location> spawns;
	private Cuboid protection;
	private Floor floor;
	private List<String> players;
	private State state;
	private int counter;

	public Arena(PaintWar plugin, String name) throws Exception {
		super(plugin);
		this.name = name;
		loader = new ConfigLoader(plugin, name + ".yml", true);
		if (!loader.loadConfig()) {
			loader.deleteConfig();
			throw new Exception("Failed to load '" + loader.getOuputFileName() + "'");
		}
		config = loader.getConfig();
		editMode = config.getBoolean("General_Settings.Edit_Mode");
		requireEmptyInventory = config.getBoolean("General_Settings.Require_Empty_Inventory");
		hungerDisabled = config.getBoolean("General_Settings.Hunger_Disabled");
		ignoredMaterials = new ArrayList<Material>();
		String ignoredBlockIdsString = config.getString("General_Settings.Ignored_Block_Ids");
		if (ignoredBlockIdsString != null)
			for (String block : ignoredBlockIdsString.split(", "))
				try {
					int id = Integer.parseInt(block);
					Material mat = Material.getMaterial(id);
					if (mat == null || !mat.isBlock())
						throw new Exception("Invalid block id '" + id + "' found in the list 'Ignored_Block_Ids'");
					ignoredMaterials.add(mat);
				} catch (Exception e) {
					throw new Exception("Invalid block id format '" + block + "' found in the list 'Ignored_Block_Ids'");
				}
		commandsDisabled = config.getBoolean("General_Settings.Command_Settings.Commands_Disabled");
		String allowedCommandsString = config.getString("General_Settings.Command_Settings.Allowed_Commands");
		allowedCommands = allowedCommandsString != null ? allowedCommandsString.split(", ") : new String[0];
		gameDuration = config.getInt("Game_Settings.Duration");
		if (gameDuration <= 0)
			throw new Exception("'Duration' value has to be greater than 0");
		countdownAmount = config.getInt("Game_Settings.Countdown");
		if (countdownAmount < 0)
			throw new Exception("'Countdown' value has to be positive");
		defaultBrushSize = config.getInt("Game_Settings.Default_Brush_Size");
		if (defaultBrushSize < 0)
			throw new Exception("'Default_Brush_Size' value has to be greater than 0");
		mode = Mode.fromName(config.getString("Game_Settings.Mode"));
		if (mode == null)
			throw new Exception("'Mode' value can't be null");
		automaticStartEnabled = config.getBoolean("Game_Settings.Automatic_Start.Enabled");
		if (automaticStartEnabled) {
			automaticStartPercent = config.getInt("Game_Settings.Automatic_Start.Percent");
			if (automaticStartPercent <= 0)
				throw new Exception("'Percent' value has to be greater than 0");
			automaticStartPlayerAmount = config.getInt("Game_Settings.Automatic_Start.Player_Amount");
			if (automaticStartPlayerAmount < 2)
				throw new Exception("'Player_Amount' value has to be greater than 1");
		}
		timeRemainingMessagesEnabled = config.getBoolean("Game_Settings.Time_Remaining_Messages.Enabled");
		if (timeRemainingMessagesEnabled) {
			timeRemainingMessagesTimeSchedule = new ArrayList<Integer>();
			String timeScheduleString = config.getString("Game_Settings.Time_Remaining_Messages.Time_Schedule");
			if (timeScheduleString != null)
				for (String num : timeScheduleString.split(", "))
					try {
						int time = Integer.parseInt(num);
						if (time < gameDuration)
							timeRemainingMessagesTimeSchedule.add(Integer.parseInt(num));
					} catch (Exception e) {
						// invalid number is ignored
					}
		}
		powerupsEnabled = config.getBoolean("Powerup_Settings.Powerups_Enabled");
		if (powerupsEnabled) {
			powerupManager = new PowerupManager(plugin, this);
			if (!powerupManager.initialize())
				throw new Exception("Failed to initialize the powerup manager (maybe there are no powerups or no time schedule!)");
		}
		itemRewardsEnabled = config.getBoolean("Reward_Settings.Item_Rewards.Enabled");
		itemRewards = new ArrayList<ItemStack>();
		if (itemRewardsEnabled)
			for (String item : config.getString("Reward_Settings.Item_Rewards.Items").split(", "))
				try {
					String[] s = item.split("-");
					int id = Integer.parseInt(s[0]);
					int amount = s.length >= 2 ? Integer.parseInt(s[1]) : 1;
					short durability = s.length == 3 ? Short.parseShort(s[2]) : 0;
					Material mat = Material.getMaterial(id);
					if (mat == null)
						throw new Exception("Invalid item id '" + id + "' found in the list 'Items'");
					itemRewards.add(new ItemStack(mat, amount, durability));
				} catch (Exception e) {
					throw new Exception("Invalid item format '" + item + "' found in the list 'Items'");
				}
		moneyRewardEnabled = config.getBoolean("Reward_Settings.Money_Reward.Enabled");
		if (moneyRewardEnabled) {
			moneyAmount = config.getDouble("Reward_Settings.Money_Reward.Money_Amount");
			if (moneyAmount < 0)
				throw new Exception("'Money_Amount' value has to be positive");
		}
		if (!moneyRewardEnabled || plugin.vault == null)
			moneyAmount = 0.0D;
		spawns = new HashMap<String, Location>();
		try {
			for (String spawn : config.getConfigurationSection("Spawns").getKeys(false))
				spawns.put(spawn, LocationUtil.parse(config.getString("Spawns." + spawn), true));
		} catch (Exception e) {
			if (!editMode)
				throw new Exception("Failed to load any spawns, there must be at least two");
		}
		if (!editMode && spawns.size() < 2)
			throw new Exception("Too few spawns, there must be at least two");
		try {
			protection = new Cuboid(LocationUtil.parse(config.getString("Bounds.Protection.p1")), LocationUtil.parse(config.getString("Bounds.Protection.p2")));
		} catch (Exception e) {
			if (!editMode)
				throw new Exception("Failed to load the protection area");
		}
		try {
			floor = new Floor(LocationUtil.parse(config.getString("Bounds.Floor.p1")), LocationUtil.parse(config.getString("Bounds.Floor.p2")), plugin, ignoredMaterials);
		} catch (Exception e) {
			if (!editMode)
				throw new Exception("Failed to load the protection area");
		}
		initialize();
	}

	@Override
	public boolean initialize() {
		players = new ArrayList<String>();
		state = State.JOINABLE;
		return true;
	}

	@Override
	public void disable() {
		broadcastMessage(plugin.message.arena_disabled());
		if (state != State.NOT_JOINABLE) {
			for (Player p : getPlayers())
				plugin.data.restoreDataBackup(p);
			if (state == State.COUNTING)
				this.cancelTasks();
		} else {
			stopGame(true);
		}
	}

	public static boolean create(PaintWar plugin, String name) {
		try {
			plugin.arena.addArena(new Arena(plugin, name));
			return true;
		} catch (Exception e) {
			plugin.l.warning(e.getMessage());
			return false;
		}
	}

	public void remove() {
		disable();
		loader.deleteConfig();
		plugin.arena.removeArena(this);
	}

	public void broadcastMessage(String message, Player... excepted) {
		List<String> exceptedNames = new ArrayList<String>();
		for (Player p : excepted)
			exceptedNames.add(p.getName());
		if (players.size() != exceptedNames.size())
			for (Player p : getPlayers())
				if (!exceptedNames.contains(p.getName()))
					p.sendMessage(message);
	}

	public void handleJoin(final Player p) {
		PlayerJoinArenaEvent e = new PlayerJoinArenaEvent(p, this);
		e.call();
		if (!e.isCancelled()) {
			String name = p.getName();
			String spawn = getRandomSpawn();
			players.add(name);
			plugin.data.createDataBackup(p);
			plugin.data.setReservedSpawn(p, spawn);
			plugin.data.setBlockMovement(p, true);
			p.teleport(spawns.get(spawn));
			String msg = plugin.message.arena_joined_other(name, this);
			if (plugin.setting.BROADCAST_PLAYER_JOIN)
				Bukkit.broadcastMessage(msg);
			else
				broadcastMessage(msg, p);
			initiateCountdown();
			update();
		}
	}

	public void handleLeave(final Player p) {
		PlayerLeaveArenaEvent e = new PlayerLeaveArenaEvent(p, this);
		e.call();
		if (!e.isCancelled()) {
			String name = p.getName();
			players.remove(name);
			String msg = plugin.message.arena_left_other(name, this);
			if (plugin.setting.BROADCAST_PLAYER_LEAVE)
				Bukkit.broadcastMessage(msg);
			else
				broadcastMessage(msg, p);
			if (state != State.NOT_JOINABLE) {
				plugin.data.restoreDataBackup(p);
				if (state == State.COUNTING && players.size() < 2) {
					cancelTasks();
					broadcastMessage(plugin.message.arena_countdown_stopped());
				}
			} else {
				plugin.data.restoreDataBackup(p);
				plugin.stats.add(name, Stat.LOST_GAMES, 1);
				if (players.size() < 2)
					stopGame(true);
			}
			new PlayerLeaveArenaEvent(p, this).call();
			update();
		}
	}

	private boolean isCountdownStartable() {
		int playerAmount = getPlayers().size();
		if (!automaticStartEnabled || state != State.JOINABLE || playerAmount < 2)
			return false;
		else if (playerAmount == automaticStartPlayerAmount || ((double) getReadyPlayers() / (double) playerAmount) * 100 >= automaticStartPercent)
			return true;
		return false;
	}

	public void initiateCountdown() {
		if (isCountdownStartable()) {
			CountdownStartEvent e = new CountdownStartEvent(this);
			e.call();
			if (!e.isCancelled())
				startCountdown();
		}
	}

	private void assignColors() {
		PaintColor[] colors = PaintColor.values().clone();
		int size = 16;
		for (Player p : getPlayers()) {
			int r = RANDOM.nextInt(size);
			plugin.data.setPaintColor(p, colors[r]);
			colors[r] = colors[size - 1];
			colors[size - 1] = null;
			size--;
		}
	}

	private void assignBrushSizes() {
		for (Player p : getPlayers())
			plugin.data.setBrushSize(p, defaultBrushSize);
	}

	public void startGame() {
		new GameStartEvent(this).call();
		floor.createBackup();
		assignColors();
		assignBrushSizes();
		for (Player p : getPlayers())
			plugin.data.setBlockMovement(p, false);
		if (powerupsEnabled)
			powerupManager.startTasks();
		scheduleTask(new Runnable() {
			@Override
			public void run() {
				stopGame(false);
			}
		}, gameDuration * 20);
		startMessageTasks();
		cancelTask(getTask(0)); // cancel scheduler task (it's the first task created)
		state = State.NOT_JOINABLE;
		update();
	}

	public void startCountdown() {
		state = State.COUNTING;
		counter = countdownAmount;
		scheduleRepeatingTask(new Runnable() {
			@Override
			public void run() {
				if (counter != 0) {
					broadcastMessage(plugin.message.arena_countdown(counter));
					counter--;
				} else {
					startGame();
					broadcastMessage(plugin.message.arena_countdown_go());
				}
			}
		}, 0, 20);
		update();
	}

	private void startMessageTasks() {
		for (final int seconds : timeRemainingMessagesTimeSchedule) {
			scheduleTask(new Runnable() {
				@Override
				public void run() {
					broadcastMessage(plugin.message.arena_time_remaining(gameDuration - seconds));
				}
			}, seconds * 20);
		}
	}

	public void activatePowerup(Player p, Powerup pow) {
		powerupManager.activatePowerup(p, pow);
	}

	public void stopGame(boolean forced) {
		new GameEndEvent(this).call();
		this.cancelTasks();
		String winnerName = "None";
		if (!forced) {
			Player w = getWinner();
			winnerName = w.getName();
			new PlayerWinGameEvent(w, this, itemRewards, moneyRewardEnabled ? moneyAmount : 0.0D).call();
			String msg = plugin.message.arena_player_won_game(winnerName, name);
			if (plugin.setting.BROADCAST_PLAYER_WIN_GAME)
				Bukkit.broadcastMessage(msg);
			else
				broadcastMessage(msg);
		} else {
			broadcastMessage(plugin.message.arena_forced_stop(name));
		}
		for (Player p : getPlayers()) {
			String name = p.getName();
			players.remove(name);
			plugin.stats.add(name, name.equals(winnerName) ? Stat.WON_GAMES : Stat.LOST_GAMES, 1);
			plugin.data.restoreDataBackup(p);
			new PlayerLeaveArenaEvent(p, this).call();
		}
		if (!winnerName.equals("None"))
			distributeRewards(Bukkit.getPlayerExact(winnerName));
		floor.restoreBackup();
		state = State.JOINABLE;
		update();
	}

	public void distributeRewards(Player p) {
		if (itemRewardsEnabled)
			for (ItemStack i : itemRewards)
				if (DataManager.hasEnoughSpace(p, i))
					p.getInventory().addItem(i);
				else
					p.getWorld().dropItemNaturally(p.getLocation(), i);
		if (moneyRewardEnabled && plugin.vault != null)
			plugin.vault.eco.depositPlayer(p.getName(), moneyAmount);
	}

	public void setFloor(Cuboid region) {
		config.set("Bounds.Floor.p1", LocationUtil.parse(region.getUpperSW()));
		config.set("Bounds.Floor.p2", LocationUtil.parse(region.getLowerNE()));
		loader.saveConfig(config);
		try {
			floor = new Floor(region.getUpperSW(), region.getLowerNE(), plugin, ignoredMaterials);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setProtection(Cuboid region) {
		config.set("Bounds.Protection.p1", LocationUtil.parse(region.getUpperSW()));
		config.set("Bounds.Protection.p2", LocationUtil.parse(region.getLowerNE()));
		loader.saveConfig(config);
		protection = region;
	}

	public void addSpawn(String name, Location loc) {
		config.set("Spawns." + name, LocationUtil.parse(loc, true));
		loader.saveConfig(config);
		spawns.put(name, loc);
	}

	public void deleteSpawn(String name) {
		config.set("Spawns." + name, null);
		loader.saveConfig(config);
		spawns.remove(name);
	}

	public void switchEditMode() {
		editMode = !editMode;
		config.set("General_Settings.Edit_Mode", editMode);
		loader.saveConfig(config);
	}

	@Override
	public void cancelTasks() {
		super.cancelTasks();
		powerupManager.cancelTasks();
		powerupManager.clearHandlers();
	}

	public void update() {
		plugin.arena.updateArena(this);
	}

	private Player getWinner() {
		Map<String, Integer> pointMap = new HashMap<String, Integer>();
		Map<PaintColor, String> colorMap = new HashMap<PaintColor, String>();
		for (Player p : getPlayers())
			colorMap.put(plugin.data.getPaintColor(p), p.getName());
		for (Entry<PaintColor, Integer> e : floor.getColorMap().entrySet()) {
			String name = colorMap.get(e.getKey());
			if (name != null)
				pointMap.put(colorMap.get(e.getKey()), e.getValue());
		}
		int highest = -1;
		String winnerName = "None";
		for (Entry<String, Integer> e : pointMap.entrySet()) {
			int points = e.getValue();
			if (points > highest) {
				highest = points;
				winnerName = e.getKey();
			}
		}
		StringBuilder builder = new StringBuilder();
		int blocks = floor.getVolume();
		for (Entry<String, Integer> e : pointMap.entrySet()) {
			String name = e.getKey();
			builder.append("\n§r " + MessageManager.coloredArrow() + " " + (name.equals(winnerName) ? "§e" : "§6") + "§l" + name + ": §8§l" + getPercentage(e.getValue(), blocks) + "%");
		}
		broadcastMessage(plugin.message.arena_game_overview(name, builder.toString()));
		return Bukkit.getPlayerExact(winnerName);
	}

	private int getPercentage(int points, int blocks) {
		double percentage = ((double) points / (double) blocks) * 100;
		return (int) percentage;
	}

	public String getPlayerDisplay() {
		int spawns = this.spawns.size();
		int players = this.players.size();
		return "§8{" + (spawns == players ? "§6" : players == 0 ? "§4" : "§a") + players + "§8/§6" + spawns + "§8}";
	}

	private List<String> getFreeSpawns() {
		List<String> usedSpawns = new ArrayList<String>();
		for (Player p : getPlayers())
			usedSpawns.add(plugin.data.getReservedSpawn(p));
		List<String> freeSpawns = new ArrayList<String>();
		for (String spawn : spawns.keySet())
			if (!usedSpawns.contains(spawn))
				freeSpawns.add(spawn);
		return freeSpawns;
	}

	private String getRandomSpawn() {
		List<String> freeSpawns = getFreeSpawns();
		return freeSpawns.get(RANDOM.nextInt(freeSpawns.size()));
	}

	public boolean isFull() {
		return getPlayerNames().size() == spawns.size();
	}

	public int getReadyPlayers() {
		int ready = 0;
		for (Player p : getPlayers())
			if (plugin.data.isReady(p))
				ready++;
		return ready;
	}

	public String getName() {
		return this.name;
	}

	public YamlConfiguration getConfig() {
		return this.config;
	}

	public boolean hasHungerDisabled() {
		return this.hungerDisabled;
	}

	public boolean isInEditMode() {
		return this.editMode;
	}

	public boolean requiresEmptyInventory() {
		return this.requireEmptyInventory;
	}

	public boolean isCommandAllowed(String cmd) {
		if (!commandsDisabled)
			return true;
		for (String acmd : allowedCommands)
			if (cmd.startsWith(acmd.replace("/", "").toLowerCase()))
				return true;
		return false;
	}

	public Mode getMode() {
		return this.mode;
	}

	public int getDefaultBrushSize() {
		return this.defaultBrushSize;
	}

	public int getColorBombSize() {
		return powerupManager.getColorBombSize();
	}

	public Floor getFloor() {
		return this.floor;
	}

	public State getState() {
		return this.state;
	}

	public List<Player> getPlayers() {
		List<Player> players = new ArrayList<Player>();
		for (int i = 0; i < this.players.size(); i++)
			players.add(Bukkit.getPlayerExact(this.players.get(i)));
		return players;
	}

	public List<String> getPlayerNames() {
		return this.players;
	}

	public Cuboid getProtection() {
		return this.protection;
	}

	public int getSpawnAmount() {
		return spawns.size();
	}

	public boolean hasSpawn(String name) {
		for (Entry<String, Location> e : spawns.entrySet())
			if (e.getKey().equalsIgnoreCase(name))
				return true;
		return false;
	}

	public boolean isReadyForUse() {
		return protection != null && floor != null && spawns.size() > 1;
	}

	public boolean isSetup() {
		return isReadyForUse() && !editMode;
	}
}

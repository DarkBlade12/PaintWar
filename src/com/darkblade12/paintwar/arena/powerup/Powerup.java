package com.darkblade12.paintwar.arena.powerup;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.darkblade12.paintwar.PaintWar;
import com.darkblade12.paintwar.arena.Arena;
import com.darkblade12.paintwar.util.LocationUtil;

public enum Powerup {
	BIG_BRUSH("Big_Brush", Material.GOLDEN_APPLE) {
		@Override
		public void activate(final PaintWar plugin, final Player p, final PowerupManager manager, final Arena a) {
			super.activate(plugin, p, manager, a);
			final String name = p.getName();
			int[] settings = manager.getSettings(this);
			plugin.player.setBrushSize(p, settings[0]);
			final Powerup pow = this;
			int task = manager.scheduleTask(new Runnable() {
				@Override
				public void run() {
					manager.removeTasks(name, pow);
					plugin.player.setBrushSize(p, a.getDefaultBrushSize());
					a.broadcastMessage(plugin.message.arena_powerup_x(false, name, getName()));
				}
			}, settings[1] * 20);
			manager.setTasks(name, this, new int[] { task, -1 });
		}
	},
	TINY_BRUSH("Tiny_Brush", Material.GOLD_NUGGET) {
		@Override
		public void activate(final PaintWar plugin, final Player p, final PowerupManager manager, final Arena a) {
			super.activate(plugin, p, manager, a);
			final String name = p.getName();
			int[] settings = manager.getSettings(this);
			for (Player ap : a.getPlayers())
				if (!ap.getName().equals(name))
					plugin.player.setBrushSize(ap, settings[0]);
			final Powerup pow = this;
			final int defaultSize = a.getDefaultBrushSize();
			int task = manager.scheduleTask(new Runnable() {
				@Override
				public void run() {
					manager.removeTasks(name, pow);
					for (Player ap : a.getPlayers())
						if (!ap.getName().equals(name) && plugin.player.getBrushSize(ap) < defaultSize)
							plugin.player.setBrushSize(ap, defaultSize);
					a.broadcastMessage(plugin.message.arena_powerup_x(false, name, getName()));
				}
			}, settings[1] * 20);
			manager.setTasks(name, this, new int[] { task, -1 });
		}
	},
	EMPTY_PAINT("Empty_Paint", Material.BUCKET) {
		@Override
		public void activate(final PaintWar plugin, final Player p, final PowerupManager manager, final Arena a) {
			super.activate(plugin, p, manager, a);
			final String name = p.getName();
			int[] settings = manager.getSettings(this);
			for (Player ap : a.getPlayers())
				if (!ap.getName().equals(name))
					plugin.player.setEmptyPaint(ap, true);
			final Powerup pow = this;
			int task = manager.scheduleTask(new Runnable() {
				@Override
				public void run() {
					manager.removeTasks(name, pow);
					for (Player ap : a.getPlayers())
						if (!ap.getName().equals(name))
							plugin.player.setEmptyPaint(ap, false);
					a.broadcastMessage(plugin.message.arena_powerup_x(false, name, getName()));
				}
			}, settings[0] * 20);
			manager.setTasks(name, this, new int[] { task, -1 });
		}
	},
	SPEED("Speed", Material.DIAMOND_BOOTS) {
		@Override
		public void activate(final PaintWar plugin, final Player p, final PowerupManager manager, final Arena a) {
			super.activate(plugin, p, manager, a);
			final String name = p.getName();
			int[] settings = manager.getSettings(this);
			final int duration = settings[1] * 20;
			p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, settings[0], true), true);
			final Powerup pow = this;
			int task = manager.scheduleTask(new Runnable() {
				@Override
				public void run() {
					manager.removeTasks(name, pow);
					a.broadcastMessage(plugin.message.arena_powerup_x(false, name, getName()));
				}
			}, duration);
			manager.setTasks(name, this, new int[] { task, -1 });
		}
	},
	FREEZE("Freeze", Material.ICE) {
		@Override
		public void activate(final PaintWar plugin, final Player p, final PowerupManager manager, final Arena a) {
			super.activate(plugin, p, manager, a);
			final String name = p.getName();
			int[] settings = manager.getSettings(this);
			for (Player ap : a.getPlayers())
				if (!ap.getName().equals(name))
					plugin.player.blockMovement(ap, true);
			final Powerup pow = this;
			int task = manager.scheduleTask(new Runnable() {
				@Override
				public void run() {
					manager.removeTasks(name, pow);
					for (Player ap : a.getPlayers())
						if (!ap.getName().equals(name))
							plugin.player.blockMovement(ap, false);
					a.broadcastMessage(plugin.message.arena_powerup_x(false, name, getName()));
				}
			}, settings[0] * 20);
			manager.setTasks(name, this, new int[] { task, -1 });
		}
	},
	BIG_BLOB("Big_Blob", Material.FIREWORK_CHARGE) {
		@Override
		public void activate(final PaintWar plugin, final Player p, final PowerupManager manager, final Arena a) {
			super.activate(plugin, p, manager, a);
			Location loc = p.getLocation();
			a.getFloor().createBlob(p, loc.getBlockX(), a.getFloor().getLowerNE().getBlockY(), loc.getBlockZ(), manager.getSettings(this)[0]);
		}
	},
	TINY_BLOBS("Tiny_Blobs", Material.PUMPKIN_SEEDS) {
		@Override
		public void activate(final PaintWar plugin, final Player p, final PowerupManager manager, final Arena a) {
			super.activate(plugin, p, manager, a);
			int[] settings = manager.getSettings(this);
			a.getFloor().createTinyBlobs(p, settings[1], settings[0]);
		}
	},
	ADVANCED_DARKNESS("Advanced_Darkness", Material.EYE_OF_ENDER) {
		@Override
		public void activate(final PaintWar plugin, final Player p, final PowerupManager manager, final Arena a) {
			super.activate(plugin, p, manager, a);
			final String name = p.getName();
			int[] settings = manager.getSettings(this);
			final int duration = settings[1] * 20;
			PotionEffect[] effects = new PotionEffect[] { new PotionEffect(PotionEffectType.BLINDNESS, duration, 1), new PotionEffect(PotionEffectType.NIGHT_VISION, duration, 1) };
			for (Player ap : a.getPlayers())
				if (!ap.getName().equals(name))
					for (PotionEffect effect : effects)
						ap.addPotionEffect(effect);
			final Powerup pow = this;
			int task = manager.scheduleTask(new Runnable() {
				@Override
				public void run() {
					manager.removeTasks(name, pow);
					a.broadcastMessage(plugin.message.arena_powerup_x(false, name, getName()));
				}
			}, duration);
			manager.setTasks(name, this, new int[] { task, -1 });
		}
	},
	DRUNKEN("Drunken", Material.POTION) {
		@Override
		public void activate(final PaintWar plugin, final Player p, final PowerupManager manager, final Arena a) {
			super.activate(plugin, p, manager, a);
			final String name = p.getName();
			int[] settings = manager.getSettings(this);
			final int duration = settings[1] * 20;
			PotionEffect effect = new PotionEffect(PotionEffectType.CONFUSION, duration, settings[0]);
			for (Player ap : a.getPlayers())
				if (!ap.getName().equals(name))
					ap.addPotionEffect(effect);
			final Powerup pow = this;
			int task = manager.scheduleTask(new Runnable() {
				@Override
				public void run() {
					manager.removeTasks(name, pow);
					a.broadcastMessage(plugin.message.arena_powerup_x(false, name, getName()));
				}
			}, duration);
			manager.setTasks(name, this, new int[] { task, -1 });
		}
	},
	SLOWNESS("Slowness", Material.COAL) {
		@Override
		public void activate(final PaintWar plugin, final Player p, final PowerupManager manager, final Arena a) {
			super.activate(plugin, p, manager, a);
			final String name = p.getName();
			int[] settings = manager.getSettings(this);
			final int duration = settings[1] * 20;
			PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, duration, settings[0]);
			for (Player ap : a.getPlayers())
				if (!ap.getName().equals(name))
					ap.addPotionEffect(effect);
			final Powerup pow = this;
			int task = manager.scheduleTask(new Runnable() {
				@Override
				public void run() {
					manager.removeTasks(name, pow);
					a.broadcastMessage(plugin.message.arena_powerup_x(false, name, getName()));
				}
			}, duration);
			manager.setTasks(name, this, new int[] { task, -1 });
		}
	},
	JUMPING("Jumping", Material.FEATHER) {
		@Override
		public void activate(final PaintWar plugin, final Player p, final PowerupManager manager, final Arena a) {
			super.activate(plugin, p, manager, a);
			final String name = p.getName();
			int[] settings = manager.getSettings(this);
			final int height = settings[0];
			final int jumpTask = manager.scheduleRepeatingTask(new Runnable() {
				@Override
				public void run() {
					for (Player ap : a.getPlayers())
						if (!ap.getName().equals(name))
							p.setVelocity(p.getVelocity().setY((height / 10.0D)));
				}
			}, 0, 40);
			final Powerup pow = this;
			int task = manager.scheduleTask(new Runnable() {
				@Override
				public void run() {
					manager.removeTasks(name, pow);
					manager.cancelTask(jumpTask);
					a.broadcastMessage(plugin.message.arena_powerup_x(false, name, getName()));
				}
			}, settings[1] * 20);
			manager.setTasks(name, this, new int[] { task, jumpTask });
		}
	},
	COLOR_BOMBS("Color_Bombs", Material.SLIME_BALL) {
		@Override
		public void activate(final PaintWar plugin, final Player p, final PowerupManager manager, final Arena a) {
			super.activate(plugin, p, manager, a);
			final String name = p.getName();
			int[] settings = manager.getSettings(this);
			p.getInventory().addItem(new ItemStack(Material.SNOW_BALL, settings[1]));
			final Powerup pow = this;
			int task = manager.scheduleTask(new Runnable() {
				@Override
				public void run() {
					manager.removeTasks(name, pow);
					a.broadcastMessage(plugin.message.arena_powerup_x(false, name, getName()));
				}
			}, settings[2] * 20);
			manager.setTasks(name, this, new int[] { task, -1 });
		}
	},
	IMMORTAL_COLOR("Immortal_Color", Material.BEDROCK) {
		@Override
		public void activate(final PaintWar plugin, final Player p, final PowerupManager manager, final Arena a) {
			super.activate(plugin, p, manager, a);
			final String name = p.getName();
			final byte color = plugin.player.getTrailColor(p);
			a.getFloor().setColorImmortal(color, true);
			final Powerup pow = this;
			int task = manager.scheduleTask(new Runnable() {
				@Override
				public void run() {
					manager.removeTasks(name, pow);
					a.getFloor().setColorImmortal(color, false);
					a.broadcastMessage(plugin.message.arena_powerup_x(false, name, getName()));
				}
			}, manager.getSettings(this)[0] * 20);
			manager.setTasks(name, this, new int[] { task, -1 });
		}
	},
	ERASER("Eraser", Material.EMPTY_MAP) {
		@Override
		public void activate(final PaintWar plugin, final Player p, final PowerupManager manager, final Arena a) {
			super.activate(plugin, p, manager, a);
			final String name = p.getName();
			plugin.player.setEraser(p, true);
			final Powerup pow = this;
			int task = manager.scheduleTask(new Runnable() {
				@Override
				public void run() {
					manager.removeTasks(name, pow);
					if (p.isOnline())
						plugin.player.setEraser(p, false);
					a.broadcastMessage(plugin.message.arena_powerup_x(false, name, getName()));
				}
			}, manager.getSettings(this)[0] * 20);
			manager.setTasks(name, this, new int[] { task, -1 });
		}
	},
	POWERUP_MAGNET("Powerup_Magnet", Material.COMPASS) {
		@Override
		public void activate(final PaintWar plugin, final Player p, final PowerupManager manager, final Arena a) {
			super.activate(plugin, p, manager, a);
			final String name = p.getName();
			int[] settings = manager.getSettings(this);
			final double range = (double) settings[0];
			final int magnetTask = manager.scheduleRepeatingTask(new Runnable() {
				@Override
				public void run() {
					for (Entity e : p.getNearbyEntities(range, range, range))
						if (e instanceof Item)
							if (a.getProtection().isInside(e.getLocation()))
								LocationUtil.moveTowards(e, p, 0.8D);
				}
			}, 10, 10);
			final Powerup pow = this;
			int task = manager.scheduleTask(new Runnable() {
				@Override
				public void run() {
					manager.removeTasks(name, pow);
					manager.cancelTask(magnetTask);
					a.broadcastMessage(plugin.message.arena_powerup_x(false, name, getName()));
				}
			}, settings[1] * 20);
			manager.setTasks(name, this, new int[] { task, magnetTask });
		}
	},
	DASH("Dash", Material.FIREWORK) {
		@Override
		public void activate(final PaintWar plugin, final Player p, final PowerupManager manager, final Arena a) {
			super.activate(plugin, p, manager, a);
			final String name = p.getName();
			int[] settings = manager.getSettings(this);
			plugin.player.setDashes(p, settings[0]);
			final Powerup pow = this;
			int task = manager.scheduleTask(new Runnable() {
				@Override
				public void run() {
					manager.removeTasks(name, pow);
					if (p.isOnline())
						plugin.player.setDashes(p, 0);
					a.broadcastMessage(plugin.message.arena_powerup_x(false, name, getName()));
				}
			}, settings[1] * 20);
			manager.setTasks(name, this, new int[] { task, -1 });
		}
	},
	NO_BORDERS("No_Borders", Material.DRAGON_EGG) {
		@Override
		public void activate(final PaintWar plugin, final Player p, final PowerupManager manager, final Arena a) {
			super.activate(plugin, p, manager, a);
			final String name = p.getName();
			plugin.player.setNoBorders(p, true);
			final Powerup pow = this;
			int task = manager.scheduleTask(new Runnable() {
				@Override
				public void run() {
					manager.removeTasks(name, pow);
					if (p.isOnline())
						plugin.player.setNoBorders(p, false);
					a.broadcastMessage(plugin.message.arena_powerup_x(false, name, getName()));
				}
			}, manager.getSettings(this)[0] * 20);
			manager.setTasks(name, this, new int[] { task, -1 });
		}
	};

	private String name;
	private ItemStack item;
	private static final Map<String, Powerup> NAME_MAP = new HashMap<String, Powerup>();

	static {
		for (Powerup pow : values())
			if (pow.name != null)
				NAME_MAP.put(pow.getName(), pow);
	}

	private Powerup(String name, Material icon) {
		this.name = name;
		item = new ItemStack(icon);
	}

	public String getName() {
		return this.name;
	}

	public ItemStack getItem() {
		return this.item;
	}

	public void activate(final PaintWar plugin, final Player p, final PowerupManager manager, final Arena a) {
		if (!manager.isEnabled(this))
			return;
	}

	public static Powerup fromName(String name) {
		if (name == null)
			return null;
		for (Entry<String, Powerup> e : NAME_MAP.entrySet())
			if (e.getKey().equalsIgnoreCase(name))
				return e.getValue();
		return null;
	}

	public static Powerup fromIcon(Material icon) {
		if (icon == null)
			return null;
		for (Powerup pow : NAME_MAP.values())
			if (pow.getItem().getType() == icon)
				return pow;
		return null;
	}
}

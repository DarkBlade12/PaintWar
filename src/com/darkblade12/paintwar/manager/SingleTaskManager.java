package com.darkblade12.paintwar.manager;

import com.darkblade12.paintwar.PaintWar;

public abstract class SingleTaskManager extends Manager {
	private int task;

	public SingleTaskManager(PaintWar plugin) {
		super(plugin);
		task = -1;
	}

	@Override
	public void disable() {
		cancelTask();
	}

	public void scheduleTask(Runnable runnable, long delay) {
		task = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, runnable, delay);
	}

	public void scheduleRepeatingTask(Runnable runnable, long startDelay, long delay) {
		task = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, runnable, startDelay, delay);
	}

	public void cancelTask() {
		if (task == -1)
			return;
		plugin.getServer().getScheduler().cancelTask(task);
		task = -1;
	}
}

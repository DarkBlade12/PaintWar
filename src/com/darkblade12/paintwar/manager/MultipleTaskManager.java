package com.darkblade12.paintwar.manager;

import java.util.ArrayList;
import java.util.List;

import com.darkblade12.paintwar.PaintWar;

public abstract class MultipleTaskManager extends Manager {
	private List<Integer> tasks;

	public MultipleTaskManager(PaintWar plugin) {
		super(plugin);
		tasks = new ArrayList<Integer>();
	}

	@Override
	public void disable() {
		cancelTasks();
	}

	public int scheduleTask(Runnable runnable, long delay) {
		int task = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, runnable, delay);
		tasks.add(task);
		return task;
	}

	public int scheduleRepeatingTask(Runnable runnable, long startDelay, long delay) {
		int task = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, runnable, startDelay, delay);
		tasks.add(task);
		return task;
	}

	public int getTask(int index) {
		return index >= tasks.size() ? -1 : tasks.get(index);
	}

	public void cancelTask(int task) {
		if (!tasks.contains(task))
			return;
		plugin.getServer().getScheduler().cancelTask(task);
		tasks.remove((Object) task);
	}

	public void cancelTasks() {
		for (int i = 0; i < tasks.size(); i++)
			cancelTask(tasks.get(i));
	}
}

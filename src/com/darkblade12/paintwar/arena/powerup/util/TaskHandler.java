package com.darkblade12.paintwar.arena.powerup.util;

import java.util.HashMap;
import java.util.Map;

import com.darkblade12.paintwar.arena.powerup.Powerup;
import com.darkblade12.paintwar.manager.MultipleTaskManager;

public class TaskHandler {
	private Map<Powerup, int[]> tasks;

	public TaskHandler(Powerup p, int... t) {
		tasks = new HashMap<Powerup, int[]>();
		tasks.put(p, t);
	}

	public void addTasks(MultipleTaskManager m, Powerup p, int... t) {
		if (!tasks.containsKey(p)) {
			tasks.put(p, t);
		} else {
			int[] ot = tasks.get(p);
			m.cancelTask(ot[0]);
			if (ot.length == 2)
				m.cancelTask(ot[1]);
			tasks.put(p, t);
		}
	}

	public void removeTasks(Powerup p) {
		tasks.remove(p);
	}

	public boolean hasTasks() {
		return tasks.size() > 0;
	}
}

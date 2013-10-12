package com.darkblade12.paintwar.manager;

import java.util.Random;

import com.darkblade12.paintwar.PaintWar;

public abstract class Manager {
	protected final static Random RANDOM = new Random();
	protected PaintWar plugin;

	public Manager(PaintWar plugin) {
		this.plugin = plugin;
	}

	public abstract boolean initialize();

	public abstract void disable();
}

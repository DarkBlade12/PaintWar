package com.darkblade12.paintwar;

import com.darkblade12.paintwar.arena.powerup.Powerup;
import com.darkblade12.paintwar.manager.Manager;

public class SettingManager extends Manager {
	public String LANGUAGE_NAME;
	public boolean BROADCAST_PLAYER_JOIN;
	public boolean BROADCAST_PLAYER_LEAVE;
	public boolean BROADCAST_PLAYER_WIN_GAME;
	public boolean AUTO_KICK_ENABLED;
	public int AUTO_KICK_TIME;
	public boolean BONUS_ENABLED;
	public int BONUS_DELAY;
	public Powerup BONUS_POWERUP;
	public String DEFAULT_SIGN_ID;

	public SettingManager(PaintWar plugin) {
		super(plugin);
		initialize();
	}

	@Override
	public boolean initialize() {
		loadProperties();
		return true;
	}

	@Override
	public void disable() {}

	public void loadProperties() {
		LANGUAGE_NAME = plugin.config.getString("General_Settings.Language_Name");
		if (LANGUAGE_NAME == null) {
			plugin.l.info("Setting language name to 'EN' because it was null!");
			LANGUAGE_NAME = "EN";
		}
		BROADCAST_PLAYER_JOIN = plugin.config.getBoolean("Arena_Settings.Broadcast_Settings.Player_Join");
		BROADCAST_PLAYER_LEAVE = plugin.config.getBoolean("Arena_Settings.Broadcast_Settings.Player_Leave");
		BROADCAST_PLAYER_WIN_GAME = plugin.config.getBoolean("Arena_Settings.Broadcast_Settings.Player_Win");
		AUTO_KICK_ENABLED = plugin.config.getBoolean("Arena_Settings.Auto_Kick.Enabled");
		AUTO_KICK_TIME = plugin.config.getInt("Arena_Settings.Auto_Kick.Time");
		BONUS_ENABLED = plugin.config.getBoolean("Arena_Settings.Bonus.Enabled");
		BONUS_DELAY = plugin.config.getInt("Arena_Settings.Bonus.Delay");
		if (BONUS_ENABLED && BONUS_DELAY < 0) {
			plugin.l.info("Setting bonus powerup delay to '10' because it was negative!");
			BONUS_DELAY = 10;
		}
		BONUS_POWERUP = Powerup.fromName(plugin.config.getString("Arena_Settings.Bonus.Powerup"));
		if (BONUS_ENABLED && BONUS_POWERUP == null) {
			plugin.l.info("Setting bonus powerup to 'Dash' because it was null!");
			BONUS_POWERUP = Powerup.DASH;
		}
		DEFAULT_SIGN_ID = plugin.config.getString("Arena_Sign_Settings.Default_Sign_Id");
	}
}

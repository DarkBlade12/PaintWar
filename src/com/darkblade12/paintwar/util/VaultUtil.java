package com.darkblade12.paintwar.util;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultUtil {
	public Economy eco;

	public VaultUtil() {
		eco = Bukkit.getServer().getServicesManager().getRegistration(Economy.class).getProvider();
	}

	public static boolean isVaultEconomyInstalled() {
		if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null)
			return false;
		RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
		return rsp != null && rsp.getProvider() != null;
	}
}

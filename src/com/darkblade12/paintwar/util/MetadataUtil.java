package com.darkblade12.paintwar.util;

import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.plugin.Plugin;

public class MetadataUtil {
	Plugin plugin;
	String pluginName;

	public MetadataUtil(Plugin plugin) {
		this.plugin = plugin;
		pluginName = plugin.getName();
	}

	public void set(Metadatable m, String key, Object value) {
		m.setMetadata(pluginName + "_" + key, new FixedMetadataValue(plugin, value));
	}

	public void remove(Metadatable m, String key) {
		m.removeMetadata(pluginName + "_" + key, plugin);
	}

	public boolean hasKey(Metadatable m, String key) {
		if (m.getMetadata(pluginName + "_" + key).size() == 0)
			return false;
		return true;
	}

	public Object get(Metadatable m, String key) {
		return m.getMetadata(pluginName + "_" + key).get(0).value();
	}

	public Object get(Metadatable m, String key, boolean remove) {
		Object o = get(m, key);
		if (remove)
			remove(m, key);
		return o;
	}

	public void removeAll(Metadatable m, String... keys) {
		for (String k : keys)
			remove(m, k);
	}

	public boolean hasKeys(Metadatable m, String... keys) {
		for (String k : keys)
			if (!hasKey(m, k))
				return false;
		return true;
	}
}

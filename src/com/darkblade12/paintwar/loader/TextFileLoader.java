package com.darkblade12.paintwar.loader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.bukkit.plugin.Plugin;

public class TextFileLoader extends FileLoader {

	public TextFileLoader(Plugin plugin, String fileName) {
		super(plugin, fileName, "plugins/" + plugin.getName() + "/");
	}

	public boolean loadFile() {
		return super.loadFile();
	}

	public boolean saveDefaultFile() {
		return super.saveResourceFile();
	}

	public BufferedReader getReader() throws Exception {
		return new BufferedReader(new InputStreamReader(new FileInputStream(outputFile), "UTF-8"));
	}
}

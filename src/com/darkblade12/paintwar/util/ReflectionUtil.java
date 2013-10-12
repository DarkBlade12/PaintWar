package com.darkblade12.paintwar.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;

public abstract class ReflectionUtil {

	public static Class<?> getClass(String name, String subPackage, boolean minecraftServer) throws Exception {
		return Class.forName(getPackageName(minecraftServer) + (subPackage.length() > 0 ? "." + subPackage : "") + "." + name);
	}

	public static Object getInstance(String className, Object... args) throws Exception {
		Class<?> c = Class.forName(ReflectionUtil.getPackageName(true) + "." + className);
		int params = args != null ? args.length : 0;
		for (Constructor<?> co : c.getConstructors())
			if (co.getParameterTypes().length == params)
				return co.newInstance(args);
		return null;
	}

	public static Method getMethod(String name, Class<?> clazz, int params) {
		for (Method m : clazz.getMethods())
			if (m.getName().equals(name) && m.getParameterTypes().length == params)
				return m;
		return null;
	}

	public static Method getMethod(String name, Class<?> clazz) {
		return getMethod(name, clazz, 0);
	}

	public static void setValue(Object instance, String fieldName, Object value) throws Exception {
		Field field = instance.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(instance, value);
	}

	public static Object getValue(Object instance, String fieldName) throws Exception {
		return instance.getClass().getField(fieldName).get(instance);
	}

	public static String getPackageName(boolean minecraftServer) {
		return minecraftServer ? "net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().substring(23, 30) : Bukkit.getServer().getClass().getPackage().getName();
	}
}

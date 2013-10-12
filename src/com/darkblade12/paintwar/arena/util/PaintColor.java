package com.darkblade12.paintwar.arena.util;

import java.util.HashMap;
import java.util.Map;

public enum PaintColor {
	WHITE((byte) 0),
	ORANGE((byte) 1),
	MAGENTA((byte) 2),
	LIGHT_BLUE((byte) 3),
	YELLOW((byte) 4),
	LIME((byte) 5),
	PINK((byte) 6),
	GRAY((byte) 7),
	LIGHT_GRAY((byte) 8),
	CYAN((byte) 9),
	PURPLE((byte) 10),
	BLUE((byte) 11),
	BROWN((byte) 12),
	GREEN((byte) 13),
	RED((byte) 14),
	BLACK((byte) 15);

	private byte correspondingData;
	private final static Map<Byte, PaintColor> CORRESPONDING_DATA_MAP = new HashMap<Byte, PaintColor>();

	static {
		for (PaintColor color : values())
			CORRESPONDING_DATA_MAP.put(color.getCorrespondingData(), color);
	}

	private PaintColor(byte correspondingData) {
		this.correspondingData = correspondingData;
	}

	public byte getCorrespondingData() {
		return this.correspondingData;
	}

	public static PaintColor fromCorrespondingData(byte data) {
		return CORRESPONDING_DATA_MAP.get(data);
	}
}

package com.darkblade12.paintwar.data;

import org.bukkit.Location;

import com.darkblade12.paintwar.arena.util.PaintColor;

public class ArenaData {
	private int brushSize;
	private PaintColor paintColor;
	private String reservedSpawn;
	private boolean ready;
	private boolean blockMovement;
	private boolean emptyPaint;
	private boolean eraser;
	private int dashes;
	private boolean noBorders;
	private String selectedArena;
	private Location[] positions;

	public ArenaData() {
		positions = new Location[2];
	}

	public void setBrushSize(int brushSize) {
		this.brushSize = brushSize;
	}

	public void setPaintColor(PaintColor paintColor) {
		this.paintColor = paintColor;
	}

	public void setReservedSpawn(String reservedSpawn) {
		this.reservedSpawn = reservedSpawn;
	}

	public void setReady(boolean ready) {
		this.ready = ready;
	}

	public void setBlockMovement(boolean blockMovement) {
		this.blockMovement = blockMovement;
	}

	public void setEmptyPaint(boolean emptyPaint) {
		this.emptyPaint = emptyPaint;
	}

	public void setEraser(boolean eraser) {
		this.eraser = eraser;
	}

	public void setDashes(int dashes) {
		this.dashes = dashes;
	}

	public void setNoBorders(boolean noBorders) {
		this.noBorders = noBorders;
	}

	public void setSelectedArena(String selectedArena) {
		this.selectedArena = selectedArena;
	}

	public void setPositions(Location[] positions) {
		this.positions = positions;
	}

	public void setPosition(Location position, boolean first) {
		positions[first ? 0 : 1] = position;
	}

	public int getBrushSize() {
		return this.brushSize;
	}

	public PaintColor getPaintColor() {
		return this.paintColor;
	}

	public String getReservedSpawn() {
		return this.reservedSpawn;
	}

	public boolean isReady() {
		return this.ready;
	}

	public boolean getBlockMovement() {
		return this.blockMovement;
	}

	public boolean hasEmptyPaint() {
		return this.emptyPaint;
	}

	public boolean isEraser() {
		return this.eraser;
	}

	public int getDashes() {
		return this.dashes;
	}

	public boolean hasNoBorders() {
		return this.noBorders;
	}

	public String getSelectedArena() {
		return this.selectedArena;
	}

	public Location[] getPositions() {
		return this.positions;
	}

	public Location getPosition(boolean first) {
		return positions[first ? 0 : 1];
	}
}

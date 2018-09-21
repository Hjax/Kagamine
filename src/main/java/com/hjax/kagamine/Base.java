package com.hjax.kagamine;

import java.util.ArrayList;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;

public class Base {
	public Point2d location;
	ArrayList<UnitInPool> gases;
	ArrayList<UnitInPool> minerals;
	
	UnitInPool walking_drone = null;
	UnitInPool queen = null;
	UnitInPool command_structure = null;
	
	public Base(Point2d l) {
		location = l;
		minerals =  new ArrayList<>();
		gases =  new ArrayList<>();
	}
	
	public void update() {
		minerals =  new ArrayList<>();
		gases =  new ArrayList<>();
		for (UnitInPool u: GameInfoCache.get_units(Alliance.NEUTRAL)) {
			if (u.unit().getPosition().toPoint2d().distance(location) < 10) {
				if (u.unit().getMineralContents().orElse(0) > 0) minerals.add(u);
				if (u.unit().getVespeneContents().orElse(0) > 0) gases.add(u);
			}
		}
		if (walking_drone != null) {
			if (!walking_drone.isAlive()) {
				walking_drone = null;
			}
		}
	}
	
	public void set_walking_drone(UnitInPool p) {
		walking_drone = p;
	}
	
	public void set_command_structure(UnitInPool p) {
		command_structure = p;
	}
	
	public void set_queen(UnitInPool p) {
		queen = p;
	}
	
	public boolean has_queen() {
		return queen != null;
	}
	
	public boolean has_command_structure() {
		return command_structure != null;
	}
	
	public boolean has_walking_drone() {
		return walking_drone != null;
	}
}

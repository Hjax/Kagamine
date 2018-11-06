package com.hjax.kagamine;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;

import java.util.ArrayList;

public class Base {
	public Point2d location;
	ArrayList<UnitInPool> gases;
	ArrayList<UnitInPool> minerals;
	
	public UnitInPool walking_drone = null;
	public UnitInPool queen = null;
	public UnitInPool command_structure = null;
	
	public Base(Point2d l) {
		location = l;
		minerals =  new ArrayList<>();
		gases =  new ArrayList<>();
	}
	
	public void update() {
		minerals =  new ArrayList<>();
		gases =  new ArrayList<>();
		if (has_walking_drone() && !walking_drone.isAlive()) walking_drone = null;
		if ((has_friendly_command_structure() || has_enemy_command_structure()) && !command_structure.isAlive()) command_structure = null;
		if (has_queen() && !queen.isAlive()) queen = null;
		if (has_friendly_command_structure() || has_enemy_command_structure()) {
			for (UnitInPool u: GameInfoCache.get_units(Alliance.NEUTRAL)) {
				if (u.unit().getPosition().toPoint2d().distance(location) < 10) {
					if (u.unit().getMineralContents().orElse(0) > 0) minerals.add(u);
					if (u.unit().getVespeneContents().orElse(0) > 0) gases.add(u);
				}
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
	
	public boolean has_friendly_command_structure() {
		return command_structure != null && command_structure.unit().getAlliance() == Alliance.SELF;
	}
	
	public boolean has_enemy_command_structure() {
		return command_structure != null && command_structure.unit().getAlliance() == Alliance.ENEMY;
	}
	
	public boolean has_command_structure() {
		return command_structure != null;
	}
	
	public boolean has_walking_drone() {
		return walking_drone != null;
	}
}

package com.hjax.kagamine.economy;

import java.util.ArrayList;

import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.Constants;
import com.hjax.kagamine.Vector2d;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.game.HjaxUnit;

public class Base {
	public Vector2d location;
	public ArrayList<HjaxUnit> gases;
	public ArrayList<HjaxUnit> minerals;
	
	public HjaxUnit walking_drone = null;
	public HjaxUnit queen = null;
	public HjaxUnit command_structure = null;
	
	public HjaxUnit ling = null;
	
	public long last_seen_frame = (long) (Constants.FPS * -60);
	
	public Base(Vector2d l) {
		location = l;
		minerals =  new ArrayList<>();
		gases =  new ArrayList<>();
	}
	
	public void update() {
		
		minerals =  new ArrayList<>();
		gases =  new ArrayList<>();
		if (has_walking_drone() && !walking_drone.alive()) walking_drone = null;
		if ((has_friendly_command_structure() || has_enemy_command_structure()) && !command_structure.alive()) command_structure = null;
		if (has_queen() && !queen.alive()) queen = null;
		if (has_friendly_command_structure() || has_enemy_command_structure()) {
			for (HjaxUnit u: GameInfoCache.get_units(Alliance.NEUTRAL)) {
				if (u.distance(location) < 10) {
					if (u.minerals() > 0) minerals.add(u);
					if (u.gas() > 0) gases.add(u);
				}
			}
		}
		
		if (Game.is_visible(location)) last_seen_frame = Game.get_frame();
	}
	
	public void set_walking_drone(HjaxUnit drone) {
		walking_drone = drone;
	}
	
	public void set_command_structure(HjaxUnit p) {
		command_structure = p;
	}
	
	public void set_queen(HjaxUnit p) {
		queen = p;
	}
	
	public boolean has_queen() {
		return queen != null;
	}
	
	public boolean has_friendly_command_structure() {
		return command_structure != null && command_structure.friendly();
	}
	
	public boolean has_enemy_command_structure() {
		return command_structure != null && !command_structure.friendly();
	}
	
	public boolean has_command_structure() {
		return command_structure != null;
	}
	
	public boolean has_walking_drone() {
		return walking_drone != null;
	}
	
	public boolean has_ling() {
		return ling != null && ling.alive();
	}
}

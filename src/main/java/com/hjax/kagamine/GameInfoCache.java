package com.hjax.kagamine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Ability;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.UnitTypeData;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Tag;
import com.github.ocraft.s2client.protocol.unit.UnitOrder;

public class GameInfoCache {
	
	static Map<Ability, Integer> production = new HashMap<>();
	static Map<Tag, UnitInPool> visible_units = new HashMap<>();
	static Map<Tag, UnitInPool> all_units = new HashMap<>();
	static Set<Tag> morphing_drones = new HashSet<>();
	
	static void start_frame() {
		for (Ability u: Game.get_abliity_data().keySet()) {
			production.put(u, 0);
		}
		
		morphing_drones.clear();
		visible_units.clear();
		
		for (UnitInPool u: Game.get_units()) {
			visible_units.put(u.getTag(), u);
		}
		
		for (UnitInPool u: Game.get_units()) {
			all_units.put(u.getTag(), u);
			if (u.isAlive()) {
				visible_units.put(u.getTag(),  u);
				if (u.unit().getBuildProgress() < Constants.DONE) {
					production.put(Game.get_unit_type_data().get(u.unit().getType()).getAbility().orElse(Abilities.INVALID), 
								   production.get(Game.get_unit_type_data().get(u.unit().getType()).getAbility().orElse(Abilities.INVALID)) + 1);
				}
				for (UnitOrder o: u.unit().getOrders()) {
					for (UnitTypeData t: Game.get_unit_type_data().values()) {
						if (o.getAbility() == t.getAbility().orElse(Abilities.INVALID)) {
							production.put(o.getAbility(), production.get(o.getAbility()) + 1);
							if (u.unit().getType() == Units.ZERG_DRONE) {
								morphing_drones.add(u.getTag());
							}
						}
					}
				}
				
			}
		}
	}
	static void on_frame() {}
	static void end_frame() {}
	
	public static int count_friendly(UnitType type) {
		int total = 0;
		for (UnitInPool unit: visible_units.values()) {
			if (unit.unit().getBuildProgress() < 0.999) continue;
			if (unit.unit().getType() == type) total++;
		}
		return total;
	}
	
	static int count_enemy(UnitType type) {
		int total = 0;
		for (UnitInPool unit: all_units.values()) {
			if (unit.isAlive() && unit.unit().getAlliance() == Alliance.ENEMY && unit.unit().getType() == type) total++;
		}
		return total;
	}
	
	static ArrayList<UnitInPool> get_units() {
		return new ArrayList<UnitInPool>(visible_units.values());
	}
	
	static ArrayList<UnitInPool> get_units(UnitType type) {
		ArrayList<UnitInPool> units = new ArrayList<>();
		for (UnitInPool u: visible_units.values()) {
			if (u.unit().getType() == type) units.add(u);
		}
		return units;
	}
	
	public static ArrayList<UnitInPool> get_units(Alliance team) {
		ArrayList<UnitInPool> units = new ArrayList<>();
		for (UnitInPool u: visible_units.values()) {
			if (u.unit().getAlliance() == team) units.add(u);
		}
		return units;
	}
	
	public static ArrayList<UnitInPool> get_units(Alliance team, UnitType type) {
		ArrayList<UnitInPool> units = new ArrayList<>();
		for (UnitInPool u: visible_units.values()) {
			if (u.unit().getAlliance() == team && u.unit().getType() == type) units.add(u);
		}
		return units;
	}
	
	// TODO add claimed geysers?
	public static boolean geyser_is_free(UnitInPool u) {
		for (UnitInPool e : get_units(Alliance.SELF, Units.ZERG_EXTRACTOR)) {
			if (e.unit().getPosition().toPoint2d().distance(u.unit().getPosition().toPoint2d()) < 1) {
				return false;
			}
		}
		return true;
	}
	
	public static int in_progress(UnitType t) {
		return production.get(Game.unit_type_data.get(t).getAbility().orElse(Abilities.INVALID));
	}
	
}

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
	static Map<Tag, UnitInPool> all_units = new HashMap<>();
	
	static Map<UnitType, Integer> counts_friendly = new HashMap<>();
	static Map<UnitType, Integer> counts_enemy = new HashMap<>();
	
	static Map<Tag, UnitInPool> visible_friendly = new HashMap<>();
	static Map<Tag, UnitInPool> visible_enemy = new HashMap<>();
	static Map<Tag, UnitInPool> visible_neutral = new HashMap<>();
	
	static Set<Tag> morphing_drones = new HashSet<>();
	
	static void start_frame() {
		for (Ability u: Game.get_abliity_data().keySet()) {
			production.put(u, 0);
		}
		
		morphing_drones.clear();
		visible_friendly.clear();
		visible_enemy.clear();
		
		counts_friendly.clear();
		counts_enemy.clear();
		visible_neutral.clear();
		
		for (UnitInPool u: Game.get_units()) {
			all_units.put(u.getTag(), u);
	
			
			if (u.isAlive()) {
				if (u.unit().getAlliance() == Alliance.SELF) {
					if (u.unit().getBuildProgress() > 0.999) {
						counts_friendly.put(u.unit().getType(), counts_friendly.getOrDefault(u.unit().getType(), 0) + 1);
					}
					visible_friendly.put(u.getTag(), u);
				} else if (u.unit().getAlliance() == Alliance.ENEMY) {
					counts_enemy.put(u.unit().getType(), counts_enemy.getOrDefault(u.unit().getType(), 0) + 1);
					visible_enemy.put(u.getTag(), u);
				} else {
					visible_neutral.put(u.getTag(), u);
				}
				if (u.unit().getBuildProgress() < Constants.DONE) {
					production.put(Game.get_unit_type_data().get(u.unit().getType()).getAbility().orElse(Abilities.INVALID), 
					production.get(Game.get_unit_type_data().get(u.unit().getType()).getAbility().orElse(Abilities.INVALID)) + 1);
				}
				for (UnitOrder o: u.unit().getOrders()) {
					production.put(o.getAbility(), production.get(o.getAbility()) + 1);
					if (u.unit().getType() == Units.ZERG_DRONE) {
						if (o.getAbility() != Abilities.HARVEST_GATHER && o.getAbility() != Abilities.HARVEST_RETURN) {
							for (UnitTypeData t: Game.get_unit_type_data().values()) {
								if (o.getAbility() == t.getAbility().orElse(Abilities.INVALID)) {
									morphing_drones.add(u.getTag());
									break;
								}
							}
							break;
						}
					}
				}
			}

		}
	}
	static void on_frame() {}
	static void end_frame() {}
	
	public static int count_friendly(UnitType type) {
		return counts_friendly.getOrDefault(type, 0);
	}
	
	static int count_enemy(UnitType type) {
		return counts_enemy.getOrDefault(type, 0);
	}
	
	static ArrayList<UnitInPool> get_units(UnitType type) {
		ArrayList<UnitInPool> units = new ArrayList<>();
		for (UnitInPool u: Game.get_units()) {
			if (u.unit().getType() == type) units.add(u);
		}
		return units;
	}
	
	public static ArrayList<UnitInPool> get_units(Alliance team) {
		if (team == Alliance.SELF) return new ArrayList<>(visible_friendly.values());
		if (team == Alliance.NEUTRAL) return new ArrayList<>(visible_neutral.values());
		return new ArrayList<>(visible_enemy.values());
	}
	
	public static ArrayList<UnitInPool> get_units(Alliance team, UnitType type) {
		ArrayList<UnitInPool> units = new ArrayList<>();
		if (team == Alliance.SELF) {
			for (UnitInPool u: visible_friendly.values()) {
				if (u.unit().getType() == type) units.add(u);
			}
		} else if (team == Alliance.ENEMY){
			for (UnitInPool u: visible_enemy.values()) {
				if (u.unit().getType() == type) units.add(u);
			}
		} else {
			for (UnitInPool u: visible_neutral.values()) {
				if (u.unit().getType() == type) units.add(u);
			}
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
		return production.get(Game.get_unit_type_data().get(t).getAbility().orElse(Abilities.INVALID));
	}
	
}

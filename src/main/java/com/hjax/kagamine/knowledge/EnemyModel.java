package com.hjax.kagamine.knowledge;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.DisplayType;
import com.github.ocraft.s2client.protocol.unit.Tag;
import com.hjax.kagamine.Constants;
import com.hjax.kagamine.economy.Base;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;

public class EnemyModel {
	public static Map<Tag, UnitType> registered = new HashMap<>();
	public static Map<UnitType, Integer> counts = new HashMap<>();
	public static Map<UnitType, Integer> inferred = new HashMap<>();
	public static Set<Tag> halluc = new HashSet<>();
	public static void on_frame() {
		for (UnitInPool u: GameInfoCache.get_units(Alliance.ENEMY)) {
			if (u.unit().getHallucination().orElse(false)) {
				halluc.add(u.getTag());
				if (registered.containsKey(u.getTag())) {
					removeFromModel(u);
				}
			}
			
			Set<Tag> to_remove = new HashSet<>();
			for (Tag t: registered.keySet()) {
				if (Game.get_frame() - GameInfoCache.all_units.get(t).getLastSeenGameLoop() > 120 * Constants.FPS) {
					to_remove.add(t);
				}
			}
			
			for (Tag t: to_remove) {
				removeFromModel(GameInfoCache.all_units.get(t));
			}
			
			if (!u.unit().getHallucination().orElse(false) && !halluc.contains(u.getTag())) {
				if (u.unit().getDisplayType() != DisplayType.SNAPSHOT && !registered.containsKey(u.getTag())) {
					registered.put(u.getTag(), u.unit().getType());
					if (counts.getOrDefault(u.unit().getType(), 0) == 0) {
						if (Game.is_structure(u.unit().getType())) {
							update(u.unit().getType());
						} 
						else {
							update(Balance.get_tech_structure(u.unit().getType()));
							UnitType best = Units.INVALID;
							for (UnitType ut: Balance.get_production_structures(u.unit().getType())) {
								if (best == Units.INVALID || Game.get_unit_type_data().get(best).getMineralCost().orElse(0) + Game.get_unit_type_data().get(best).getVespeneCost().orElse(0) > Game.get_unit_type_data().get(ut).getMineralCost().orElse(0) + Game.get_unit_type_data().get(ut).getVespeneCost().orElse(0)) {
									best = ut;
								}
							}
							update(best);
						}
					}
					
					inferred.put(u.unit().getType(), Math.max(inferred.getOrDefault(u.unit().getType(), 0) - 1, 0));
					counts.put(u.unit().getType(), counts.getOrDefault(u.unit().getType(), 0) + 1);
				}
				if (u.unit().getDisplayType() != DisplayType.SNAPSHOT && u.unit().getType() != registered.get(u.getTag())) {
					counts.put(registered.get(u.getTag()), counts.getOrDefault(registered.get(u.getTag()), 0) - 1);
					counts.put(u.unit().getType(), counts.getOrDefault(u.unit().getType(), 0) + 1);
					registered.put(u.getTag(), u.unit().getType());
				}
			}
		}
	}
	
	public static void removeFromModel(UnitInPool u) {
		counts.put(u.unit().getType(), counts.getOrDefault(u.unit().getType(), 1) - 1);
		registered.remove(u.unit().getTag());
	}
	
	public static void update(UnitType u) {
		if (u == Units.INVALID) return;
		if (counts.getOrDefault(u, 0) == 0 && inferred.getOrDefault(u, 0) == 0) {
			inferred.put(u, inferred.getOrDefault(u, 0) + 1);
		}
		if (Game.get_unit_type_data().get(u).getTechRequirement().isPresent()) {
			if (Game.get_unit_type_data().get(u).getTechRequirement().get() != Units.INVALID) {
				update(Game.get_unit_type_data().get(u).getTechRequirement().get());
			}
		}
	}
	
	public static int[] resourcesSpent() {
		int[] res = new int[2];
		for (UnitType u : counts.keySet()) {
			res[0] += Game.get_unit_type_data().get(u).getMineralCost().orElse(0) * counts.get(u);
			res[1] += Game.get_unit_type_data().get(u).getVespeneCost().orElse(0) * counts.get(u);
		}
		for (UnitType u : inferred.keySet()) {
			res[0] += Game.get_unit_type_data().get(u).getMineralCost().orElse(0) * inferred.get(u);
			res[1] += Game.get_unit_type_data().get(u).getVespeneCost().orElse(0) * inferred.get(u);
		}
		res[0] -= 1000; // take into account free stuff in spending
		return res;
	}
	
	public static int[] resourceEstimate() {
		int[] res = resourcesSpent();
		res[0] = ResourceTracking.estimate_enemy_minerals() - res[0];
		res[0] -= Game.minerals_killed();
		res[1] = ResourceTracking.estimate_enemy_gas() - res[1];
		res[1] -= Game.gas_killed();
		return res;
	}
	
	public static float enemySupply() {
		float result = 0;
		for (UnitType ut: counts.keySet()) {
			result += Game.supply(ut) * counts.get(ut);
		}
		return result;
	}
	
	public static float enemyArmy() {
		float result = 0;
		for (UnitType ut: counts.keySet()) {
			if (!Game.is_worker(ut) && !(ut == Units.ZERG_QUEEN)) {
				result += Game.supply(ut) * counts.get(ut);
			}
		}
		return result;
	}
	
	public static int enemyWorkers() {
		return counts.getOrDefault(Units.TERRAN_SCV, 0) + counts.getOrDefault(Units.PROTOSS_PROBE, 0) + counts.getOrDefault(Units.ZERG_DRONE, 0);
	}
	
	public static int enemyBaseCount() {
		int result = 1;
		for (Base b : BaseManager.bases) {
			if (b.has_command_structure() && b.location.distance(Scouting.closest_enemy_spawn()) < 5) {
				result -= 1;
			}
		}
		for (UnitInPool u: GameInfoCache.get_units(Alliance.ENEMY)) {
			if (Game.is_town_hall(u.unit().getType())) result++;
		}
		return result;
	}

}
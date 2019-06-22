package com.hjax.kagamine.knowledge;

import java.util.HashMap;
import java.util.Map;

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
	public static void on_frame() {
		for (UnitInPool u: GameInfoCache.get_units(Alliance.ENEMY)) {
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
	
	public static int[] resourcesAlive() {
		int[] res = resourcesSpent();
		res[0] -= Game.minerals_killed();
		res[0] += 1000; // free stuff can still be alive
		res[1] -= Game.gas_killed();
		return res;
	}
	
	public static int[] resourceEstimate() {
		int[] res = resourcesSpent();
		res[0] = ResourceTracking.estimate_enemy_minerals() - res[0];
		res[1] = ResourceTracking.estimate_enemy_gas() - res[1];
		return res;
	}
	
	public static float enemySupply() {
		float result = 0;
		for (UnitType ut: counts.keySet()) {
			result += Game.get_unit_type_data().get(ut).getFoodRequired().orElse(0.0f) * counts.get(ut);
		}
		return result;
	}
	
	public static float enemyArmy() {
		float result = 0;
		for (UnitType ut: counts.keySet()) {
			if (!Game.is_worker(ut)) {
				result += Game.get_unit_type_data().get(ut).getFoodRequired().orElse(0.0f) * counts.get(ut);
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
	
	public static void printStats()  {
		int[] alive = resourcesAlive();
		int[] spent = resourcesSpent();
		Game.chat("You have " + Integer.toString(alive[0]) + " minerals and " + Integer.toString(alive[1]) + " gas in living units");
		Game.chat("You have " + Integer.toString(ResourceTracking.estimate_enemy_minerals()) + " minerals and " + Integer.toString(ResourceTracking.estimate_enemy_gas()) + " gas mined");
		Game.chat("You have lost " + Integer.toString(Game.minerals_killed()) + " minerals and " + Integer.toString(Game.gas_killed()) + " gas");
		Game.chat("You have " + Integer.toString(ResourceTracking.estimate_enemy_minerals() - spent[0]) + " minerals and " + Integer.toString(ResourceTracking.estimate_enemy_gas() - alive[1]) + " gas unaccounted for");
	}
}
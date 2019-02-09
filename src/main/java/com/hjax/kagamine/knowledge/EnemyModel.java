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
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;

public class EnemyModel {
	public static Set<Tag> registered = new HashSet<>();
	public static Map<UnitType, Integer> counts = new HashMap<>();
	public static Map<UnitType, Integer> inferred = new HashMap<>();
	public static void on_frame() {
		for (UnitInPool u: GameInfoCache.get_units(Alliance.ENEMY)) {
			if (u.unit().getDisplayType() != DisplayType.SNAPSHOT && !registered.contains(u.getTag())) {
				registered.add(u.getTag());
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
		}
		
		//if (Game.get_frame() % (Constants.FRAME_SKIP * 100) == 0) printStats();
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
	
	public static int enemyBaseCount() {
		int result = 0;
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
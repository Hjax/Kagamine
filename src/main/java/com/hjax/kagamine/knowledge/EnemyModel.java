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
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;

public class EnemyModel {
	public static Set<Tag> registered = new HashSet<>();
	public static Map<UnitType, Integer> counts = new HashMap<>();
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
				
				counts.put(u.unit().getType(), counts.getOrDefault(u.unit().getType(), 0) + 1);
			}
		}
	}
	
	public static void update(UnitType u) {
		if (u == Units.INVALID) return;
		Game.chat("You have a: " + u.toString());
		if (counts.getOrDefault(u, 0) == 0) {
			counts.put(u, counts.getOrDefault(u, 0) + 1);
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
		return res;
	}
	
	public static int[] resourcesAlive() {
		int[] res = resourcesSpent();
		res[0] -= Game.minerals_killed();
		res[1] -= Game.gas_killed();
		return res;
	}
}
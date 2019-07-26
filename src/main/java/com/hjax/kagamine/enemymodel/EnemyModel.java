package com.hjax.kagamine.enemymodel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Tag;
import com.hjax.kagamine.Constants;
import com.hjax.kagamine.economy.Base;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.game.HjaxUnit;
import com.hjax.kagamine.knowledge.Balance;
import com.hjax.kagamine.knowledge.Scouting;

public class EnemyModel {
	private static final Map<Tag, UnitType> registered = new HashMap<>();
	public static final Map<UnitType, Integer> counts = new HashMap<>();
	private static final Map<UnitType, Integer> inferred = new HashMap<>();
	private static final Set<Tag> halluc = new HashSet<>();
	public static void on_frame() {
		for (HjaxUnit unit: GameInfoCache.get_units(Alliance.ENEMY)) {
			if (unit.is_halluc()) {
				halluc.add(unit.tag());
				if (registered.containsKey(unit.tag())) {
					removeFromModel(unit);
				}
			}
			
			Set<Tag> to_remove = new HashSet<>();
			for (Tag t: registered.keySet()) {
				if (Game.get_frame() - GameInfoCache.all_units.get(t).last_seen() > 120 * Constants.FPS) {
					to_remove.add(t);
				}
			}
			
			for (Tag t: to_remove) {
				removeFromModel(GameInfoCache.all_units.get(t));
			}
			
			if (!unit.is_halluc() && !halluc.contains(unit.tag())) {
				if (unit.is_not_snapshot() && !registered.containsKey(unit.tag())) {
					registered.put(unit.tag(), unit.type());
					if (counts.getOrDefault(unit.type(), 0) == 0) {
						if (Game.is_structure(unit.type())) {
							update(unit.type());
						} 
						else {
							update(Balance.get_tech_structure(unit.type()));
							UnitType best = Units.INVALID;
							for (UnitType ut: Balance.get_production_structures(unit.type())) {
								if (best == Units.INVALID || Game.get_unit_type_data().get(best).getMineralCost().orElse(0) + Game.get_unit_type_data().get(best).getVespeneCost().orElse(0) > Game.get_unit_type_data().get(ut).getMineralCost().orElse(0) + Game.get_unit_type_data().get(ut).getVespeneCost().orElse(0)) {
									best = ut;
								}
							}
							update(best);
						}
					}
					
					inferred.put(unit.type(), Math.max(inferred.getOrDefault(unit.type(), 0) - 1, 0));
					counts.put(unit.type(), counts.getOrDefault(unit.type(), 0) + 1);
				}
				if (unit.is_not_snapshot() && unit.type() != registered.get(unit.tag())) {
					counts.put(registered.get(unit.tag()), counts.getOrDefault(registered.get(unit.tag()), 0) - 1);
					counts.put(unit.type(), counts.getOrDefault(unit.type(), 0) + 1);
					registered.put(unit.tag(), unit.type());
				}
			}
		}
	}
	
	public static void removeFromModel(HjaxUnit u) {
		counts.put(u.type(), counts.getOrDefault(u.type(), 1) - 1);
		registered.remove(u.tag());
	}
	
	private static void update(UnitType u) {
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
	
	private static int[] resourcesSpent() {
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
		for (HjaxUnit u: GameInfoCache.get_units(Alliance.ENEMY)) {
			if (Game.is_town_hall(u.type())) result++;
		}
		return result;
	}
	
	public static boolean enemy_floated() {
		for (HjaxUnit u : GameInfoCache.get_units(Alliance.ENEMY)) {
			if (Game.is_structure(u.type()) && !u.flying()) {
				return false;
			}
		}
		return true;
	}

}
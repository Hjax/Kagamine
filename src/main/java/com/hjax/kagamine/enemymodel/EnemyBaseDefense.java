package com.hjax.kagamine.enemymodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.Constants;
import com.hjax.kagamine.economy.Base;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.game.HjaxUnit;

public class EnemyBaseDefense {
	
	public static Map<HjaxUnit, Base> defenses = new HashMap<>();
	public static Map<Base, Double> ground_defense = new HashMap<>();
	public static Map<Base, Double> air_defense = new HashMap<>();
	
	public static Map<UnitType, Pair<Double, Double>> threats = new HashMap<>();
	static {
		threats.put(Units.TERRAN_MARINE, Pair.of(1.0, 1.0));
		threats.put(Units.TERRAN_REAPER, Pair.of(1.0, 0.0));
		threats.put(Units.TERRAN_MARAUDER, Pair.of(2.0, 0.0));
		threats.put(Units.TERRAN_GHOST, Pair.of(2.0, 2.0));
		
		threats.put(Units.TERRAN_WIDOWMINE, Pair.of(2.0, 2.0));
		threats.put(Units.TERRAN_HELLION, Pair.of(2.0, 0.0));
		threats.put(Units.TERRAN_HELLION_TANK, Pair.of(3.0, 0.0));
		threats.put(Units.TERRAN_SIEGE_TANK, Pair.of(6.0, 0.0));
		threats.put(Units.TERRAN_THOR, Pair.of(6.0, 10.0));
		
		threats.put(Units.TERRAN_BANSHEE, Pair.of(4.0, 0.0));
		threats.put(Units.TERRAN_LIBERATOR, Pair.of(4.0, 6.0));
		threats.put(Units.TERRAN_BATTLECRUISER, Pair.of(12.0, 10.0));
		
		threats.put(Units.TERRAN_PLANETARY_FORTRESS, Pair.of(20.0, 0.0));
		threats.put(Units.TERRAN_MISSILE_TURRET, Pair.of(0.0, 6.0));
		
		threats.put(Units.ZERG_ZERGLING, Pair.of(0.5, 0.0));
		threats.put(Units.ZERG_ROACH, Pair.of(2.0, 0.0));
		threats.put(Units.ZERG_RAVAGER, Pair.of(2.5, 0.0));
		threats.put(Units.ZERG_HYDRALISK, Pair.of(2.0, 2.0));
		threats.put(Units.ZERG_QUEEN, Pair.of(1.0, 2.0));
		threats.put(Units.ZERG_BANELING, Pair.of(1.0, 2.0));
		threats.put(Units.ZERG_LURKER_MP, Pair.of(6.0, 0.0));
		threats.put(Units.ZERG_INFESTOR, Pair.of(4.0, 4.0));
		threats.put(Units.ZERG_SWARM_HOST_MP, Pair.of(4.0, 0.0));
		threats.put(Units.ZERG_CORRUPTOR, Pair.of(0.0, 4.0));
		threats.put(Units.ZERG_BROODLORD, Pair.of(8.0, 0.0));
		threats.put(Units.ZERG_MUTALISK, Pair.of(2.0, 2.0));
		threats.put(Units.ZERG_ULTRALISK, Pair.of(8.0, 0.0));
		
		threats.put(Units.ZERG_SPINE_CRAWLER, Pair.of(4.0, 0.0));
		threats.put(Units.ZERG_SPORE_CRAWLER, Pair.of(0.0, 4.0));
		
	}

	public static void on_frame() {
		List<HjaxUnit> to_remove = new ArrayList<>();
		for (HjaxUnit u: defenses.keySet()) {
			if ((Game.get_frame() - u.last_seen()) > Constants.MEMORY || !u.alive()) {
				to_remove.add(u);
			}
		}
		for (HjaxUnit u : to_remove) {
			defenses.remove(u);
		}
		
		for (HjaxUnit u: GameInfoCache.get_units(Alliance.ENEMY)) {
			Base best = null;
			for (Base base : BaseManager.bases) {
				if (base.has_enemy_command_structure()) {
					if (best == null || u.distance(best) > u.distance(base)) {
						best = base;
					}
				}
			}
			if (best != null) {
				defenses.put(u, best);
			}
		}
		
		for (Base b : BaseManager.bases) {
			ground_defense.put(b, 0.0);
			air_defense.put(b, 0.0);
		}
		
		for (HjaxUnit u : defenses.keySet()) {
			if (threats.containsKey(u.type())) {
				ground_defense.put(defenses.get(u), threats.get(u.type()).getLeft() + ground_defense.get(defenses.get(u)));
				air_defense.put(defenses.get(u), threats.get(u.type()).getRight() + air_defense.get(defenses.get(u)));
			}
		}
		
	}
	
	public static Base best_air_target(int limit) {
		Base best = null;
		for (Base b : air_defense.keySet()) {
			if (b.has_enemy_command_structure() && air_defense.get(b) <= limit) {
				if (best == null || air_defense.get(best) > air_defense.get(b)) {
					best = b;
				}
			}
		}
		
		return best;
	}
	
	public static Base best_ground_target(int limit) {
		Base best = null;
		for (Base b : ground_defense.keySet()) {
			if (b.has_enemy_command_structure() && ground_defense.get(b) <= limit) {
				if (best == null || ground_defense.get(best) > ground_defense.get(b)) {
					best = b;
				}
			}
		}
		
		return best;
	}
	
}

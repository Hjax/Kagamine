package com.hjax.kagamine.build;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.data.Weapon;
import com.github.ocraft.s2client.protocol.data.Weapon.TargetType;
import com.github.ocraft.s2client.protocol.game.Race;
import com.hjax.kagamine.build.TechLevelManager.TechLevel;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.enemymodel.EnemyModel;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.knowledge.Wisdom;

public class Composition {

	private static Map<TechLevelManager.TechLevel, Map<UnitType, Map<UnitType, Double>>> counters = new HashMap<>();
	private static Map<UnitType, Integer> limits = new HashMap<>();
	
	private static List<UnitType> units = new ArrayList<>();
	private static Set<UnitType> flying = new HashSet<>();
	
	static {
		
		units = Arrays.asList(Units.ZERG_ZERGLING, Units.PROTOSS_WARP_PRISM, Units.ZERG_BANELING, Units.ZERG_QUEEN, Units.ZERG_ROACH, Units.ZERG_RAVAGER, Units.ZERG_HYDRALISK, Units.ZERG_LURKER_MP, Units.ZERG_SWARM_HOST_MP, Units.ZERG_INFESTOR, Units.ZERG_ULTRALISK, Units.ZERG_MUTALISK, Units.ZERG_VIPER, Units.ZERG_CORRUPTOR, Units.ZERG_BROODLORD,
				Units.TERRAN_BANSHEE, Units.TERRAN_BATTLECRUISER, Units.TERRAN_GHOST, Units.TERRAN_MARINE, Units.TERRAN_LIBERATOR_AG, Units.TERRAN_MARAUDER, Units.TERRAN_REAPER, Units.TERRAN_HELLION, Units.TERRAN_HELLION_TANK, Units.TERRAN_SIEGE_TANK, Units.TERRAN_WIDOWMINE, Units.TERRAN_THOR, Units.TERRAN_LIBERATOR, Units.TERRAN_VIKING_FIGHTER, Units.TERRAN_RAVEN, Units.TERRAN_CYCLONE,
				Units.PROTOSS_ZEALOT, Units.PROTOSS_ADEPT, Units.PROTOSS_STALKER, Units.PROTOSS_DISRUPTOR, Units.PROTOSS_HIGH_TEMPLAR, Units.PROTOSS_DARK_TEMPLAR, Units.PROTOSS_ARCHON, Units.PROTOSS_ORACLE, Units.PROTOSS_SENTRY, Units.PROTOSS_IMMORTAL, Units.PROTOSS_COLOSSUS, Units.PROTOSS_PHOENIX, Units.PROTOSS_VOIDRAY, Units.PROTOSS_CARRIER, Units.PROTOSS_TEMPEST, Units.PROTOSS_MOTHERSHIP);
	
		flying = new HashSet<>(Arrays.asList(Units.PROTOSS_CARRIER, Units.PROTOSS_PHOENIX, Units.PROTOSS_ORACLE, Units.PROTOSS_MOTHERSHIP, Units.PROTOSS_TEMPEST,
				Units.TERRAN_LIBERATOR, Units.TERRAN_BANSHEE, Units.TERRAN_BATTLECRUISER, Units.TERRAN_VIKING_FIGHTER, 
				Units.ZERG_MUTALISK, Units.ZERG_BROODLORD, Units.ZERG_VIPER, Units.ZERG_CORRUPTOR));
		
		for (UnitType u : units) {
			limits.put(u, 999);
		}
		
		limits.put(Units.ZERG_VIPER, 2);
		limits.put(Units.ZERG_INFESTOR, 6);
		limits.put(Units.ZERG_BROODLORD, 10);
		limits.put(Units.ZERG_BANELING, 30);
		limits.put(Units.ZERG_SWARM_HOST_MP, 5);
		limits.put(Units.ZERG_LURKER_MP, 12);
		
		for (TechLevel t: TechLevelManager.TechLevel.values()) {
			counters.put(t, new HashMap<>());
			for (UnitType u : units) {
				counters.get(t).put(u, new HashMap<>());
			}
		}
		
		counters.get(TechLevel.HATCH).get(Units.PROTOSS_SENTRY).put(Units.ZERG_RAVAGER, 0.9);
		counters.get(TechLevel.HATCH).get(Units.PROTOSS_WARP_PRISM).put(Units.ZERG_RAVAGER, 4.0);
		
		counters.get(TechLevel.LAIR).get(Units.PROTOSS_TEMPEST).put(Units.ZERG_CORRUPTOR, 2.0);
		counters.get(TechLevel.LAIR).get(Units.PROTOSS_CARRIER).put(Units.ZERG_CORRUPTOR, 3.0);
		counters.get(TechLevel.LAIR).get(Units.PROTOSS_STALKER).put(Units.ZERG_LURKER_MP, 0.3);
		counters.get(TechLevel.LAIR).get(Units.PROTOSS_ZEALOT).put(Units.ZERG_LURKER_MP, 0.45);
		counters.get(TechLevel.LAIR).get(Units.PROTOSS_ADEPT).put(Units.ZERG_LURKER_MP, 0.20);
		counters.get(TechLevel.LAIR).get(Units.PROTOSS_ARCHON).put(Units.ZERG_LURKER_MP, 1.0);
		counters.get(TechLevel.LAIR).get(Units.PROTOSS_IMMORTAL).put(Units.ZERG_LURKER_MP, 1.0);
		
		counters.get(TechLevel.LAIR).get(Units.ZERG_ROACH).put(Units.ZERG_LURKER_MP, 0.25);
		
		counters.get(TechLevel.LAIR).get(Units.PROTOSS_COLOSSUS).put(Units.ZERG_CORRUPTOR, 3.0);
		
		counters.get(TechLevel.LAIR).get(Units.PROTOSS_CARRIER).put(Units.ZERG_INFESTOR, 1.0);
		counters.get(TechLevel.LAIR).get(Units.PROTOSS_MOTHERSHIP).put(Units.ZERG_INFESTOR, 1.0);
		
		counters.get(TechLevel.LAIR).get(Units.TERRAN_BATTLECRUISER).put(Units.ZERG_INFESTOR, 1.0);
		
		counters.get(TechLevel.HIVE).get(Units.PROTOSS_CARRIER).put(Units.ZERG_VIPER, 0.5);
		
		counters.get(TechLevel.HIVE).get(Units.TERRAN_MARINE).put(Units.ZERG_INFESTOR, 0.15);
		
		counters.get(TechLevel.HIVE).get(Units.TERRAN_THOR).put(Units.ZERG_INFESTOR, 1.0);

		counters.get(TechLevel.HIVE).get(Units.TERRAN_SIEGE_TANK).put(Units.ZERG_VIPER, 0.6);
		
		counters.get(TechLevel.HIVE).get(Units.PROTOSS_COLOSSUS).put(Units.ZERG_VIPER, 0.5);
		counters.get(TechLevel.HIVE).get(Units.PROTOSS_DISRUPTOR).put(Units.ZERG_VIPER, 0.5);
		
		for (UnitType unit : units) {
			if (!flying.contains(unit)) {
				double supply_handled = 6;
				for (Weapon w: Game.get_unit_type_data().get(unit).getWeapons()) {
					if (w.getTargetType() == TargetType.ANY || w.getTargetType() == TargetType.AIR) {
						supply_handled -= 1;
					}
				}
				counters.get(TechLevel.HIVE).get(unit).put(Units.ZERG_BROODLORD, 1.0 * Game.get_unit_type_data().get(unit).getFoodRequired().orElse(6.0f) / supply_handled);
			}
		}
		
		counters.get(TechLevel.LAIR).get(Units.PROTOSS_HIGH_TEMPLAR).put(Units.ZERG_HYDRALISK, 4.0);
		counters.get(TechLevel.LAIR).get(Units.PROTOSS_HIGH_TEMPLAR).put(Units.TERRAN_LIBERATOR, 3.0);
		counters.get(TechLevel.LAIR).get(Units.TERRAN_BATTLECRUISER).put(Units.ZERG_HYDRALISK, 5.0);
		counters.get(TechLevel.LAIR).get(Units.TERRAN_BATTLECRUISER).put(Units.ZERG_CORRUPTOR, 4.0);
		counters.get(TechLevel.LAIR).get(Units.TERRAN_LIBERATOR).put(Units.ZERG_CORRUPTOR, 2.0);
		counters.get(TechLevel.LAIR).get(Units.TERRAN_LIBERATOR_AG).put(Units.ZERG_CORRUPTOR, 2.0);
		
	}
	
	
	
	public static Map<UnitType, Integer> comp() {

		Map<UnitType, Integer> comp = new HashMap<>();
		
		if (Game.race() != Race.ZERG) {
			return comp;
		}
		
		if (Game.army_supply() < 60) return comp;
		
		Map<UnitType, Double> enemy_units = new HashMap<>();
		
		for (UnitType u : EnemyModel.counts.keySet()) {
			enemy_units.put(u, (double) EnemyModel.counts.get(u));
		}
		
		for (int i = 0; i < 5; i++) {
			for (UnitType unit : units) {
				if (enemy_units.getOrDefault(unit, 0.0) > 0) {
					UnitType best = Units.INVALID;
					TechLevel best_tech = TechLevel.HATCH;
					for (TechLevel tech: TechLevelManager.TechLevel.values()) {
						if (tech.ordinal() <= TechLevelManager.getTechLevel().ordinal()) {
							if (counters.get(tech).containsKey(unit)) {
								for (UnitType possible_counter : counters.get(tech).get(unit).keySet()) {
									if (possible_counter == Units.ZERG_CORRUPTOR && Game.army_supply() < 30) continue;
									if (possible_counter == Units.ZERG_INFESTOR && Game.supply() < 140) continue;
									if (comp.getOrDefault(possible_counter, 0) >= limits.get(possible_counter)) continue;
									
									if (best == Units.INVALID) {
										best = possible_counter;
										best_tech = tech;
									} else {

										double best_supply = counters.get(best_tech).get(unit).get(best) * Game.get_unit_type_data().get(best).getFoodRequired().orElse(0.0f);
										double current_supply = counters.get(tech).get(unit).get(possible_counter) * Game.get_unit_type_data().get(possible_counter).getFoodRequired().orElse(0.0f);

										if ((best_supply > current_supply) || 
										   ((best_supply == current_supply) && best_tech.ordinal() > tech.ordinal())) {
											
											best = possible_counter;
											best_tech = tech;
											
										}

									}
								}
							}
						}
					}
					
					if (best != Units.INVALID) {
						
						double num_add = EnemyModel.counts.getOrDefault(unit, 0) * counters.get(best_tech).get(unit).get(best) * 1.5;
						if (num_add + comp.getOrDefault(best, 0) > limits.get(best)) {
							num_add = limits.get(best) - comp.getOrDefault(best, 0);
						}
						
						System.out.println("Adding " + num_add + " " + best + " to beat " + unit);
						
						enemy_units.put(unit, enemy_units.get(unit) - num_add / counters.get(best_tech).get(unit).get(best));
						
						comp.put(best, (int) (comp.getOrDefault(best, 0) + num_add));
					}
				}
			}
		}
		
		for (UnitType u : comp.keySet()) {
			if (comp.get(u) > 0.1) {
				comp.put(u, (int) Math.ceil(Math.min(comp.get(u), limits.getOrDefault(u, 999))));
			}
		}
		
		
		return comp;
		
	}
	
	public static List<UnitType> full_comp() {
		List<UnitType> comp = new ArrayList<>(filler_comp());
		for (UnitType u : comp().keySet()) {
			comp.add(u);
		}
		return comp;
	}
	
	public static List<UnitType> filler_comp() {

		if (Game.race() == Race.PROTOSS) {
			return Arrays.asList(Units.PROTOSS_ZEALOT, Units.PROTOSS_STALKER);
		}
		
		if ((EnemyModel.enemy_floated() || EnemyModel.enemyBaseCount() == 1) && Game.army_supply() > EnemyModel.enemyArmy() * 3 && BaseManager.base_count() >= 4) {
			return Arrays.asList(Units.ZERG_MUTALISK);
		}
		
		if (Wisdom.proxy_detected() || Wisdom.cannon_rush()) {
			if (Game.army_supply() < 70) {
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_ROACH, Units.ZERG_RAVAGER);
			}
		}
		
		if (TechLevelManager.getTechLevel() == TechLevel.HATCH) {
			if (GameInfoCache.count(Units.ZERG_DRONE) < 40) {
				return Arrays.asList(Units.ZERG_ZERGLING);
			}
			if (Wisdom.all_in_detected()) {
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_ROACH, Units.ZERG_RAVAGER);
			}
			switch(GameInfoCache.get_opponent_race()) {
			case PROTOSS:
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_ROACH);
			case TERRAN:
				return Arrays.asList(Units.ZERG_ZERGLING);
			case ZERG:
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_ROACH);
			default:
				return Arrays.asList(Units.ZERG_ZERGLING);
			
			}
		} else if (TechLevelManager.getTechLevel() == TechLevel.LAIR) {
			if (Game.worker_count() < 85 && GameInfoCache.count(Units.ZERG_MUTALISK) < 10 && EnemyModel.counts.getOrDefault(Units.TERRAN_FACTORY, 0) + EnemyModel.counts.getOrDefault(Units.TERRAN_STARPORT, 0) > EnemyModel.counts.getOrDefault(Units.TERRAN_BARRACKS, 0)) {
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_MUTALISK);
			}
			if (GameInfoCache.get_opponent_race() == Race.TERRAN) {
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_HYDRALISK);
			}
			return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_ROACH, Units.ZERG_RAVAGER);
		} else {
			return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_HYDRALISK);
		}
	}
}

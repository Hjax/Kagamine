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
import com.hjax.kagamine.enemymodel.EnemyModel;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.knowledge.Wisdom;

public class Composition {

	private static Map<TechLevelManager.TechLevel, Map<UnitType, Map<UnitType, Double>>> counters = new HashMap<>();
	private static Map<UnitType, Integer> limits = new HashMap<>();
	
	private static List<UnitType> units = new ArrayList<>();
	private static Set<UnitType> flying = new HashSet<>();
	
	static {
		
		units = Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_BANELING, Units.ZERG_QUEEN, Units.ZERG_ROACH, Units.ZERG_RAVAGER, Units.ZERG_HYDRALISK, Units.ZERG_LURKER_MP, Units.ZERG_SWARM_HOST_MP, Units.ZERG_INFESTOR, Units.ZERG_ULTRALISK, Units.ZERG_MUTALISK, Units.ZERG_VIPER, Units.ZERG_CORRUPTOR, Units.ZERG_BROODLORD,
				Units.TERRAN_BANSHEE, Units.TERRAN_BATTLECRUISER, Units.TERRAN_GHOST, Units.TERRAN_MARINE, Units.TERRAN_MARAUDER, Units.TERRAN_REAPER, Units.TERRAN_HELLION, Units.TERRAN_HELLION_TANK, Units.TERRAN_SIEGE_TANK, Units.TERRAN_WIDOWMINE, Units.TERRAN_THOR, Units.TERRAN_LIBERATOR, Units.TERRAN_VIKING_FIGHTER, Units.TERRAN_RAVEN, Units.TERRAN_CYCLONE,
				Units.PROTOSS_ZEALOT, Units.PROTOSS_ADEPT, Units.PROTOSS_STALKER, Units.PROTOSS_DISRUPTOR, Units.PROTOSS_HIGH_TEMPLAR, Units.PROTOSS_DARK_TEMPLAR, Units.PROTOSS_ARCHON, Units.PROTOSS_ORACLE, Units.PROTOSS_SENTRY, Units.PROTOSS_IMMORTAL, Units.PROTOSS_COLOSSUS, Units.PROTOSS_PHOENIX, Units.PROTOSS_VOIDRAY, Units.PROTOSS_CARRIER, Units.PROTOSS_TEMPEST, Units.PROTOSS_MOTHERSHIP);
	
		flying = new HashSet<>(Arrays.asList(Units.PROTOSS_CARRIER, Units.PROTOSS_PHOENIX, Units.PROTOSS_ORACLE, Units.PROTOSS_MOTHERSHIP, Units.PROTOSS_TEMPEST,
				Units.TERRAN_LIBERATOR, Units.TERRAN_BANSHEE, Units.TERRAN_BATTLECRUISER, Units.TERRAN_VIKING_FIGHTER, 
				Units.ZERG_MUTALISK, Units.ZERG_BROODLORD, Units.ZERG_VIPER, Units.ZERG_CORRUPTOR));
		
		for (UnitType u : units) {
			limits.put(u, 999);
		}
		
		limits.put(Units.ZERG_VIPER, 4);
		limits.put(Units.ZERG_INFESTOR, 10);
		limits.put(Units.ZERG_BROODLORD, 10);
		limits.put(Units.ZERG_SWARM_HOST_MP, 5);
		limits.put(Units.ZERG_LURKER_MP, 12);
		
		for (TechLevel t: TechLevelManager.TechLevel.values()) {
			counters.put(t, new HashMap<>());
			for (UnitType u : units) {
				counters.get(t).put(u, new HashMap<>());
			}
		}
		
		counters.get(TechLevel.HATCH).get(Units.PROTOSS_VOIDRAY).put(Units.ZERG_QUEEN, 1.5);
		counters.get(TechLevel.HATCH).get(Units.PROTOSS_ORACLE).put(Units.ZERG_QUEEN, 1.5);
		counters.get(TechLevel.HATCH).get(Units.TERRAN_BANSHEE).put(Units.ZERG_QUEEN, 1.5);
		
		counters.get(TechLevel.LAIR).get(Units.PROTOSS_TEMPEST).put(Units.ZERG_CORRUPTOR, 2.0);
		counters.get(TechLevel.LAIR).get(Units.PROTOSS_CARRIER).put(Units.ZERG_CORRUPTOR, 3.0);
		counters.get(TechLevel.LAIR).get(Units.PROTOSS_STALKER).put(Units.ZERG_LURKER_MP, 0.3);
		counters.get(TechLevel.LAIR).get(Units.PROTOSS_ZEALOT).put(Units.ZERG_LURKER_MP, 0.45);
		counters.get(TechLevel.LAIR).get(Units.PROTOSS_ADEPT).put(Units.ZERG_LURKER_MP, 0.20);
		counters.get(TechLevel.LAIR).get(Units.PROTOSS_ARCHON).put(Units.ZERG_LURKER_MP, 1.0);
		counters.get(TechLevel.LAIR).get(Units.PROTOSS_IMMORTAL).put(Units.ZERG_LURKER_MP, 1.0);
		
		counters.get(TechLevel.LAIR).get(Units.PROTOSS_COLOSSUS).put(Units.ZERG_CORRUPTOR, 3.0);
		
		counters.get(TechLevel.LAIR).get(Units.PROTOSS_CARRIER).put(Units.ZERG_INFESTOR, 1.0);
		counters.get(TechLevel.LAIR).get(Units.PROTOSS_MOTHERSHIP).put(Units.ZERG_INFESTOR, 1.0);
		
		counters.get(TechLevel.HIVE).get(Units.TERRAN_BATTLECRUISER).put(Units.ZERG_INFESTOR, 1.0);
		counters.get(TechLevel.HIVE).get(Units.PROTOSS_CARRIER).put(Units.ZERG_VIPER, 0.5);
		
		counters.get(TechLevel.HIVE).get(Units.TERRAN_MARINE).put(Units.ZERG_INFESTOR, 0.1);
		
		counters.get(TechLevel.HIVE).get(Units.TERRAN_THOR).put(Units.ZERG_INFESTOR, 1.0);

		counters.get(TechLevel.HIVE).get(Units.TERRAN_SIEGE_TANK).put(Units.ZERG_VIPER, 0.4);
		
		counters.get(TechLevel.HIVE).get(Units.PROTOSS_COLOSSUS).put(Units.ZERG_VIPER, 0.5);
		counters.get(TechLevel.HIVE).get(Units.PROTOSS_DISRUPTOR).put(Units.ZERG_VIPER, 0.5);
		
		for (UnitType unit : units) {
			if (!flying.contains(unit)) {
				double supply_handled = 7;
				for (Weapon w: Game.get_unit_type_data().get(unit).getWeapons()) {
					if (w.getTargetType() == TargetType.ANY || w.getTargetType() == TargetType.AIR) {
						supply_handled -= 2;
					}
				}
				counters.get(TechLevel.HIVE).get(unit).put(Units.ZERG_BROODLORD, 1.0 * Game.get_unit_type_data().get(unit).getFoodRequired().orElse(6.0f) / supply_handled);
			}
		}
		
		for (UnitType unit : units) {
			if (flying.contains(unit)) {
				counters.get(TechLevel.LAIR).get(unit).put(Units.ZERG_CORRUPTOR, 1.0 * Game.get_unit_type_data().get(unit).getFoodRequired().orElse(6.0f) / 2.0);
			}
		}
		
		for (UnitType unit : units) {
			if (flying.contains(unit)) {
				counters.get(TechLevel.HIVE).get(unit).put(Units.ZERG_INFESTOR, 1.0 * Game.get_unit_type_data().get(unit).getFoodRequired().orElse(6.0f) / 4.0);
			} else {
				counters.get(TechLevel.HIVE).get(unit).put(Units.ZERG_INFESTOR, 1.0 * Game.get_unit_type_data().get(unit).getFoodRequired().orElse(6.0f) / 2.1);
			}
		}
		
		for (UnitType unit : units) {
			if (!flying.contains(unit)) {
				counters.get(TechLevel.HATCH).get(unit).put(Units.ZERG_ROACH, 1.0 * Game.get_unit_type_data().get(unit).getFoodRequired().orElse(6.0f) / 1.5);
			}
		}
		
		for (UnitType unit : units) {
			counters.get(TechLevel.LAIR).get(unit).put(Units.ZERG_HYDRALISK, 1.0 * Game.get_unit_type_data().get(unit).getFoodRequired().orElse(6.0f) / 2);
		}
		counters.get(TechLevel.LAIR).get(Units.PROTOSS_HIGH_TEMPLAR).put(Units.ZERG_HYDRALISK, 4.0);
	}
	
	
	
	public static Map<UnitType, Integer> comp() {

		Map<UnitType, Integer> comp = new HashMap<>();
		
		if (Game.race() != Race.ZERG) {
			return comp;
		}
		
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
									
									if (comp.getOrDefault(possible_counter, 0) >= limits.get(possible_counter)) continue;
									
									if (best == Units.INVALID || 
											counters.get(best_tech).get(unit).get(best) * Game.get_unit_type_data().get(best).getFoodRequired().orElse(0.0f) > 
											counters.get(tech).get(unit).get(possible_counter) * Game.get_unit_type_data().get(possible_counter).getFoodRequired().orElse(0.0f)) {
										
										best = possible_counter;
										best_tech = tech;
									}
								}
							}
						}
					}
					
					if (best != Units.INVALID) {
						
						double num_add = EnemyModel.counts.getOrDefault(unit, 0) * counters.get(best_tech).get(unit).get(best);
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
				comp.put(u, (int) Math.ceil(Math.min(comp.get(u) * 2, limits.getOrDefault(u, 999))));
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
		
		
		if (Wisdom.proxy_detected() || Wisdom.cannon_rush()) {
			if (Game.army_supply() < 50) {
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_ROACH, Units.ZERG_RAVAGER);
			}
		}
		
		if (TechLevelManager.getTechLevel() == TechLevel.HATCH) {
			return Arrays.asList(Units.ZERG_ZERGLING);
		} else if (TechLevelManager.getTechLevel() == TechLevel.LAIR) {
			return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_HYDRALISK);
		} else {
			return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_HYDRALISK);
		}
	}
}

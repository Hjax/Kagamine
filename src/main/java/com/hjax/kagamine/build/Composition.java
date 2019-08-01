package com.hjax.kagamine.build;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.ocraft.s2client.protocol.data.DamageBonus;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.data.Weapon;
import com.github.ocraft.s2client.protocol.data.Weapon.TargetType;
import com.github.ocraft.s2client.protocol.game.Race;
import com.hjax.kagamine.enemymodel.EnemyModel;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.knowledge.Wisdom;

public class Composition {
	
	private static boolean initialized = false;
	private static List<UnitType> units = new ArrayList<>();
	private static Map<UnitType, Integer> scaling = new HashMap<>();
	private static Map<UnitType, Map<UnitType, Integer>> scores = new HashMap<>();
	
	private static Set<UnitType> flying = new HashSet<>();
	
	static {
		units = Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_BANELING, Units.ZERG_QUEEN, Units.ZERG_ROACH, Units.ZERG_RAVAGER, Units.ZERG_HYDRALISK, Units.ZERG_LURKER_MP, Units.ZERG_SWARM_HOST_MP, Units.ZERG_INFESTOR, Units.ZERG_ULTRALISK, Units.ZERG_MUTALISK, Units.ZERG_VIPER, Units.ZERG_CORRUPTOR, Units.ZERG_BROODLORD,
				Units.TERRAN_BANSHEE, Units.TERRAN_BATTLECRUISER, Units.TERRAN_GHOST, Units.TERRAN_MARINE, Units.TERRAN_MARAUDER, Units.TERRAN_REAPER, Units.TERRAN_HELLION, Units.TERRAN_HELLION_TANK, Units.TERRAN_SIEGE_TANK, Units.TERRAN_WIDOWMINE, Units.TERRAN_THOR, Units.TERRAN_LIBERATOR, Units.TERRAN_VIKING_FIGHTER, Units.TERRAN_RAVEN, Units.TERRAN_CYCLONE,
				Units.PROTOSS_ZEALOT, Units.PROTOSS_ADEPT, Units.PROTOSS_STALKER, Units.PROTOSS_HIGH_TEMPLAR, Units.PROTOSS_DARK_TEMPLAR, Units.PROTOSS_ARCHON, Units.PROTOSS_SENTRY, Units.PROTOSS_IMMORTAL, Units.PROTOSS_COLOSSUS, Units.PROTOSS_PHOENIX, Units.PROTOSS_VOIDRAY, Units.PROTOSS_CARRIER, Units.PROTOSS_TEMPEST, Units.PROTOSS_MOTHERSHIP);
	
		flying = new HashSet<>(Arrays.asList(Units.PROTOSS_CARRIER, Units.PROTOSS_PHOENIX, Units.PROTOSS_ORACLE, Units.PROTOSS_MOTHERSHIP, Units.PROTOSS_TEMPEST,
				Units.TERRAN_LIBERATOR, Units.TERRAN_BANSHEE, Units.TERRAN_BATTLECRUISER, Units.TERRAN_VIKING_FIGHTER, 
				Units.ZERG_MUTALISK, Units.ZERG_BROODLORD, Units.ZERG_VIPER, Units.ZERG_CORRUPTOR));
		
	}
	
	
	
	public static List<UnitType> comp() {
		
		if (!initialized) {
			initialized = true;
			
			for (UnitType attacker : units) {
				scores.put(attacker, new HashMap<>());
				for (UnitType defender : units) {
					scores.get(attacker).put(defender, 0);
					for (Weapon w: Game.get_unit_type_data().get(attacker).getWeapons()) {
						if (w.getTargetType() == TargetType.ANY || (w.getTargetType() == TargetType.AIR) == flying.contains(defender)) {
							int score = 1;
							for (DamageBonus d: w.getDamageBonuses()) {
								if (Game.get_unit_type_data().get(defender).getAttributes().contains(d.getAttribute())) {
									score += w.getDamage() + d.getBonus() / w.getDamage();
								}
							}
							if (scores.get(attacker).get(defender) < score) {
								scores.get(attacker).put(defender, score);
							}
						}
					}
				}
				if (attacker == Units.ZERG_INFESTOR) {
					
					for (UnitType defender : units) {
						scores.get(attacker).put(defender, 1);
					}
					
					scores.get(attacker).put(Units.TERRAN_THOR, 2);
					scores.get(attacker).put(Units.TERRAN_THOR_AP, 2);
					scores.get(attacker).put(Units.TERRAN_MARINE, 2);
					scores.get(attacker).put(Units.TERRAN_BATTLECRUISER, 2);
					
					scores.get(attacker).put(Units.PROTOSS_CARRIER, 2);
					scores.get(attacker).put(Units.PROTOSS_TEMPEST, 2);
					scores.get(attacker).put(Units.PROTOSS_MOTHERSHIP, 3);
					scores.get(attacker).put(Units.PROTOSS_COLOSSUS, 2);
				}
			}
		}

		if (Game.race() == Race.PROTOSS) {
			return Arrays.asList(Units.PROTOSS_ZEALOT, Units.PROTOSS_STALKER);
		}
		
		if (GameInfoCache.get_opponent_race() == Race.TERRAN) {
			if (GameInfoCache.count_friendly(Units.ZERG_DRONE) > 50 && GameInfoCache.count_friendly(Units.ZERG_GREATER_SPIRE) > 0) {
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_INFESTOR, Units.ZERG_HYDRALISK, Units.ZERG_CORRUPTOR, Units.ZERG_BROODLORD);
			}
			if (EnemyModel.enemy_floated()) {
				return Arrays.asList(Units.ZERG_MUTALISK);
			}

			if (BuildPlanner.is_all_in && EnemyModel.enemyBaseCount() == 1 && (Wisdom.cannon_rush() || Wisdom.proxy_detected())) {
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_ROACH, Units.ZERG_RAVAGER);
			}
			if (Game.army_supply() < 10 || GameInfoCache.count_friendly(Units.ZERG_DRONE) < 35) {
				return Arrays.asList(Units.ZERG_ZERGLING);
			}
			if (EnemyModel.counts.getOrDefault(Units.TERRAN_BATTLECRUISER, 0) >= 2 && GameInfoCache.count_friendly(Units.ZERG_SPIRE) > 0) {
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_CORRUPTOR);
			}
			if (EnemyModel.counts.getOrDefault(Units.TERRAN_BATTLECRUISER, 0) >= 2) {
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_HYDRALISK, Units.ZERG_CORRUPTOR);
			}
			if (Game.army_supply() < 80) {
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_HYDRALISK);
			}
			return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_HYDRALISK,  Units.ZERG_INFESTOR, Units.ZERG_ULTRALISK);
		}
		if (GameInfoCache.get_opponent_race() == Race.ZERG) {
			if (Wisdom.cannon_rush() || Wisdom.proxy_detected()) {
				return Arrays.asList(Units.ZERG_ZERGLING);
			}
			if (Game.army_supply() < 30) {
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_ROACH);
			}
			if (Wisdom.cannon_rush() || Wisdom.proxy_detected()) {
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_ROACH, Units.ZERG_RAVAGER);
			}
			if (EnemyModel.counts.getOrDefault(Units.ZERG_MUTALISK, 0) > 0) {
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_HYDRALISK);
			}
			if (Game.army_supply() < 80) {
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_ROACH, Units.ZERG_RAVAGER);
			}
			return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_ROACH, Units.ZERG_RAVAGER, Units.ZERG_HYDRALISK, Units.ZERG_LURKER_MP);
		}
		if (GameInfoCache.get_opponent_race() == Race.PROTOSS) {
			if (GameInfoCache.count_friendly(Units.ZERG_DRONE) > 50 && GameInfoCache.count_friendly(Units.ZERG_GREATER_SPIRE) > 0) {
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_INFESTOR, Units.ZERG_CORRUPTOR, Units.ZERG_BROODLORD);
			}
			if (BuildPlanner.is_all_in && (Wisdom.cannon_rush() || Wisdom.proxy_detected())) {
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_ROACH, Units.ZERG_RAVAGER);
			}
			if (EnemyModel.counts.getOrDefault(Units.PROTOSS_CARRIER, 0) > 0) {
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_HYDRALISK, Units.ZERG_INFESTOR);
			}
			if (EnemyModel.counts.getOrDefault(Units.PROTOSS_VOIDRAY, 0) > 0) {
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_HYDRALISK);
			}
			if (EnemyModel.counts.getOrDefault(Units.PROTOSS_TEMPEST, 0) > 2) {
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_CORRUPTOR);
			}
			if (Game.army_supply() < 15) {
				return Arrays.asList(Units.ZERG_ZERGLING);
			}
			if (Wisdom.all_in_detected() && Game.army_supply() < 50) {
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_ROACH, Units.ZERG_QUEEN);
			}
			if (Game.army_supply() < 60) {
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_HYDRALISK, Units.ZERG_LURKER_MP);
			}
			return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_HYDRALISK, Units.ZERG_LURKER_MP, Units.ZERG_CORRUPTOR, Units.ZERG_BROODLORD);
		}
		return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_ROACH, Units.ZERG_HYDRALISK);
	}
}

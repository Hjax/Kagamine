package com.hjax.kagamine.build;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.data.Upgrades;
import com.github.ocraft.s2client.protocol.game.Race;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.economy.Base;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.knowledge.Wisdom;

public class BuildPlanner {
	private static boolean worker_rush = false;
	private static boolean is_all_in = false;
	public static void on_frame() {
		if (Wisdom.worker_rush() && !worker_rush) {
			worker_rush = true;
			Game.chat("Sorry im not tycklish");
			Build.build = new ArrayList<>(Arrays.asList(new ImmutablePair<Integer, UnitType>(13, Units.ZERG_OVERLORD),
					new ImmutablePair<Integer, UnitType>(14, Units.ZERG_SPAWNING_POOL)));
			Build.composition = Collections.singletonList(Units.ZERG_ZERGLING);
			Build.ideal_gases = 0;
			Build.ideal_hatches = 1;
			Build.scout = false;
			Build.push_supply = 30;
			Build.ideal_workers = 14;
			Build.tech_drones = 12;
			Build.upgrades = new ArrayList<>();
		}
		
		if (!is_all_in && GameInfoCache.count_friendly(Units.ZERG_DRONE) < 30 && (GameInfoCache.count_enemy(Units.PROTOSS_NEXUS) == 2 || (GameInfoCache.count_enemy(Units.TERRAN_COMMAND_CENTER) + GameInfoCache.count_enemy(Units.TERRAN_ORBITAL_COMMAND)) == 2)) {
			Build.build = new ArrayList<>(Arrays.asList(new ImmutablePair<Integer, UnitType>(13, Units.ZERG_OVERLORD),
														new ImmutablePair<Integer, UnitType>(17, Units.ZERG_HATCHERY),
														new ImmutablePair<Integer, UnitType>(17, Units.ZERG_EXTRACTOR),
														new ImmutablePair<Integer, UnitType>(17, Units.ZERG_SPAWNING_POOL)));
			Build.composition = Arrays.asList(Units.ZERG_ZERGLING);
			Build.ideal_hatches = 3;
			Build.scout = false;
			Build.push_supply = 55;
			Build.pull_off_gas = true;
			Build.ideal_workers = 25;
			Build.max_queens = 3;
			Build.ideal_gases = 1;
			Build.tech_drones = 12;
			Build.upgrades = Arrays.asList(Upgrades.ZERGLING_MOVEMENT_SPEED);
			is_all_in = true;
		}
		
		if (GameInfoCache.count_enemy(Units.ZERG_ZERGLING) > 0 && Game.get_frame() < 2700) {
			//Game.chat("Anti cheese ling flood");
			Build.composition = Arrays.asList(Units.ZERG_ZERGLING);
			Build.ideal_gases = 1;
			Build.ideal_hatches = 2;
			Build.scout = false;
			Build.push_supply = 40;
			Build.ideal_workers = 19;
			Build.pull_off_gas = true;
			Build.max_queens = 2;
			Build.upgrades = Arrays.asList(Upgrades.ZERGLING_MOVEMENT_SPEED);
		}
		
		if (!is_all_in && Game.get_opponent_race() != Race.ZERG) {
			if (BuildExecutor.count(Units.ZERG_DRONE) < 30 && (Wisdom.cannon_rush() || Wisdom.proxy_detected())) {
				do_ravager_all_in();
				if (GameInfoCache.count_friendly(Units.ZERG_HATCHERY) == 1) {
					for (UnitInPool u: GameInfoCache.get_units(Alliance.SELF, Units.ZERG_HATCHERY)) {
						if (u.unit().getBuildProgress() < 0.999) {
							Game.unit_command(u, Abilities.CANCEL);
							for (Base b: BaseManager.bases) {
								if (b.location.distance(u.unit().getPosition().toPoint2d()) < 4) {
									b.set_walking_drone(null);
								}
							}
						}
					}
				}
			}
		}
		
		if (is_all_in && Game.army_killed() - Game.army_lost() < -500) {
			is_all_in = false;
			decide_build();
		}

		if (is_all_in && Game.supply() > 70 && Game.get_opponent_race() == Race.TERRAN) {
			hunter_killer();
		}
		
		// switch back to hydras vs anti muta
		if (Game.get_opponent_race() == Race.PROTOSS) {
			if (GameInfoCache.count_enemy(Units.PROTOSS_CARRIER) > 0 ||
					GameInfoCache.count_enemy(Units.PROTOSS_VOIDRAY) > 0 || 
					GameInfoCache.count_enemy(Units.PROTOSS_TEMPEST) > 0 ||
					GameInfoCache.count_enemy(Units.PROTOSS_MOTHERSHIP) > 0 ||
					GameInfoCache.count_enemy(Units.PROTOSS_ARCHON) > 0 ||
					GameInfoCache.count_enemy(Units.PROTOSS_STARGATE) > 0) {
				if (Build.composition.contains(Units.ZERG_ROACH) || Build.composition.contains(Units.ZERG_MUTALISK)) {
					Build.build = new ArrayList<>();
					Build.composition = Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_HYDRALISK);
					Build.ideal_hatches = -1;
					Build.push_supply = 185;
					Build.ideal_workers = 70;
					Build.ideal_gases = 8;
					Build.max_queens = -1;
					Build.upgrades = Arrays.asList(Upgrades.ZERGLING_MOVEMENT_SPEED, Upgrades.EVOLVE_GROOVED_SPINES, Upgrades.EVOLVE_MUSCULAR_AUGMENTS, Upgrades.ZERG_GROUND_ARMORS_LEVEL1, Upgrades.ZERG_GROUND_ARMORS_LEVEL2, Upgrades.ZERG_MISSILE_WEAPONS_LEVEL1, Upgrades.ZERG_MISSILE_WEAPONS_LEVEL2);
				}
			}
		}
	}


	private static void do_ravager_all_in() {
		is_all_in = true;
		Build.build = new ArrayList<>();
				Build.composition = Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_ROACH, Units.ZERG_RAVAGER);
				Build.ideal_gases = 2;
				Build.ideal_hatches = 1;
				Build.max_queens = 1;
				Build.tech_drones = 16;
				Build.scout = true;
				Build.push_supply = 40;
				Build.ideal_workers = 22;
				Build.pull_off_gas = false;
				Build.upgrades = new ArrayList<>();
	}
	
	private static void hunter_killer() {
		Build.build = new ArrayList<>();
				Build.composition = Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_MUTALISK);
				Build.ideal_gases = 6;
				Build.ideal_hatches = -1;
				Build.tech_drones = 40;
				Build.scout = true;
				Build.push_supply = 40;
				Build.ideal_workers = 60;
				Build.pull_off_gas = false;
				Build.upgrades = new ArrayList<>();
	}
	
	public static void decide_build() {
		switch (Game.get_opponent_race()) {
			case PROTOSS:
				Build.build = new ArrayList<>(Arrays.asList(new ImmutablePair<Integer, UnitType>(13, Units.ZERG_OVERLORD),
															new ImmutablePair<Integer, UnitType>(17, Units.ZERG_HATCHERY),
															new ImmutablePair<Integer, UnitType>(17, Units.ZERG_EXTRACTOR),
															new ImmutablePair<Integer, UnitType>(17, Units.ZERG_SPAWNING_POOL)));
				Build.composition = Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_ROACH, Units.ZERG_HYDRALISK);
				Build.ideal_hatches = -1;
				Build.push_supply = 185;
				Build.ideal_workers = 70;
				Build.ideal_gases = 7;
				Build.tech_drones = 25;
				Build.pull_off_gas = true;
				Build.max_queens = -1;
				Build.upgrades = Arrays.asList(Upgrades.ZERGLING_MOVEMENT_SPEED, Upgrades.EVOLVE_GROOVED_SPINES, Upgrades.EVOLVE_MUSCULAR_AUGMENTS, Upgrades.ZERG_GROUND_ARMORS_LEVEL1, Upgrades.ZERG_GROUND_ARMORS_LEVEL2, Upgrades.ZERG_MISSILE_WEAPONS_LEVEL1, Upgrades.ZERG_MISSILE_WEAPONS_LEVEL2);
				break;
			case ZERG:
				Build.build = new ArrayList<>(Arrays.asList(new ImmutablePair<Integer, UnitType>(13, Units.ZERG_OVERLORD),
						new ImmutablePair<Integer, UnitType>(17, Units.ZERG_HATCHERY),
						new ImmutablePair<Integer, UnitType>(17, Units.ZERG_EXTRACTOR),
						new ImmutablePair<Integer, UnitType>(17, Units.ZERG_SPAWNING_POOL)));
				Build.composition = Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_BANELING, Units.ZERG_ROACH, Units.ZERG_HYDRALISK);
				Build.ideal_hatches = -1;
				Build.scout = true;
				Build.push_supply = 190;
				Build.ideal_gases = 6;
				Build.ideal_workers = 65;
				Build.pull_off_gas = false;
				Build.tech_drones = 25;
				Build.max_queens = -1;
				Build.upgrades = Arrays.asList(Upgrades.ZERGLING_MOVEMENT_SPEED, Upgrades.ZERG_MISSILE_WEAPONS_LEVEL1, Upgrades.ZERG_GROUND_ARMORS_LEVEL1, Upgrades.ZERG_MISSILE_WEAPONS_LEVEL2, Upgrades.ZERG_GROUND_ARMORS_LEVEL2);
				break;			
			case TERRAN:
				Build.build = new ArrayList<>(Arrays.asList(new ImmutablePair<Integer, UnitType>(13, Units.ZERG_OVERLORD),
						new ImmutablePair<Integer, UnitType>(17, Units.ZERG_HATCHERY),
						new ImmutablePair<Integer, UnitType>(17, Units.ZERG_EXTRACTOR),
						new ImmutablePair<Integer, UnitType>(17, Units.ZERG_SPAWNING_POOL)));
				Build.composition = Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_BANELING, Units.ZERG_HYDRALISK, Units.ZERG_ULTRALISK);
				Build.ideal_hatches = -1;
				Build.scout = true;
				Build.ideal_gases = 8;
				Build.push_supply = 190;
				Build.ideal_workers = 75;
				Build.pull_off_gas = true;
				Build.upgrades = Arrays.asList(Upgrades.ZERGLING_MOVEMENT_SPEED, Upgrades.ZERGLING_ATTACK_SPEED, Upgrades.CENTRIFICAL_HOOKS, Upgrades.EVOLVE_GROOVED_SPINES, Upgrades.EVOLVE_MUSCULAR_AUGMENTS, Upgrades.ZERG_MELEE_WEAPONS_LEVEL1, Upgrades.ZERG_MELEE_WEAPONS_LEVEL2, Upgrades.ZERG_MELEE_WEAPONS_LEVEL3, Upgrades.ZERG_GROUND_ARMORS_LEVEL1, Upgrades.ZERG_GROUND_ARMORS_LEVEL2, Upgrades.ZERG_GROUND_ARMORS_LEVEL3, Upgrades.CHITINOUS_PLATING, Upgrades.ANABOLIC_SYNTHESIS);
				break;
			default:
				Build.build = new ArrayList<>(Arrays.asList(new ImmutablePair<Integer, UnitType>(13, Units.ZERG_OVERLORD),
						new ImmutablePair<Integer, UnitType>(17, Units.ZERG_HATCHERY),
						new ImmutablePair<Integer, UnitType>(17, Units.ZERG_EXTRACTOR),
						new ImmutablePair<Integer, UnitType>(17, Units.ZERG_SPAWNING_POOL)));
				Build.composition = Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_MUTALISK);
				Build.ideal_hatches = -1;
				Build.scout = true;
				Build.ideal_gases = 8;
				Build.push_supply = 190;
				Build.ideal_workers = 70;
				Build.pull_off_gas = true;
				Build.upgrades = Arrays.asList(Upgrades.ZERGLING_MOVEMENT_SPEED, Upgrades.CENTRIFICAL_HOOKS, Upgrades.ZERG_MELEE_WEAPONS_LEVEL1, Upgrades.ZERG_MELEE_WEAPONS_LEVEL2, Upgrades.ZERG_GROUND_ARMORS_LEVEL1, Upgrades.ZERG_GROUND_ARMORS_LEVEL2, Upgrades.ZERG_GROUND_ARMORS_LEVEL3, Upgrades.ZERG_FLYER_WEAPONS_LEVEL1, Upgrades.ZERG_FLYER_WEAPONS_LEVEL2);
				
		}
	}
}

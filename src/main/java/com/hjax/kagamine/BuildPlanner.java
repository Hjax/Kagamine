package com.hjax.kagamine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.data.Upgrades;
import com.github.ocraft.s2client.protocol.game.Race;
import com.github.ocraft.s2client.protocol.unit.Alliance;

import javafx.util.Pair;

public class BuildPlanner {
	private static boolean worker_rush = false;
	public static void on_frame() {
		if (Wisdom.worker_rush() && !worker_rush) {
			worker_rush = true;
			Game.chat("Sorry im not tycklish");
			Build.build = new ArrayList<>(Arrays.asList(new Pair<Integer, UnitType>(13, Units.ZERG_OVERLORD),
					new Pair<Integer, UnitType>(14, Units.ZERG_SPAWNING_POOL)));
			Build.composition = Arrays.asList(Units.ZERG_ZERGLING);
			Build.ideal_gases = 0;
			Build.ideal_hatches = 1;
			Build.scout = false;
			Build.push_supply = 30;
			Build.ideal_workers = 14;
			Build.upgrades = new HashSet<>();
		}
		if ((BuildExecutor.count(Units.ZERG_DRONE) < 30 && Wisdom.cannon_rush()) || Game.get_opponent_race() == Race.PROTOSS && Wisdom.proxy_detected()) {
			do_ravager_all_in();
			if (GameInfoCache.count_friendly(Units.ZERG_HATCHERY) == 1) {
				for (UnitInPool u: GameInfoCache.get_units(Alliance.SELF, Units.ZERG_HATCHERY)) {
					if (u.unit().getBuildProgress() < 0.999) {
						Game.unit_command(u, Abilities.CANCEL);
					}
				}
			}
		}
		//if (is_all_in && Game.supply() > 70 && Game.get_opponent_race() == Race.TERRAN) hunter_killer();
		if (Game.get_opponent_race() == Race.PROTOSS) {
			if (GameInfoCache.count_enemy(Units.PROTOSS_CARRIER) > 0 ||
				GameInfoCache.count_enemy(Units.PROTOSS_VOIDRAY) > 0 || 
				GameInfoCache.count_enemy(Units.PROTOSS_TEMPEST) > 0 ||
				GameInfoCache.count_enemy(Units.PROTOSS_MOTHERSHIP) > 0 ||
				GameInfoCache.count_enemy(Units.PROTOSS_STARGATE) > 0) {
				if (Build.composition.contains(Units.ZERG_ROACH)) {
					Build.composition = Arrays.asList(Units.ZERG_HYDRALISK, Units.ZERG_ZERGLING);
					Build.ideal_workers = 70;
					Build.max_queens = 4;
				}
			}
		}
	}
	
	
	private static void do_ravager_all_in() {
		Build.build = new ArrayList<>();
				Build.composition = Arrays.asList(Units.ZERG_ROACH, Units.ZERG_RAVAGER);
				Build.ideal_gases = 2;
				Build.ideal_hatches = 1;
				Build.scout = true;
				Build.push_supply = 40;
				Build.ideal_workers = 22;
				Build.upgrades = new HashSet<>();
	}
	
	public static void decide_build() {
		switch (Game.get_opponent_race()) {
			case PROTOSS:
				Build.build = new ArrayList<>(Arrays.asList(new Pair<Integer, UnitType>(13, Units.ZERG_OVERLORD),
															new Pair<Integer, UnitType>(17, Units.ZERG_HATCHERY),
															new Pair<Integer, UnitType>(17, Units.ZERG_EXTRACTOR),
															new Pair<Integer, UnitType>(17, Units.ZERG_SPAWNING_POOL)));
				Build.composition = Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_ROACH, Units.ZERG_HYDRALISK);
				Build.ideal_hatches = -1;
				Build.scout = true;
				Build.push_supply = 185;
				Build.ideal_workers = 70;
				Build.ideal_gases = 6;
				Build.upgrades = new HashSet<>(Arrays.asList(Upgrades.ZERGLING_MOVEMENT_SPEED, Upgrades.EVOLVE_MUSCULAR_AUGMENTS, Upgrades.ZERG_GROUND_ARMORS_LEVEL1, Upgrades.ZERG_GROUND_ARMORS_LEVEL2, Upgrades.ZERG_MISSILE_WEAPONS_LEVEL1, Upgrades.ZERG_MISSILE_WEAPONS_LEVEL2));
				break;
			case ZERG:
				Build.build = new ArrayList<>(Arrays.asList(new Pair<Integer, UnitType>(13, Units.ZERG_OVERLORD),
						new Pair<Integer, UnitType>(17, Units.ZERG_EXTRACTOR),
						new Pair<Integer, UnitType>(17, Units.ZERG_SPAWNING_POOL),
						new Pair<Integer, UnitType>(17, Units.ZERG_HATCHERY)));
				Build.composition = Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_ROACH);
				Build.ideal_hatches = -1;
				Build.scout = false;
				Build.push_supply = 140;
				Build.ideal_gases = 4;
				Build.ideal_workers = 55;
				Build.max_queens = -1;
				Build.upgrades = new HashSet<>(Arrays.asList(Upgrades.ZERGLING_MOVEMENT_SPEED));
				break;			
			case TERRAN:
				Build.build = new ArrayList<>(Arrays.asList(new Pair<Integer, UnitType>(13, Units.ZERG_OVERLORD),
						new Pair<Integer, UnitType>(17, Units.ZERG_HATCHERY),
						new Pair<Integer, UnitType>(17, Units.ZERG_EXTRACTOR),
						new Pair<Integer, UnitType>(17, Units.ZERG_SPAWNING_POOL)));
				Build.composition = Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_BANELING, Units.ZERG_MUTALISK);
				Build.ideal_hatches = -1;
				Build.scout = true;
				Build.ideal_gases = 8;
				Build.push_supply = 190;
				Build.ideal_workers = 70;
				Build.pull_off_gas = true;
				Build.upgrades = new HashSet<>(Arrays.asList(Upgrades.ZERGLING_MOVEMENT_SPEED, Upgrades.CENTRIFICAL_HOOKS, Upgrades.ZERG_MELEE_WEAPONS_LEVEL1, Upgrades.ZERG_MELEE_WEAPONS_LEVEL2, Upgrades.ZERG_GROUND_ARMORS_LEVEL1, Upgrades.ZERG_GROUND_ARMORS_LEVEL2, Upgrades.ZERG_FLYER_WEAPONS_LEVEL1, Upgrades.ZERG_FLYER_WEAPONS_LEVEL2));
				break;
			default:
				
		}
	}
}

package com.hjax.kagamine.build;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.game.Race;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.Chat;
import com.hjax.kagamine.economy.Base;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.knowledge.Wisdom;

public class BuildPlanner {
	private static boolean worker_rush = false;
	public static boolean is_all_in = false;
	public static void on_frame() {
		if (Wisdom.worker_rush() && !worker_rush) {
			worker_rush = true;
			Chat.sendMessage("Sorry im not tycklish");
			Build.build = new ArrayList<>(Arrays.asList(new ImmutablePair<Integer, UnitType>(13, Units.ZERG_OVERLORD),
					new ImmutablePair<Integer, UnitType>(14, Units.ZERG_SPAWNING_POOL)));
			Build.ideal_gases = 0;
			Build.ideal_hatches = 1;
			Build.scout = false;
			Build.push_supply = 30;
			Build.ideal_workers = 14;
			Build.tech_drones = 12;
			is_all_in = true;
		}
		
		if (!is_all_in && GameInfoCache.get_opponent_race() != Race.ZERG) {
			if (BuildExecutor.count(Units.ZERG_DRONE) < 30 && (Wisdom.cannon_rush() || Wisdom.proxy_detected())) {
				Chat.sendMessage("Oh you are cheesing me, I guess I can't play a macro game");
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
		
		if (is_all_in && Game.army_killed() - Game.army_lost() < -400) {
			Chat.sendMessage("You held my attack! Really well done, but can you beat me if I go back to macro?");
			is_all_in = false;
			decide_build();
		}

	}


	private static void do_ravager_all_in() {
		is_all_in = true;
		Build.build = new ArrayList<>();
				Build.ideal_gases = 2;
				Build.ideal_hatches = 1;
				Build.max_queens = 1;
				Build.tech_drones = 16;
				Build.scout = true;
				Build.push_supply = 40;
				Build.ideal_workers = 22;
				Build.pull_off_gas = false;
	}
	
	// TODO replace hunter killer
	
	public static void decide_build() {
		switch (GameInfoCache.get_opponent_race()) {
			case PROTOSS:
				Build.build = new ArrayList<>(Arrays.asList(new ImmutablePair<Integer, UnitType>(13, Units.ZERG_OVERLORD),
															new ImmutablePair<Integer, UnitType>(16, Units.ZERG_HATCHERY),
															new ImmutablePair<Integer, UnitType>(18, Units.ZERG_EXTRACTOR),
															new ImmutablePair<Integer, UnitType>(17, Units.ZERG_SPAWNING_POOL)));
				Build.ideal_hatches = -1;
				Build.push_supply = 185;
				Build.ideal_workers = 70;
				Build.ideal_gases = 8;
				Build.tech_drones = 25;
				Build.pull_off_gas = true;
				Build.max_queens = -1;			
				break;
			case ZERG:
				Build.build = new ArrayList<>(Arrays.asList(new ImmutablePair<Integer, UnitType>(13, Units.ZERG_OVERLORD),
						new ImmutablePair<Integer, UnitType>(16, Units.ZERG_HATCHERY),
						new ImmutablePair<Integer, UnitType>(18, Units.ZERG_EXTRACTOR),
						new ImmutablePair<Integer, UnitType>(17, Units.ZERG_SPAWNING_POOL)));
				Build.ideal_hatches = -1;
				Build.scout = true;
				Build.push_supply = 150;
				Build.ideal_gases = 6;
				Build.ideal_workers = 65;
				Build.pull_off_gas = true;
				Build.tech_drones = 25;
				Build.max_queens = 4;		
				break;			
			case TERRAN:
				Build.build = new ArrayList<>(Arrays.asList(new ImmutablePair<Integer, UnitType>(13, Units.ZERG_OVERLORD),
						new ImmutablePair<Integer, UnitType>(16, Units.ZERG_HATCHERY),
						new ImmutablePair<Integer, UnitType>(18, Units.ZERG_EXTRACTOR),
						new ImmutablePair<Integer, UnitType>(17, Units.ZERG_SPAWNING_POOL)));
				Build.ideal_hatches = -1;
				Build.scout = true;
				Build.ideal_gases = 8;
				Build.push_supply = 170;
				Build.ideal_workers = 75;
				Build.max_queens = -1;
				Build.pull_off_gas = true;
				Build.tech_drones = 25;
				break;
			default:
				Build.build = new ArrayList<>(Arrays.asList(new ImmutablePair<Integer, UnitType>(13, Units.ZERG_OVERLORD),
						new ImmutablePair<Integer, UnitType>(16, Units.ZERG_HATCHERY),
						new ImmutablePair<Integer, UnitType>(18, Units.ZERG_EXTRACTOR),
						new ImmutablePair<Integer, UnitType>(17, Units.ZERG_SPAWNING_POOL)));
				Build.ideal_hatches = -1;
				Build.scout = true;
				Build.ideal_gases = 8;
				Build.push_supply = 190;
				Build.ideal_workers = 70;
				Build.pull_off_gas = true;	
		}
	}
}

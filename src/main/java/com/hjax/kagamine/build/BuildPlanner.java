package com.hjax.kagamine.build;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.game.Race;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.Chat;
import com.hjax.kagamine.economy.Base;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.game.HjaxUnit;
import com.hjax.kagamine.knowledge.Wisdom;

public class BuildPlanner {
	public static boolean worker_rush = false;
	public static boolean is_all_in;
	public static void on_frame() {
		if (Wisdom.worker_rush() && !worker_rush) {
			worker_rush = true;
			Build.build = new ArrayList<>(Arrays.asList(new ImmutablePair<>(13, Units.ZERG_OVERLORD),
					new ImmutablePair<>(14, Units.ZERG_SPAWNING_POOL)));
			Build.ideal_gases = 0;
			is_all_in = true;
		}
		
		if (GameInfoCache.count_enemy(Units.TERRAN_STARPORT_TECHLAB) > 1) {
			Build.pull_off_gas = false;
		}
		
		if (!is_all_in && GameInfoCache.get_opponent_race() != Race.ZERG) {
			if (GameInfoCache.count(Units.ZERG_DRONE) < 30 && (Wisdom.cannon_rush() || Wisdom.proxy_detected())) {
				do_ravager_all_in();
				if (GameInfoCache.count_friendly(Units.ZERG_HATCHERY) == 1) {
					for (HjaxUnit unit: GameInfoCache.get_units(Alliance.SELF, Units.ZERG_HATCHERY)) {
						if (!unit.done()) {
							unit.cancel();
							for (Base base: BaseManager.bases) {
								if (unit.distance(base.location) < 4) {
									base.set_walking_drone(null);
								}
							}
						}
					}
				}
			}
		}
		
		if (is_all_in && Game.army_killed() - Game.army_lost() < -400) {
			is_all_in = false;
			decide_build();
		}

	}


	private static void do_ravager_all_in() {
		is_all_in = true;
		Build.build = new ArrayList<>();
				Build.ideal_gases = 2;
				Build.pull_off_gas = false;
	}
	
	// TODO replace hunter killer
	
	public static void decide_build() {
		if (Game.race() == Race.ZERG) {
			switch (GameInfoCache.get_opponent_race()) {
			case PROTOSS:
				Build.build = new ArrayList<>(Arrays.asList(new ImmutablePair<>(13, Units.ZERG_OVERLORD),
						new ImmutablePair<>(16, Units.ZERG_HATCHERY),
						new ImmutablePair<>(18, Units.ZERG_EXTRACTOR),
						new ImmutablePair<>(17, Units.ZERG_SPAWNING_POOL)));
				Build.ideal_gases = 10;
				Build.pull_off_gas = true;	
				break;
			case ZERG:
				Build.build = new ArrayList<>(Arrays.asList(new ImmutablePair<>(13, Units.ZERG_OVERLORD),
						new ImmutablePair<>(16, Units.ZERG_HATCHERY),
						new ImmutablePair<>(18, Units.ZERG_EXTRACTOR),
						new ImmutablePair<>(17, Units.ZERG_SPAWNING_POOL)));
				Build.ideal_gases = 8;
				Build.pull_off_gas = true;
				break;			
			case TERRAN:
				Build.build = new ArrayList<>(Arrays.asList(new ImmutablePair<>(13, Units.ZERG_OVERLORD),
						new ImmutablePair<>(16, Units.ZERG_HATCHERY),
						new ImmutablePair<>(18, Units.ZERG_EXTRACTOR),
						new ImmutablePair<>(17, Units.ZERG_SPAWNING_POOL)));
				Build.ideal_gases = 10;
				Build.pull_off_gas = true;
				break;
			default:
				Build.build = new ArrayList<>(Arrays.asList(new ImmutablePair<>(13, Units.ZERG_OVERLORD),
						new ImmutablePair<>(16, Units.ZERG_HATCHERY),
						new ImmutablePair<>(18, Units.ZERG_EXTRACTOR),
						new ImmutablePair<>(17, Units.ZERG_SPAWNING_POOL)));
				Build.ideal_gases = 8;
				Build.pull_off_gas = true;	
			}
		} else {
			Build.build = new ArrayList<>(Arrays.asList(new ImmutablePair<>(14, Units.PROTOSS_PYLON),
					new ImmutablePair<>(16, Units.PROTOSS_GATEWAY),
					new ImmutablePair<>(16, Units.PROTOSS_ASSIMILATOR),
					new ImmutablePair<>(20, Units.PROTOSS_NEXUS),
					new ImmutablePair<>(20, Units.PROTOSS_CYBERNETICS_CORE),
					new ImmutablePair<>(21, Units.PROTOSS_ASSIMILATOR),
					new ImmutablePair<>(22, Units.PROTOSS_PYLON)));
			Build.ideal_gases = 2;
			Build.pull_off_gas = true;	
		}
	}
}

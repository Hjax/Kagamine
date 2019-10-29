package com.hjax.kagamine.knowledge;

import java.util.HashMap;
import java.util.Map;

import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.game.Race;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.army.ThreatManager;
import com.hjax.kagamine.build.ZergBuildExecutor;
import com.hjax.kagamine.build.BuildPlanner;
import com.hjax.kagamine.build.Composition;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.economy.EconomyManager;
import com.hjax.kagamine.enemymodel.EnemyModel;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.game.HjaxUnit;
import com.hjax.kagamine.game.RaceInterface;

public class ZergWisdom {
	
	public static int army_target() {
		int target = Math.max((int) (ThreatManager.threat() - Math.min(EconomyManager.larva_rate(), 6)), 3);
		
		if (target < 10) {
			if (Wisdom.all_in_detected()) target = 20;
			if (Wisdom.proxy_detected() && !Wisdom.cannon_rush()) target = 30;
		}
		return target;
	}

	public static boolean should_build_army() {
		
		if (GameInfoCache.get_opponent_race() == Race.ZERG && Game.worker_count() >= EnemyModel.enemyWorkers() && Game.army_supply() < 30) return true; 
		
		if (BuildPlanner.worker_rush && Game.army_supply() < 15) {
			return true;
		}
		
		if (ZergBuildExecutor.next_army_unit() == Units.INVALID) {
			return false;
		}
		
		if (Wisdom.all_in_detected() && GameInfoCache.get_opponent_race() == Race.ZERG && Game.army_supply() < 15) {
			return true;
		}
		
		if (GameInfoCache.get_opponent_race() == Race.ZERG) {
			if (GameInfoCache.count(Units.ZERG_BANELING) == 0 && Composition.full_comp().contains(Units.ZERG_BANELING) && GameInfoCache.count_friendly(Units.ZERG_BANELING_NEST) > 0) return true;
		}
		if (Wisdom.ahead() && Wisdom.shouldAttack()) return true;

		
		if (Game.army_supply() < army_target() || (ThreatManager.attacking_supply() > GameInfoCache.attacking_army_supply())) {
			if (ZergBuildExecutor.next_army_unit() != Units.INVALID) {
				return true;
			}
		}
		
		if (Wisdom.all_in_detected() && GameInfoCache.count(RaceInterface.get_race_worker()) > 25 * EnemyModel.enemyBaseCount() && Game.army_supply() < GameInfoCache.count(RaceInterface.get_race_worker()) + 15 * EnemyModel.enemyBaseCount() && GameInfoCache.count(RaceInterface.get_race_worker()) > 30) return true;

		return GameInfoCache.count(RaceInterface.get_race_worker()) >= Wisdom.worker_cap();
	}
	
	public static int queen_target() {
		int target = 0;
		
		if (Wisdom.all_in_detected()) return BaseManager.base_count();
		
		if (GameInfoCache.get_opponent_race() == Race.ZERG) {
			return Math.min(BaseManager.base_count() + 4, 6);
		}
		
		if (BaseManager.base_count() + GameInfoCache.in_progress(Units.ZERG_HATCHERY) < 3) {
			target = BaseManager.base_count();
		} else {
			target = Math.min(BaseManager.base_count() + 5, 10);
		}
		if (Game.minerals() > 400) {
			target += 1;
		}
		
		if (Game.army_supply() < 60 && EnemyModel.counts.getOrDefault(Units.TERRAN_BANSHEE, 0) >= 2) {
			target += 4;
		}
		
		if (Game.supply() > 120) target = Math.min(BaseManager.base_count() + 2, 4);
		if (Wisdom.proxy_detected()) {
			target = 1;
			if (Game.minerals() > 200) {
				target += 1;
			}
		}
		
		return target;
	}
	
	public static int needed_spine_count() {
		
		if (Game.army_supply() >= 2 && Game.army_supply() < 40) {
			
			if (EnemyModel.counts.getOrDefault(Units.ZERG_ROACH, 0) + EnemyModel.counts.getOrDefault(Units.ZERG_ROACH_WARREN, 0) > 0 && EnemyModel.enemyBaseCount() == 1) {
				return 4;
			}
			
			if (GameInfoCache.get_opponent_race() != Race.ZERG) {
				if (BaseManager.base_count() == 1 && Wisdom.proxy_detected()) {
					if (!Wisdom.cannon_rush()) {
						if (GameInfoCache.count_friendly(Units.ZERG_SPAWNING_POOL) > 0) {
							if (Wisdom.all_in_detected() || Wisdom.proxy_detected()) {
								return 3;
							}
						}
					}
				} else if (BaseManager.base_count() >= 2 && EnemyModel.enemyBaseCount() == 1) {
					if (GameInfoCache.count_friendly(Units.ZERG_SPAWNING_POOL) > 0) {
						if (Wisdom.all_in_detected() || Wisdom.proxy_detected()) {
							if (!Wisdom.cannon_rush()) {
								return (int) Math.min(Math.max((EnemyModel.enemyArmy() / 3.0), 3), 7);
							}
						}
					}
				}
			}
		}
		
		return 0;
		
	}
	
	private static final Map<Long, Boolean> should_build_queens_map = new HashMap<>();
	public static boolean should_build_queens() {
		return should_build_queens_map.computeIfAbsent(Game.get_frame(), frame -> {
			if (Wisdom.worker_rush() && GameInfoCache.count(Units.ZERG_LARVA) > 0) return false;
			if (Game.race() != Race.ZERG) return false;
			if (GameInfoCache.count_friendly(Units.ZERG_SPAWNING_POOL) == 0) return false;

			if (Composition.full_comp().contains(Units.ZERG_QUEEN) && GameInfoCache.count(Units.ZERG_QUEEN) < Composition.comp().getOrDefault(Units.ZERG_QUEEN, 0)) return true;
			
			if (should_build_army() && GameInfoCache.count(Units.ZERG_LARVA) > 0 && Game.army_supply() < 10) {
				return false;
			}
			
			return GameInfoCache.count(Units.ZERG_QUEEN) < queen_target() && GameInfoCache.count_friendly(Units.ZERG_SPAWNING_POOL) > 0;
		});
	}
	
	public static boolean should_build_workers() {
		
		/*
		 * Save larva for zvz cheese defense
		 */
		if (GameInfoCache.get_opponent_race() == Race.ZERG && EnemyModel.enemyBaseCount() == 1 && Game.worker_count() >= 14 && Game.army_supply() < 10 && Wisdom.all_in_detected()) {
			return false;
		}
		
		int minerals = EconomyManager.total_minerals();
		for (HjaxUnit in_progress : GameInfoCache.get_units(Alliance.SELF, Units.ZERG_HATCHERY)) {
			if (!in_progress.done() && in_progress.progress() > 0.6) {
				minerals += 16;
			}
		}
		if (Wisdom.all_in_detected() && EnemyModel.enemyWorkers() + 12 < GameInfoCache.count(RaceInterface.get_race_worker()) && Game.worker_count() > 30 && Composition.full_comp().contains(Units.ZERG_ZERGLING) && (Game.army_supply() < 30 * EnemyModel.enemyBaseCount())) {
			return false;
		}
		minerals += GameInfoCache.in_progress(Units.ZERG_EXTRACTOR) * 3 + BaseManager.active_gases() * 3;
		minerals -= GameInfoCache.in_progress(RaceInterface.get_race_worker());
		return (GameInfoCache.count(RaceInterface.get_race_worker()) < Wisdom.worker_cap() && GameInfoCache.count_friendly(RaceInterface.get_race_worker()) < minerals);
	}
	public static boolean should_expand() {
		if (Game.minerals() > 800) return true;
		if (GameInfoCache.in_progress(Units.ZERG_HATCHERY) > 1) return false;
		if (GameInfoCache.get_opponent_race() == Race.ZERG && Game.army_supply() < 10) return false;
		if ((Wisdom.cannon_rush() || Wisdom.proxy_detected() && BaseManager.base_count() >= EnemyModel.enemyBaseCount())) return false;
		if (Wisdom.all_in_detected() && BaseManager.base_count() < 4 && BaseManager.base_count() > EnemyModel.enemyBaseCount() && EconomyManager.total_minerals() >= EnemyModel.enemyBaseCount() * 8) return false;
		if (GameInfoCache.get_opponent_race() == Race.ZERG && Wisdom.all_in_detected() && BaseManager.base_count() < 4 && BaseManager.base_count() >= EnemyModel.enemyBaseCount() && EconomyManager.total_minerals() >= EnemyModel.enemyBaseCount() * 8) return false;
		if (BaseManager.base_count() < 3 && GameInfoCache.count(RaceInterface.get_race_worker()) > 23 && !should_build_army()) return true;
		//if (EconomyManager.total_minerals() + GameInfoCache.in_progress(Units.ZERG_HATCHERY) * 16 + BaseManager.active_gases() * 3 + 16 < GameInfoCache.count(Units.ZERG_DRONE)) return true;
		return EconomyManager.free_minerals() <= 9;
	}

}

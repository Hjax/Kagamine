package com.hjax.kagamine.knowledge;

import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.game.Race;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.Constants;
import com.hjax.kagamine.army.ThreatManager;
import com.hjax.kagamine.build.Build;
import com.hjax.kagamine.build.ZergBuildExecutor;
import com.hjax.kagamine.build.Composition;
import com.hjax.kagamine.economy.Base;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.economy.EconomyManager;
import com.hjax.kagamine.enemymodel.EnemyModel;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.game.HjaxUnit;
import com.hjax.kagamine.game.RaceInterface;

public class ZergWisdom {
	

	public static boolean should_build_army() {
		if (GameInfoCache.get_opponent_race() == Race.ZERG) {
			if (GameInfoCache.count(Units.ZERG_BANELING) == 0 && Composition.comp().contains(Units.ZERG_BANELING) && GameInfoCache.count_friendly(Units.ZERG_BANELING_NEST) > 0) return true;
		}
		if (Wisdom.ahead()) return true;
		
		if (GameInfoCache.get_opponent_race() == Race.ZERG && EnemyModel.enemyBaseCount() == 1 && Game.worker_count() >= 16) {
			return true;
		}
		
		double army_multiplier = 0.7;
		if (BaseManager.base_count() <= 4) {
			if (GameInfoCache.get_opponent_race() == Race.ZERG) {
				if (BaseManager.base_count() > EnemyModel.enemyBaseCount()) {
					army_multiplier = 1.3;
				}
			} else {
				if (BaseManager.base_count() > EnemyModel.enemyBaseCount() + 1) {
					army_multiplier = 1.3;
				}
			}
			if (Wisdom.all_in_detected()) {
				army_multiplier = 1.1;
			}
		}
		if (EnemyModel.enemyWorkers() + 8 < Game.worker_count()) army_multiplier += 0.4;
		if (ThreatManager.under_attack()) army_multiplier += 0.4;
		int target = (int) Math.max(2 + 2 * GameInfoCache.count(Units.ZERG_QUEEN), EnemyModel.enemyArmy() * army_multiplier + GameInfoCache.count(Units.ZERG_QUEEN) * 2);
		//if (GameInfoCache.get_opponent_race() == Race.ZERG) target = 15;
		if (target < 10) {
			if (Wisdom.all_in_detected()) target = 10;
			if (Wisdom.proxy_detected()) target = 30;
		}
		if (Game.army_supply() < target || (ThreatManager.under_attack() && Game.army_supply() < EnemyModel.enemyArmy() * 2)) {
			if (ZergBuildExecutor.next_army_unit() != Units.INVALID) {
				return true;
			}
		}
		
		if (GameInfoCache.attacking_army_supply() * Math.max(EnemyModel.enemyBaseCount(), 1) < GameInfoCache.count(RaceInterface.get_race_worker()) && GameInfoCache.count(RaceInterface.get_race_worker()) > (10 + 20 * Math.max(EnemyModel.enemyBaseCount(), 1))) {
			return true;
		}
		
		if (Wisdom.all_in_detected() && Game.army_supply() < GameInfoCache.count(RaceInterface.get_race_worker()) && GameInfoCache.count(RaceInterface.get_race_worker()) > 30) return true;
		if (GameInfoCache.count(RaceInterface.get_race_worker()) > 50 && Game.gas() > 100 && ZergBuildExecutor.next_army_unit() == Units.ZERG_MUTALISK && GameInfoCache.count(Units.ZERG_MUTALISK) < 10) return true;
		return GameInfoCache.count(RaceInterface.get_race_worker()) >= Wisdom.worker_cap();
	}
	
	public static boolean should_build_queens() {
		if (Wisdom.worker_rush()) return false;
		if (Game.race() != Race.ZERG) return false;
		if (GameInfoCache.count_friendly(Units.ZERG_SPAWNING_POOL) == 0) return false;

		if (Composition.comp().contains(Units.ZERG_QUEEN) && GameInfoCache.count(Units.ZERG_QUEEN) < 25) return true;
		
		if (should_build_army() && GameInfoCache.count(Units.ZERG_LARVA) > 0) {
			return false;
		}
		
		if (GameInfoCache.count_friendly(Units.ZERG_CREEP_TUMOR) > 20 && GameInfoCache.count(Units.ZERG_QUEEN) >= 3) {
			return false;
		}
		
		if (ThreatManager.under_attack() && GameInfoCache.count(Units.ZERG_LARVA) > 0) return false; 
		if (GameInfoCache.count(RaceInterface.get_race_worker()) < 35 && GameInfoCache.count(Units.ZERG_LARVA) > 0 && GameInfoCache.count(Units.ZERG_QUEEN) < 2 && GameInfoCache.count(RaceInterface.get_race_worker()) > 15) return false; 
		
		int queen_target = 0;
		if (Build.max_queens == -1) {
			if (GameInfoCache.count(Units.ZERG_HATCHERY) < 3) {
				queen_target = BaseManager.base_count() - 1;
			} else {
				queen_target = Math.min(BaseManager.base_count() + 4, 12);
			}
			if (Game.minerals() > 400) {
				queen_target += 1;
			}
		} else {
			queen_target = Build.max_queens;
		}
		if (Game.supply() > 120) queen_target = Math.min(BaseManager.base_count(), 6);
		if (Wisdom.proxy_detected()) {
			queen_target = 1;
			if (Game.minerals() > 200) {
				queen_target += 1;
			}
		}
		if (GameInfoCache.count(Units.ZERG_QUEEN) < queen_target && GameInfoCache.count_friendly(Units.ZERG_SPAWNING_POOL) > 0) {
			return true;
		}
		return false;
	}
	
	public static boolean should_build_workers() {
		
		if (EconomyManager.free_minerals() == 0) return false;
		if (Wisdom.all_in_detected() && EnemyModel.enemyWorkers() + 12 < GameInfoCache.count(RaceInterface.get_race_worker())) {
			return false;
		}

		return (GameInfoCache.count(RaceInterface.get_race_worker()) < Wisdom.worker_cap());
	}
	public static boolean should_expand() {
		if (Wisdom.all_in_detected() && BaseManager.base_count() < 4 && BaseManager.base_count() > EnemyModel.enemyBaseCount() && EconomyManager.total_minerals() >= EnemyModel.enemyBaseCount() * 8) return false;
		if (GameInfoCache.get_opponent_race() == Race.ZERG && Wisdom.all_in_detected() && BaseManager.base_count() < 4 && BaseManager.base_count() >= EnemyModel.enemyBaseCount() && EconomyManager.total_minerals() >= EnemyModel.enemyBaseCount() * 8) return false;
		if (BaseManager.base_count() < 3 && GameInfoCache.count(RaceInterface.get_race_worker()) > 23) return true;
		return EconomyManager.free_minerals() <= 4 && ((BaseManager.base_count() < Build.ideal_hatches) || (Build.ideal_hatches == -1));
	}

}

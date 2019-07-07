package com.hjax.kagamine.knowledge;

import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.game.Race;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.Constants;
import com.hjax.kagamine.army.ThreatManager;
import com.hjax.kagamine.build.Build;
import com.hjax.kagamine.build.BuildExecutor;
import com.hjax.kagamine.build.Composition;
import com.hjax.kagamine.economy.Base;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.economy.EconomyManager;
import com.hjax.kagamine.enemymodel.EnemyModel;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.game.HjaxUnit;

public class Wisdom {
	
	public static boolean early_cheese = false;
	
	public static boolean proxy_detected() {
		if (Game.army_supply() >= 30) return false;
		for (HjaxUnit u: GameInfoCache.get_units(Alliance.ENEMY)) {
			if (Game.is_structure(u.type()) && u.type() != Units.PROTOSS_PYLON) {
				if (u.distance(BaseManager.main_base().location) < u.distance(Scouting.closest_enemy_spawn())) {
					return true;
				}
			}
		}
		return false;
	}
	public static boolean air_detected() {
		for (HjaxUnit u: GameInfoCache.get_units(Alliance.ENEMY)) {
			if (u.type() == Units.ZERG_SPIRE || u.type() == Units.TERRAN_STARPORT || u.type() == Units.PROTOSS_STARGATE) {
				return true;
			}
		}
		return false;
	}
	public static boolean all_in_detected() {
		if (EnemyModel.counts.getOrDefault(Units.ZERG_ZERGLING, 0) > 0 && GameInfoCache.count_friendly(Units.ZERG_SPAWNING_POOL) == 0) {
			early_cheese = true;
		}
		if (Game.get_frame() < 2 * 60 * Constants.FPS) {
			if (early_cheese) {
				return true;
			}
		}
		if (Game.army_supply() >= 60) return false;
		if (enemy_bases() > 3) return false;
		return enemy_production() >= 3 * Math.max(enemy_bases(), 1);
	}

	public static int enemy_bases() {
		int result = 0;
		for (HjaxUnit u: GameInfoCache.get_units(Alliance.ENEMY)) {
			if (Game.is_town_hall(u.type())) result++;
		}
		return result;
	}
	public static boolean play_safe() {
		return enemy_bases() <= 1;
	}
	public static boolean cannon_rush() {
		for (HjaxUnit u: GameInfoCache.get_units(Alliance.ENEMY, Units.PROTOSS_PHOTON_CANNON)) {
			if (u.distance(BaseManager.main_base().location) < u.distance(Scouting.closest_enemy_spawn())) {
				return true;
			}
		}
		return false;
	}
	
	public static int enemy_production() {
		int production = 0;
		for (HjaxUnit u: GameInfoCache.get_units(Alliance.ENEMY)) {
			if (Balance.is_production_structure(u.type())) {
				production++;
			}
		}
		return production;
	}
	
	public static boolean confused() {
		if (GameInfoCache.get_opponent_race() == Race.ZERG) return false;
		return enemy_production() == 0 && enemy_bases() >= 1;
	}
	
	public static boolean ahead() {
		int[] res = EnemyModel.resourceEstimate();
		return EnemyModel.enemyArmy() + ((Math.max(res[0], 0) + Math.max(res[1], 0) * 2) / 100) < GameInfoCache.attacking_army_supply();
	}
	
	private static long shouldAttackFrame = -1;
	private static boolean shouldAttack = false;
	public static boolean shouldAttack() {
		if (shouldAttackFrame != Game.get_frame()) {
			shouldAttackFrame = Game.get_frame();
			if (EnemyModel.enemyArmy() < 5) return true;
			if (Game.supply() >= Build.push_supply) return true;
			if (all_in_detected() && GameInfoCache.attacking_army_supply() > (2.2 * EnemyModel.enemyArmy())) return false;
			else if (GameInfoCache.get_opponent_race() == Race.ZERG && Game.army_supply() < 25 && (Game.army_supply() - GameInfoCache.count_friendly(Units.ZERG_QUEEN) * 2) > 5) {
				shouldAttack = ahead() || (GameInfoCache.attacking_army_supply() > (1.2 * EnemyModel.enemyArmy())) || ((GameInfoCache.attacking_army_supply() > (EnemyModel.enemyArmy())) && (GameInfoCache.count_friendly(Units.ZERG_DRONE) < (EnemyModel.enemyWorkers() - 6)));
			} else {
				shouldAttack = ahead() || (GameInfoCache.attacking_army_supply() > (1.2 * EnemyModel.enemyArmy() + GameInfoCache.count_enemy(Units.TERRAN_BUNKER) * 8 + GameInfoCache.count_enemy(Units.PROTOSS_PHOTON_CANNON) * 4)) || ((GameInfoCache.attacking_army_supply() > (EnemyModel.enemyArmy() * 1.1 + GameInfoCache.count_enemy(Units.PROTOSS_PHOTON_CANNON) * 4)) && (GameInfoCache.count_friendly(Units.ZERG_DRONE) < (EnemyModel.enemyWorkers() - 10)));
			}
		}
		return shouldAttack;
	}
	
	public static boolean worker_rush() {
		int total = 0;
		outer: for (HjaxUnit enemy: GameInfoCache.get_units(Alliance.ENEMY)) {
			if (Game.is_worker(enemy.type())) {
				for (Base b: BaseManager.bases) {
					if (b.has_friendly_command_structure() && b.command_structure.done()) {
						if (enemy.distance(b.location) < 15) {
							total++;
							continue outer;
						}
					}
				}
			}
		}
		return total >= 5;
	}
	public static boolean should_build_army() {
		if (GameInfoCache.get_opponent_race() == Race.ZERG) {
			if (GameInfoCache.count(Units.ZERG_BANELING) == 0 && Composition.comp().contains(Units.ZERG_BANELING) && GameInfoCache.count_friendly(Units.ZERG_BANELING_NEST) > 0) return true;
		}
		if (ahead()) return true;
		
		if (GameInfoCache.get_opponent_race() == Race.ZERG && EnemyModel.enemyBaseCount() == 1 && Game.worker_count() >= 16) {
			return true;
		}
		
		double army_multiplier = 0.6;
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
			if (all_in_detected()) {
				army_multiplier = 1.1;
			}
		}
		if (EnemyModel.enemyWorkers() + 8 < Game.worker_count()) army_multiplier += 0.4;
		if (ThreatManager.under_attack()) army_multiplier += 0.4;
		int target = (int) Math.max(2 + 2 * GameInfoCache.count(Units.ZERG_QUEEN), EnemyModel.enemyArmy() * army_multiplier + GameInfoCache.count(Units.ZERG_QUEEN) * 2);
		//if (GameInfoCache.get_opponent_race() == Race.ZERG) target = 15;
		if (target < 10) {
			if (all_in_detected()) target = 10;
			if (proxy_detected()) target = 30;
		}
		if (Game.army_supply() < target || (ThreatManager.under_attack() && Game.army_supply() < EnemyModel.enemyArmy() * 2)) {
			if (BuildExecutor.next_army_unit() != Units.INVALID) {
				return true;
			}
		}
		
		if (GameInfoCache.attacking_army_supply() * Math.max(EnemyModel.enemyBaseCount(), 1) < GameInfoCache.count(Units.ZERG_DRONE) && GameInfoCache.count(Units.ZERG_DRONE) > (10 + 20 * Math.max(EnemyModel.enemyBaseCount(), 1))) {
			return true;
		}
		
		if (all_in_detected() && Game.army_supply() < GameInfoCache.count(Units.ZERG_DRONE) && GameInfoCache.count(Units.ZERG_DRONE) > 30) return true;
		if (GameInfoCache.count(Units.ZERG_DRONE) > 50 && Game.gas() > 100 && BuildExecutor.next_army_unit() == Units.ZERG_MUTALISK && GameInfoCache.count(Units.ZERG_MUTALISK) < 10) return true;
		return GameInfoCache.count(Units.ZERG_DRONE) >= Wisdom.worker_cap();
	}
	
	public static boolean should_build_queens() {
		if (worker_rush()) return false;
		if (GameInfoCache.count_friendly(Units.ZERG_SPAWNING_POOL) == 0) return false;

		if (Composition.comp().contains(Units.ZERG_QUEEN) && GameInfoCache.count(Units.ZERG_QUEEN) < 25) return true;
		
		if (should_build_army() && GameInfoCache.count(Units.ZERG_LARVA) > 0) {
			return false;
		}
		
		if (GameInfoCache.count_friendly(Units.ZERG_CREEP_TUMOR) > 20 && GameInfoCache.count(Units.ZERG_QUEEN) >= 3) {
			return false;
		}
		
		if (ThreatManager.under_attack() && GameInfoCache.count(Units.ZERG_LARVA) > 0) return false; 
		if (GameInfoCache.count(Units.ZERG_DRONE) < 35 && GameInfoCache.count(Units.ZERG_LARVA) > 0 && GameInfoCache.count(Units.ZERG_QUEEN) < 2 && GameInfoCache.count(Units.ZERG_DRONE) > 15) return false; 
		
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
		if (proxy_detected()) {
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
	
	public static boolean should_build_drones() {
		if (EconomyManager.free_minerals() == 0) return false;
		if (all_in_detected() && EnemyModel.enemyWorkers() + 12 < GameInfoCache.count(Units.ZERG_DRONE)) {
			return false;
		}

		return (GameInfoCache.count(Units.ZERG_DRONE) < Wisdom.worker_cap());
	}
	public static boolean should_expand() {
		if (all_in_detected() && BaseManager.base_count() < 4 && BaseManager.base_count() > EnemyModel.enemyBaseCount() && EconomyManager.total_minerals() >= EnemyModel.enemyBaseCount() * 8) return false;
		if (GameInfoCache.get_opponent_race() == Race.ZERG && all_in_detected() && BaseManager.base_count() < 4 && BaseManager.base_count() >= EnemyModel.enemyBaseCount() && EconomyManager.total_minerals() >= EnemyModel.enemyBaseCount() * 8) return false;
		if (BaseManager.base_count() < 3 && GameInfoCache.count(Units.ZERG_DRONE) > 23) return true;
		return EconomyManager.free_minerals() <= 4 && ((BaseManager.base_count() < Build.ideal_hatches) || (Build.ideal_hatches == -1));
	}
	public static int worker_cap() {
		int drone_target = 100;
		return Math.min(drone_target, Build.ideal_workers);
	}
}

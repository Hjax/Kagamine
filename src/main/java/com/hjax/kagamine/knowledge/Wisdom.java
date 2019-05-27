package com.hjax.kagamine.knowledge;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
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
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;

public class Wisdom {
	public static boolean proxy_detected() {
		if (Game.army_supply() >= 30) return false;
		for (UnitInPool u: GameInfoCache.get_units(Alliance.ENEMY)) {
			if (Game.is_structure(u.unit().getType()) && u.unit().getType() != Units.PROTOSS_PYLON) {
				if (u.unit().getPosition().toPoint2d().distance(BaseManager.main_base().location) < u.unit().getPosition().toPoint2d().distance(Scouting.closest_enemy_spawn())) {
					return true;
				}
			}
		}
		return false;
	}
	public static boolean air_detected() {
		for (UnitInPool u: GameInfoCache.get_units(Alliance.ENEMY)) {
			if (u.unit().getType() == Units.ZERG_SPIRE || u.unit().getType() == Units.TERRAN_STARPORT || u.unit().getType() == Units.PROTOSS_STARGATE) {
				return true;
			}
		}
		return false;
	}
	public static boolean all_in_detected() {
		if (Game.army_supply() >= 30) return false;
		int t1 = GameInfoCache.count_enemy(Units.TERRAN_BARRACKS) + GameInfoCache.count_enemy(Units.PROTOSS_GATEWAY);
		return t1 >= 3 * Math.max(enemy_bases(), 1);
	}
	public static boolean aggression_detected() {
		if (Game.army_supply() >= 30) return false;
		int t1 = GameInfoCache.count_enemy(Units.TERRAN_BARRACKS) + GameInfoCache.count_enemy(Units.PROTOSS_GATEWAY);
		return t1 >= 2 * Math.max(enemy_bases(), 1);
	}
	public static int enemy_bases() {
		int result = 0;
		for (UnitInPool u: GameInfoCache.get_units(Alliance.ENEMY)) {
			if (Game.is_town_hall(u.unit().getType())) result++;
		}
		return result;
	}
	public static boolean play_safe() {
		return enemy_bases() <= 1;
	}
	public static boolean cannon_rush() {
		for (UnitInPool u: GameInfoCache.get_units(Alliance.ENEMY, Units.PROTOSS_PHOTON_CANNON)) {
			if (u.unit().getPosition().toPoint2d().distance(BaseManager.main_base().location) < u.unit().getPosition().toPoint2d().distance(Scouting.closest_enemy_spawn())) {
				return true;
			}
		}
		return false;
	}
	
	public static int enemy_production() {
		int production = 0;
		for (UnitInPool u: GameInfoCache.get_units(Alliance.ENEMY)) {
			if (Balance.is_production_structure(u.unit().getType())) {
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
		return Game.army_killed() - Game.army_lost() > (200 * ((Game.get_frame() / Constants.FPS)/ 60.0));
	}
	
	private static long shouldAttackFrame = -1;
	private static boolean shouldAttack = false;
	public static boolean shouldAttack() {
		if (shouldAttackFrame != Game.get_frame()) {
			shouldAttackFrame = Game.get_frame();
			if (EnemyModel.enemyArmy() < 5) return true;
			else if (GameInfoCache.get_opponent_race() == Race.ZERG && Game.army_supply() < 25 && (Game.army_supply() - GameInfoCache.count_friendly(Units.ZERG_QUEEN) * 2) > 5) {
				shouldAttack = ahead() || (GameInfoCache.attacking_army_supply() > (2 * EnemyModel.enemyArmy())) || ((GameInfoCache.attacking_army_supply() > (EnemyModel.enemyArmy())) && (GameInfoCache.count_friendly(Units.ZERG_DRONE) < (EnemyModel.enemyWorkers() - 6)));
			} else {
				shouldAttack = ahead() || (GameInfoCache.attacking_army_supply() > (2 * EnemyModel.enemyArmy() + GameInfoCache.count_enemy(Units.PROTOSS_PHOTON_CANNON) * 4)) || ((GameInfoCache.attacking_army_supply() > (EnemyModel.enemyArmy() * 1.5 + GameInfoCache.count_enemy(Units.PROTOSS_PHOTON_CANNON) * 4)) && (GameInfoCache.count_friendly(Units.ZERG_DRONE) < (EnemyModel.enemyWorkers() - 10)));
			}
		}
		return shouldAttack;
	}
	
	public static boolean worker_rush() {
		int total = 0;
		outer: for (UnitInPool enemy: GameInfoCache.get_units(Alliance.ENEMY)) {
			if (Game.is_worker(enemy.unit().getType())) {
				for (Base b: BaseManager.bases) {
					if (b.has_friendly_command_structure() && b.command_structure.unit().getBuildProgress() > 0.999) {
						if (enemy.unit().getPosition().toPoint2d().distance(b.location) < 15) {
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
			if (BuildExecutor.count(Units.ZERG_BANELING) == 0 && Composition.comp().contains(Units.ZERG_BANELING) && GameInfoCache.count_friendly(Units.ZERG_BANELING_NEST) > 0) return true;
		}
		if (ahead()) return true;
		double army_multiplier = 1.2;
		if (BaseManager.base_count(Alliance.SELF) <= 4) {
			if (GameInfoCache.get_opponent_race() == Race.ZERG) {
				if (BaseManager.base_count(Alliance.SELF) > EnemyModel.enemyBaseCount()) {
					army_multiplier = 2;
				}
			} else {
				if (BaseManager.base_count(Alliance.SELF) > EnemyModel.enemyBaseCount() + 1) {
					army_multiplier = 2;
				}
			}
		}
		int target = (int) Math.max(2 + 2 * BuildExecutor.count(Units.ZERG_QUEEN), EnemyModel.enemyArmy() * army_multiplier + BuildExecutor.count(Units.ZERG_QUEEN) * 2);
		//if (GameInfoCache.get_opponent_race() == Race.ZERG) target = 15;
		if (all_in_detected()) target = 10;
		if (proxy_detected()) target = 10;
		if (Game.army_supply() < target || (ThreatManager.under_attack() && Game.army_supply() < EnemyModel.enemyArmy() * 2)) {
			if (BuildExecutor.next_army_unit() != Units.INVALID) {
				return true;
			}
		}
		
		if (GameInfoCache.attacking_army_supply() * Math.max(EnemyModel.enemyBaseCount(), 1) < BuildExecutor.count(Units.ZERG_DRONE) && BuildExecutor.count(Units.ZERG_DRONE) > (10 + 20 * Math.max(EnemyModel.enemyBaseCount(), 1))) {
			return true;
		}
		
		if (all_in_detected() && Game.army_supply() < BuildExecutor.count(Units.ZERG_DRONE) && BuildExecutor.count(Units.ZERG_DRONE) > 30) return true;
		if (BuildExecutor.count(Units.ZERG_DRONE) > 50 && Game.gas() > 100 && BuildExecutor.next_army_unit() == Units.ZERG_MUTALISK && BuildExecutor.count(Units.ZERG_MUTALISK) < 10) return true;
		return BuildExecutor.count(Units.ZERG_DRONE) >= BuildExecutor.worker_cap();
	}
	
	public static boolean should_build_queens() {
		if (worker_rush()) return false;
		if (GameInfoCache.count_friendly(Units.ZERG_SPAWNING_POOL) == 0) return false;
		
		if (Composition.comp().contains(Units.ZERG_QUEEN) && BuildExecutor.count(Units.ZERG_QUEEN) < 25) return true;
		
		if (ThreatManager.under_attack() && BuildExecutor.count(Units.ZERG_LARVA) > 0) return false; 
		if (BuildExecutor.count(Units.ZERG_DRONE) < 35 && BuildExecutor.count(Units.ZERG_LARVA) > 0 && BuildExecutor.count(Units.ZERG_QUEEN) < 2 && BuildExecutor.count(Units.ZERG_DRONE) > 15) return false; 
		
		int queen_target = 0;
		if (Build.max_queens == -1) {
			if (BuildExecutor.count(Units.ZERG_HATCHERY) < 3) {
				queen_target = BaseManager.base_count(Alliance.SELF) - 1;
			} else {
				queen_target = Math.min(BaseManager.base_count(Alliance.SELF) + 4, 12);
			}
			if (Game.minerals() > 400) {
				queen_target += 1;
			}
		} else {
			queen_target = Build.max_queens;
		}
		if (Game.supply() > 120) queen_target = Math.min(BaseManager.base_count(Alliance.SELF), 6);
		if (proxy_detected()) {
			queen_target = 1;
			if (Game.minerals() > 200) {
				queen_target += 1;
			}
		}
		if (BuildExecutor.count(Units.ZERG_QUEEN) < queen_target && GameInfoCache.count_friendly(Units.ZERG_SPAWNING_POOL) > 0) {
			return true;
		}
		return false;
	}
	
	public static boolean should_build_drones() {
		if (EconomyManager.free_minerals() == 0) return false;
		return (BuildExecutor.count(Units.ZERG_DRONE) < BuildExecutor.worker_cap());
	}
}

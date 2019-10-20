package com.hjax.kagamine.knowledge;

import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.data.Upgrades;
import com.github.ocraft.s2client.protocol.game.Race;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.Constants;
import com.hjax.kagamine.build.Build;
import com.hjax.kagamine.economy.Base;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.enemymodel.EnemyModel;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.game.HjaxUnit;
import com.hjax.kagamine.game.RaceInterface;

public class Wisdom {
	
	
	private static boolean early_cheese = false;
	
	public static boolean proxy_detected() {
		if (EnemyModel.enemyBaseCount() > 1) return false;
		for (HjaxUnit u: GameInfoCache.get_units(Alliance.ENEMY)) {
			if (Game.is_structure(u.type()) && u.type() != Units.PROTOSS_PYLON) {
				if (u.distance(BaseManager.main_base().location) < u.distance(Scouting.closest_enemy_spawn())) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean all_in_detected() {
		if (EnemyModel.enemyBaseCount() > 2) return false;
		if (GameInfoCache.get_opponent_race() == Race.ZERG && EnemyModel.enemyBaseCount() == 1) return true;
		if (EnemyModel.counts.getOrDefault(Units.ZERG_ZERGLING, 0) > 0 && GameInfoCache.count_friendly(Units.ZERG_SPAWNING_POOL) == 0) {
			early_cheese = true;
		}
		if (Game.get_frame() < 4.0 * 60 * Constants.FPS) {
			if (early_cheese) {
				return true;
			}
		}
		if (Game.army_supply() >= 85) return false;
		if (Game.worker_count() > 40 && EnemyModel.enemyBaseCount() == 2 && GameInfoCache.get_opponent_race() == Race.PROTOSS) return true;
		return enemy_production() >= 3 * Math.max(EnemyModel.enemyBaseCount(), 1);
	}

	public static boolean cannon_rush() {
		for (HjaxUnit u: GameInfoCache.get_units(Alliance.ENEMY, Units.PROTOSS_PHOTON_CANNON)) {
			if (u.distance(BaseManager.main_base().location) < u.distance(Scouting.closest_enemy_spawn())) {
				return true;
			}
		}
		return false;
	}
	
	private static int enemy_production() {
		int production = 0;
		for (HjaxUnit u: GameInfoCache.get_units(Alliance.ENEMY)) {
			if (Balance.is_production_structure(u.type()) && u.type() != Units.TERRAN_STARPORT) {
				production++;
			}
		}
		return production;
	}
	
	public static boolean confused() {
		if (GameInfoCache.get_opponent_race() == Race.ZERG) return false;
		return enemy_production() == 0 && Game.worker_count() > 20;
	}
	
	public static boolean ahead() {
		int[] res = EnemyModel.resourceEstimate();
		if ((Game.army_killed() - Game.army_lost()) < 5000) return false;
		return EnemyModel.enemyArmy() + EnemyModel.enemyBaseCount() * 5 + ((Math.max(res[0], 0) + Math.max(res[1], 0) * 2) / 100) < GameInfoCache.attacking_army_supply();
	}
	
	public static double army_ratio() {
		
		double my_army = GameInfoCache.attacking_army_supply();
		double enemy_army = EnemyModel.enemyArmy();
		
		int bonus = 0;
		for (HjaxUnit enemy : GameInfoCache.get_units(Alliance.ENEMY)) {
			if (enemy.done()) {
				if (enemy.type() == Units.TERRAN_PLANETARY_FORTRESS) bonus += 20;
				if (enemy.type() == Units.TERRAN_BUNKER) bonus += 10;
				if (enemy.type() == Units.PROTOSS_PHOTON_CANNON) bonus += 6;
			}
		}
		
		bonus = Math.min(bonus, 25);
		
		return (my_army / (enemy_army + bonus));
		
	}
	
	private static long shouldAttackFrame = -1;
	private static boolean shouldAttack = false;
	public static boolean shouldAttack() {
		if (shouldAttackFrame != Game.get_frame()) {
			shouldAttackFrame = Game.get_frame();
			
			if (all_in_detected() && army_ratio() < 2.0) {
				shouldAttack = false;
				return false;
			}
			
			if (!Game.has_upgrade(Upgrades.ZERGLING_MOVEMENT_SPEED)) return false;
			
			if (Game.supply() >= 190 || (shouldAttack && Game.supply() >= 150 && army_ratio() > 0.8)) {
				shouldAttack = true;
				return true;
			}
			
			if ((Game.army_killed() - Game.army_lost()) < -1000 && EnemyModel.enemyBaseCount() == 1) {
				shouldAttack = false;
				return false;
			}
			
			if (EnemyModel.enemyArmy() < 1) {
				shouldAttack = true;
				return shouldAttack;
			}
			
			if ((army_ratio() > 1.2 && shouldAttack) || (army_ratio() > 1.6)) {
				shouldAttack = true;
				return true;
			}
			
			if (EnemyModel.enemyWorkers() < GameInfoCache.count(RaceInterface.get_race_worker()) - 20 && army_ratio() < 2.0) {
				shouldAttack = false;
				return false;
			}
			
			shouldAttack = false;
			return false;
			
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
	
	public static boolean should_build_workers() {
		if (Game.race() == Race.ZERG) {
			return ZergWisdom.should_build_workers();
		} else if (Game.race() == Race.PROTOSS) {
			return ProtossWisdom.should_build_workers();
		}
		return true;
	}
	
	public static boolean should_build_army() {
		if (Game.race() == Race.ZERG) {
			return ZergWisdom.should_build_army();
		} else if (Game.race() == Race.PROTOSS) {
			return ProtossWisdom.should_build_army();
		}
		return true;
	}
	
	public static boolean should_expand() {
		if (Game.race() == Race.ZERG) {
			return ZergWisdom.should_expand();
		} else if (Game.race() == Race.PROTOSS) {
			return ProtossWisdom.should_expand();
		}
		return false;
	}
	
	public static int worker_cap() {
		return 85;
	}
}

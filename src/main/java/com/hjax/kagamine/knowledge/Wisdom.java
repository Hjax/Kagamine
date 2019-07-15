package com.hjax.kagamine.knowledge;

import com.github.ocraft.s2client.protocol.data.Units;
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
		return EnemyModel.enemyArmy() + EnemyModel.enemyBaseCount() * 5 + ((Math.max(res[0], 0) + Math.max(res[1], 0) * 2) / 100) < GameInfoCache.attacking_army_supply();
	}
	
	private static long shouldAttackFrame = -1;
	private static boolean shouldAttack = false;
	public static boolean shouldAttack() {
		if (shouldAttackFrame != Game.get_frame()) {
			shouldAttackFrame = Game.get_frame();
			if (EnemyModel.enemyArmy() < 1) {
				shouldAttack = true;
				return shouldAttack;
			}
			if (Game.supply() >= Build.push_supply) {
				shouldAttack = true;
				return true;
			}
			if (all_in_detected() && GameInfoCache.attacking_army_supply() < (2.4 * EnemyModel.enemyArmy())) {
				shouldAttack = false;
				return false;
			}
			if (EnemyModel.enemyWorkers() < GameInfoCache.count(RaceInterface.get_race_worker()) - 20) {
				shouldAttack = false;
				return false;
			}
			else if (GameInfoCache.get_opponent_race() == Race.ZERG && Game.army_supply() < 25 && (Game.army_supply() - GameInfoCache.count_friendly(Units.ZERG_QUEEN) * 2) > 5) {
				shouldAttack = ahead() || (GameInfoCache.attacking_army_supply() > (1.3 * EnemyModel.enemyArmy())) || ((GameInfoCache.attacking_army_supply() > (EnemyModel.enemyArmy())) && (GameInfoCache.count_friendly(RaceInterface.get_race_worker()) < (EnemyModel.enemyWorkers() - 6)));
			} else {
				shouldAttack = ahead() || (GameInfoCache.attacking_army_supply() > (1.3 * EnemyModel.enemyArmy() + GameInfoCache.count_enemy(Units.TERRAN_PLANETARY_FORTRESS) * 8 + GameInfoCache.count_enemy(Units.TERRAN_BUNKER) * 8 + GameInfoCache.count_enemy(Units.PROTOSS_PHOTON_CANNON) * 4)) || ((GameInfoCache.attacking_army_supply() > (EnemyModel.enemyArmy() * 1.1 + GameInfoCache.count_enemy(Units.PROTOSS_PHOTON_CANNON) * 4)) && (GameInfoCache.count_friendly(RaceInterface.get_race_worker()) < (EnemyModel.enemyWorkers() - 10)));
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
		int drone_target = 100;
		return Math.min(drone_target, Build.ideal_workers);
	}
}

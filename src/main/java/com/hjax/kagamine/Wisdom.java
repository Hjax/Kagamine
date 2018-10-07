package com.hjax.kagamine;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.game.Race;
import com.github.ocraft.s2client.protocol.unit.Alliance;

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
		return t1 >= 3 * enemy_bases() && enemy_bases() != 0;
	}
	public static boolean aggression_detected() {
		if (Game.army_supply() >= 30) return false;
		int t1 = GameInfoCache.count_enemy(Units.TERRAN_BARRACKS) + GameInfoCache.count_enemy(Units.PROTOSS_GATEWAY);
		return t1 >= 2 * enemy_bases() && enemy_bases() == 1;
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
		if (Game.get_opponent_race() == Race.ZERG) return false;
		return enemy_production() == 0 && enemy_bases() >= 1;
	}
	
	public static boolean ahead() {
		return Game.army_killed() - Game.army_lost() > (200 * ((Game.get_frame() / Constants.FPS)/ 60.0));
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
}

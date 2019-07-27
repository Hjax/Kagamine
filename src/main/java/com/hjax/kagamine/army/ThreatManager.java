package com.hjax.kagamine.army;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.game.Race;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.economy.Base;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.enemymodel.EnemyModel;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.game.HjaxUnit;

public class ThreatManager {
	
	public static double threat = 0;
	public static double attacking_threat = 0;
	
	public static void on_frame() {
		Point2d center;
		
		threat = 0;
		attacking_threat = 0;
		
		for (Set<HjaxUnit> squad : EnemySquadManager.enemy_squads) {
			center = EnemySquadManager.average_point(new ArrayList<>(squad));
			Base closest = null;
			for (Base b : BaseManager.bases) {
				if (closest == null || b.location.distance(center) < closest.location.distance(center)) {
					closest = b;
				}
			}
			
			double supply = total_supply(new ArrayList<>(squad));
			
			boolean only_medivacs = true;
			
			for (HjaxUnit u: squad) {
				if (u.type() != Units.TERRAN_MEDIVAC) {
					only_medivacs = false;
					break;
				}
			}
			
			if (only_medivacs) supply *= 10;
			
			if (closest.has_friendly_command_structure()) {
				threat += supply * 1.5;
				attacking_threat += supply;
			} else if (closest.has_enemy_command_structure()) {
				if (GameInfoCache.get_opponent_race() == Race.ZERG) {
					threat += supply;
				} else {
					threat += supply * 0.5;
				}
				
			} else {
				threat += supply * 1.1;
			}
		}
		
		threat += 5 * EnemyModel.enemyBaseCount();
	}
	
	public static double total_supply(List<HjaxUnit> list) {
		int result = 0;
		for (HjaxUnit u : list) {
			result += u.supply();
		}
		return result;
	}
	
	public static double threat() {
		return threat;
	}
	
	public static double attacking_supply() {
		return attacking_threat;
	}
	
	public static boolean is_safe(Point2d p) {
		for (HjaxUnit enemy: GameInfoCache.get_units(Alliance.ENEMY)) {
			if (enemy.is_combat()) {
				if (enemy.distance(p) < 10) {
					return false;
				}
			}
		}
		return true;
	}
}

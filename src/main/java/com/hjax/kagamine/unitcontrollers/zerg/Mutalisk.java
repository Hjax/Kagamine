package com.hjax.kagamine.unitcontrollers.zerg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.data.Upgrades;
import com.github.ocraft.s2client.protocol.debug.Color;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.Utilities;
import com.hjax.kagamine.Vector2d;
import com.hjax.kagamine.army.UnitMovementManager;
import com.hjax.kagamine.army.EnemySquadManager;
import com.hjax.kagamine.economy.Base;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.enemymodel.EnemyBaseDefense;
import com.hjax.kagamine.enemymodel.EnemyModel;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.game.HjaxUnit;
import com.hjax.kagamine.unitcontrollers.GenericUnit;

public class Mutalisk {
	
	private static final Map<UnitType, Integer> scores = new HashMap<>();
	private static final Map<UnitType, Integer> threats = new HashMap<>();
	static {
		scores.put(Units.TERRAN_SCV, 1);
		scores.put(Units.TERRAN_MEDIVAC, 2);
		scores.put(Units.TERRAN_VIKING_FIGHTER, 3);
		scores.put(Units.TERRAN_MARINE, 3);
		scores.put(Units.TERRAN_CYCLONE, 4);
		scores.put(Units.TERRAN_LIBERATOR, 5);
		scores.put(Units.TERRAN_MISSILE_TURRET, 6);
		scores.put(Units.TERRAN_BUNKER, 6);
		scores.put(Units.TERRAN_THOR, 7);
		
		scores.put(Units.PROTOSS_PROBE, 1);
		scores.put(Units.PROTOSS_STALKER, 2);
		scores.put(Units.PROTOSS_TEMPEST, 2);
		scores.put(Units.PROTOSS_PHOENIX, 4);
		scores.put(Units.PROTOSS_VOIDRAY, 4);
		scores.put(Units.PROTOSS_CARRIER, 5);
		scores.put(Units.PROTOSS_PHOTON_CANNON, 3);
		scores.put(Units.PROTOSS_SENTRY, 7);
		scores.put(Units.PROTOSS_ARCHON, 6);
		
		scores.put(Units.ZERG_OVERLORD, 1);
		scores.put(Units.ZERG_OVERSEER, 1);
		scores.put(Units.ZERG_DRONE, 2);
		scores.put(Units.ZERG_QUEEN, 3);
		scores.put(Units.ZERG_HYDRALISK, 5);
		scores.put(Units.ZERG_MUTALISK, 6);
		scores.put(Units.ZERG_CORRUPTOR, 4);
		scores.put(Units.ZERG_SPORE_CRAWLER, 7);
		
		threats.put(Units.ZERG_SPORE_CRAWLER, 8);
		threats.put(Units.ZERG_QUEEN, 3);
		threats.put(Units.ZERG_HYDRALISK, 3);
		threats.put(Units.ZERG_MUTALISK, 2);
		threats.put(Units.ZERG_CORRUPTOR, 3);
		
		threats.put(Units.TERRAN_MISSILE_TURRET, 5);
		threats.put(Units.TERRAN_THOR, 15);
		threats.put(Units.TERRAN_CYCLONE, 3);
		threats.put(Units.TERRAN_VIKING_FIGHTER, 3);
		threats.put(Units.TERRAN_GHOST, 3);
		threats.put(Units.TERRAN_LIBERATOR, 4);
		
		threats.put(Units.PROTOSS_SENTRY, 1);
		threats.put(Units.PROTOSS_STALKER, 2);
		threats.put(Units.PROTOSS_PHOENIX, 3);
		threats.put(Units.PROTOSS_CARRIER, 10);
		threats.put(Units.PROTOSS_PHOTON_CANNON, 5);
		threats.put(Units.PROTOSS_TEMPEST, 8);
		threats.put(Units.PROTOSS_ARCHON, 8);
		threats.put(Units.PROTOSS_VOIDRAY, 4);
	}
	
	private static Point2d swarm_center = null;
	private static Point2d swarm_target = null;
	private static Base target = null;
	private static final List<HjaxUnit> swarm = new ArrayList<>();
	public static void on_frame() {
		
		if (EnemyModel.enemyBaseCount() == 0) {
			return;
		}
		
		swarm_center = null;
		swarm_target = null;
		target = null;
		
		for (HjaxUnit muta : GameInfoCache.get_units(Alliance.SELF, Units.ZERG_MUTALISK)) {
			swarm.clear();
			if (UnitMovementManager.assignments.containsKey(muta.tag())) continue;
			for (HjaxUnit muta2 : GameInfoCache.get_units(Alliance.SELF, Units.ZERG_MUTALISK)) {
				if (UnitMovementManager.assignments.containsKey(muta2.tag())) continue;
				if (muta.distance(muta) < 10) {
					swarm.add(muta2);
				}
			}
			if (swarm.size() > GameInfoCache.count_friendly(Units.ZERG_MUTALISK) * 0.5) {
				swarm_center = EnemySquadManager.average_point(swarm);
				break;
			}
			
		}
		
		if (swarm_center != null) {
			target = EnemyBaseDefense.best_air_target(swarm.size() * 3);
			if (target != null) {
				swarm_target = pressure(swarm_center, target.location);
				Game.draw_line(swarm_target, swarm_center, Color.GREEN);
			}
		}
		
		

		float threat = 0;
		int marines = 0;
		int medivacs = 0;
		float mutas = 0;
		
		if (swarm_target != null && swarm_center != null) {

			for (HjaxUnit enemy: GameInfoCache.get_units(Alliance.ENEMY)) {
				if (enemy.alive()) {
					double dist = enemy.distance(swarm_center);
					if (dist < 15) {
						if (enemy.type() == Units.TERRAN_MARINE) {
							marines++;
						}
						else if (enemy.type() == Units.TERRAN_MEDIVAC) {
							medivacs++;
						}
						else if (threats.containsKey(enemy.type())) {
							if (!Game.is_structure(enemy.type()) || enemy.done()) {
								threat += threats.get(enemy.type()) * enemy.health() / enemy.health_max();
							}
						}
					}
				}
			}

			medivacs = Math.min(marines, medivacs);
			threat += marines + marines * medivacs * 0.1;
			for (HjaxUnit mutaf: GameInfoCache.get_units(Alliance.SELF)) {
				if (mutaf.distance(swarm_center) < 8) {
					mutas++;
				}
			}
			if (Game.has_upgrade(Upgrades.ZERG_FLYER_WEAPONS_LEVEL1)) mutas = (float) Math.pow(mutas, 1.13);
			else if (Game.has_upgrade(Upgrades.ZERG_FLYER_WEAPONS_LEVEL2)) mutas = (float) Math.pow(mutas, 1.4);
			else mutas = (float) Math.pow(mutas, 1.1);

			threat -= mutas;
			HjaxUnit best = null;
			if (threat < 0) {
				for (HjaxUnit enemy: GameInfoCache.get_units(Alliance.ENEMY)) {
					if (enemy.distance(swarm_center) < 10 && enemy.distance(target.location) < 15) {
						if (best == null || get_score(enemy.type()) > get_score(best.type()) || 
								(enemy.distance(swarm_center) < best.distance(swarm_center) &&
										get_score(enemy.type()) == get_score(best.type()))) {
							best = enemy;
						}
					}
				}
			}
			
			if (best != null) {
				for (HjaxUnit muta : swarm) {
					muta.attack(best);
				}
			} else {
				for (HjaxUnit muta : swarm) {
					muta.attack(swarm_target);
				}
			}
			
		}

	}

	
	private static Point2d pressure(Point2d swarm, Point2d target) {

		List<Vector2d> negative_pressure = new ArrayList<>();
		List<Vector2d> positive_pressure = new ArrayList<>();
		
		Point2d min = Game.get_game_info().getStartRaw().get().getPlayableArea().getP0().toPoint2d();
		Point2d max = Game.get_game_info().getStartRaw().get().getPlayableArea().getP1().toPoint2d();
		
		negative_pressure.add(new Vector2d(0, (float) (500 / Math.pow(max.getY() - swarm.getY(), 3))));
		negative_pressure.add(new Vector2d(0, (float) (-500 / Math.pow(swarm.getY() - min.getY(), 3))));
		negative_pressure.add(new Vector2d((float) (500 / Math.pow(max.getX() - swarm.getX(), 3)), 0));
		negative_pressure.add(new Vector2d((float) (-500 / Math.pow(swarm.getX() - min.getX(), 3)), 0));
		
		positive_pressure.add(Utilities.direction_to(Vector2d.of(swarm), Vector2d.of(target)).scale(20));
		
		for (HjaxUnit enemy: GameInfoCache.get_units(Alliance.ENEMY)) {
			if (enemy.alive()) {
				if (enemy.distance(swarm) < 30) {
					if (Game.hits_air(enemy.type())) {
						negative_pressure.add(Utilities.direction_to(Vector2d.of(swarm), Vector2d.of(enemy.location())).scale((float) (70 / Math.pow(swarm.distance(enemy.location()), 2))));
					} 
				}
			}
		}
		float x = 0;
		float y = 0;
		
		

		for (Vector2d v : negative_pressure) {
			x -= v.x;
			y -= v.y;
		}

		for (Vector2d v : positive_pressure) {
			x += v.x;
			y += v.y;
		}
		Vector2d pressure = Utilities.normalize(new Vector2d(x, y));

		return Point2d.of(swarm.getX() + 7 * pressure.x, swarm.getY() + 7 * pressure.y);
	}

	public static void on_frame(HjaxUnit u) {
		
		GenericUnit.on_frame(u, true);
		
//		if (EnemyModel.enemyBaseCount() == 0) {
//			GenericUnit.on_frame(u, true);
//			return;
//		}
//		
//		if (UnitMovementManager.assignments.containsKey(u.tag())) {
//			u.attack(UnitMovementManager.assignments.get(u.tag()));
//			return;
//		}
//		
//		if (!swarm.contains(u) && swarm_center != null) {
//			u.move(pressure(u.location(), swarm_center));
//		} 
//		
//		if (swarm_center == null || swarm_target == null) {
//			u.move(pressure(u.location(), BaseManager.main_base().location));
//		}
//		
		
	}

	private static int get_score(UnitType t) {
		return scores.getOrDefault(t, 0);
	}
}

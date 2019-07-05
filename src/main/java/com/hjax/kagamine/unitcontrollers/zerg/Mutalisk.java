package com.hjax.kagamine.unitcontrollers.zerg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.data.Upgrades;
import com.github.ocraft.s2client.protocol.debug.Color;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.DisplayType;
import com.hjax.kagamine.Utilities;
import com.hjax.kagamine.Vector2d;
import com.hjax.kagamine.army.BaseDefense;
import com.hjax.kagamine.army.EnemySquadManager;
import com.hjax.kagamine.economy.Base;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.enemymodel.EnemyBaseDefense;
import com.hjax.kagamine.enemymodel.EnemyModel;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.unitcontrollers.GenericUnit;

public class Mutalisk {
	
	public static Map<UnitType, Integer> scores = new HashMap<>();
	public static Map<UnitType, Integer> threats = new HashMap<>();
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
	
	public static Point2d swarm_center = null;
	public static Point2d swarm_target = null;
	public static Base target = null;
	public static List<UnitInPool> swarm = new ArrayList<UnitInPool>();
	public static void on_frame() {
		
		if (EnemyModel.enemyBaseCount() == 0) {
			return;
		}
		
		swarm_center = null;
		swarm_target = null;
		target = null;
		
		for (UnitInPool muta : GameInfoCache.get_units(Alliance.SELF, Units.ZERG_MUTALISK)) {
			swarm.clear();
			if (BaseDefense.assignments.containsKey(muta.unit().getTag())) continue;
			for (UnitInPool muta2 : GameInfoCache.get_units(Alliance.SELF, Units.ZERG_MUTALISK)) {
				if (BaseDefense.assignments.containsKey(muta2.unit().getTag())) continue;
				if (muta.unit().getPosition().distance(muta.unit().getPosition()) < 10) {
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
				Game.draw_line(swarm_target, swarm_center, Color.GREEN);
				swarm_target = pressure(swarm_center, target.location);
			}
		}
		
		

		float threat = 0;
		int marines = 0;
		int medivacs = 0;
		float mutas = 0;
		
		if (swarm_target != null && swarm_center != null) {

			for (UnitInPool enemy: GameInfoCache.get_units(Alliance.ENEMY)) {
				if (enemy.isAlive() && enemy.unit().getDisplayType() != DisplayType.SNAPSHOT) {
					double dist = enemy.unit().getPosition().toPoint2d().distance(swarm_center);
					if (dist < 15) {
						if (enemy.unit().getType() == Units.TERRAN_MARINE) {
							marines++;
						}
						else if (enemy.unit().getType() == Units.TERRAN_MEDIVAC) {
							medivacs++;
						}
						else if (threats.containsKey(enemy.unit().getType())) {
							if (!Game.is_structure(enemy.unit().getType()) || enemy.unit().getBuildProgress() > 0.999) {
								threat += threats.get(enemy.unit().getType()) * enemy.unit().getHealth().orElse((float) 0) / enemy.unit().getHealthMax().get();
							}
						}
					}
				}
			}

			medivacs = Math.min(marines, medivacs);
			threat += marines + marines * medivacs * 0.1;
			for (UnitInPool mutaf: GameInfoCache.get_units(Alliance.SELF)) {
				if (mutaf.unit().getPosition().toPoint2d().distance(swarm_center) < 8) {
					mutas++;
				}
			}
			if (Game.has_upgrade(Upgrades.ZERG_FLYER_WEAPONS_LEVEL1)) mutas = (float) Math.pow(mutas, 1.13);
			else if (Game.has_upgrade(Upgrades.ZERG_FLYER_WEAPONS_LEVEL2)) mutas = (float) Math.pow(mutas, 1.4);
			else mutas = (float) Math.pow(mutas, 1.1);

			threat -= mutas;
			UnitInPool best = null;
			if (threat < 0) {
				for (UnitInPool enemy: GameInfoCache.get_units(Alliance.ENEMY)) {
					if (enemy.unit().getPosition().toPoint2d().distance(swarm_center) < 10 && enemy.unit().getPosition().toPoint2d().distance(target.location) < 15) {
						if (best == null || get_score(enemy.unit().getType()) > get_score(best.unit().getType()) || 
								(enemy.unit().getPosition().toPoint2d().distance(swarm_center) < best.unit().getPosition().toPoint2d().distance(swarm_center) &&
										get_score(enemy.unit().getType()) == get_score(best.unit().getType()))) {
							best = enemy;
						}
					}
				}
			}
			
			if (best != null) {
				Game.unit_command(swarm, Abilities.ATTACK, best.unit());
			} else {
				Game.unit_command(swarm, Abilities.MOVE, swarm_target);
			}
			
		}

	}

	
	public static Point2d pressure(Point2d swarm, Point2d target) {

		List<Vector2d> negative_pressure = new ArrayList<>();
		List<Vector2d> positive_pressure = new ArrayList<>();
		
		Point2d min = Game.get_game_info().getStartRaw().get().getPlayableArea().getP0().toPoint2d();
		Point2d max = Game.get_game_info().getStartRaw().get().getPlayableArea().getP1().toPoint2d();
		
		negative_pressure.add(new Vector2d(0, (float) (500 / Math.pow(max.getY() - swarm.getY(), 3))));
		negative_pressure.add(new Vector2d(0, (float) (-500 / Math.pow(swarm.getY() - min.getY(), 3))));
		negative_pressure.add(new Vector2d((float) (500 / Math.pow(max.getX() - swarm.getX(), 3)), 0));
		negative_pressure.add(new Vector2d((float) (-500 / Math.pow(swarm.getX() - min.getX(), 3)), 0));
		
		positive_pressure.add(Utilities.direction_to(Vector2d.of(swarm), Vector2d.of(target)).scale(20));
		
		for (UnitInPool enemy: GameInfoCache.get_units(Alliance.ENEMY)) {
			if (enemy.isAlive()) {
				if (swarm.distance(enemy.unit().getPosition().toPoint2d()) < 30) {
					if (Game.hits_air(enemy.unit().getType())) {
						negative_pressure.add(Utilities.direction_to(Vector2d.of(swarm), Vector2d.of(enemy.unit().getPosition().toPoint2d())).scale((float) (70 / Math.pow((double) swarm.distance(enemy.unit().getPosition().toPoint2d()), 2))));
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

	public static void on_frame(UnitInPool muta) {
		
		if (EnemyModel.enemyBaseCount() == 0) {
			GenericUnit.on_frame(muta, true);
			return;
		}
		
		if (BaseDefense.assignments.containsKey(muta.unit().getTag())) {
			if (BaseDefense.assignments.get(muta.getTag()).distance(muta.unit().getPosition().toPoint2d()) < 10) {
				Game.unit_command(muta, Abilities.ATTACK, BaseDefense.assignments.get(muta.getTag()));
				return;
			} else {
				Game.unit_command(muta, Abilities.MOVE, BaseDefense.assignments.get(muta.getTag()));
				return;
			}
		}
		
		if (!swarm.contains(muta) && swarm_center != null) {
			Game.unit_command(muta, Abilities.MOVE, pressure(muta.unit().getPosition().toPoint2d(), swarm_center));
		} 
		
		if (swarm_center == null || swarm_target == null) {
			Game.unit_command(muta, Abilities.MOVE, pressure(muta.unit().getPosition().toPoint2d(), BaseManager.main_base().location));
		}
		
		
	}

	public static int get_score(UnitType t) {
		return scores.getOrDefault(t, 0);
	}
}

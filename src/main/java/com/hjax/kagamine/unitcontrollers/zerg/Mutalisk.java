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
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.DisplayType;
import com.hjax.kagamine.Utilities;
import com.hjax.kagamine.Vector2d;
import com.hjax.kagamine.army.BaseDefense;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;

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

	
	public static void pressure(UnitInPool muta) {
		
		List<Vector2d> negative_pressure = new ArrayList<>();
		List<Vector2d> positive_pressure = new ArrayList<>();
		
		Point2d min = Game.get_game_info().getStartRaw().get().getPlayableArea().getP0().toPoint2d();
		Point2d max = Game.get_game_info().getStartRaw().get().getPlayableArea().getP1().toPoint2d();
		
		
		negative_pressure.add(new Vector2d(0, (float) (500 / Math.pow(max.getY() - muta.unit().getPosition().getY(), 3))));
		negative_pressure.add(new Vector2d(0, (float) (-500 / Math.pow(muta.unit().getPosition().getY() - min.getY(), 3))));
		negative_pressure.add(new Vector2d((float) (500 / Math.pow(max.getX() - muta.unit().getPosition().getX(), 3)), 0));
		negative_pressure.add(new Vector2d((float) (-500 / Math.pow(muta.unit().getPosition().getX() - min.getX(), 3)), 0));
		
		for (UnitInPool enemy: GameInfoCache.get_units(Alliance.ENEMY)) {
			if (enemy.isAlive()) {
				if (Game.is_town_hall(enemy.unit().getType())) {
					positive_pressure.add(Utilities.direction_to(Vector2d.of(muta.unit().getPosition().toPoint2d()), Vector2d.of(enemy.unit().getPosition().toPoint2d())).scale((float) (10 / (Math.max((double) muta.unit().getPosition().toPoint2d().distance(enemy.unit().getPosition().toPoint2d()), 6.0)))));
				}
				else if (Game.is_worker(enemy.unit().getType())) {
					positive_pressure.add(Utilities.direction_to(Vector2d.of(muta.unit().getPosition().toPoint2d()), Vector2d.of(enemy.unit().getPosition().toPoint2d())).scale((float) (1.2 / (Math.pow(Math.max((double) muta.unit().getPosition().toPoint2d().distance(enemy.unit().getPosition().toPoint2d()), 3.0), 1.6)))));
				}
				else if (muta.unit().getPosition().toPoint2d().distance(enemy.unit().getPosition().toPoint2d()) < 20) {
					if (enemy.unit().getType() == Units.TERRAN_VIKING_FIGHTER || enemy.unit().getType() == Units.PROTOSS_VOIDRAY || enemy.unit().getType() == Units.PROTOSS_STALKER || enemy.unit().getType() == Units.PROTOSS_PHOENIX || enemy.unit().getType() == Units.TERRAN_MARINE || enemy.unit().getType() == Units.TERRAN_LIBERATOR) {
						negative_pressure.add(Utilities.direction_to(Vector2d.of(muta.unit().getPosition().toPoint2d()), Vector2d.of(enemy.unit().getPosition().toPoint2d())).scale((float) (700 / Math.pow((double) muta.unit().getPosition().toPoint2d().distance(enemy.unit().getPosition().toPoint2d()), 3))));
					} else if (enemy.unit().getType() == Units.TERRAN_THOR) {
						negative_pressure.add(Utilities.direction_to(Vector2d.of(muta.unit().getPosition().toPoint2d()), Vector2d.of(enemy.unit().getPosition().toPoint2d())).scale((float) (1000 / Math.pow((double) muta.unit().getPosition().toPoint2d().distance(enemy.unit().getPosition().toPoint2d()), 2))));
					} else if (enemy.unit().getType() == Units.TERRAN_BUNKER) {
						negative_pressure.add(Utilities.direction_to(Vector2d.of(muta.unit().getPosition().toPoint2d()), Vector2d.of(enemy.unit().getPosition().toPoint2d())).scale((float) (500 / Math.pow((double) muta.unit().getPosition().toPoint2d().distance(enemy.unit().getPosition().toPoint2d()), 3))));
					} else if (enemy.unit().getType() == Units.TERRAN_MISSILE_TURRET) {
						negative_pressure.add(Utilities.direction_to(Vector2d.of(muta.unit().getPosition().toPoint2d()), Vector2d.of(enemy.unit().getPosition().toPoint2d())).scale((float) (300 / Math.pow((double) muta.unit().getPosition().toPoint2d().distance(enemy.unit().getPosition().toPoint2d()), 2))));
					} else if (enemy.unit().getType() == Units.PROTOSS_PHOTON_CANNON) {
						negative_pressure.add(Utilities.direction_to(Vector2d.of(muta.unit().getPosition().toPoint2d()), Vector2d.of(enemy.unit().getPosition().toPoint2d())).scale((float) (200 / Math.pow((double) muta.unit().getPosition().toPoint2d().distance(enemy.unit().getPosition().toPoint2d()), 2))));
					} else if (enemy.unit().getType() == Units.PROTOSS_ARCHON) {
						negative_pressure.add(Utilities.direction_to(Vector2d.of(muta.unit().getPosition().toPoint2d()), Vector2d.of(enemy.unit().getPosition().toPoint2d())).scale((float) (1000 / Math.pow((double) muta.unit().getPosition().toPoint2d().distance(enemy.unit().getPosition().toPoint2d()), 2))));
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

		Game.unit_command(muta, Abilities.MOVE, Point2d.of(muta.unit().getPosition().getX() + 4 * pressure.x, muta.unit().getPosition().getY() + 4 * pressure.y));
	}

	public static void on_frame(UnitInPool muta) {
		float threat = 0;
		int marines = 0;
		int medivacs = 0;
		float mutas = 0;

		boolean stutter = false;
		
		if (BaseDefense.assignments.containsKey(muta.unit().getTag())) {
			if (BaseDefense.assignments.get(muta.getTag()).distance(muta.unit().getPosition().toPoint2d()) < 10) {
				Game.unit_command(muta, Abilities.ATTACK, BaseDefense.assignments.get(muta.getTag()));
			} else {
				Game.unit_command(muta, Abilities.MOVE, BaseDefense.assignments.get(muta.getTag()));
			}
			
		}
		else {
			for (UnitInPool enemy: GameInfoCache.get_units(Alliance.ENEMY)) {
				if (enemy.isAlive() && enemy.unit().getDisplayType() != DisplayType.SNAPSHOT) {
					double dist = enemy.unit().getPosition().toPoint2d().distance(muta.unit().getPosition().toPoint2d());
					if (dist < 12) {
						if (dist <= 3) stutter = true;
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
			if (stutter && muta.unit().getWeaponCooldown().get() < 0.01) {
				Game.unit_command(muta, Abilities.HOLD_POSITION);
			} else {
				medivacs = Math.min(marines, medivacs);
				threat += marines + marines * medivacs * 0.1;
				for (UnitInPool mutaf: GameInfoCache.get_units(Alliance.SELF)) {
					if (mutaf.unit().getPosition().toPoint2d().distance(muta.unit().getPosition().toPoint2d()) < 8) {
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
						if (enemy.unit().getPosition().toPoint2d().distance(muta.unit().getPosition().toPoint2d()) < 11) {
							if (best == null || get_score(enemy.unit().getType()) > get_score(best.unit().getType())) {
								best = enemy;
							}
						}
					}
				}
				if (best != null) {
					Game.unit_command(muta, Abilities.ATTACK, best.unit());
				} else {
					pressure(muta);
				}
			}
		}
	}

	public static int get_score(UnitType t) {
		return scores.getOrDefault(t, 0);
	}
}

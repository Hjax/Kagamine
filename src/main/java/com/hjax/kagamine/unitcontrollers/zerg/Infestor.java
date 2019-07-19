package com.hjax.kagamine.unitcontrollers.zerg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.data.Upgrades;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.Constants;
import com.hjax.kagamine.Utilities;
import com.hjax.kagamine.Vector2d;
import com.hjax.kagamine.army.ArmyManager;
import com.hjax.kagamine.army.EnemySquadManager;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.game.HjaxUnit;

public class Infestor {
	
	private static Map<Point2d, Long> fungal_frames = new HashMap<>();
	
	public static Set<UnitType> neural_targets = new HashSet<>();
	static {
		neural_targets.add(Units.PROTOSS_CARRIER);
		neural_targets.add(Units.PROTOSS_TEMPEST);
		neural_targets.add(Units.PROTOSS_COLOSSUS);
		neural_targets.add(Units.PROTOSS_IMMORTAL);
		
		neural_targets.add(Units.TERRAN_SIEGE_TANK);
		neural_targets.add(Units.TERRAN_SIEGE_TANK_SIEGED);
		neural_targets.add(Units.TERRAN_THOR);
		neural_targets.add(Units.TERRAN_THOR_AP);
		neural_targets.add(Units.TERRAN_BATTLECRUISER);
		
	}
	
	public static void on_frame(HjaxUnit u) {
		
		Set<Point2d> to_remove = new HashSet<>();
		for (Point2d p : fungal_frames.keySet()) {
			if (Game.get_frame() - fungal_frames.get(p) > Constants.FPS * 2.6) {
				to_remove.add(p);
			}
		}
		for (Point2d p : to_remove) fungal_frames.remove(p);
		
		if (Game.has_upgrade(Upgrades.NEURAL_PARASITE) && u.energy() > 100) {
			for (HjaxUnit enemy : GameInfoCache.get_units(Alliance.ENEMY)) {
				// TODO dont hardcode this
				if (neural_targets.contains(enemy.type())) {
					if (enemy.distance(u) < Game.get_ability_data().get(Abilities.EFFECT_NEURAL_PARASITE).getCastRange().orElse(0.0f)) {
						u.use_ability(Abilities.EFFECT_NEURAL_PARASITE, enemy);
						return;
					}
				}
			}
		}

		HjaxUnit best_target = null;
		fungal_target: for (HjaxUnit enemy : GameInfoCache.get_units(Alliance.ENEMY)) {
			if (enemy.distance(u) < 15) {
				int count = 0;
				for (HjaxUnit enemy2 : GameInfoCache.get_units(Alliance.ENEMY)) {
					if (enemy.distance(enemy2) < 2.25) {
						count++;
					}
				}
				if (count > 5) {
					for (Point2d p : fungal_frames.keySet()) { 
						if (enemy.distance(p) < 2.25) {
							continue fungal_target;
						}
					}
					if (best_target == null || enemy.distance(u) < best_target.distance(u)) {
						best_target = enemy;
					}
				}
			}
		}
		if (best_target != null) {
			u.use_ability(Abilities.EFFECT_FUNGAL_GROWTH, best_target.location());
		}
		

		if (u.energy() > 25) {
			int ally_count = 0;
			boolean pf = false;
			List<HjaxUnit> enemies = new ArrayList<>();
			for (HjaxUnit enemy : GameInfoCache.get_units(Alliance.ENEMY)) {
				if (enemy.distance(u) < 9) {
					enemies.add(enemy);
					if (enemy.type() == Units.TERRAN_PLANETARY_FORTRESS) pf = true;
				}
			}
			for (HjaxUnit ally : GameInfoCache.get_units(Alliance.SELF)) {
				if (Game.is_combat(ally.type()) && ally.type() != Units.ZERG_INFESTOR) {
					if (ally.distance(u) < 12) {
						ally_count++;
					}
				}
			}
			
			if (enemies.size() > ally_count * 1.2 || enemies.size() > 20 || pf) {
				u.use_ability(Abilities.EFFECT_INFESTED_TERRANS, 
						Vector2d.of(u.location()).add(
								Utilities.direction_to(Vector2d.of(u.location()), Vector2d.of(EnemySquadManager.average_point(enemies))).scale(5)).toPoint2d());
			}
		}
		
		if (ArmyManager.army_center.distance(Point2d.of(0, 0)) > 1) {
			if (u.distance(ArmyManager.army_center) > 4) {
				u.move(ArmyManager.army_center);
			}
		} else {
			if (u.distance(ArmyManager.army_center) > 4) {
				u.move(ArmyManager.army_center);
			}
		}
	}
}

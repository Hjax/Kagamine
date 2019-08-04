package com.hjax.kagamine.unitcontrollers.zerg;

import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.Utilities;
import com.hjax.kagamine.Vector2d;
import com.hjax.kagamine.army.BanelingAvoidance;
import com.hjax.kagamine.army.UnitMovementManager;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.game.HjaxUnit;
import com.hjax.kagamine.unitcontrollers.GenericUnit;

public class Zergling {
	public static void on_frame(HjaxUnit u) {
		
		if (BanelingAvoidance.banelingAssignments.containsKey(u.tag())) {
			if (GameInfoCache.get_unit(BanelingAvoidance.banelingAssignments.get(u.tag())) != null) {
				u.attack(GameInfoCache.get_unit(BanelingAvoidance.banelingAssignments.get(u.tag())));
				return;
			}
		}
		
		for (HjaxUnit enemyBane: GameInfoCache.get_units(Alliance.ENEMY, Units.ZERG_BANELING)) {
			if (u.location().distance(enemyBane.location()) < 4) {
				Vector2d offset = new Vector2d(u.location().getX() - enemyBane.location().getX(), u.location().getY() - enemyBane.location().getY());
				offset = Utilities.normalize(offset).scale(3f);
				u.move(offset.add(Vector2d.of(u.location())).toPoint2d());
				return;
			}
		}


		if (UnitMovementManager.assignments.containsKey(u.tag()) && UnitMovementManager.surroundCenter.containsKey(u.tag())) {
			if (UnitMovementManager.surroundCenter.get(u.tag()).distance(UnitMovementManager.assignments.get(u.tag())) < 1) {
				u.attack(UnitMovementManager.assignments.get(u.tag()));
			} else {
				Vector2d offset = Utilities.direction_to(Vector2d.of(UnitMovementManager.surroundCenter.get(u.tag())), Vector2d.of(UnitMovementManager.assignments.get(u.tag())));
				Point2d result = Vector2d.of(UnitMovementManager.assignments.get(u.tag())).add(offset.scale(6)).toPoint2d();
				if (Game.pathable(result) && Math.abs(Game.height(result) - Game.height(u.location())) < 0.5) {
					u.move(result);
				} else {
					u.attack(UnitMovementManager.assignments.get(u.tag()));
				}
			}
		} else {
			GenericUnit.on_frame(u, true);
		}
	}
}

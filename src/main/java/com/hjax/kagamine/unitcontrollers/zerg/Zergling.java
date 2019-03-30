package com.hjax.kagamine.unitcontrollers.zerg;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.Utilities;
import com.hjax.kagamine.Vector2d;
import com.hjax.kagamine.army.BanelingAvoidance;
import com.hjax.kagamine.army.BaseDefense;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.unitcontrollers.GenericUnit;

public class Zergling {
	public static void on_frame(UnitInPool u) {
		
		if (BanelingAvoidance.banelingAssignments.containsKey(u.getTag())) {
			if (Game.get_unit(BanelingAvoidance.banelingAssignments.get(u.getTag())) != null) {
				Game.unit_command(u, Abilities.ATTACK, Game.get_unit(BanelingAvoidance.banelingAssignments.get(u.getTag())).unit());
				return;
			}
		}
		
		for (UnitInPool enemyBane: GameInfoCache.get_units(Alliance.ENEMY, Units.ZERG_BANELING)) {
			if (u.unit().getPosition().toPoint2d().distance(enemyBane.unit().getPosition().toPoint2d()) < 4) {
				Vector2d offset = new Vector2d(u.unit().getPosition().toPoint2d().getX() - enemyBane.unit().getPosition().toPoint2d().getX(), u.unit().getPosition().toPoint2d().getY() - enemyBane.unit().getPosition().toPoint2d().getY());
				offset = Utilities.normalize(offset).scale(3f);
				Game.unit_command(u, Abilities.MOVE, offset.add(Vector2d.of(u.unit().getPosition().toPoint2d())).toPoint2d());
				return;
			}
		}


		if (BaseDefense.assignments.containsKey(u.unit().getTag()) && BaseDefense.surroundCenter.containsKey(u.getTag())) {
			if (BaseDefense.surroundCenter.get(u.getTag()).distance(BaseDefense.assignments.get(u.getTag())) < 1) {
				Game.unit_command(u, Abilities.ATTACK, BaseDefense.assignments.get(u.getTag()));
			} else {
				Vector2d offset = Utilities.direction_to(Vector2d.of(BaseDefense.surroundCenter.get(u.getTag())), Vector2d.of(BaseDefense.assignments.get(u.getTag())));
				Point2d result = Vector2d.of(BaseDefense.assignments.get(u.getTag())).add(offset.scale(6)).toPoint2d();
				if (Game.pathable(result) && Math.abs(Game.height(result) - Game.height(u.unit().getPosition().toPoint2d())) < 0.5) {
					Game.unit_command(u, Abilities.MOVE, result);
				} else {
					Game.unit_command(u, Abilities.ATTACK, BaseDefense.assignments.get(u.getTag()));
				}
			}
		} else {
			GenericUnit.on_frame(u, true);
		}
	}
}

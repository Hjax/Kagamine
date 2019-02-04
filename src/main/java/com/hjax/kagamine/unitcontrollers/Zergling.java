package com.hjax.kagamine.unitcontrollers;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.hjax.kagamine.Utilities;
import com.hjax.kagamine.Vector2d;
import com.hjax.kagamine.army.BaseDefense;
import com.hjax.kagamine.game.Game;

public class Zergling {
	public static void on_frame(UnitInPool u) {
		if (BaseDefense.assignments.containsKey(u.unit().getTag())) {
			if (BaseDefense.surroundCenter.get(u.getTag()).distance(BaseDefense.assignments.get(u.getTag())) < 1) {
				Game.unit_command(u, Abilities.ATTACK, BaseDefense.assignments.get(u.getTag()));
			} else {
				if (BaseDefense.surroundCenter.get(u.getTag()).distance(Point2d.of(0, 0)) < 1) {
					GenericUnit.on_frame(u, true);
				} else {
					Vector2d offset = Utilities.direction_to(Vector2d.of(BaseDefense.surroundCenter.get(u.getTag())), Vector2d.of(BaseDefense.assignments.get(u.getTag())));
					Point2d result = Vector2d.of(BaseDefense.assignments.get(u.getTag())).add(offset.scale(6)).toPoint2d();
					Game.unit_command(u, Abilities.MOVE, result);
				}
			}
			
		} else {
			GenericUnit.on_frame(u, true);
		}
	}
}

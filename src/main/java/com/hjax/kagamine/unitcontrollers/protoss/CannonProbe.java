package com.hjax.kagamine.unitcontrollers.protoss;

import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.debug.Color;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.Vector2d;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.game.HjaxUnit;

public class CannonProbe {
	
	private static final int EDGE_RANGE = 3;
	private static final int ITERATIONS = 9;
	
	public static void on_frame(HjaxUnit u) {

		Vector2d target = u.location();
		
		
		for (int i = 0; i < ITERATIONS; i ++) {
			
			Vector2d pressure = new Vector2d(0, 0);
			
			double points = 0;
			
			for (int x = -EDGE_RANGE; x < EDGE_RANGE; x++) {
				for (int y = -EDGE_RANGE; y < EDGE_RANGE; y++) {
					double distance = Math.sqrt(x * x + y * y);
					if (distance < EDGE_RANGE && distance >= 0.1) {
						Vector2d current = Vector2d.of(x + target.getX(), y + target.getY());
						if (!Game.pathable(current) || Math.abs(Game.height(current) - Game.height(target)) > 0.25) {
							pressure = pressure.add(new Vector2d(x, y).scale(-4 / Math.pow(distance, 2)));
							Game.draw_box(current, Color.RED);
							points++;
						}
					}
				}
			}
			
			if (points > 0) {
				pressure = pressure.scale(1 / points);
			}
			
			for (HjaxUnit enemy : GameInfoCache.get_units(Alliance.ENEMY)) {
				if (!Game.is_structure(enemy.type())) {
					pressure = pressure.add(enemy.location().directionTo(target).normalized().scale(1 / enemy.distance(target)));
				}
			}
			
			//pressure = pressure.add(u.location().direction_to(GameInfoCache.get_enemy_spawn()).normalized().scale(2));
			
			target = target.add(pressure.normalized().scale(0.5));
			
			Game.draw_box(target, Color.GREEN);
			
		}
		
		u.move(target);
		
	}
	
	public static boolean can_build(HjaxUnit u) {	
		
		return u.orders().size() == 0 || (u.orders().get(0).getTargetedUnitTag().isPresent() && u.orders().get(0).getAbility() == Abilities.HARVEST_GATHER && GameInfoCache.get_unit(u.orders().get(0).getTargetedUnitTag().get()).minerals() > 0);
		
	}
	
}

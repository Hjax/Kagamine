package com.hjax.kagamine.unitcontrollers.zerg;

import java.util.ArrayList;
import java.util.List;

import com.github.ocraft.s2client.protocol.debug.Color;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.Utilities;
import com.hjax.kagamine.Vector2d;
import com.hjax.kagamine.economy.Base;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.game.HjaxUnit;

public class Dropperlord {
	
	private static void pressure(HjaxUnit ovie, Base target) {

		List<Vector2d> negative_pressure = new ArrayList<>();
		List<Vector2d> positive_pressure = new ArrayList<>();
		
		Point2d min = Game.get_game_info().getStartRaw().get().getPlayableArea().getP0().toPoint2d();
		Point2d max = Game.get_game_info().getStartRaw().get().getPlayableArea().getP1().toPoint2d();
		
		negative_pressure.add(new Vector2d(0, (float) (500 / Math.pow(max.getY() - ovie.location().getY(), 3))));
		negative_pressure.add(new Vector2d(0, (float) (-500 / Math.pow(ovie.location().getY() - min.getY(), 3))));
		negative_pressure.add(new Vector2d((float) (500 / Math.pow(max.getX() - ovie.location().getX(), 3)), 0));
		negative_pressure.add(new Vector2d((float) (-500 / Math.pow(ovie.location().getX() - min.getX(), 3)), 0));
		
		positive_pressure.add(Utilities.direction_to(Vector2d.of(ovie.location()), Vector2d.of(target.location)).scale(10));
		
		Game.draw_line(ovie.location(), target.location, Color.GREEN);
		
		for (HjaxUnit enemy: GameInfoCache.get_units(Alliance.ENEMY)) {
			if (Game.hits_air(enemy.type())) {
				negative_pressure.add(Utilities.direction_to(Vector2d.of(ovie.location()), Vector2d.of(enemy.location())).scale((float) (300 / Math.pow(ovie.location().distance(enemy.location()), 2))));
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

		ovie.move(Point2d.of(ovie.location().getX() + 4 * pressure.x, ovie.location().getY() + 4 * pressure.y));
	}
	
	public static void on_frame(HjaxUnit u) {
		
	}
}

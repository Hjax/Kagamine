package com.hjax.kagamine.unitcontrollers.zerg;

import java.util.ArrayList;
import java.util.List;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.Utilities;
import com.hjax.kagamine.Vector2d;
import com.hjax.kagamine.economy.Base;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.knowledge.Scouting;

public class Overlord {
	public static void on_frame(UnitInPool u) {
		if (Scouting.overlords.containsKey(u.getTag())) {
			pressure(u, Scouting.overlords.get(u.getTag()));
		} else if (u.unit().getPosition().toPoint2d().distance(BaseManager.main_base().location) > 10) {
			pressure(u, BaseManager.main_base);
		}
	}
	
	public static void pressure(UnitInPool ovie, Base target) {

		List<Vector2d> negative_pressure = new ArrayList<>();
		List<Vector2d> positive_pressure = new ArrayList<>();
		
		Point2d min = Game.get_game_info().getStartRaw().get().getPlayableArea().getP0().toPoint2d();
		Point2d max = Game.get_game_info().getStartRaw().get().getPlayableArea().getP1().toPoint2d();
		
		negative_pressure.add(new Vector2d(0, (float) (500 / Math.pow(max.getY() - ovie.unit().getPosition().getY(), 3))));
		negative_pressure.add(new Vector2d(0, (float) (-500 / Math.pow(ovie.unit().getPosition().getY() - min.getY(), 3))));
		negative_pressure.add(new Vector2d((float) (500 / Math.pow(max.getX() - ovie.unit().getPosition().getX(), 3)), 0));
		negative_pressure.add(new Vector2d((float) (-500 / Math.pow(ovie.unit().getPosition().getX() - min.getX(), 3)), 0));
		
		positive_pressure.add(Utilities.direction_to(Vector2d.of(ovie.unit().getPosition().toPoint2d()), Vector2d.of(target.location)));
		
		for (UnitInPool enemy: GameInfoCache.get_units(Alliance.ENEMY)) {
			if (enemy.isAlive()) {
				if (ovie.unit().getPosition().toPoint2d().distance(enemy.unit().getPosition().toPoint2d()) < 15) {
					if (Game.hits_air(enemy.unit().getType())) {
						negative_pressure.add(Utilities.direction_to(Vector2d.of(ovie.unit().getPosition().toPoint2d()), Vector2d.of(enemy.unit().getPosition().toPoint2d())).scale((float) (50 / Math.pow((double) ovie.unit().getPosition().toPoint2d().distance(enemy.unit().getPosition().toPoint2d()), 2))));
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

		Game.unit_command(ovie, Abilities.MOVE, Point2d.of(ovie.unit().getPosition().getX() + 4 * pressure.x, ovie.unit().getPosition().getY() + 4 * pressure.y));
	}
}

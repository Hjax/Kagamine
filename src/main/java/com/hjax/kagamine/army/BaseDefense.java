package com.hjax.kagamine.army;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.debug.Color;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Tag;
import com.hjax.kagamine.Constants;
import com.hjax.kagamine.economy.Base;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.game.ControlGroups;
import com.hjax.kagamine.game.Game;

public class BaseDefense {
	public static Set<Tag> used = new HashSet<>();
	public static Map<Tag, Point2d> assignments = new HashMap<>();
	public static Map<Tag, Point2d> surroundCenter = new HashMap<>();
	public static void on_frame() {
		used.clear();
		assignments.clear();
		surroundCenter.clear();
		for (Set<UnitInPool> enemy_squad : EnemySquadManager.enemy_squads) {
			float ground_supply = 0;
			float flyer_supply = 0;
			Point2d average = EnemySquadManager.average_point(new ArrayList<>(enemy_squad));
			for (Base b : BaseManager.bases) {
				if ((b.has_friendly_command_structure() || b.equals(BaseManager.get_next_base())) && b.location.distance(average) < Constants.THREAT_DISTANCE) {
					for (UnitInPool enemy : enemy_squad) {
						if (enemy.unit().getFlying().orElse(false)) {
							flyer_supply += Game.supply(enemy.unit().getType());
						} else {
							ground_supply += Game.supply(enemy.unit().getType());
						}
					}
					ArrayList<UnitInPool> assigned = new ArrayList<>();
					float assigned_supply = 0;
					while (assigned_supply < ground_supply * 1.5 || ground_supply > 30) {
						UnitInPool current = closest_free(average, false);
						if (current == null) current = closest_free(average, true);
						if (current == null) break;
						assigned_supply += Game.supply(current.unit().getType());
						assigned.add(current);
					}
					assigned_supply = 0;
					while (assigned_supply < flyer_supply * 1 || flyer_supply > 30) {
						UnitInPool current = closest_free(average, true);
						if (current == null) break;
						assigned_supply += Game.supply(current.unit().getType());
						assigned.add(current);
					}
					Point2d center = average_point_zergling(assigned, average);
					for (UnitInPool u: assigned) {
						assignments.put(u.getTag(), average);
						if (center.distance(Point2d.of(0, 0)) > 1 && enemy_squad.size() * 2 <= assigned.size()) {
							surroundCenter.put(u.getTag(), center);
						}
						Game.draw_line(average, u.unit().getPosition().toPoint2d(), Color.GREEN);
						Game.draw_line(center, u.unit().getPosition().toPoint2d(), Color.RED);
					}
					break;
				}
			}
			
		}
	}
	
	public static UnitInPool closest_free(Point2d p, boolean aa) {
		UnitInPool best = null;
		for (UnitInPool ally : ControlGroups.get(0)) {
			if (aa != Game.hits_air(ally.unit().getType())) continue;
			if (!Game.is_structure(ally.unit().getType()) && Game.is_combat(ally.unit().getType())) {
				if (!used.contains(ally.getTag())) {
					if (best == null || 
							(best.unit().getPosition().toPoint2d().distance(p) / Game.get_unit_type_data().get(best.unit().getType()).getMovementSpeed().orElse((float) 1)) > 
							(ally.unit().getPosition().toPoint2d().distance(p)) / Game.get_unit_type_data().get(ally.unit().getType()).getMovementSpeed().orElse((float) 1)) {
						best = ally;
					}
				}
			}
		}
		if (best != null) {
			used.add(best.getTag());
		}
		return best;
	}
	
	public static Point2d average_point_zergling(List<UnitInPool> l, Point2d target_center) {
		float x = 0;
		float y = 0;
		int n = 0;
		for (UnitInPool u : l) {
			if (u.unit().getType() == Units.ZERG_ZERGLING) {
				if (u.unit().getPosition().toPoint2d().distance(target_center) < 5) {
					x += u.unit().getPosition().getX();
					y += u.unit().getPosition().getY();
					n++;
				}
			}
		}
		return Point2d.of(x / n, y / n);
	}
}

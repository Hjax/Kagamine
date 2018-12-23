package com.hjax.kagamine.army;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.debug.Color;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Tag;
import com.hjax.kagamine.economy.Base;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.game.ControlGroups;
import com.hjax.kagamine.game.Game;

public class BaseDefense {
	public static Set<Tag> used = new HashSet<>();
	public static Map<Tag, Point2d> assignments = new HashMap<>();
	public static void on_frame() {
		used.clear();
		assignments.clear();
		for (Set<UnitInPool> enemy_squad : EnemySquadManager.enemy_squads) {
			float ground_supply = 0;
			float flyer_supply = 0;
			Point2d average = EnemySquadManager.average_point(new ArrayList<>(enemy_squad));
			for (Base b : BaseManager.bases) {
				if (b.has_friendly_command_structure() && b.location.distance(average) < 15) {
					for (UnitInPool enemy : enemy_squad) {
						if (enemy.unit().getFlying().orElse(false)) {
							flyer_supply += Game.get_unit_type_data().get(enemy.unit().getType()).getFoodRequired().orElse((float) 0);
						} else {
							ground_supply += Game.get_unit_type_data().get(enemy.unit().getType()).getFoodRequired().orElse((float) 0);
						}
					}
					float assigned_supply = 0;
					while (assigned_supply < ground_supply * 1.5 || ground_supply > 30) {
						UnitInPool current = closest_free(average, false);
						if (current == null) break;
						assigned_supply += Game.get_unit_type_data().get(current.unit().getType()).getFoodRequired().orElse((float) 0);
						assignments.put(current.getTag(), average);
						Game.draw_line(average, current.unit().getPosition().toPoint2d(), Color.GREEN);
					}
					assigned_supply = 0;
					while (assigned_supply < flyer_supply * 1.5 || flyer_supply > 30) {
						UnitInPool current = closest_free(average, true);
						if (current == null) break;
						assigned_supply += Game.get_unit_type_data().get(current.unit().getType()).getFoodRequired().orElse((float) 0);
						assignments.put(current.getTag(), average);
						Game.draw_line(average, current.unit().getPosition().toPoint2d(), Color.GREEN);
					}
					break;
				}
			}
			
		}
	}
	
	public static UnitInPool closest_free(Point2d p, boolean aa) {
		UnitInPool best = null;
		for (UnitInPool ally : ControlGroups.get(0)) {
			if (aa && !Game.hits_air(ally.unit().getType())) continue;
			if (!Game.is_structure(ally.unit().getType()) && Game.is_combat(ally.unit().getType())) {
				if (!used.contains(ally.getTag())) {
					if (best == null || (best.unit().getPosition().toPoint2d().distance(p) / Game.get_unit_type_data().get(best.unit().getType()).getMovementSpeed().orElse((float) 1)) > (ally.unit().getPosition().toPoint2d().distance(p)) / Game.get_unit_type_data().get(ally.unit().getType()).getMovementSpeed().orElse((float) 1)) {
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
}

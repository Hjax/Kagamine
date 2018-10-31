package com.hjax.kagamine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Tag;

public class BaseDefense {
	public static Set<Tag> used = new HashSet<>();
	public static Map<Tag, Point2d> assignments = new HashMap<>();
	public static void on_frame() {
		used.clear();
		assignments.clear();
		for (Set<UnitInPool> enemy_squad : SquadManager.enemy_squads) {
			int supply = 0;
			boolean flyers = false;
			Point2d average = SquadManager.average_point(new ArrayList<>(enemy_squad));
			for (Base b : BaseManager.bases) {
				if (b.has_friendly_command_structure() && b.location.distance(average) < 15) {
					for (UnitInPool enemy : enemy_squad) {
						supply += Game.get_unit_type_data().get(enemy.unit().getType()).getFoodRequired().orElse((float) 0);
						flyers = enemy.unit().getFlying().orElse(false) || flyers;
					}
					float assigned_supply = 0;
					Set<Tag> unit_group = new HashSet<>();
					while (assigned_supply < supply * 1.5 || supply > 30) {
						UnitInPool current = closest_free(enemy_squad.iterator().next().unit().getPosition().toPoint2d());
						if (current == null) break;
						assigned_supply += Game.get_unit_type_data().get(current.unit().getType()).getFoodRequired().orElse((float) 0);
						assignments.put(current.getTag(), average);
					}
					break;
				}
			}
			
		}
	}
	
	public static UnitInPool closest_free(Point2d p) {
		UnitInPool best = null;
		for (UnitInPool ally : GameInfoCache.get_units(Alliance.SELF)) {
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

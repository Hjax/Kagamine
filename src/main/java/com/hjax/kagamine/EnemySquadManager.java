package com.hjax.kagamine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Tag;

public class EnemySquadManager {
	public static ArrayList<Set<UnitInPool>> enemy_squads = new ArrayList<>();
	public static void on_frame() {
		Set<Tag> parsed = new HashSet<>();
		enemy_squads.clear();
		for (UnitInPool a: GameInfoCache.get_units(Alliance.ENEMY)) {
			if (Game.is_structure(a.unit().getType())) continue;
			if (!parsed.contains(a.getTag())) {
				List<UnitInPool> open = new ArrayList<>();
				Set<UnitInPool> squad = new HashSet<>();
				open.add(a);
				squad.add(a);
				while (open.size() > 0) {
					UnitInPool current = open.remove(0);
					for (UnitInPool b: GameInfoCache.get_units(Alliance.ENEMY)) {
						if (Game.is_structure(b.unit().getType())) continue;
						if (b.getTag() != current.getTag() && !parsed.contains(b.getTag())) {
							if (b.unit().getPosition().toPoint2d().distance(current.unit().getPosition().toPoint2d()) < Constants.ENEMY_SQUAD_DISTANCE) {
								open.add(b);
								parsed.add(b.getTag());
								squad.add(b);
							}
						}
					}
				}
				enemy_squads.add(squad);
			}
		}
	}
	
	public static Point2d average_point(List<UnitInPool> l) {
		float x = 0;
		float y = 0;
		int n = 0;
		for (UnitInPool u : l) {
			x += u.unit().getPosition().getX();
			y += u.unit().getPosition().getY();
			n++;
		}
		return Point2d.of(x / n, y / n);
	}
}

package com.hjax.kagamine;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Tag;

import java.util.*;

public class SquadManager {
	public static Map<Integer, ArrayList<UnitInPool>> enemy_squads = new HashMap<>();
	public static Map<Integer, ArrayList<UnitInPool>> ally_squads = new HashMap<>();
	public static Map<Tag, Integer> enemy_assignments = new HashMap<>();
	public static Map<Tag, Integer> ally_assignments = new HashMap<>();
	public static void on_frame() {
		// any dead unit or unit out of vision should be removed from the squad
		for (int squad : enemy_squads.keySet()) {
			for (UnitInPool u: enemy_squads.get(squad)) {
				if (!u.isAlive() || !(u.getLastSeenGameLoop() == Game.get_frame())) {
					enemy_squads.get(squad).remove(u);
				}
			}
		}
		
		// regenerate squads with 

	}

	public static List<UnitInPool> generate(UnitInPool e) {
		Set<Tag> parsed = new HashSet<>();
		List<UnitInPool> open = new ArrayList<>();
		List<UnitInPool> squad = new ArrayList<>();
		open.add(e);
		squad.add(e);
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
		return squad;
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

package com.hjax.kagamine.army;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Tag;
import com.hjax.kagamine.Constants;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.game.HjaxUnit;

public class EnemySquadManager {
	public static final ArrayList<Set<HjaxUnit>> enemy_squads = new ArrayList<>();
	public static void on_frame() {
		Set<Tag> parsed = new HashSet<>();
		enemy_squads.clear();
		for (HjaxUnit enemy: GameInfoCache.get_units(Alliance.ENEMY)) {
			if (Game.is_structure(enemy.type()) || !Game.is_combat(enemy.type())) continue;
			if (!parsed.contains(enemy.tag())) {
				List<HjaxUnit> open = new ArrayList<>();
				Set<HjaxUnit> squad = new HashSet<>();
				open.add(enemy);
				squad.add(enemy);
				while (open.size() > 0) {
					HjaxUnit current = open.remove(0);
					for (HjaxUnit enemy2: GameInfoCache.get_units(Alliance.ENEMY)) {
						if (Game.is_structure(enemy2.type()) || !Game.is_combat(enemy2.type())) continue;
						if (enemy2.tag() != current.tag() && !parsed.contains(enemy2.tag())) {
							if (enemy2.distance(current) < Constants.ENEMY_SQUAD_DISTANCE) {
								open.add(enemy2);
								parsed.add(enemy2.tag());
								squad.add(enemy2);
							}
						}
					}
				}
				enemy_squads.add(squad);
				Game.write_text(String.valueOf(ThreatManager.total_supply(new ArrayList<>(squad))), average_point(new ArrayList<>(squad)));
			}
		}
	}
	
	public static Point2d average_point(List<HjaxUnit> l) {
		if (l.size() == 0) return Point2d.of(0, 0);
		
		float x = 0;
		float y = 0;
		int n = 0;
		for (HjaxUnit u : l) {
			x += u.location().getX();
			y += u.location().getY();
			n++;
		}
		return Point2d.of(x / n, y / n);
	}
	
}

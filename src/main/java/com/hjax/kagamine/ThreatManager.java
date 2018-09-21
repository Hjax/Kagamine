package com.hjax.kagamine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Tag;

public class ThreatManager {
	public static Map<Tag, Integer> seen = new HashMap<>();
	static void on_frame() {
		Set<Tag> to_remove = new HashSet<>();
		for (Tag t: seen.keySet()) {
			seen.put(t, seen.get(t) + 1);
			if (Game.get_unit(t) == null || !Game.get_unit(t).isAlive() || seen.get(t) > (Constants.FPS * 20) / Constants.FRAME_SKIP) {
				to_remove.add(t);
			}
		}
		for (Tag t: to_remove) {
			seen.remove(t);
		}
		for (UnitInPool u : GameInfoCache.get_units(Alliance.ENEMY)) {
			if (Game.is_worker(u.unit().getType())) continue;
			for (Base b: BaseManager.bases) {
				if (!b.has_command_structure()) continue;
				if (b.location.distance(u.unit().getPosition().toPoint2d()) < 30 && u.unit().getType() != Units.PROTOSS_ADEPT_PHASE_SHIFT) {
					seen.put(u.getTag(), 0);
				}
			}
		}
	}
	public static boolean under_attack() {
		return seen.size() > 1;
	}
}

package com.hjax.kagamine.army;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Tag;
import com.hjax.kagamine.Constants;
import com.hjax.kagamine.economy.Base;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;

public class ThreatManager {
	public static Map<Tag, Integer> seen = new HashMap<>();
	public static void on_frame() {
		Set<Tag> to_remove = new HashSet<>();
		for (Tag t: seen.keySet()) {
			seen.put(t, seen.get(t) + 1);
			if (Game.get_unit(t) == null || !Game.get_unit(t).isAlive() || seen.get(t) > (Constants.FPS * 20)) {
				to_remove.add(t);
			}
		}
		for (Tag t: to_remove) {
			seen.remove(t);
		}
		for (UnitInPool u : GameInfoCache.get_units(Alliance.ENEMY)) {
			for (Base b: BaseManager.bases) {
				if (!b.has_friendly_command_structure()) continue;
				if (b.location.distance(u.unit().getPosition().toPoint2d()) < Constants.THREAT_DISTANCE && u.unit().getType() != Units.PROTOSS_ADEPT_PHASE_SHIFT) {
					seen.put(u.getTag(), 0);
				}
			}
		}
	}
	public static boolean under_attack() {
		return seen.size() >= 10;
	}
	
	public static boolean is_safe(Point2d p) {
		for (UnitInPool e: GameInfoCache.get_units(Alliance.ENEMY)) {
			if (Game.is_combat(e.unit().getType())) {
				if (e.unit().getPosition().toPoint2d().distance(p) < 10) {
					return false;
				}
			}
		}
		return true;
	}
}

package com.hjax.kagamine.army;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Tag;
import com.hjax.kagamine.Constants;
import com.hjax.kagamine.economy.Base;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.game.HjaxUnit;

public class ThreatManager {
	public static final Map<Tag, Integer> seen = new HashMap<>();
	public static void on_frame() {
		Set<Tag> to_remove = new HashSet<>();
		for (Tag t: seen.keySet()) {
			seen.put(t, seen.get(t) + 1);
			if (GameInfoCache.get_unit(t) == null || !GameInfoCache.get_unit(t).alive() || seen.get(t) > (Constants.FPS * 20)) {
				to_remove.add(t);
			}
		}
		for (Tag t: to_remove) {
			seen.remove(t);
		}
		for (HjaxUnit unit : GameInfoCache.get_units(Alliance.ENEMY)) {
			for (Base b: BaseManager.bases) {
				if (!b.has_friendly_command_structure()) continue;
				if (unit.distance(b.location) < Constants.THREAT_DISTANCE && unit.type() != Units.PROTOSS_ADEPT_PHASE_SHIFT) {
					seen.put(unit.tag(), 0);
				}
			}
		}
	}
	public static boolean under_attack() {
		return seen.size() >= 3;
	}
	
	public static boolean is_safe(Point2d p) {
		for (HjaxUnit enemy: GameInfoCache.get_units(Alliance.ENEMY)) {
			if (enemy.is_combat()) {
				if (enemy.distance(p) < 10) {
					return false;
				}
			}
		}
		return true;
	}
}

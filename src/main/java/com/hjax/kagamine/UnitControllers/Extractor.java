package com.hjax.kagamine.UnitControllers;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.Base;
import com.hjax.kagamine.BaseManager;
import com.hjax.kagamine.BuildPlanner;
import com.hjax.kagamine.ProxyManager;
import com.hjax.kagamine.ThreatManager;
import com.hjax.kagamine.Game;
import com.hjax.kagamine.GameInfoCache;

public class Extractor {
	public static void on_frame(UnitInPool u) {
		if (!ThreatManager.is_safe(u.unit().getPosition().toPoint2d())) return;
		if (u.unit().getBuildProgress() > 0.999) {
			if (!BuildPlanner.pulled_off_gas && is_near_base(u.unit().getPosition().toPoint2d())) {
				if (u.unit().getAssignedHarvesters().get() < u.unit().getIdealHarvesters().get()) {
					UnitInPool best = BaseManager.get_free_worker(u.unit().getPosition().toPoint2d());
					if (best != null) {
						Game.unit_command(best, Abilities.SMART, u.unit());
					}
				}
			}
		}
	}
	
	public static boolean is_near_base(Point2d p) {
		for (Base b: BaseManager.bases) {
			if (b.has_command_structure() && b.location.distance(p) < 10) return true;
		}
		return false;
	}
}

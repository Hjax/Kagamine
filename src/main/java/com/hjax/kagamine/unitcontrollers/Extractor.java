package com.hjax.kagamine.unitcontrollers;

import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.hjax.kagamine.army.ThreatManager;
import com.hjax.kagamine.build.BuildExecutor;
import com.hjax.kagamine.economy.Base;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.game.HjaxUnit;

public class Extractor {
	public static void on_frame(HjaxUnit unit) {
		if (!ThreatManager.is_safe(unit.location())) return;
		if (unit.done()) {
			if (!BuildExecutor.pulled_off_gas && is_near_base(unit.location())) {
				if (unit.assigned_workers() < unit.ideal_workers()) {
					HjaxUnit best = BaseManager.get_free_worker(unit.location());
					if (best != null) {
						best.use_ability(Abilities.SMART, unit);
					}
				}
			}
		}
	}
	
	public static boolean is_near_base(Point2d p) {
		for (Base b: BaseManager.bases) {
			if (b.has_friendly_command_structure() && b.location.distance(p) < 10) return true;
		}
		return false;
	}
}

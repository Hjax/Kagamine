package com.hjax.kagamine.unitcontrollers;

import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Tag;
import com.hjax.kagamine.army.ThreatManager;
import com.hjax.kagamine.build.ZergBuildExecutor;
import com.hjax.kagamine.economy.Base;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.game.HjaxUnit;
import com.hjax.kagamine.game.RaceInterface;

public class Extractor {
	public static void on_frame(HjaxUnit unit) {
		if (unit.assigned_workers() > 3) {
			for (HjaxUnit u: GameInfoCache.get_units(Alliance.SELF, RaceInterface.get_race_worker())) {
				if (u.ability() == Abilities.HARVEST_GATHER) {
					if (u.orders().get(0).getTargetedUnitTag().get().equals(unit.tag())) {
						u.stop();
						return;
					}
				}
			}
		}
		if (!ThreatManager.is_safe(unit.location())) return;
		if (unit.done()) {
			if (is_near_base(unit.location())) {
				if (unit.assigned_workers() < unit.ideal_workers()) {
					if (!ZergBuildExecutor.pulled_off_gas) {
						HjaxUnit best = BaseManager.get_free_worker(unit.location());
						if (best != null) {
							best.use_ability(Abilities.SMART, unit);
						}
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

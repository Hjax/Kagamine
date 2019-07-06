package com.hjax.kagamine.economy;

import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.game.HjaxUnit;

public class EconomyManager {

	public static void on_frame() {
		// worker transfer code
		for (Base b : BaseManager.bases) {
			if (!b.has_friendly_command_structure()) continue;
			if (b.command_structure.assigned_workers() > b.command_structure.ideal_workers()) {
				for (Base target: BaseManager.bases) {
					if (!target.has_friendly_command_structure()) continue;
					if (target.minerals.size() == 0) continue;
					if (target.command_structure.assigned_workers() + GameInfoCache.in_progress(Units.ZERG_DRONE) < target.command_structure.ideal_workers()) {
						for (HjaxUnit worker : GameInfoCache.get_units(Alliance.SELF, Units.ZERG_DRONE)) {
							if (worker.distance(b.location) < 10) {
								// TODO remove try catch, fix crashing
								try {
									if (worker.ability() == Abilities.HARVEST_GATHER && GameInfoCache.get_unit(worker.orders().get(0).getTargetedUnitTag().get()).minerals() > 0) {
										worker.use_ability(Abilities.SMART, target.minerals.get(0));
										return;
									}
								} catch (Exception e) {
									worker.use_ability(Abilities.SMART, target.minerals.get(0));
									return;
								}
							}
						}
					}
				}
			}
		}
	}

	public static float larva_rate() {
		int total = 0;
		for (Base b: BaseManager.bases) {
			if (b.has_friendly_command_structure()) {
				total++;
				if (b.has_queen()) total++;
			}
		}
		return total;
	}

	public static void assign_worker(HjaxUnit ally) {
		for (Base b : BaseManager.bases) {
			if (b.has_friendly_command_structure() && b.command_structure.done()) {
				if (b.command_structure.assigned_workers() < b.command_structure.ideal_workers()) {
					if (b.minerals.size() > 0) {
						ally.use_ability(Abilities.SMART, b.minerals.get(0));
						return;
					}
				}
			}
		}
		for (Base b : BaseManager.bases) {
			if (b.has_friendly_command_structure() && b.command_structure.done()) {
				if (b.command_structure.assigned_workers()< b.command_structure.ideal_workers() * 1.5) {
					if (b.minerals.size() > 0) {
					    ally.use_ability(Abilities.SMART, b.minerals.get(0));
						return;
					}
				}
			}
		}
	}
	
	public static int free_minerals() {
		int result = 0;
		for (Base b: BaseManager.bases) {
			if (b.has_friendly_command_structure())  {
				result += Math.max(b.minerals.size() * 2 - b.command_structure.assigned_workers(), 0);
			}
		}
		return result;
	}
	
	public static int total_minerals() {
		int result = 0;
		for (Base b: BaseManager.bases) {
			if (b.has_friendly_command_structure())  {
				result += b.minerals.size() * 2;
			}
		}
		return result;
	}

}

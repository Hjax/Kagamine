package com.hjax.kagamine.economy;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;

public class EconomyManager {

	public static void on_frame() {
		// worker transfer code
		for (Base b : BaseManager.bases) {
			if (!b.has_friendly_command_structure()) continue;
			if (b.command_structure.unit().getAssignedHarvesters().orElse(0) > b.command_structure.unit().getIdealHarvesters().orElse(0)) {
				for (Base target: BaseManager.bases) {
					if (!target.has_friendly_command_structure()) continue;
					if (target.minerals.size() == 0) continue;
					if (target.command_structure.unit().getAssignedHarvesters().orElse(0) + GameInfoCache.in_progress(Units.ZERG_DRONE) < target.command_structure.unit().getIdealHarvesters().orElse(0)) {
						for (UnitInPool worker : GameInfoCache.get_units(Alliance.SELF, Units.ZERG_DRONE)) {
							if (worker.unit().getPosition().toPoint2d().distance(b.location) < 10) {
								// TODO remove try catch, fix crashing
								try {
									if (worker.unit().getOrders().get(0).getAbility() == Abilities.HARVEST_GATHER && Game.get_unit(worker.unit().getOrders().get(0).getTargetedUnitTag().get()).unit().getMineralContents().orElse(0) > 0) {
										Game.unit_command(worker, Abilities.SMART, target.minerals.get(0).unit());
										return;
									}
								} catch (Exception e) {
									Game.unit_command(worker, Abilities.SMART, target.minerals.get(0).unit());
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

	public static void assign_worker(UnitInPool u) {
		for (Base b : BaseManager.bases) {
			if (b.has_friendly_command_structure() && b.command_structure.unit().getBuildProgress() > 0.999) {
				if (b.command_structure.unit().getAssignedHarvesters().orElse(0) < b.command_structure.unit().getIdealHarvesters().orElse(0)) {
					if (b.minerals.size() > 0) {
						Game.unit_command(u, Abilities.SMART, b.minerals.get(0).unit());
						return;
					}
				}
			}
		}
		for (Base b : BaseManager.bases) {
			if (b.has_friendly_command_structure() && b.command_structure.unit().getBuildProgress() > 0.999) {
				if (b.command_structure.unit().getAssignedHarvesters().orElse(0) < b.command_structure.unit().getIdealHarvesters().orElse(0) * 1.5) {
					if (b.minerals.size() > 0) {
						Game.unit_command(u, Abilities.SMART, b.minerals.get(0).unit());
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
				result += Math.max(b.minerals.size() * 2 - b.command_structure.unit().getAssignedHarvesters().orElse(0), 0);
			}
		}
		return result;
	}

}

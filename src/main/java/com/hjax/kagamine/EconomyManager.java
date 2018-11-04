package com.hjax.kagamine;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Tag;

public class EconomyManager {

	// assignments for worker pairing
	private static Map<Tag, Tag> assignments = new HashMap<>();
	
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
		
		// mineral stacking code
		Set<Tag> to_remove = new HashSet<>();
		for (Tag u: assignments.keySet()) {
			if (Game.get_unit(u) == null) to_remove.add(u);
			else if (!Game.get_unit(u).getUnit().isPresent() || !Game.is_worker(Game.get_unit(u).unit().getType()) || !Game.get_unit(u).isAlive()) {
				if (Game.get_unit(u).unit().getOrders().size() == 0 || (Game.get_unit(u).unit().getOrders().get(0).getAbility() != Abilities.HARVEST_GATHER && Game.get_unit(u).unit().getOrders().get(0).getAbility() != Abilities.HARVEST_RETURN)) {
					to_remove.add(u);
				}
			}
		}
		for (Tag t: to_remove) assignments.remove(t);
		for (UnitInPool u: GameInfoCache.get_units(Alliance.SELF)) {
			if (Game.is_worker(u.unit().getType())) {
				if (u.unit().getOrders().size() > 0 && u.unit().getOrders().get(0).getAbility() == Abilities.HARVEST_GATHER) {
					if (u.unit().getOrders().get(0).getTargetedUnitTag().isPresent() && Game.get_unit(u.unit().getOrders().get(0).getTargetedUnitTag().get()) != null) {
						if (Game.get_unit(u.unit().getOrders().get(0).getTargetedUnitTag().get()).unit().getMineralContents().orElse(0) > 0) {
							Base current = null;
							for (Base b: BaseManager.bases) {
								for (UnitInPool mineral: b.minerals) {
									if (mineral.getTag().equals(u.unit().getOrders().get(0).getTargetedUnitTag().get())) {
										current = b;
										break;
									}
								}
							}
							if (current == null) continue;
							// this worker is worth optimizing
							// if we havent assigned it to a patch yet
							if (!assignments.containsKey(u.getTag())) {
								for (UnitInPool mineral: current.minerals) {
									if (Collections.frequency(assignments.values(), mineral.getTag()) < 2) {
										assignments.put(u.getTag(), mineral.getTag());
										Game.unit_command(u, Abilities.HARVEST_GATHER,  mineral.unit());
										break;
									}
								}
							} else {
								boolean changed_base = true;
								for (UnitInPool mineral: current.minerals) {
									if (mineral.getTag().equals(assignments.get(u.getTag()))) {
										changed_base = false;
										break;
									}
								}
								if (changed_base) assignments.remove(u.getTag());
								else if (!u.unit().getOrders().get(0).getTargetedUnitTag().get().equals(assignments.get(u.getTag()))) {
									Game.unit_command(u, Abilities.HARVEST_GATHER,  Game.get_unit(assignments.get(u.getTag())).unit());
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

}

package com.hjax.kagamine.economy;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Tag;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;

public class MiningOptimizer {
	// mappings of drones to mineral patches
	private static Map<Tag, Tag> assignments = new HashMap<>();
	public static void on_frame() {
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
}

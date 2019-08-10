package com.hjax.kagamine.economy;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.debug.Color;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Tag;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.game.HjaxUnit;

public class MiningOptimizer {
	// mappings of drones to mineral patches
	private static final Map<Tag, Tag> assignments = new HashMap<>();
	
	
	public static void on_frame() {
		
		Set<Tag> to_remove = new HashSet<>();
		for (Tag u: assignments.keySet()) {
			if (GameInfoCache.get_unit(u) == null || !Game.is_worker(GameInfoCache.get_unit(u).type()) || !GameInfoCache.get_unit(u).alive()) {
				to_remove.add(u);
			} 
			if (GameInfoCache.get_unit(u).orders().size() == 0 || (GameInfoCache.get_unit(u).orders().get(0).getAbility() != Abilities.HARVEST_GATHER && GameInfoCache.get_unit(u).orders().get(0).getAbility() != Abilities.HARVEST_RETURN)) {
				to_remove.add(u);
			}
			if (GameInfoCache.get_unit(u).orders().size() > 0 && GameInfoCache.get_unit(u).orders().get(0).getAbility() == Abilities.HARVEST_GATHER) {
				if (GameInfoCache.get_unit(u).orders().get(0).getTargetedUnitTag().isPresent() && GameInfoCache.get_unit(GameInfoCache.get_unit(u).orders().get(0).getTargetedUnitTag().get()) != null && GameInfoCache.get_unit(GameInfoCache.get_unit(u).orders().get(0).getTargetedUnitTag().get()).is_gas()) {
					to_remove.add(u);
				}
			}
		}
		for (Tag t: to_remove) assignments.remove(t);
		
		for (Tag t : assignments.keySet()) {
			Game.draw_line(GameInfoCache.get_unit(t).location(), GameInfoCache.get_unit(assignments.get(t)).location(), Color.BLUE);
		}
		
		for (HjaxUnit u: GameInfoCache.get_units(Alliance.SELF)) {
			if (Game.is_worker(u.type())) {
				if (u.ability() == Abilities.HARVEST_GATHER) {
					if (u.orders().get(0).getTargetedUnitTag().isPresent() && GameInfoCache.get_unit(u.orders().get(0).getTargetedUnitTag().get()) != null) {
						if (GameInfoCache.get_unit(u.orders().get(0).getTargetedUnitTag().get()).minerals() > 0) {
							Base current = null;
							for (Base b: BaseManager.bases) {
								for (HjaxUnit mineral: b.minerals) {
									if (mineral.tag().equals(u.orders().get(0).getTargetedUnitTag().get())) {
										current = b;
										break;
									}
								}
							}
							if (current == null) continue;
							// this worker is worth optimizing
							// if we havent assigned it to a patch yet
							if (!assignments.containsKey(u.tag())) {
								HjaxUnit best = null;
								for (HjaxUnit mineral: current.minerals) {
									if (Collections.frequency(assignments.values(), mineral.tag()) < 2 && (best == null || (mineral.distance(current) < best.distance(current)))) {
										best = mineral;
									}
								}
								if (best != null) {
									assignments.put(u.tag(), best.tag());
									u.use_ability(Abilities.HARVEST_GATHER, best);
								}
							} else {
								boolean changed_base = true;
								for (HjaxUnit mineral: current.minerals) {
									if (mineral.tag().equals(assignments.get(u.tag()))) {
										changed_base = false;
										break;
									}
								}
								if (changed_base) assignments.remove(u.tag());
								else if (!u.orders().get(0).getTargetedUnitTag().get().equals(assignments.get(u.tag()))) {
									if (GameInfoCache.get_unit(assignments.get(u.tag())) != null) {
										u.use_ability(Abilities.HARVEST_GATHER,  GameInfoCache.get_unit(assignments.get(u.tag())));										
									}
								}
							}
						}
					}
				}
			}
		}
	}
}

package com.hjax.kagamine;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Tag;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Inference {
	public static Set<Tag> registered = new HashSet<>();
	public static Map<UnitType, Bound> bounds = new HashMap<>();
	public static void on_frame() {
		for (UnitInPool u: GameInfoCache.get_units(Alliance.ENEMY)) {
			if (!registered.contains(u.getTag())) {
				registered.add(u.getTag());
				// if a unit is a structure, we call update on it directly
				int build_time = (int) Game.get_unit_type_data().get(u.unit().getType()).getBuildTime().orElse((float) 0).floatValue();
				int frames_done = (int) (u.unit().getBuildProgress() * build_time);
				if (Game.is_structure(u.unit().getType())) {
					update(u.unit().getType(), (int) ((build_time - frames_done) + Game.get_frame()), u.unit().getBuildProgress() < 0.999);
				} 
				else if (Game.get_unit_type_data().get(u.unit().getType()).getTechRequirement().isPresent()) {
					if (Game.get_unit_type_data().get(u.unit().getType()).getTechRequirement().get() != Units.INVALID) {
						update(Game.get_unit_type_data().get(u.unit().getType()).getTechRequirement().get(), (int) (Game.get_frame() - frames_done), false);
					}
				}
			}
		}
	}
	
	// updates our information with the knowledge of a unit on a frame 
	// makes recursive calls to itself for tech requirements 
	public static void update(UnitType u, int frame, boolean exact) {
		if (!bounds.containsKey(u)) Game.chat("You have a: " + u.toString());
		bounds.put(u, bounds.getOrDefault(u, new Bound(frame, exact).update(frame, exact)));
		if (Game.get_unit_type_data().get(u).getTechRequirement().isPresent()) {
			if (Game.get_unit_type_data().get(u).getTechRequirement().get() != Units.INVALID) {
				update(Game.get_unit_type_data().get(u).getTechRequirement().get(), (int) (frame - Game.get_unit_type_data().get(u).getBuildTime().orElse((float) 0)), false);
			}
		}
	}
}

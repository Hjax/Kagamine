package com.hjax.kagamine.knowledge;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.unit.DisplayType;
import com.github.ocraft.s2client.protocol.unit.Tag;
import com.hjax.kagamine.Constants;
import com.hjax.kagamine.economy.Base;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.game.Game;

public class ResourceTracking {
	
	private static Map<Tag, Integer> self_mined = new HashMap<>();
	private static Map<Tag, Integer> start_value = new HashMap<>();
	private static Map<Tag, ImmutablePair<Integer, Integer>> recent_value = new HashMap<>();
	private static Map<Tag, ImmutablePair<Integer, Integer>> previous_value = new HashMap<>();
	private static Set<Tag> gas = new HashSet<>();
	private static Set<Tag> my_resources = new HashSet<>();
	
	public static void on_frame() {
		
		my_resources.clear();
		for (Base b: BaseManager.bases) {
			if (b.has_friendly_command_structure()) {
				for (UnitInPool r: b.minerals) {
					my_resources.add(r.getTag());
				}
				for (UnitInPool r: b.gases) {
					my_resources.add(r.getTag());
				}
			}
		}
		
		for (UnitInPool u: Game.get_units()) {
			if (u.unit().getDisplayType() != DisplayType.SNAPSHOT && Game.is_resource(u.unit().getType())) {
				if (recent_value.containsKey(u.getTag())) {
					if ((Game.get_frame() - recent_value.get(u.getTag()).left > Constants.RESOURCE_UPDATE_TIMER) && Game.isVisible(u.unit().getPosition().toPoint2d())) {
						previous_value.put(u.getTag(), recent_value.get(u.getTag()));
						recent_value.put(u.getTag(), new ImmutablePair<>((int) Game.get_frame(), u.unit().getVespeneContents().orElse(0) + u.unit().getMineralContents().orElse(0)));
						if (my_resources.contains(u.getTag())) {
							self_mined.put(u.getTag(), self_mined.getOrDefault(u.getTag(), 0) + previous_value.get(u.getTag()).right - recent_value.get(u.getTag()).right);
						}
					}
				} else {
					if (Game.get_unit_type_data().get(u.unit().getType()).isHasVespene()) {
						start_value.put(u.getTag(), 2250);
						previous_value.put(u.getTag(), new ImmutablePair<>(0, 2250));
						gas.add(u.getTag());
					} else if (u.unit().getType().toString().contains("750")) {
						start_value.put(u.getTag(), 900);
						previous_value.put(u.getTag(), new ImmutablePair<>(0, 900));
					} else {
						start_value.put(u.getTag(), 1800);
						previous_value.put(u.getTag(), new ImmutablePair<>(0, 1800));
					}
					recent_value.put(u.getTag(), new ImmutablePair<>((int) Game.get_frame(), u.unit().getVespeneContents().orElse(0) + u.unit().getMineralContents().orElse(0)));
				}
			}
		}
	}
	
	private static int previous_estimate_mins = 0;
	private static int previous_estimate_frame_mins = 0;
	public static int estimate_enemy_minerals() {
		if (previous_estimate_frame_mins == Game.get_frame()) return previous_estimate_mins;
		int total = 0;
		for (Tag u : recent_value.keySet()) {
			if (!gas.contains(u)) {
				total += start_value.get(u) - recent_value.get(u).right; // known mining
				if (!my_resources.contains(u) && previous_value.containsKey(u)) {
					float rate = ((float) (previous_value.get(u).right - recent_value.get(u).right) / (float) (recent_value.get(u).left - previous_value.get(u).left));
					long frames = Game.get_frame() - recent_value.get(u).left;
					total += Math.min(rate * frames, start_value.get(u)); // predicted mining
				}
				if (self_mined.containsKey(u)) {
					total -= self_mined.get(u);
				}
			}
		}
		previous_estimate_frame_mins = (int) Game.get_frame();
		previous_estimate_mins = total;
		return total;
	}
	
	private static int previous_estimate_gas = 0;
	private static int previous_estimate_frame_gas = 0;
	public static int estimate_enemy_gas() {
		if (previous_estimate_frame_gas == Game.get_frame()) return previous_estimate_gas;
		int total = 0;
		for (Tag u : recent_value.keySet()) {
			if (gas.contains(u)) {
				total += start_value.get(u) - recent_value.get(u).right; // known mining
				if (!my_resources.contains(u) && previous_value.containsKey(u)) {
					float rate = ((float) (previous_value.get(u).right - recent_value.get(u).right) / (float) (recent_value.get(u).left - previous_value.get(u).left));
					long frames = Game.get_frame() - recent_value.get(u).left;
					total += Math.min(rate * frames, start_value.get(u)); // predicted mining
				}
				if (self_mined.containsKey(u)) {
					total -= self_mined.get(u);
				}
			}
		}
		previous_estimate_frame_gas = (int) Game.get_frame();
		previous_estimate_gas = total;
		return total;
	}
}

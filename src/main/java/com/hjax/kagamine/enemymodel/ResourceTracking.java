package com.hjax.kagamine.enemymodel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.github.ocraft.s2client.protocol.unit.Tag;
import com.hjax.kagamine.Constants;
import com.hjax.kagamine.economy.Base;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.game.HjaxUnit;

public class ResourceTracking {
	
	private static Map<Tag, Integer> self_mined = new HashMap<>();
	private static Map<Tag, Integer> start_value = new HashMap<>();
	private static Map<Tag, ImmutablePair<Integer, Integer>> recent_value = new HashMap<>();
	private static Map<Tag, ImmutablePair<Integer, Integer>> previous_value = new HashMap<>();
	private static Set<Tag> gas = new HashSet<>();
	private static Set<Tag> my_resources = new HashSet<>();
	private static Set<Tag> enemy_resources = new HashSet<>();
	
	public static void on_frame() {
		
		my_resources.clear();
		for (Base b: BaseManager.bases) {
			if (b.has_friendly_command_structure()) {
				for (HjaxUnit r: b.minerals) {
					my_resources.add(r.tag());
				}
				for (HjaxUnit r: b.gases) {
					my_resources.add(r.tag());
				}
			}
			if (b.has_enemy_command_structure()) {
				for (HjaxUnit r: b.minerals) {
					enemy_resources.add(r.tag());
				}
				for (HjaxUnit r: b.gases) {
					enemy_resources.add(r.tag());
				}
			}
		}
		
		for (HjaxUnit u: GameInfoCache.get_units()) {
			if (u.is_snapshot() && Game.is_resource(u.type())) {
				if (recent_value.containsKey(u.tag())) {
					if ((Game.get_frame() - recent_value.get(u.tag()).left > Constants.RESOURCE_UPDATE_TIMER) && Game.isVisible(u.location())) {
						previous_value.put(u.tag(), recent_value.get(u.tag()));
						recent_value.put(u.tag(), new ImmutablePair<>((int) Game.get_frame(), u.gas() + u.minerals()));
						if (my_resources.contains(u.tag())) {
							self_mined.put(u.tag(), self_mined.getOrDefault(u.tag(), 0) + previous_value.get(u.tag()).right - recent_value.get(u.tag()).right);
						}
					}
				} else {
					if (Game.get_unit_type_data().get(u.type()).isHasVespene()) {
						start_value.put(u.tag(), 2250);
						previous_value.put(u.tag(), new ImmutablePair<>(0, 2250));
						gas.add(u.tag());
					} else if (u.type().toString().contains("750")) {
						start_value.put(u.tag(), 900);
						previous_value.put(u.tag(), new ImmutablePair<>(0, 900));
					} else {
						start_value.put(u.tag(), 1800);
						previous_value.put(u.tag(), new ImmutablePair<>(0, 1800));
					}
					recent_value.put(u.tag(), new ImmutablePair<>((int) Game.get_frame(), u.gas() + u.minerals()));
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
					total += Math.min(rate * frames, recent_value.get(u).right); // predicted mining
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
				if (!my_resources.contains(u) && previous_value.containsKey(u) && enemy_resources.contains(u)) {
					float rate = ((float) (previous_value.get(u).right - recent_value.get(u).right) / (float) (recent_value.get(u).left - previous_value.get(u).left));
					long frames = Game.get_frame() - recent_value.get(u).left;
					total += Math.min(rate * frames, recent_value.get(u).right); // predicted mining
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

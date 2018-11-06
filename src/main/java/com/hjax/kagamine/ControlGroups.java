package com.hjax.kagamine;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ControlGroups {
	public static Map<Integer, ArrayList<UnitInPool>> groups = new HashMap<>();
	public static Map<Tag, Integer> assignments = new HashMap<>();
	static {
		groups.put(0, new ArrayList<>());
	}
	
	public static void on_frame() {
		for (UnitInPool u : GameInfoCache.get_units(Alliance.SELF)) {
			if (Game.is_combat(u.unit().getType()) && !Game.is_worker(u.unit().getType())) {
				if (!assignments.containsKey(u.getTag())) {
					add(u, 0);
				}
			}
		}
	}
	
	public static void add(UnitInPool u, int group) {
		if (assignments.containsKey(u.getTag())) {
			groups.get(assignments.get(u.getTag())).remove(u);
		}
		assignments.put(u.getTag(), group);
		groups.get(group).add(u);
	}
	
	public static void disband(int group) {
		for (UnitInPool u : groups.remove(group)) {
			add(u, 0);
		}
	}
	
	public static int create(UnitInPool u) {
		int group = 0;
		while (groups.containsKey(group)) group++;
		groups.put(group, new ArrayList<>());
		groups.get(group).add(u);
		return group;
	}
	
}

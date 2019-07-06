package com.hjax.kagamine.game;

import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ControlGroups {
    public static Map<Integer, ArrayList<HjaxUnit>> groups = new HashMap<>();
    public static Map<Tag, Integer> assignments = new HashMap<>();

    public static void on_frame() {
        for (HjaxUnit u : GameInfoCache.get_units(Alliance.SELF)) {
            if (Game.is_combat(u.unit().getType()) && !Game.is_worker(u.unit().getType())) {
                if (!assignments.containsKey(u.tag())) {
                    add(u, 0);
                }
            }
        }
        ArrayList<Integer> empty = new ArrayList<>();
        for (Integer g : groups.keySet()) {
        	ArrayList<HjaxUnit> new_group = new ArrayList<>();
            for (HjaxUnit u : groups.get(g)) {
            	if (u.alive()) {
            		new_group.add(u);
            	}
            }
            groups.put(g, new_group);
            if (groups.get(g).size() == 0) empty.add(g);
        }
        for (int i : empty) {
        	disband(i);
        }
    }

    public static void add(HjaxUnit u, int group) {
        if (assignments.containsKey(u.tag())) {
            groups.get(assignments.get(u.tag())).remove(u);
        }
        assignments.put(u.tag(), group);
        if (!groups.containsKey(group)) {
        	groups.put(group, new ArrayList<>());
        }
        groups.get(group).add(u);
    }

    public static void disband(int group) {
        for (HjaxUnit u : groups.remove(group)) {
            add(u, 0);
        }
    }

    public static int create(HjaxUnit u) {
        int group = 0;
        while (groups.containsKey(group)) group++;
        groups.put(group, new ArrayList<>());
        groups.get(group).add(u);
        return group;
    }
    
    public static ArrayList<HjaxUnit> get(int i) {
    	if (groups.containsKey(i)) {
    		return groups.get(i);
    	}
    	return new ArrayList<>();
    }

}
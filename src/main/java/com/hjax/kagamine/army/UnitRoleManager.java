package com.hjax.kagamine.army;

import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Tag;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.game.HjaxUnit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UnitRoleManager {
	
	public enum UnitRole {
		ARMY,
		DEFENDER,
		HARASS
	}
	
    private static final Map<UnitRole, ArrayList<HjaxUnit>> groups = new HashMap<>();
    private static final Map<Tag, UnitRole> roles = new HashMap<>();

    public static void on_frame() {
    	
    	roles.clear();
    	for (UnitRole role : UnitRole.values()) {
    		groups.put(role, new ArrayList<>());
    	}
    	
        for (HjaxUnit u : GameInfoCache.get_units(Alliance.SELF)) {
        	if (!Game.is_structure(u.type()) && Game.is_combat(u.type()) && !Game.is_worker(u.type())) {
            	add(u, UnitRole.ARMY);
            }
        }
    }

    public static void add(HjaxUnit u, UnitRole group) {
        if (roles.containsKey(u.tag())) {
            groups.get(roles.get(u.tag())).remove(u);
        }
        roles.put(u.tag(), group);
        if (!groups.containsKey(group)) {
        	groups.put(group, new ArrayList<>());
        }
        groups.get(group).add(u);
    }
    
    public static ArrayList<HjaxUnit> get(UnitRole i) {
		if (groups.containsKey(i)) {
			ArrayList<HjaxUnit> result = new ArrayList<>(groups.get(i));
    		return result;
    	}
    	return new ArrayList<>();
    }

}
package com.hjax.kagamine.knowledge;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Ability;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.UnitTypeData;
import com.github.ocraft.s2client.protocol.data.Units;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;

public class Balance {
	
	static Map<UnitType, UnitType> overrides;
	static {
		overrides = new HashMap<>();
		overrides.put(Units.ZERG_LAIR, Units.ZERG_SPAWNING_POOL);
	}
	
	public static boolean has_tech_requirement(UnitType u) {
		UnitType current = u;
		while (Game.get_unit_type_data().get(current).getTechRequirement().orElse(Units.INVALID) != Units.INVALID) {
			if (overrides.containsKey(current)) {
				current = overrides.get(current);
			}
			else {
				current = Game.get_unit_type_data().get(current).getTechRequirement().orElse(Units.INVALID);
			}
			if (current == Units.ZERG_LAIR && GameInfoCache.get_units(Units.ZERG_HIVE).size() > 0) continue;
			if (GameInfoCache.count_friendly(current) == 0) return true;
		}
		return false;
	}
	
	public static UnitType next_tech_requirement(UnitType u) {
		UnitType current = u;
		UnitType best = Units.INVALID;
		while (Game.get_unit_type_data().get(current).getTechRequirement().orElse(Units.INVALID) != Units.INVALID) {
			if (overrides.containsKey(current)) {
				current = overrides.get(current);
			}
			else {
				current = Game.get_unit_type_data().get(current).getTechRequirement().orElse(Units.INVALID);
			}
			if (current == Units.ZERG_LAIR && GameInfoCache.get_units(Units.ZERG_HIVE).size() > 0) continue;
			if (GameInfoCache.count_friendly(current) == 0) best = current;
		}
		return best;
	}
	
	static Set<UnitType> get_production_structures(UnitType u) {
		Set<UnitType> result = new HashSet<>();
		for (UnitTypeData d : Game.get_unit_type_data().values()) {
			for (Ability a: d.getUnitType().getAbilities()) {
				if (Game.get_unit_type_data().get(u).getAbility().orElse(Abilities.INVALID) == a) {
					result.add(d.getUnitType());
				}
			}
		}
		return result;
	}
	
	public static boolean is_production_structure(UnitType u) {
		return u == Units.TERRAN_BARRACKS || u == Units.TERRAN_FACTORY || u == Units.TERRAN_STARPORT || u == Units.PROTOSS_WARP_GATE || u == Units.PROTOSS_GATEWAY || u == Units.PROTOSS_ROBOTICS_FACILITY || u == Units.PROTOSS_STARGATE || u == Units.ZERG_HATCHERY;
	}

	public static UnitType get_tech_structure(UnitType u) {
		if (overrides.containsKey(u)) {
			return overrides.get(u);
		}
		if (Game.get_unit_type_data().get(u).getTechRequirement().isPresent()) {
			return Game.get_unit_type_data().get(u).getTechRequirement().get();
		}
		return Units.INVALID;
	}
}

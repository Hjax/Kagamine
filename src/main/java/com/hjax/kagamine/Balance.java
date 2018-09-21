package com.hjax.kagamine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Ability;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.UnitTypeData;
import com.github.ocraft.s2client.protocol.data.Units;

public class Balance {
	
	static Map<UnitType, UnitType> overrides;
	static {
		overrides = new HashMap<>();
		overrides.put(Units.ZERG_LAIR, Units.ZERG_SPAWNING_POOL);
	}
	
	static boolean has_tech_requirement(UnitType u) {
		UnitType current = u;
		while (Game.get_unit_type_data().get(current).getTechRequirement().orElse(Units.INVALID) != Units.INVALID) {
			if (overrides.containsKey(current)) {
				current = overrides.get(current);
			}
			else {
				current = Game.get_unit_type_data().get(current).getTechRequirement().orElse(Units.INVALID);
			}
			if (GameInfoCache.get_units(current).size() == 0) return true;
		}
		return false;
	}
	
	static UnitType next_tech_requirement(UnitType u) {
		UnitType current = u;
		UnitType best = Units.INVALID;
		while (Game.get_unit_type_data().get(current).getTechRequirement().orElse(Units.INVALID) != Units.INVALID) {
			if (overrides.containsKey(current)) {
				current = overrides.get(current);
			}
			else {
				current = Game.get_unit_type_data().get(current).getTechRequirement().orElse(Units.INVALID);
			}
			if (GameInfoCache.get_units(current).size() == 0) best = current;
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

	public static UnitType get_tech_structure(UnitType u) {
		if (overrides.containsKey(u)) {
			return overrides.get(u);
		}
		return Game.get_unit_type_data().get(u).getTechRequirement().get();
	}
}

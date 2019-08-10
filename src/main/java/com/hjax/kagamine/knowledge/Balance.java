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
	
	private static final Map<UnitType, UnitType> overrides;
	
	private static final Map<UnitType, UnitType> morphs;
	
	static {
		overrides = new HashMap<>();
		overrides.put(Units.ZERG_LAIR, Units.ZERG_SPAWNING_POOL);
		overrides.put(Units.PROTOSS_ZEALOT, Units.PROTOSS_GATEWAY);
		overrides.put(Units.ZERG_HIVE, Units.ZERG_INFESTATION_PIT);
		overrides.put(Units.ZERG_GREATER_SPIRE, Units.ZERG_SPIRE);
		
		morphs = new HashMap<>();
		morphs.put(Units.ZERG_LURKER_MP, Units.ZERG_HYDRALISK);
		morphs.put(Units.ZERG_BANELING, Units.ZERG_ZERGLING);
		morphs.put(Units.ZERG_OVERSEER, Units.ZERG_OVERLORD);
		morphs.put(Units.ZERG_RAVAGER, Units.ZERG_ROACH);
		morphs.put(Units.ZERG_BROODLORD, Units.ZERG_CORRUPTOR);
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
			if (current == Units.ZERG_SPIRE && GameInfoCache.get_units(Units.ZERG_GREATER_SPIRE).size() > 0) continue;
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
			if (current == Units.ZERG_SPIRE && GameInfoCache.get_units(Units.ZERG_GREATER_SPIRE).size() > 0) continue;
			if (GameInfoCache.count_friendly(current) == 0) best = current;
		}
		return best;
	}
	
	public static Set<UnitType> get_production_structures(UnitType u) {
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
	
	public static boolean is_morph(UnitType u) {
		return morphs.containsKey(u);
	}
	
	public static UnitType get_morph_source(UnitType u) {
		return morphs.getOrDefault(u, Units.INVALID);
	}
}

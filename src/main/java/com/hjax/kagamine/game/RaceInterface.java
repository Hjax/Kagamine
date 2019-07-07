package com.hjax.kagamine.game;

import java.util.List;

import com.github.ocraft.s2client.protocol.data.Ability;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.game.Race;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.unitcontrollers.Worker;

public class RaceInterface {

	public static void make(UnitType type) {
		Ability creation_ability = Game.production_ability(type);
		List<UnitType> creation_units = Game.unit_with_ability(creation_ability);
		
		for (UnitType creation_unit : creation_units) {
			List<HjaxUnit> available_creators = GameInfoCache.get_units(Alliance.SELF, creation_unit);
			
			for (HjaxUnit u : available_creators) {
				if (u.is_worker()) {
					if (Worker.can_build(u)) {
						BaseManager.build(type);
						return;
					}
				} else if (u.idle()){
					u.use_ability(creation_ability);
					return;
				}
			}
		}
	}
	
	public static UnitType get_race_command_structure() {
		if (Game.race() == Race.PROTOSS) {
			return Units.PROTOSS_NEXUS;
		} else if (Game.race() == Race.TERRAN) {
			return Units.TERRAN_COMMAND_CENTER;
		} else {
			return Units.ZERG_HATCHERY;
		}
	}
	
	public static UnitType get_race_worker() {
		if (Game.race() == Race.PROTOSS) {
			return Units.PROTOSS_PROBE;
		} else if (Game.race() == Race.TERRAN) {
			return Units.TERRAN_SCV;
		} else {
			return Units.ZERG_DRONE;
		}
	}
	
	public static UnitType get_race_supply_structure() {
		if (Game.race() == Race.PROTOSS) {
			return Units.PROTOSS_PYLON;
		} else if (Game.race() == Race.TERRAN) {
			return Units.TERRAN_SUPPLY_DEPOT;
		} else {
			return Units.ZERG_OVERLORD;
		}
	}
	
	public static UnitType get_race_gas() {
		if (Game.race() == Race.PROTOSS) {
			return Units.PROTOSS_ASSIMILATOR;
		} else if (Game.race() == Race.TERRAN) {
			return Units.TERRAN_REFINERY;
		} else {
			return Units.ZERG_EXTRACTOR;
		}
	}
}

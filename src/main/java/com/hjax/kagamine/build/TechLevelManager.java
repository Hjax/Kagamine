package com.hjax.kagamine.build;

import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.economy.Base;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.enemymodel.EnemyModel;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.game.HjaxUnit;
import com.hjax.kagamine.game.RaceInterface;
import com.hjax.kagamine.knowledge.Balance;
import com.hjax.kagamine.knowledge.Wisdom;

public class TechLevelManager {
	
	public enum TechLevel {
		HATCH,
		LAIR,
		HIVE;
	}
	
	public static void on_frame() {

		if (getTechLevel() == TechLevel.HATCH && GameInfoCache.count(Units.ZERG_LAIR) == 0) {
			if (BaseManager.base_count() >= 2 && GameInfoCache.count(Units.ZERG_DRONE) > 30 && (Game.gas() > 100 || GameInfoCache.count(Units.ZERG_DRONE) > 45)) {
				if (Game.can_afford(Units.ZERG_LAIR)) {
					for (Base b: BaseManager.bases) {
						if (b.has_friendly_command_structure() && b.command_structure.done() && b.command_structure.idle()) {
							b.command_structure.use_ability(Abilities.MORPH_LAIR);
							break;
						}
					}
				}
				Game.purchase(Units.ZERG_LAIR);
			}
		} 
		
		if ((GameInfoCache.count(Units.ZERG_HIVE) > 0) || 
		   (getTechLevel().ordinal() >= TechLevel.LAIR.ordinal() && (EnemyModel.counts.getOrDefault(Units.PROTOSS_ROBOTICS_BAY, 0) > 0 || 
																	 EnemyModel.counts.getOrDefault(Units.PROTOSS_FLEET_BEACON, 0) > 0 || 													 
																	 EnemyModel.counts.getOrDefault(Units.TERRAN_FUSION_CORE, 0) > 0) &&
		   															 Game.worker_count() > 50)) {
			if (GameInfoCache.count(Units.ZERG_SPIRE) == 0) {
				if (Game.can_afford(Units.ZERG_SPIRE)) {
					BaseManager.build(Units.ZERG_SPIRE);
				}
				Game.purchase(Units.ZERG_SPIRE);
			}
			
			
		}
		
		if (getTechLevel() == TechLevel.LAIR && GameInfoCache.count(Units.ZERG_HIVE) == 0) {
			if ((BaseManager.base_count() >= 4 && GameInfoCache.count(Units.ZERG_DRONE) > 70 && Game.army_supply() >= 80)) {
				if (!Balance.has_tech_requirement(Units.ZERG_HIVE)) {
					if (Game.can_afford(Units.ZERG_HIVE)) {
						RaceInterface.make(Units.ZERG_HIVE);
					}
					Game.purchase(Units.ZERG_HIVE);
				} else if (GameInfoCache.count(Balance.next_tech_requirement(Units.ZERG_HIVE)) == 0) {
					if (Game.can_afford(Balance.next_tech_requirement(Units.ZERG_HIVE))) {
						RaceInterface.make(Balance.next_tech_requirement(Units.ZERG_HIVE));
					}
					Game.purchase(Balance.next_tech_requirement(Units.ZERG_HIVE));
				}
			}
		} 
	}
	
	public static TechLevel getTechLevel() {
		for (HjaxUnit hive : GameInfoCache.get_units(Alliance.SELF, Units.ZERG_HIVE)) {
			if (hive.done()) return TechLevel.HIVE;
		}
		if (GameInfoCache.get_units(Alliance.SELF, Units.ZERG_HIVE).size() > 0) {
			return TechLevel.HIVE;
		}
		for (HjaxUnit lair : GameInfoCache.get_units(Alliance.SELF, Units.ZERG_LAIR)) {
			if (lair.done()) return TechLevel.LAIR;
		}
		return TechLevel.HATCH;
	}
}

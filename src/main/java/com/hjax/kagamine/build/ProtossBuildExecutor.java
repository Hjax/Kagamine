package com.hjax.kagamine.build;

import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.army.ThreatManager;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.game.HjaxUnit;
import com.hjax.kagamine.game.RaceInterface;
import com.hjax.kagamine.knowledge.Balance;
import com.hjax.kagamine.knowledge.Wisdom;
import com.hjax.kagamine.unitcontrollers.Worker;

class ProtossBuildExecutor {

	public static void on_frame() {
		if (!build_completed()) execute_build();
		else {
			if (Game.supply_cap() < 200) {
				float production = GameInfoCache.count(Units.PROTOSS_GATEWAY) + GameInfoCache.count(Units.PROTOSS_WARP_GATE) + 2;
				if (Game.supply_cap() + (GameInfoCache.in_progress(RaceInterface.get_race_supply_structure()) * 8) - Game.supply() <= production * 2) {
					if (Game.can_afford(RaceInterface.get_race_supply_structure())) {
						RaceInterface.make(RaceInterface.get_race_supply_structure());
					}
					Game.purchase(RaceInterface.get_race_supply_structure());
				}
			}

			if (BaseManager.active_gases() + GameInfoCache.in_progress(Units.PROTOSS_ASSIMILATOR) < 5) {
				if (GameInfoCache.count(RaceInterface.get_race_worker()) > 45 || GameInfoCache.count(RaceInterface.get_race_gas()) == 0) {
					if ((Game.gas() < 400 && GameInfoCache.in_progress(RaceInterface.get_race_gas()) == 0) || Game.gas() < 150) {
						if ((BaseManager.active_gases() + GameInfoCache.in_progress(RaceInterface.get_race_gas()) < Build.ideal_gases) && ((BaseManager.active_gases() + GameInfoCache.in_progress(RaceInterface.get_race_gas())) < ((GameInfoCache.count(RaceInterface.get_race_worker())) / 8))) {
							if (Game.can_afford(RaceInterface.get_race_gas())) {
								BaseManager.build(RaceInterface.get_race_gas());
								Game.purchase(RaceInterface.get_race_gas());
							}
						}
					}
				}
			}


			if (Wisdom.should_build_workers()) {
				if (Game.can_afford(RaceInterface.get_race_worker())) {
					Game.purchase(RaceInterface.get_race_worker());
					RaceInterface.make(RaceInterface.get_race_worker());
				}
			} 

			if (ThreatManager.is_safe(BaseManager.get_next_base().location) ) {
				if (((!Wisdom.all_in_detected() && !Wisdom.proxy_detected()) || Game.army_supply() > 60 || Game.minerals() > 700) && GameInfoCache.in_progress(RaceInterface.get_race_command_structure()) == 0 && Wisdom.should_expand()) {
					if (!Game.can_afford(RaceInterface.get_race_command_structure())) {
						if (!BaseManager.get_next_base().has_walking_drone() && Game.minerals() > 100) {
							HjaxUnit drone = BaseManager.get_free_worker(BaseManager.get_next_base().location);
							if (drone != null) {
								BaseManager.get_next_base().set_walking_drone(drone);
							}
						}
					} else if (Game.can_afford(RaceInterface.get_race_command_structure())){
						BaseManager.build(RaceInterface.get_race_command_structure());
					}
					Game.purchase(RaceInterface.get_race_command_structure());
				}
			}

			UpgradeManager.on_frame();

			for (UnitType u: Composition.filler_comp()) {
				if (Balance.has_tech_requirement(u)) {
					if (!(GameInfoCache.count(Balance.next_tech_requirement(u)) > 0)) {
						if (Game.can_afford(Balance.next_tech_requirement(u))) {
							RaceInterface.make(Balance.next_tech_requirement(u));
						}
						Game.purchase(Balance.next_tech_requirement(u));
					}
				}
			}

			if (Wisdom.should_build_army() && next_army_unit() != Units.INVALID) {
				if (next_army_unit() != Units.INVALID) {
					if (Game.can_afford(next_army_unit())) {
						Game.purchase(next_army_unit());
						RaceInterface.make(next_army_unit());
					}
				}
			}
			
			if (GameInfoCache.count(Units.PROTOSS_GATEWAY) < Math.min(16, 1 + (BaseManager.base_count()) * 3)) {
				if (Game.can_afford(Units.PROTOSS_GATEWAY)) {
					RaceInterface.make(Units.PROTOSS_GATEWAY);
					Game.purchase(Units.PROTOSS_GATEWAY);
				}
			}
		}
	}

	private static void execute_build() {
		if (Build.build.get(Build.build_index).getKey() <= Game.supply()) {
			if (Build.build.get(Build.build_index).getValue() == Units.PROTOSS_NEXUS && !(BaseManager.get_next_base().has_walking_drone()) && Game.minerals() > 250) {
				for (HjaxUnit u: GameInfoCache.get_units(Alliance.SELF, RaceInterface.get_race_worker())) {
					if (Worker.can_build(u)) {
						BaseManager.get_next_base().set_walking_drone(u);
						return;
					}
				}
			}
			if (Game.can_afford(Build.build.get(Build.build_index).getValue())) {
				RaceInterface.make(Build.build.get(Build.build_index).getValue());
				Game.purchase(Build.build.get(Build.build_index).getValue());
				Build.build_index++;
			}
			return;
		}
		if (Game.can_afford(RaceInterface.get_race_worker())) {
			Game.purchase(RaceInterface.get_race_worker());
			RaceInterface.make(RaceInterface.get_race_worker());
		}
	}

	private static boolean build_completed() {
		return Build.build_index >= Build.build.size();
	}

	private static UnitType next_army_unit() {
		UnitType best = Units.INVALID;
		for (UnitType u: Composition.filler_comp()) {
			if (GameInfoCache.count_friendly(Balance.get_tech_structure(u)) > 0) {
				if (best == Units.INVALID) best = u;
				if (Game.get_unit_type_data().get(u).getVespeneCost().orElse(0) < Game.gas()) {
					if (Game.get_unit_type_data().get(u).getVespeneCost().orElse(0) > Game.get_unit_type_data().get(best).getVespeneCost().orElse(0)) {
						best = u;
					}
				}
			}
		}
		return best;
	}

}

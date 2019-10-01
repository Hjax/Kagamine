package com.hjax.kagamine.build;


import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.data.Upgrades;
import com.github.ocraft.s2client.protocol.game.Race;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.army.UnitMovementManager;
import com.hjax.kagamine.build.TechLevelManager.TechLevel;
import com.hjax.kagamine.army.ThreatManager;
import com.hjax.kagamine.economy.Base;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.economy.EconomyManager;
import com.hjax.kagamine.enemymodel.EnemyModel;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.game.HjaxUnit;
import com.hjax.kagamine.knowledge.Balance;
import com.hjax.kagamine.knowledge.Wisdom;
import com.hjax.kagamine.knowledge.ZergWisdom;
import com.hjax.kagamine.unitcontrollers.Worker;
import com.hjax.kagamine.unitcontrollers.zerg.Larva;

public class ZergBuildExecutor {
	public static boolean pulled_off_gas = false;
	
	public static void on_frame() {
		if (!build_completed()) execute_build();
		else {
			if (Game.supply_cap() < 200) {
				float larva = EconomyManager.larva_rate();
				if (ZergWisdom.should_build_queens()) larva += 2;
				int hatches = 0;
				for (HjaxUnit building_hatch : GameInfoCache.get_units(Alliance.SELF, Units.ZERG_HATCHERY)) {
					if (building_hatch.progress() > 0.5 && !building_hatch.done()) {
						hatches++;
					}
				}
				if (Game.supply_cap() + (GameInfoCache.in_progress(Units.ZERG_OVERLORD) * 8) + hatches * 4 - Game.supply() <= larva * 2) {
					if (Larva.has_larva()) {
						if (Game.can_afford(Units.ZERG_OVERLORD)) {
							Larva.produce_unit(Units.ZERG_OVERLORD);
						} 
						Game.purchase(Units.ZERG_OVERLORD);
					}
				}
			}
			
			if (Wisdom.should_build_army() && EnemyModel.enemyArmy() > Game.army_supply() * 1.5) {
				if (Larva.has_larva() && Game.can_afford(next_army_unit())) {
					if (next_army_unit() != Units.INVALID) {
						Game.purchase(next_army_unit());
						Larva.produce_unit(next_army_unit());
					}
				}
			}
			
			if (GameInfoCache.count(Units.ZERG_DRONE) > 45 || GameInfoCache.count(Units.ZERG_EXTRACTOR) == 0) {
				if ((Game.gas() < 400 && GameInfoCache.in_progress(Units.ZERG_EXTRACTOR) == 0) || Game.gas() < 150) {
					if ((BaseManager.active_gases() + GameInfoCache.in_progress(Units.ZERG_EXTRACTOR) < Build.ideal_gases) && ((BaseManager.active_gases() + GameInfoCache.in_progress(Units.ZERG_EXTRACTOR)) < ((GameInfoCache.count(Units.ZERG_DRONE) - 15) / 8))) {
						if (Game.can_afford(Units.ZERG_EXTRACTOR)) {
							BaseManager.build(Units.ZERG_EXTRACTOR);
							Game.purchase(Units.ZERG_EXTRACTOR);
						}
					}
				}
			}
			
			if (BaseManager.base_count() == 1 && Wisdom.cannon_rush() || Wisdom.proxy_detected()) {
				if (GameInfoCache.count(Units.ZERG_EXTRACTOR) < 2) {
					if (Game.can_afford(Units.ZERG_EXTRACTOR)) {
						BaseManager.build(Units.ZERG_EXTRACTOR);
					}
					Game.purchase(Units.ZERG_EXTRACTOR);
				}
			}
			
			// TODO make this less of a hack
			int total_gas = Game.gas();
			
			int total_needed = Game.get_upgrade_data().get(Upgrades.OVERLORD_SPEED).getVespeneCost().orElse(0) + Game.get_upgrade_data().get(Upgrades.ZERGLING_MOVEMENT_SPEED).getVespeneCost().orElse(0);
			
			if (GameInfoCache.is_researching(Upgrades.ZERGLING_MOVEMENT_SPEED) || Game.has_upgrade(Upgrades.ZERGLING_MOVEMENT_SPEED)) {
				total_gas += Game.get_upgrade_data().get(Upgrades.ZERGLING_MOVEMENT_SPEED).getVespeneCost().orElse(0);
			}
			if ((GameInfoCache.is_researching(Upgrades.OVERLORD_SPEED) || Game.has_upgrade(Upgrades.OVERLORD_SPEED))) {
				total_gas += Game.get_upgrade_data().get(Upgrades.OVERLORD_SPEED).getVespeneCost().orElse(0);
			}
			if (Wisdom.all_in_detected() && GameInfoCache.get_opponent_race() == Race.ZERG) {
				total_gas += Game.get_upgrade_data().get(Upgrades.OVERLORD_SPEED).getVespeneCost().orElse(0);
			}
			if ((GameInfoCache.count(Units.ZERG_DRONE) <= 12 && !Build.pull_off_gas) || (Build.pull_off_gas && total_gas > total_needed)) {
				pulled_off_gas = true;
				for (HjaxUnit drone: GameInfoCache.get_units(Alliance.SELF, Units.ZERG_DRONE)) {
					if (drone.ability() == Abilities.HARVEST_GATHER && 
							drone.orders().get(0).getTargetedUnitTag().isPresent() && GameInfoCache.get_unit((drone.orders().get(0).getTargetedUnitTag()).get()) != null && GameInfoCache.get_unit((drone.orders().get(0).getTargetedUnitTag()).get()).type() == Units.ZERG_EXTRACTOR) {
						drone.stop();
					}
				}
			}
			
			if ((GameInfoCache.count(Units.ZERG_DRONE) > 35 && pulled_off_gas) || !Build.pull_off_gas) {
				pulled_off_gas = false;
				Build.pull_off_gas = false;
			}
			if (Game.army_supply() >= 2 && Game.army_supply() < 30) {
				if (GameInfoCache.get_opponent_race() != Race.ZERG) {
					if (BaseManager.base_count() == 1 && Wisdom.proxy_detected()) {
						if (GameInfoCache.count(Units.ZERG_SPINE_CRAWLER) < 3 && !Wisdom.cannon_rush()) {
							if (GameInfoCache.count_friendly(Units.ZERG_SPAWNING_POOL) > 0) {
								if (Wisdom.all_in_detected() || Wisdom.proxy_detected()) {
									if (Game.can_afford(Units.ZERG_SPINE_CRAWLER)) {
										BaseManager.build(Units.ZERG_SPINE_CRAWLER);
									}
									Game.purchase(Units.ZERG_SPINE_CRAWLER);
								}
							}
						}
					} else if (BaseManager.base_count() >= 2 && EnemyModel.enemyBaseCount() == 1) {
						if (GameInfoCache.count(Units.ZERG_SPINE_CRAWLER) < Math.min(Math.max((EnemyModel.enemyArmy() / 3.0), 3), 7) && !Wisdom.cannon_rush() && Game.worker_count() > 20) {
							if (GameInfoCache.count_friendly(Units.ZERG_SPAWNING_POOL) > 0) {
								if (Wisdom.all_in_detected() || Wisdom.proxy_detected()) {
									if (Game.can_afford(Units.ZERG_SPINE_CRAWLER)) {
										BaseManager.build(Units.ZERG_SPINE_CRAWLER);
									}
									Game.purchase(Units.ZERG_SPINE_CRAWLER);
								}
							}
						}
					}
				}
			}

			if (BaseManager.get_next_base() != null && ThreatManager.is_safe(BaseManager.get_next_base().location) ) {
				if (((!Wisdom.all_in_detected() && !Wisdom.proxy_detected()) || Game.army_supply() > 60 || Game.minerals() > 700) && GameInfoCache.in_progress(Units.ZERG_HATCHERY) == 0 && Wisdom.should_expand()) {
					if (!Game.can_afford(Units.ZERG_HATCHERY)) {
						if (!BaseManager.get_next_base().has_walking_drone() && Game.minerals() > 100) {
							HjaxUnit drone = BaseManager.get_free_worker(BaseManager.get_next_base().location);
							if (drone != null) {
								BaseManager.get_next_base().set_walking_drone(drone);
							}
						}
					} else {
						BaseManager.build(Units.ZERG_HATCHERY);
					}
					Game.purchase(Units.ZERG_HATCHERY);
				}
			}

			if (GameInfoCache.count(Units.ZERG_DRONE) >= 20) {
				boolean needs_spores = false;
				for (UnitType u : EnemyModel.counts.keySet()) {
					if (u == Units.TERRAN_BATTLECRUISER || u == Units.TERRAN_STARPORT_TECHLAB ||  u == Units.PROTOSS_DARK_SHRINE || u == Units.ZERG_MUTALISK || u == Units.PROTOSS_DARK_TEMPLAR || u == Units.TERRAN_BANSHEE || u == Units.PROTOSS_ORACLE) {
						if (EnemyModel.counts.get(u) > 0) {
							needs_spores = true;
						}
					}
					if (u == Units.PROTOSS_PHOENIX && EnemyModel.counts.get(u) > 3) {
						needs_spores = true;
					}
				}
				
				if (EnemyModel.counts.getOrDefault(Units.ZERG_MUTALISK, 0) > 15 || EnemyModel.counts.getOrDefault(Units.TERRAN_BANSHEE, 0) > 5 || EnemyModel.counts.getOrDefault(Units.PROTOSS_PHOENIX, 0) > 7 || EnemyModel.counts.getOrDefault(Units.TERRAN_BATTLECRUISER, 0) > 2) {
					if (GameInfoCache.count(Units.ZERG_DRONE) > 50 && TechLevelManager.getTechLevel() == TechLevel.HATCH) {
						BaseManager.build_triangle_spores();
					}
				}
				// TODO remove this hack
				if (needs_spores) {
					BaseManager.build_defensive_spores();
				}
			}
			
			if (Game.minerals() > 50 && Game.gas() > 50 && GameInfoCache.count_friendly(Units.ZERG_LAIR) > 0 && GameInfoCache.count(Units.ZERG_OVERSEER) + GameInfoCache.count(Units.ZERG_OVERLORD_COCOON) < Math.max(UnitMovementManager.detection_points, 2)) {
				for (HjaxUnit ovie: GameInfoCache.get_units(Alliance.SELF, Units.ZERG_OVERLORD)) {
					Game.spend(50, 50);
					ovie.use_ability(Abilities.MORPH_OVERSEER);
					break;
				}
			}

			UpgradeManager.on_frame();

			for (UnitType u: Composition.full_comp()) {
				if (!pulled_off_gas && ((GameInfoCache.count(Units.ZERG_DRONE) > 25 || Wisdom.proxy_detected() || Wisdom.worker_rush() || Wisdom.cannon_rush() || (Wisdom.all_in_detected() && GameInfoCache.count(Units.ZERG_DRONE) > 25)) || (u == Units.ZERG_BANELING && GameInfoCache.get_opponent_race() == Race.ZERG && GameInfoCache.count(Units.ZERG_DRONE) >= 16))) {
					if (Balance.has_tech_requirement(u)) {
						if (!(GameInfoCache.count(Balance.next_tech_requirement(u)) > 0)) {
							if (Balance.next_tech_requirement(u) == Units.ZERG_INFESTATION_PIT && (BaseManager.base_count() < 4 || GameInfoCache.count(Units.ZERG_DRONE) < 60)) continue;
							if (Balance.next_tech_requirement(u) == Units.ZERG_GREATER_SPIRE) {
								for (HjaxUnit spire : GameInfoCache.get_units(Alliance.SELF, Units.ZERG_SPIRE)) {
									if (spire.done()) {
										if (Game.can_afford(Balance.next_tech_requirement(u))) {
											spire.use_ability(Abilities.MORPH_GREATER_SPIRE);
											break;
										}
										Game.purchase(Units.ZERG_GREATER_SPIRE);
									}
								}
							} else {
								if (Game.can_afford(Balance.next_tech_requirement(u))) {
									BaseManager.build(Balance.next_tech_requirement(u));
								}
								Game.purchase(Balance.next_tech_requirement(u));
							}
						}
					}
				}
			}

			if (ZergWisdom.should_build_queens()) {
				if (Game.supply_cap() - Game.supply() >= 2) {
					if (Game.can_afford(Units.ZERG_QUEEN)) {
						for (HjaxUnit u: GameInfoCache.get_units(Alliance.SELF)) {
							if (u.is_command() && u.done() && u.idle()) {
								Game.purchase(Units.ZERG_QUEEN);
								u.use_ability(Abilities.TRAIN_QUEEN);
								break;
							}
						}
					} else if (GameInfoCache.count(Units.ZERG_QUEEN) < BaseManager.base_count()) {
						Game.purchase(Units.ZERG_QUEEN);
					}
				}
			}

			if ((ThreatManager.attacking_supply() < GameInfoCache.attacking_army_supply()) || Wisdom.cannon_rush() || Wisdom.proxy_detected() || GameInfoCache.attacking_army_supply() > 20) {
				if (GameInfoCache.count_friendly(Units.ZERG_ROACH) > 0 && Composition.full_comp().contains(Units.ZERG_RAVAGER)) {
					if (Game.minerals() > 25 && Game.gas() > 75) {
						for (HjaxUnit unit: GameInfoCache.get_units(Alliance.SELF, Units.ZERG_ROACH)) {
							unit.use_ability(Abilities.MORPH_RAVAGER);
							break;
						}
					}
					Game.spend(25, 75);
				}
				if (GameInfoCache.count_friendly(Units.ZERG_CORRUPTOR) > 0 && Composition.full_comp().contains(Units.ZERG_BROODLORD) && GameInfoCache.count(Units.ZERG_BROODLORD) < Composition.comp().getOrDefault(Units.ZERG_BROODLORD, 0)) {
					if (Game.minerals() >= 150 && Game.gas() >= 150) {
						for (HjaxUnit unit: GameInfoCache.get_units(Alliance.SELF, Units.ZERG_CORRUPTOR)) {
							unit.use_ability(Abilities.MORPH_BROODLORD);
							break;
						}
					}
					Game.spend(150, 150);
				}
				if (GameInfoCache.count_friendly(Units.ZERG_HYDRALISK) > 0 && Composition.full_comp().contains(Units.ZERG_LURKER_MP) && GameInfoCache.count(Units.ZERG_LURKER_MP) + GameInfoCache.count(Units.ZERG_LURKER_MP_EGG) < Composition.comp().getOrDefault(Units.ZERG_LURKER_MP, 0)) {
					if (Game.minerals() >= 50 && Game.gas() >= 100) {
						for (HjaxUnit unit: GameInfoCache.get_units(Alliance.SELF, Units.ZERG_HYDRALISK)) {
							unit.use_ability(Abilities.MORPH_LURKER);
							break;
						}
					}
					Game.spend(50, 100);
				}
				if (GameInfoCache.get_opponent_race() != Race.ZERG) {
					if ((Game.minerals() > 25 && Game.gas() > 25 && (GameInfoCache.count_friendly(Units.ZERG_ZERGLING) >= 10) && GameInfoCache.count_friendly(Units.ZERG_ZERGLING) > GameInfoCache.count_friendly(Units.ZERG_BANELING) * 2 && Composition.full_comp().contains(Units.ZERG_BANELING))) {
						for (HjaxUnit unit: GameInfoCache.get_units(Alliance.SELF, Units.ZERG_ZERGLING)) {
							unit.use_ability(Abilities.TRAIN_BANELING);
							Game.spend(25, 25);
							break;
						}
					}
				}
				else {
					if ((Game.minerals() > 25 && Game.gas() > 25 && GameInfoCache.get_opponent_race() == Race.ZERG && GameInfoCache.count(Units.ZERG_BANELING) < 6 && GameInfoCache.count_friendly(Units.ZERG_ZERGLING) > 0 && Composition.full_comp().contains(Units.ZERG_BANELING))) {
						for (HjaxUnit unit: GameInfoCache.get_units(Alliance.SELF, Units.ZERG_ZERGLING)) {
							unit.use_ability(Abilities.TRAIN_BANELING);
							Game.spend(25, 25);
							break;
						}
					}
				}
			}
			
			if (!Wisdom.should_build_workers() || (Wisdom.should_build_army() && next_army_unit() != Units.INVALID)) {
				if (next_army_unit() != Units.INVALID) {
					if (Larva.has_larva() && Game.can_afford(next_army_unit())) {
						Game.purchase(next_army_unit());
						Larva.produce_unit(next_army_unit());
					}
				}
			}
			else if (Wisdom.should_build_workers()) {
				if (Game.can_afford(Units.ZERG_DRONE) && Larva.has_larva()) {
					Game.purchase(Units.ZERG_DRONE);
					Larva.produce_unit(Units.ZERG_DRONE);
				}
			}

			if ((GameInfoCache.count(Units.ZERG_DRONE) >= Wisdom.worker_cap()) && !Wisdom.should_build_workers() && BaseManager.active_gases() + GameInfoCache.in_progress(Units.ZERG_EXTRACTOR) < Build.ideal_gases) {
				if ((Game.gas() < 400 && GameInfoCache.in_progress(Units.ZERG_EXTRACTOR) == 0) || Game.gas() < 150) {
					if (Game.can_afford(Units.ZERG_EXTRACTOR)) {
						BaseManager.build(Units.ZERG_EXTRACTOR);
						Game.purchase(Units.ZERG_EXTRACTOR);
					}
				}
			}
		}
	}
		

	private static void execute_build() {
		if (Build.build.get(Build.build_index).getKey() <= Game.supply()) {
			if (Build.build.get(Build.build_index).getValue() == Units.ZERG_HATCHERY && !(BaseManager.get_next_base().has_walking_drone()) && Game.minerals() > 150) {
				for (HjaxUnit u: GameInfoCache.get_units(Alliance.SELF, Units.ZERG_DRONE)) {
					if (Worker.can_build(u)) {
						BaseManager.get_next_base().set_walking_drone(u);
						return;
					}
				}
			}
			if (Game.can_afford(Build.build.get(Build.build_index).getValue())) {
				if (Game.is_structure(Build.build.get(Build.build_index).getValue())) {
					BaseManager.build(Build.build.get(Build.build_index).getValue());
					Game.purchase(Build.build.get(Build.build_index).getValue());
					Build.build_index++;
				} else {
					if (Larva.has_larva()) {
						Game.purchase(Build.build.get(Build.build_index).getValue());
						Larva.produce_unit(Build.build.get(Build.build_index).getValue());
						Build.build_index++;
					}
				}
			}
			return;
		}
		if (Larva.has_larva() && Game.can_afford(Units.ZERG_DRONE)) {
			Game.purchase(Units.ZERG_DRONE);
			Larva.produce_unit(Units.ZERG_DRONE);
		}
	}
	
	private static boolean build_completed() {
		return Build.build_index >= Build.build.size();
	}
	
	public static UnitType next_army_unit() {
		UnitType best = Units.INVALID;
		
		boolean save_gas = false;
		
		for (UnitType u: Composition.comp().keySet()) {
			if (u == Units.ZERG_QUEEN) continue;
			UnitType current = u;
			int limit = Composition.comp().getOrDefault(current, 0);
			if (Balance.is_morph(u)) {
				current = Balance.get_morph_source(u);
				limit +=  Composition.comp().getOrDefault(current, 0);
				if (Balance.get_tech_structure(u) != Units.ZERG_SPIRE || GameInfoCache.count(Units.ZERG_GREATER_SPIRE) == 0) {
					if (!(GameInfoCache.count(Balance.get_tech_structure(u)) > 0)) continue;
				}
			}
			if (!Balance.has_tech_requirement(current)) {
				if (Game.get_unit_type_data().get(current).getVespeneCost().orElse(0) < Math.max(Game.gas(), 1)) {
					if (GameInfoCache.count(current) >= limit) continue;
					if (best == Units.INVALID || Game.get_unit_type_data().get(current).getVespeneCost().orElse(0) > Game.get_unit_type_data().get(best).getVespeneCost().orElse(0)) {
						best = current;
					}
				} else {
					save_gas = true;
				}
			}
		}
		
		for (UnitType u: Composition.filler_comp()) {
			UnitType current = u;
			if (Balance.is_morph(u)) {
				current = Balance.get_morph_source(u);
			}
			if (!Balance.has_tech_requirement(current)) {
				if (Game.get_unit_type_data().get(current).getVespeneCost().orElse(0) < Math.max(Game.gas(), 1)) {
					if (Game.get_unit_type_data().get(current).getVespeneCost().orElse(0) < 1 || !save_gas) {
	 					if (best == Units.INVALID || Game.get_unit_type_data().get(current).getVespeneCost().orElse(0) > Game.get_unit_type_data().get(best).getVespeneCost().orElse(0)) {
							best = current;
						}
					}
				}
			}
		}
		return best;
	}
	
}

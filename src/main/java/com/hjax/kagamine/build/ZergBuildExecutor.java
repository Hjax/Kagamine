package com.hjax.kagamine.build;


import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.data.Upgrades;
import com.github.ocraft.s2client.protocol.game.Race;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.army.BaseDefense;
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
			
			// TODO make this less of a hack
			if ((GameInfoCache.count(Units.ZERG_DRONE) <= 12 && !Build.pull_off_gas) || (Build.pull_off_gas && Game.gas() > 200) || ((GameInfoCache.is_researching(Upgrades.ZERGLING_MOVEMENT_SPEED) || Game.has_upgrade(Upgrades.ZERGLING_MOVEMENT_SPEED)) && (GameInfoCache.is_researching(Upgrades.OVERLORD_SPEED) || Game.has_upgrade(Upgrades.OVERLORD_SPEED)) && Build.pull_off_gas)) {
				pulled_off_gas = true;
				for (HjaxUnit drone: GameInfoCache.get_units(Alliance.SELF, Units.ZERG_DRONE)) {
					if (drone.ability() == Abilities.HARVEST_GATHER && 
							GameInfoCache.get_unit((drone.orders().get(0).getTargetedUnitTag()).get()).type() == Units.ZERG_EXTRACTOR) {
						drone.stop();
					}
				}
			}
			
			if ((GameInfoCache.count(Units.ZERG_DRONE) > 35 && pulled_off_gas) || !Build.pull_off_gas) {
				pulled_off_gas = false;
				Build.pull_off_gas = false;
			}
			if (Game.army_supply() >= 2 && Game.army_supply() < 30 && BaseManager.base_count() < 3) {
				if (GameInfoCache.get_opponent_race() != Race.ZERG) {
					if (GameInfoCache.count(Units.ZERG_SPINE_CRAWLER) < 3 && !Wisdom.cannon_rush() && Build.scout) {
						if (GameInfoCache.count_friendly(Units.ZERG_SPAWNING_POOL) > 0 && (BaseManager.base_count() > 1 || Wisdom.proxy_detected())) {
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
					if (u == Units.TERRAN_BATTLECRUISER || u == Units.ZERG_MUTALISK || u == Units.PROTOSS_DARK_TEMPLAR || u == Units.TERRAN_BANSHEE || u == Units.PROTOSS_PHOENIX || u == Units.PROTOSS_ORACLE) {
						if (EnemyModel.counts.get(u) > 0) {
							needs_spores = true;
						}
					}
				}
				
				if (EnemyModel.counts.getOrDefault(Units.ZERG_MUTALISK, 0) > 5 || EnemyModel.counts.getOrDefault(Units.TERRAN_BANSHEE, 0) > 1 || EnemyModel.counts.getOrDefault(Units.PROTOSS_PHOENIX, 0) > 3) {
					if (GameInfoCache.count(Units.ZERG_DRONE) > 30) {
						BaseManager.build_triangle_spores();
					}
				}
				// TODO remove this hack
				if (needs_spores) {
					if (!Game.can_afford(Units.ZERG_SPORE_CRAWLER) && GameInfoCache.count(Units.ZERG_SPORE_CRAWLER) < 1) return;
					BaseManager.build_defensive_spores();
				}
			}
			
			if (Game.minerals() > 50 && Game.gas() > 50 && GameInfoCache.count_friendly(Units.ZERG_LAIR) > 0 && GameInfoCache.count(Units.ZERG_OVERSEER) + GameInfoCache.count(Units.ZERG_OVERLORD_COCOON) < Math.max(BaseDefense.detection_points, 2)) {
				for (HjaxUnit ovie: GameInfoCache.get_units(Alliance.SELF, Units.ZERG_OVERLORD)) {
					Game.spend(50, 50);
					ovie.use_ability(Abilities.MORPH_OVERSEER);
					break;
				}
			}

			UpgradeManager.on_frame();

			for (UnitType u: Composition.comp()) {
				if (!pulled_off_gas && ((GameInfoCache.count(Units.ZERG_DRONE) > Build.tech_drones || (Wisdom.all_in_detected() && GameInfoCache.count(Units.ZERG_DRONE) > 25)) || (u == Units.ZERG_BANELING && GameInfoCache.get_opponent_race() == Race.ZERG && GameInfoCache.count(Units.ZERG_DRONE) >= 16))) {
					if (Balance.has_tech_requirement(u)) {
						if (!(GameInfoCache.count(Balance.next_tech_requirement(u)) > 0)) {
							if (Balance.next_tech_requirement(u) == Units.ZERG_INFESTATION_PIT && (BaseManager.base_count() < 4 || GameInfoCache.count(Units.ZERG_DRONE) < 60)) continue;
							if (Balance.next_tech_requirement(u) == Units.ZERG_LAIR) {
								if (Build.two_base_tech || (BaseManager.base_count() >= 2 && GameInfoCache.count(Units.ZERG_DRONE) > 40)) {
									if (Game.can_afford(Balance.next_tech_requirement(u))) {
										for (Base b: BaseManager.bases) {
											if (b.has_friendly_command_structure() && b.command_structure.done() && b.command_structure.idle()) {
												b.command_structure.use_ability(Abilities.MORPH_LAIR);
												break;
											}
										}
									}
									Game.purchase(Units.ZERG_LAIR);
								}
							} else if (Balance.next_tech_requirement(u) == Units.ZERG_HIVE) {
								if (Game.can_afford(Balance.next_tech_requirement(u))) {
									for (Base b: BaseManager.bases) {
										if (b.has_friendly_command_structure() && b.command_structure.done() && b.command_structure.idle() && b.command_structure.type() == Units.ZERG_LAIR) {
											b.command_structure.use_ability(Abilities.MORPH_HIVE);
											break;
										}
									}
								}
								Game.purchase(Units.ZERG_HIVE);
							} else if (Balance.next_tech_requirement(u) == Units.ZERG_GREATER_SPIRE) {
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

			if (!ThreatManager.under_attack() || Wisdom.cannon_rush() || Wisdom.proxy_detected() || GameInfoCache.attacking_army_supply() > 50) {
				if (Game.minerals() > 25 && Game.gas() > 75 && GameInfoCache.count_friendly(Units.ZERG_ROACH) > 0 && Composition.comp().contains(Units.ZERG_RAVAGER)) {
					for (HjaxUnit unit: GameInfoCache.get_units(Alliance.SELF, Units.ZERG_ROACH)) {
						unit.use_ability(Abilities.MORPH_RAVAGER);
						Game.spend(25, 75);
						break;
					}
				}
				if (GameInfoCache.count_friendly(Units.ZERG_CORRUPTOR) > 0 && Composition.comp().contains(Units.ZERG_BROODLORD) && GameInfoCache.count(Units.ZERG_BROODLORD) < 15) {
					if (Game.minerals() >= 150 && Game.gas() >= 150) {
						for (HjaxUnit unit: GameInfoCache.get_units(Alliance.SELF, Units.ZERG_CORRUPTOR)) {
							unit.use_ability(Abilities.MORPH_BROODLORD);
							break;
						}
					}
					Game.spend(150, 150);
				}
				if (GameInfoCache.count_friendly(Units.ZERG_HYDRALISK) > 0 && Composition.comp().contains(Units.ZERG_LURKER_MP) && GameInfoCache.count(Units.ZERG_LURKER_MP) < 15) {
					if (Game.minerals() >= 50 && Game.gas() >= 100) {
						for (HjaxUnit unit: GameInfoCache.get_units(Alliance.SELF, Units.ZERG_HYDRALISK)) {
							unit.use_ability(Abilities.MORPH_LURKER);
							break;
						}
					}
					Game.spend(50, 100);
				}
				if (GameInfoCache.get_opponent_race() != Race.ZERG) {
					if ((Game.minerals() > 25 && Game.gas() > 25 && (GameInfoCache.count_friendly(Units.ZERG_ZERGLING) >= 10) && GameInfoCache.count_friendly(Units.ZERG_ZERGLING) > GameInfoCache.count_friendly(Units.ZERG_BANELING) * 2 && Composition.comp().contains(Units.ZERG_BANELING))) {
						for (HjaxUnit unit: GameInfoCache.get_units(Alliance.SELF, Units.ZERG_ZERGLING)) {
							unit.use_ability(Abilities.TRAIN_BANELING);
							Game.spend(25, 25);
							break;
						}
					}
				}
				else {
					if ((Game.minerals() > 25 && Game.gas() > 25 && GameInfoCache.get_opponent_race() == Race.ZERG && GameInfoCache.count(Units.ZERG_BANELING) < 6 && GameInfoCache.count_friendly(Units.ZERG_ZERGLING) > 0 && Composition.comp().contains(Units.ZERG_BANELING))) {
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

			if ((GameInfoCache.count(Units.ZERG_DRONE) >= Wisdom.worker_cap() || GameInfoCache.count(Units.ZERG_HATCHERY) >= Build.ideal_bases && Build.ideal_bases > 0) && !Wisdom.should_build_workers() && BaseManager.active_gases() + GameInfoCache.in_progress(Units.ZERG_EXTRACTOR) < Build.ideal_gases) {
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
		if (GameInfoCache.get_opponent_race() == Race.ZERG) {
			if (GameInfoCache.count(Units.ZERG_BANELING) == 0 && Composition.comp().contains(Units.ZERG_BANELING) && GameInfoCache.count_friendly(Units.ZERG_BANELING_NEST) > 0) return Units.ZERG_ZERGLING;
		}
		UnitType best = Units.INVALID;
		for (UnitType u: Composition.comp()) {
			if (u == Units.ZERG_BANELING) continue;
			if (u == Units.ZERG_RAVAGER) continue;
			if (u == Units.ZERG_BROODLORD) continue;
			if (u == Units.ZERG_LURKER_MP) continue;
			if (u == Units.ZERG_ULTRALISK && GameInfoCache.count(Units.ZERG_ULTRALISK) > 6) continue; 
			if (Composition.comp().contains(Units.ZERG_BANELING) && u == Units.ZERG_HYDRALISK && GameInfoCache.count(Units.ZERG_HYDRALISK) >= 15) continue;
			if (u == Units.ZERG_HYDRALISK && GameInfoCache.count(Units.ZERG_HYDRALISK) < 15 && Composition.comp().contains(Units.ZERG_BROODLORD)) return u;
			if (u == Units.ZERG_CORRUPTOR && GameInfoCache.count(Units.ZERG_CORRUPTOR) >= 15) continue;
			if (u == Units.ZERG_CORRUPTOR && Composition.comp().contains(Units.ZERG_BROODLORD) && GameInfoCache.count_friendly(Units.ZERG_GREATER_SPIRE) == 0) continue;
			if (u == Units.ZERG_INFESTOR && GameInfoCache.count(Units.ZERG_INFESTOR) >= 6) continue;
			if (u == Units.ZERG_VIPER && GameInfoCache.count(Units.ZERG_VIPER) >= 4) continue;
			if (u == Units.ZERG_BANELING && GameInfoCache.count(Units.ZERG_BANELING) < 30 && GameInfoCache.get_opponent_race() != Race.ZERG) return Units.ZERG_ZERGLING;
			if (u == Units.ZERG_HYDRALISK && GameInfoCache.count(Units.ZERG_HYDRALISK) >= 15 && Composition.comp().contains(Units.ZERG_BROODLORD) && GameInfoCache.count(Units.ZERG_BROODLORD) < 15) continue;
			if (u == Units.ZERG_CORRUPTOR && GameInfoCache.count(Units.ZERG_CORRUPTOR) >= 4 && Composition.comp().contains(Units.ZERG_BROODLORD) && GameInfoCache.count(Units.ZERG_BROODLORD) < 10) continue;
			if (u == Units.ZERG_CORRUPTOR && GameInfoCache.count(Units.ZERG_CORRUPTOR) < 10 && Game.army_supply() > 30 && BaseManager.active_gases() >= 4 && (GameInfoCache.count_friendly(Units.ZERG_SPIRE) + GameInfoCache.count_friendly(Units.ZERG_GREATER_SPIRE)) > 0) return Units.ZERG_CORRUPTOR;
			if (u == Units.ZERG_MUTALISK && GameInfoCache.count(Units.ZERG_MUTALISK) >= 8 && GameInfoCache.get_opponent_race() == Race.TERRAN) continue;
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

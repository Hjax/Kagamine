package com.hjax.kagamine.build;


import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.game.Race;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.CloakState;
import com.hjax.kagamine.army.ThreatManager;
import com.hjax.kagamine.economy.Base;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.economy.EconomyManager;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.knowledge.Balance;
import com.hjax.kagamine.knowledge.EnemyModel;
import com.hjax.kagamine.knowledge.Wisdom;
import com.hjax.kagamine.unitcontrollers.Drone;
import com.hjax.kagamine.unitcontrollers.zerg.Larva;

public class BuildExecutor {
	public static boolean pulled_off_gas = false;
	
	public static void on_frame() {
		if (!build_completed()) execute_build();
		else {
			if (Game.supply_cap() < 200) {
				float larva = EconomyManager.larva_rate();
				if (should_build_queens()) larva += 2;
				if (Game.supply_cap() + (GameInfoCache.in_progress(Units.ZERG_OVERLORD) * 8) - Game.supply() <= larva * 2) {
					if (Larva.has_larva()) {
						if (Game.can_afford(Units.ZERG_OVERLORD)) {
							Larva.produce_unit(Units.ZERG_OVERLORD);
						} 
						Game.purchase(Units.ZERG_OVERLORD);
					}
				}
			}
			
			if (should_build_army() && Game.army_supply() < 5) {
				if (Larva.has_larva() && Game.can_afford(next_army_unit())) {
					if (next_army_unit() != Units.INVALID) {
						Game.purchase(next_army_unit());
						Larva.produce_unit(next_army_unit());
					}
				}
			}
			
			if (count(Units.ZERG_DRONE) > 45 || count(Units.ZERG_EXTRACTOR) == 0) {
				if ((BaseManager.active_extractors() + GameInfoCache.in_progress(Units.ZERG_EXTRACTOR) < Build.ideal_gases) && ((BaseManager.active_extractors() + GameInfoCache.in_progress(Units.ZERG_EXTRACTOR)) < ((count(Units.ZERG_DRONE) - 15) / 8))) {
					if (Game.can_afford(Units.ZERG_EXTRACTOR)) {
						BaseManager.build(Units.ZERG_EXTRACTOR);
						Game.purchase(Units.ZERG_EXTRACTOR);
					}
				}
			}
			
			// TODO make this less of a hack
			if ((count(Units.ZERG_DRONE) <= 12 && !Build.pull_off_gas) || (GameInfoCache.count_friendly(Units.ZERG_SPAWNING_POOL) > 0 && !(GameInfoCache.get_units(Alliance.SELF, Units.ZERG_SPAWNING_POOL).get(0).unit().getOrders().size() == 0) && Build.pull_off_gas)) {
				pulled_off_gas = true;
				for (UnitInPool drone: GameInfoCache.get_units(Alliance.SELF, Units.ZERG_DRONE)) {
					if (!(drone.unit().getOrders().size() == 0) && 
							drone.unit().getOrders().get(0).getAbility() == Abilities.HARVEST_GATHER && 
							Game.get_unit((drone.unit().getOrders().get(0).getTargetedUnitTag()).get()).unit().getType() == Units.ZERG_EXTRACTOR) {
						Game.unit_command(drone, Abilities.STOP);
					}
				}
			}
			if ((count(Units.ZERG_DRONE) > 30 && pulled_off_gas) || !Build.pull_off_gas) {
				pulled_off_gas = false;
				Build.pull_off_gas = false;
			}
			if (Game.army_supply() >= 2 && Game.army_supply() < 30 && BaseManager.base_count(Alliance.SELF) < 3) {
				if (count(Units.ZERG_SPINE_CRAWLER) < 3 && !Wisdom.cannon_rush() && Build.scout) {
					if (GameInfoCache.count_friendly(Units.ZERG_SPAWNING_POOL) > 0 && (BaseManager.base_count(Alliance.SELF) > 1 || Wisdom.proxy_detected())) {
						if (Wisdom.all_in_detected() || Wisdom.proxy_detected() || (count(Units.ZERG_SPINE_CRAWLER) < 1 && Wisdom.aggression_detected() && EnemyModel.enemyBaseCount() < 2)) {
							if (Game.can_afford(Units.ZERG_SPINE_CRAWLER)) {
								BaseManager.build(Units.ZERG_SPINE_CRAWLER);
							}
							Game.purchase(Units.ZERG_SPINE_CRAWLER);
						}
					}
				}
			}

			if (ThreatManager.is_safe(BaseManager.get_next_base().location) ) {
				if (((!Wisdom.all_in_detected() && !Wisdom.proxy_detected()) || Game.army_supply() > 60 || Game.minerals() > 700) && GameInfoCache.in_progress(Units.ZERG_HATCHERY) == 0 && should_expand()) {
					if (!Game.can_afford(Units.ZERG_HATCHERY)) {
						if (!BaseManager.get_next_base().has_walking_drone() && Game.minerals() > 100) {
							UnitInPool drone = BaseManager.get_free_worker(BaseManager.get_next_base().location);
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

			if (count(Units.ZERG_DRONE) >= 20) {
				boolean needs_spores = Wisdom.air_detected();
				for (UnitInPool u: GameInfoCache.get_units(Alliance.ENEMY)) {
					if (u.unit().getCloakState().orElse(CloakState.NOT_CLOAKED) == CloakState.CLOAKED_DETECTED || u.unit().getFlying().orElse(false)) {
						if (u.unit().getType() != Units.ZERG_OVERLORD && 
							u.unit().getType() != Units.ZERG_OVERSEER &&
							u.unit().getType() != Units.TERRAN_MEDIVAC &&
							u.unit().getType() != Units.TERRAN_RAVEN &&
							u.unit().getType() != Units.PROTOSS_OBSERVER) {
							needs_spores = true;
						}
					}
				}
				// TODO remove this hack
				if (GameInfoCache.count_enemy(Units.PROTOSS_DARK_SHRINE) > 0) needs_spores = true;
				if (Game.get_opponent_race() == Race.PROTOSS && count(Units.ZERG_DRONE) > 30 + 10 * Math.min(EnemyModel.enemyBaseCount(), 4)) needs_spores = true;
				if (needs_spores) {
					if (!Game.can_afford(Units.ZERG_SPORE_CRAWLER) && count(Units.ZERG_SPORE_CRAWLER) < 1) return;
					BaseManager.build_defensive_spores();
				}
			}
			
			if (Game.minerals() > 50 && Game.gas() > 50 && GameInfoCache.count_friendly(Units.ZERG_LAIR) > 0 && count(Units.ZERG_OVERSEER) + count(Units.ZERG_OVERLORD_COCOON) < 2) {
				for (UnitInPool ovie: GameInfoCache.get_units(Alliance.SELF, Units.ZERG_OVERLORD)) {
					Game.spend(50, 50);
					Game.unit_command(ovie, Abilities.MORPH_OVERSEER);
					break;
				}
			}

			TechManager.on_frame();

			for (UnitType u: Build.composition) {
				if (!pulled_off_gas && ((count(Units.ZERG_DRONE) > Build.tech_drones || (Wisdom.all_in_detected() && count(Units.ZERG_DRONE) > 25)) || (u == Units.ZERG_BANELING && Game.get_opponent_race() == Race.ZERG && count(Units.ZERG_DRONE) >= 16))) {
					if (Balance.has_tech_requirement(u)) {
						if (!(count(Balance.next_tech_requirement(u)) > 0)) {
							if (Balance.next_tech_requirement(u) == Units.ZERG_INFESTATION_PIT && (BaseManager.base_count(Alliance.SELF) < 4 || count(Units.ZERG_DRONE) < 60)) continue;
							if (Balance.next_tech_requirement(u) == Units.ZERG_LAIR) {
								if (Build.two_base_tech || (BaseManager.base_count(Alliance.SELF) >= 2 && count(Units.ZERG_DRONE) > 40)) {
									if (Game.minerals() < 150 && Game.gas() > 100) return;
									if (Game.can_afford(Balance.next_tech_requirement(u))) {
										for (Base b: BaseManager.bases) {
											if (b.has_friendly_command_structure() && b.command_structure.unit().getBuildProgress() > 0.999 && b.command_structure.unit().getOrders().size() == 0) {
												Game.unit_command(b.command_structure, Abilities.MORPH_LAIR);
												Game.purchase(Units.ZERG_LAIR);
												break;
											}
										}
									}
								}
							} else if (Balance.next_tech_requirement(u) == Units.ZERG_HIVE) {
								if (Game.minerals() < 200 && Game.gas() > 150) return;
								if (Game.can_afford(Balance.next_tech_requirement(u))) {
									for (Base b: BaseManager.bases) {
										if (b.has_friendly_command_structure() && b.command_structure.unit().getBuildProgress() > 0.999 && b.command_structure.unit().getOrders().size() == 0 && b.command_structure.unit().getType() == Units.ZERG_LAIR) {
											Game.unit_command(b.command_structure, Abilities.MORPH_HIVE);
											Game.purchase(Units.ZERG_HIVE);
											break;
										}
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

			if (should_build_queens()) {
				if (Game.supply_cap() - Game.supply() >= 2) {
					if (Game.can_afford(Units.ZERG_QUEEN)) {
						for (UnitInPool u: GameInfoCache.get_units(Alliance.SELF)) {
							if (Game.is_town_hall(u.unit().getType()) && u.unit().getBuildProgress() > 0.999) {
								if (u.unit().getOrders().size() == 0) {
									Game.purchase(Units.ZERG_QUEEN);
									Game.unit_command(u, Abilities.TRAIN_QUEEN);
									break;
								}
							}
						}
					} else if (count(Units.ZERG_QUEEN) < BaseManager.base_count(Alliance.SELF)) {
						Game.purchase(Units.ZERG_QUEEN);
					}
				}
			}

			if (!ThreatManager.under_attack() || Wisdom.cannon_rush() || Wisdom.proxy_detected()) {
				if (Game.minerals() > 25 && Game.gas() > 75 && GameInfoCache.count_friendly(Units.ZERG_ROACH) > 0 && Build.composition.contains(Units.ZERG_RAVAGER)) {
					for (UnitInPool u: GameInfoCache.get_units(Alliance.SELF, Units.ZERG_ROACH)) {
						Game.unit_command(u, Abilities.MORPH_RAVAGER);
						Game.spend(25, 75);
						break;
					}
				}
				if (Game.get_opponent_race() != Race.ZERG) {
					if ((Game.minerals() > 25 && Game.gas() > 25 && (GameInfoCache.count_friendly(Units.ZERG_ZERGLING) >= 20) && GameInfoCache.count_friendly(Units.ZERG_ZERGLING) > GameInfoCache.count_friendly(Units.ZERG_BANELING) * 2 && Build.composition.contains(Units.ZERG_BANELING))) {
						for (UnitInPool u: GameInfoCache.get_units(Alliance.SELF, Units.ZERG_ZERGLING)) {
							Game.unit_command(u, Abilities.TRAIN_BANELING);
							Game.spend(25, 25);
							break;
						}
					}
				}
				else {
					if ((Game.minerals() > 25 && Game.gas() > 25 && Game.get_opponent_race() == Race.ZERG && count(Units.ZERG_BANELING) < 6 && GameInfoCache.count_friendly(Units.ZERG_ZERGLING) > 0 && Build.composition.contains(Units.ZERG_BANELING))) {
						for (UnitInPool u: GameInfoCache.get_units(Alliance.SELF, Units.ZERG_ZERGLING)) {
							Game.unit_command(u, Abilities.TRAIN_BANELING);
							Game.spend(25, 25);
							break;
						}
					}
				}
			}
			
			if (count(Units.ZERG_EXTRACTOR) >= 3) {
				if (count(Units.ZERG_EVOLUTION_CHAMBER) < 2 && count(Units.ZERG_DRONE) > 40 && BaseManager.base_count(Alliance.SELF) > 3) {
					if (Game.can_afford(Units.ZERG_EVOLUTION_CHAMBER)) {
						BaseManager.build(Units.ZERG_EVOLUTION_CHAMBER);
					}
					Game.purchase(Units.ZERG_EVOLUTION_CHAMBER);
				}
			}
			
			if (should_build_army() || !should_build_drones()) {
				if (Larva.has_larva() && Game.can_afford(next_army_unit())) {
					if (next_army_unit() != Units.INVALID) {
						Game.purchase(next_army_unit());
						Larva.produce_unit(next_army_unit());
					}
				}
			}
			else if (should_build_drones()) {
				if (Game.can_afford(Units.ZERG_DRONE) && Larva.has_larva()) {
					Game.purchase(Units.ZERG_DRONE);
					Larva.produce_unit(Units.ZERG_DRONE);
				}
			}

			if ((count(Units.ZERG_DRONE) >= worker_cap() || count(Units.ZERG_HATCHERY) >= Build.ideal_hatches && Build.ideal_hatches > 0) && !should_build_drones() && BaseManager.active_extractors() + GameInfoCache.in_progress(Units.ZERG_EXTRACTOR) < Build.ideal_gases) {
				if (Game.can_afford(Units.ZERG_EXTRACTOR)) {
					BaseManager.build(Units.ZERG_EXTRACTOR);
					Game.purchase(Units.ZERG_EXTRACTOR);
				}
			}
		}
	}
		

	public static void execute_build() {
		if (Build.build.get(Build.build_index).getKey() <= Game.supply()) {
			if (Build.build.get(Build.build_index).getValue() == Units.ZERG_HATCHERY && !(BaseManager.get_next_base().has_walking_drone()) && Game.minerals() > 150) {
				for (UnitInPool u: GameInfoCache.get_units(Alliance.SELF, Units.ZERG_DRONE)) {
					if (Drone.can_build(u)) {
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
	
	public static boolean build_completed() {
		return Build.build_index >= Build.build.size();
	}
	
	public static int count(UnitType u) {
		return GameInfoCache.count_friendly(u) + GameInfoCache.in_progress(u);
	}
	
	public static UnitType next_army_unit() {
		if (Game.get_opponent_race() == Race.ZERG) {
			if (count(Units.ZERG_BANELING) == 0 && GameInfoCache.count_friendly(Units.ZERG_BANELING_NEST) > 0) return Units.ZERG_ZERGLING;
		}
		UnitType best = Units.INVALID;
		for (UnitType u: Build.composition) {
			if (u == Units.ZERG_BANELING) continue;
			if (u == Units.ZERG_RAVAGER) continue;
			if (u == Units.ZERG_CORRUPTOR && count(Units.ZERG_CORRUPTOR) >= 10) continue;
			if (u == Units.ZERG_CORRUPTOR && count(Units.ZERG_CORRUPTOR) < 10 && Game.army_supply() > 30 && BaseManager.active_extractors() >= 4 && GameInfoCache.count_friendly(Units.ZERG_SPIRE) > 0) return Units.ZERG_CORRUPTOR;
			if (u == Units.ZERG_MUTALISK && count(Units.ZERG_MUTALISK) >= 15 && Game.get_opponent_race() == Race.TERRAN) continue;
			if (u == Units.ZERG_HYDRALISK && count(Units.ZERG_HYDRALISK) >= 15 && GameInfoCache.count_enemy(Units.TERRAN_BATTLECRUISER) == 0 && Game.get_opponent_race() == Race.TERRAN) continue;
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
	
	public static boolean should_build_queens() {
		if (Wisdom.worker_rush()) return false;
		if (GameInfoCache.count_friendly(Units.ZERG_SPAWNING_POOL) == 0) return false;
		
		if (Build.composition.contains(Units.ZERG_QUEEN) && count(Units.ZERG_QUEEN) < 25) return true;
		
		if (ThreatManager.under_attack() && count(Units.ZERG_LARVA) > 0) return false; 
		if (count(Units.ZERG_DRONE) < 35 && count(Units.ZERG_LARVA) > 0) return false; 
		
		int queen_target = 0;
		if (Build.max_queens == -1) {
			if (count(Units.ZERG_HATCHERY) < 3) {
				queen_target = BaseManager.base_count(Alliance.SELF) - 1;
			} else {
				queen_target = Math.min(BaseManager.base_count(Alliance.SELF) + 4, 12);
			}
			if (Game.minerals() > 400) {
				queen_target += 1;
			}
		} else {
			queen_target = Build.max_queens;
		}
		if (Game.supply() > 120) queen_target = Math.min(BaseManager.base_count(Alliance.SELF), 6);
		if (Wisdom.proxy_detected()) {
			queen_target = 1;
			if (Game.minerals() > 200) {
				queen_target += 1;
			}
		}
		if (count(Units.ZERG_QUEEN) < queen_target && GameInfoCache.count_friendly(Units.ZERG_SPAWNING_POOL) > 0) {
			return true;
		}
		return false;
	}
	
	public static boolean should_build_army() {
		if (Game.get_opponent_race() == Race.ZERG) {
			if (count(Units.ZERG_BANELING) == 0 && GameInfoCache.count_friendly(Units.ZERG_BANELING_NEST) > 0) return true;
		}
		if (Wisdom.ahead()) return true;
		int target = 2 + 2 * count(Units.ZERG_QUEEN);
		if (Game.get_opponent_race() == Race.ZERG) target = 15;
		if (Wisdom.all_in_detected()) target = 10;
		if (Wisdom.proxy_detected()) target = 10;
		if (Game.army_supply() < target || (ThreatManager.under_attack() && Game.army_supply() < 4 * ThreatManager.seen.size())) {
			if (next_army_unit() != Units.INVALID) {
				return true;
			}
		}
		
		if (Game.army_supply() * Math.max(EnemyModel.enemyBaseCount(), 1) < count(Units.ZERG_DRONE) && count(Units.ZERG_DRONE) > (15 + 20 * Math.max(EnemyModel.enemyBaseCount(), 1))) {
			return true;
		}
		
		if (Wisdom.all_in_detected() && Game.army_supply() < count(Units.ZERG_DRONE) && count(Units.ZERG_DRONE) > 30) return true;
		if (count(Units.ZERG_DRONE) > 50 && Game.gas() > 100 && next_army_unit() == Units.ZERG_MUTALISK && count(Units.ZERG_MUTALISK) < 10) return true;
		return count(Units.ZERG_DRONE) >= worker_cap();
	}
	
	public static boolean should_build_drones() {
		if (EconomyManager.free_minerals() == 0) return false;
		return (count(Units.ZERG_DRONE) < worker_cap());
	}
	
	public static int worker_cap() {
		int drone_target = 100;
		return Math.min(drone_target, Build.ideal_workers);
	}
	
	public static boolean should_expand() {
		if (count(Units.ZERG_HATCHERY) < 3 && count(Units.ZERG_DRONE) > 23) return true;
		return EconomyManager.free_minerals() <= 4 && ((BaseManager.base_count(Alliance.SELF) < Build.ideal_hatches) || (Build.ideal_hatches == -1));
	}
	
	
}

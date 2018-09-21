package com.hjax.kagamine;

import java.util.Arrays;
import java.util.HashSet;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.game.Race;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.UnitControllers.Drone;
import com.hjax.kagamine.UnitControllers.Larva;

public class BuildPlanner {
	public static boolean is_all_in = false;
	public static boolean pulled_off_gas = false;
	
	public static void on_frame() {
		if (build_completed()) execute_build();
		else {
			if (!is_all_in && count(Units.ZERG_DRONE) < 30 && Wisdom.cannon_rush()) do_ravager_all_in();
			if (is_all_in && Game.supply() > 70 && Game.get_opponent_race() == Race.TERRAN) hunter_killer();
			if (Game.get_opponent_race() == Race.PROTOSS) {
				if (GameInfoCache.count_enemy(Units.PROTOSS_CARRIER) > 0 ||
					GameInfoCache.count_enemy(Units.PROTOSS_VOIDRAY) > 0 || 
					GameInfoCache.count_enemy(Units.PROTOSS_TEMPEST) > 0 ||
					GameInfoCache.count_enemy(Units.PROTOSS_MOTHERSHIP) > 0 ||
					GameInfoCache.count_enemy(Units.PROTOSS_STARGATE) > 0) {
					if (Build.composition.contains(Units.ZERG_ROACH)) {
						Build.composition = new HashSet<>(Arrays.asList(Units.ZERG_HYDRALISK, Units.ZERG_ZERGLING));
						Build.ideal_workers = 70;
						Build.max_queens = 4;
					}
				}
			}
			if (Game.supply_cap() < 200) {
				float larva = BaseManager.larva_rate();
				if (should_build_queens()) larva += 2;
				if (Game.supply_cap() + (GameInfoCache.production.get(Units.ZERG_OVERLORD) * 8) - Game.supply() <= larva * 1.5) {
					if (Game.can_afford(Units.ZERG_OVERLORD)) {
						if (Larva.has_larva()) {
							Game.purchase(Units.ZERG_OVERLORD);
							Larva.produce_unit(Units.ZERG_OVERLORD);
						}
					} else return;
				}
			}
			
			// TODO make this less of a hack
			if (GameInfoCache.count_friendly(Units.ZERG_SPAWNING_POOL) > 0 && !(GameInfoCache.get_units(Alliance.SELF, Units.ZERG_SPAWNING_POOL).get(0).unit().getOrders().size() == 0) && Build.pull_off_gas) {
				pulled_off_gas = true;
				for (UnitInPool drone: GameInfoCache.get_units(Alliance.SELF, Units.ZERG_DRONE)) {
					if (!(drone.unit().getOrders().size() == 0) && 
							drone.unit().getOrders().get(0).getAbility() == Abilities.HARVEST_GATHER && 
							Game.get_unit((drone.unit().getOrders().get(0).getTargetedUnitTag()).get()).unit().getType() == Units.ZERG_EXTRACTOR) {
						Game.unit_command(drone, Abilities.STOP);
					}
				}
			}
			if (count(Units.ZERG_DRONE) > 40 && pulled_off_gas) {
				pulled_off_gas = false;
				Build.pull_off_gas = false;
			}
			if (Game.army_supply() < 30 && BaseManager.base_count() < 3) {
				if (count(Units.ZERG_SPINE_CRAWLER) < 3 && !Wisdom.cannon_rush() && Build.scout) {
					if (GameInfoCache.count_friendly(Units.ZERG_SPAWNING_POOL) > 0 && (BaseManager.base_count() > 1 || Wisdom.proxy_detected())) {
						if (Wisdom.all_in_detected() || Wisdom.proxy_detected()) {
							if (!Game.can_afford(Units.ZERG_SPINE_CRAWLER)) return;
							Game.purchase(Units.ZERG_SPINE_CRAWLER);
							BaseManager.build(Units.ZERG_SPINE_CRAWLER);
						}
					}
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
		return GameInfoCache.count_friendly(u) + GameInfoCache.production.get(u);
	}
	
	public static UnitType next_army_unit() {
		UnitType best = Units.INVALID;
		for (UnitType u: Build.composition) {
			if (u == Units.ZERG_BANELING) continue;
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
	
	public static boolean should_build_army() {
		int target = 2 + 2 * count(Units.ZERG_QUEEN);
		if (Game.get_opponent_race() == Race.ZERG) target = 10;
		if (Wisdom.all_in_detected()) target = 10;
		if (Wisdom.proxy_detected()) target = 20;
		if (Game.army_supply() < target || (ThreatManager.under_attack() || Game.army_supply() < 4 * ThreatManager.seen.size())) {
			if (next_army_unit() != Units.INVALID) {
				return true;
			}
		}
		if (Wisdom.play_safe() && Game.army_supply() * 1.5 < count(Units.ZERG_DRONE) && count(Units.ZERG_DRONE) > 43) return true;
		if (Game.army_supply() * 2 < count(Units.ZERG_DRONE) && count(Units.ZERG_DRONE) > 45) return true;
		if (Game.get_opponent_race() == Race.PROTOSS && count(Units.ZERG_DRONE) > 45 && Game.army_supply() < 45) return true;
		if (Wisdom.all_in_detected() && Game.army_supply() < count(Units.ZERG_DRONE) && count(Units.ZERG_DRONE) > 30) return true;
		if (count(Units.ZERG_DRONE) > 50 && Game.gas() > 100 && next_army_unit() == Units.ZERG_MUTALISK && count(Units.ZERG_MUTALISK) < 10) return true;
		return count(Units.ZERG_DRONE) >= worker_cap();
	}
	
	public static int worker_cap() {
		int drone_target = 70;
		return Math.min(drone_target, Build.ideal_workers);
	}
}

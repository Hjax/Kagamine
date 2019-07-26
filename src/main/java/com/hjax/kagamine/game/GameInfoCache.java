	package com.hjax.kagamine.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Ability;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.UnitTypeData;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.data.Upgrade;
import com.github.ocraft.s2client.protocol.debug.Color;
import com.github.ocraft.s2client.protocol.game.PlayerInfo;
import com.github.ocraft.s2client.protocol.game.Race;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.DisplayType;
import com.github.ocraft.s2client.protocol.unit.Tag;
import com.github.ocraft.s2client.protocol.unit.UnitOrder;
import com.hjax.kagamine.Constants;
import com.hjax.kagamine.build.Composition;

public class GameInfoCache {
	
	public static Map<Ability, Integer> production = new HashMap<>();
	public static Map<Tag, HjaxUnit> all_units = new HashMap<>();
	
	public static Map<Tag, HjaxUnit> visible_friendly = new HashMap<>();
	public static Map<Tag, HjaxUnit> visible_enemy = new HashMap<>();
	public static Map<Tag, HjaxUnit> visible_neutral = new HashMap<>();
	
	public static Map<UnitType, List<HjaxUnit>> visible_friendly_types = new HashMap<>();
	public static Map<UnitType, List<HjaxUnit>> visible_enemy_types = new HashMap<>();
	public static Map<UnitType, List<HjaxUnit>> visible_neutral_types = new HashMap<>();
	
	public static Map<UnitType, Integer> double_counts = new HashMap<>();
	
	static Set<Tag> claimed_gases = new HashSet<>();
	static Set<Tag> morphing_drones = new HashSet<>();
	
	public static void start_frame() {
		
		production.clear();
		double_counts.clear();
		
		morphing_drones.clear();
		
		Set<Tag> to_remove = new HashSet<>();
		for (Tag t : visible_enemy.keySet()) {
			Game.draw_box(visible_enemy.get(t).location(), Color.RED);
			if (Game.get_frame() - visible_enemy.get(t).last_seen() > Constants.MEMORY || !visible_enemy.get(t).alive() || (Game.is_visible(visible_enemy.get(t).location()) && visible_enemy.get(t).last_seen() != Game.get_frame())) {
				to_remove.add(t);
			}
		}
		for (Tag t : to_remove) visible_enemy.remove(t);
		
		visible_friendly.clear();
		visible_neutral.clear();
		
		visible_friendly_types.clear();
		visible_enemy_types.clear();
		visible_neutral_types.clear();
		
		claimed_gases.clear(); 
		
		for (UnitInPool unit: Game.get_units()) {
			
			if (unit.unit().getDisplayType() != DisplayType.VISIBLE) continue;
			
			HjaxUnit current = HjaxUnit.getInstance(unit);
			
			all_units.put(current.tag(), current);

			if (current.alive()) {
				if (current.alliance() == Alliance.SELF) {
					visible_friendly.put(current.tag(), current);
					if (!current.done()) {
						
						production.put(Game.production_ability(current.type()), 
									   production.getOrDefault(Game.production_ability(current.type()), 0) + 1);
						double_counts.put(current.type(), double_counts.getOrDefault(current.type(), 0) + 1);
						
					} 
					if (visible_friendly_types.containsKey(current.type())) {
						visible_friendly_types.get(current.type()).add(current);
					} else {
						visible_friendly_types.put(current.type(), new ArrayList<>(List.of(current)));
					}
					
					for (UnitOrder o: current.orders()) {
						production.put(o.getAbility(), production.getOrDefault(o.getAbility(), 0) + 1);
						if (Game.is_worker(current.type())) {
							if (o.getAbility() == Abilities.BUILD_EXTRACTOR || o.getAbility() == Abilities.BUILD_REFINERY || o.getAbility() == Abilities.BUILD_ASSIMILATOR) {
								claimed_gases.add(o.getTargetedUnitTag().get());
							}
							if (o.getAbility() != Abilities.HARVEST_GATHER && o.getAbility() != Abilities.HARVEST_RETURN) {
								for (UnitTypeData t: Game.get_unit_type_data().values()) {
									if (o.getAbility() == t.getAbility().orElse(Abilities.INVALID)) {
										morphing_drones.add(current.tag());
										break;
									}
								}
								break;
							}
						}
					}
				} else if (current.alliance() == Alliance.ENEMY) {
					visible_enemy.put(current.tag(), current);
					if (visible_enemy_types.containsKey(current.type())) {
						visible_enemy_types.get(current.type()).add(current);
					} else {
						visible_enemy_types.put(current.type(), new ArrayList<>(List.of(current)));
					}
				} else {
					visible_neutral.put(current.tag(), current);
					if (visible_neutral_types.containsKey(current.type())) {
						visible_neutral_types.get(current.type()).add(current);
					} else {
						visible_neutral_types.put(current.type(), new ArrayList<>(List.of(current)));
					}
				}
			}

		}
	}
	static void on_frame() {}
	public static void end_frame() {}
	
	public static int count_friendly(UnitType type) {
		if (Game.is_worker(type)) return Game.worker_count();
		if (Game.is_structure(type)) {
			return visible_friendly_types.getOrDefault(type, new ArrayList<>()).size() - double_counts.getOrDefault(type, 0);
		}
		return visible_friendly_types.getOrDefault(type, new ArrayList<>()).size();
	}
	
	public static int count_enemy(UnitType type) {
		return visible_enemy_types.getOrDefault(type, new ArrayList<>()).size();
	}
	
	public static int count(UnitType u) {
		return count_friendly(u) + in_progress(u);
	}
	
	// TODO filter out old units
	public static ArrayList<HjaxUnit> get_units() {
		ArrayList<HjaxUnit> units = new ArrayList<>();
		for (HjaxUnit u: all_units.values()) {
			units.add(u);
		}
		return units;
	}
	
	public static ArrayList<HjaxUnit> get_units(UnitType type) {
		ArrayList<HjaxUnit> units = new ArrayList<>();
		for (HjaxUnit u: all_units.values()) {
			if (u.type() == type) {
				units.add(u);
			}
		}
		return units;
	}
	
	public static HjaxUnit get_unit(Tag t) {
		return all_units.getOrDefault(t, null);
	}
	
	public static ArrayList<HjaxUnit> get_units(Alliance team) {
		if (team == Alliance.SELF) return new ArrayList<>(visible_friendly.values());
		if (team == Alliance.NEUTRAL) return new ArrayList<>(visible_neutral.values());
		return new ArrayList<>(visible_enemy.values());
	}
	
	public static ArrayList<HjaxUnit> get_units(Alliance team, UnitType type) {
		if (team == Alliance.SELF) {
			return (ArrayList<HjaxUnit>) visible_friendly_types.getOrDefault(type, new ArrayList<>());
		} else if (team == Alliance.ENEMY){
			return (ArrayList<HjaxUnit>) visible_enemy_types.getOrDefault(type, new ArrayList<>());
		} else {
			return (ArrayList<HjaxUnit>) visible_neutral_types.getOrDefault(type, new ArrayList<>());
		}
	}
	
	public static boolean geyser_is_free(HjaxUnit unit) {
		for (HjaxUnit e : get_units(Alliance.SELF, RaceInterface.get_race_gas())) {
			if (e.distance(unit) < 1) {
				return false;
			}
		}
		return !claimed_gases.contains(unit.tag());
	}
	
	public static int in_progress(UnitType t) {
		return production.getOrDefault(Game.get_unit_type_data().get(t).getAbility().orElse(Abilities.INVALID), 0);
	}
	
	public static long last_seen(Tag t) {
		return all_units.get(t).last_seen();
		
	}
	
	public static boolean is_researching(Upgrade u) {
		if (Game.get_ability_data().get(Game.get_upgrade_data().get(u).getAbility().get()).getRemapsToAbility().isPresent()) {
			if (production.getOrDefault(Game.get_ability_data().get(Game.get_upgrade_data().get(u).getAbility().get()).getRemapsToAbility().get(), 0) > 0) {
				return true;
			}
		}
		return production.getOrDefault(Game.get_upgrade_data().get(u).getAbility().get(), 0) > 0;
	}
	
	public static Race get_opponent_race() {
		if (count_enemy(Units.PROTOSS_PROBE) > 0) return Race.PROTOSS;
		if (count_enemy(Units.PROTOSS_PYLON) > 0) return Race.PROTOSS;
		if (count_enemy(Units.PROTOSS_GATEWAY) > 0) return Race.PROTOSS;
		if (count_enemy(Units.PROTOSS_PHOTON_CANNON) > 0) return Race.PROTOSS;
		
		if (count_enemy(Units.TERRAN_SCV) > 0) return Race.TERRAN;
		if (count_enemy(Units.TERRAN_SUPPLY_DEPOT) > 0) return Race.TERRAN;
		if (count_enemy(Units.TERRAN_BARRACKS) > 0) return Race.TERRAN;
		if (count_enemy(Units.TERRAN_MARINE) > 0) return Race.TERRAN;
		for (PlayerInfo player: Game.get_game_info().getPlayersInfo()) {
			if (player.getPlayerId() != Game.get_player_id()) {
				if (player.getRequestedRace() != Race.RANDOM) {
					return player.getRequestedRace();
				}
			}
		}
		return Race.ZERG;
	}
	
	private static long aas_frame = -1;
	private static float aas_value = 0;
	public static float attacking_army_supply() {
		if (Game.get_frame() == aas_frame) {
			return aas_value;
		}
		float result = 0;
		boolean queens_count = Composition.comp().contains(Units.ZERG_QUEEN);
		for (HjaxUnit unit: get_units(Alliance.SELF)) {
			if (unit.done() && Game.is_combat(unit.type()) && !(!queens_count && unit.type() == Units.ZERG_QUEEN)) {
				result += Game.supply(unit.type());
			}
		}
		aas_frame = Game.get_frame();
		aas_value = result;
		return result;
	}
	
}

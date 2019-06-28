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
import com.github.ocraft.s2client.protocol.game.PlayerInfo;
import com.github.ocraft.s2client.protocol.game.Race;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Tag;
import com.github.ocraft.s2client.protocol.unit.UnitOrder;
import com.hjax.kagamine.Constants;
import com.hjax.kagamine.build.Composition;

public class GameInfoCache {
	
	public static Map<Ability, Integer> production = new HashMap<>();
	public static Map<Tag, UnitInPool> all_units = new HashMap<>();
	
	public static Map<Tag, UnitInPool> visible_friendly = new HashMap<>();
	public static Map<Tag, UnitInPool> visible_enemy = new HashMap<>();
	public static Map<Tag, UnitInPool> visible_neutral = new HashMap<>();
	
	public static Map<Tag, Integer> last_seen_frame = new HashMap<>();
	
	public static Map<UnitType, List<UnitInPool>> visible_friendly_types = new HashMap<>();
	public static Map<UnitType, List<UnitInPool>> visible_enemy_types = new HashMap<>();
	public static Map<UnitType, List<UnitInPool>> visible_neutral_types = new HashMap<>();
	
	
	static Set<Tag> claimed_gases = new HashSet<>();
	static Set<Tag> morphing_drones = new HashSet<>();
	
	public static void start_frame() {
		
		production.clear();
		
		morphing_drones.clear();
		
		visible_enemy.clear();
		visible_friendly.clear();
		visible_neutral.clear();
		
		visible_friendly_types.clear();
		visible_enemy_types.clear();
		visible_neutral_types.clear();
		
		claimed_gases.clear(); 
		
		for (UnitInPool u: Game.get_units()) {
			
			last_seen_frame.put(u.getTag(), (int) Game.get_frame());
			
			all_units.put(u.getTag(), u);
	
			
			if (u.isAlive()) {
				if (u.unit().getAlliance() == Alliance.SELF) {
					visible_friendly.put(u.getTag(), u);
					if (visible_friendly_types.containsKey(u.unit().getType())) {
						visible_friendly_types.get(u.unit().getType()).add(u);
					} else {
						visible_friendly_types.put(u.unit().getType(), new ArrayList<>(List.of(u)));
					}
					
					for (UnitOrder o: u.unit().getOrders()) {
						production.put(o.getAbility(), production.getOrDefault(o.getAbility(), 0) + 1);
						if (Game.is_worker(u.unit().getType())) {
							if (o.getAbility() == Abilities.BUILD_EXTRACTOR || o.getAbility() == Abilities.BUILD_REFINERY || o.getAbility() == Abilities.BUILD_ASSIMILATOR) {
								claimed_gases.add(o.getTargetedUnitTag().get());
							}
							if (o.getAbility() != Abilities.HARVEST_GATHER && o.getAbility() != Abilities.HARVEST_RETURN) {
								for (UnitTypeData t: Game.get_unit_type_data().values()) {
									if (o.getAbility() == t.getAbility().orElse(Abilities.INVALID)) {
										morphing_drones.add(u.getTag());
										break;
									}
								}
								break;
							}
						}
					}
					if (u.unit().getBuildProgress() < Constants.DONE) {
						production.put(Game.get_unit_type_data().get(u.unit().getType()).getAbility().orElse(Abilities.INVALID), 
						production.getOrDefault(Game.get_unit_type_data().get(u.unit().getType()).getAbility().orElse(Abilities.INVALID), 0) + 1);
					}
				} else if (u.unit().getAlliance() == Alliance.ENEMY) {
					visible_enemy.put(u.getTag(), u);
					if (visible_enemy_types.containsKey(u.unit().getType())) {
						visible_enemy_types.get(u.unit().getType()).add(u);
					} else {
						visible_enemy_types.put(u.unit().getType(), new ArrayList<>(List.of(u)));
					}
				} else {
					visible_neutral.put(u.getTag(), u);
					if (visible_neutral_types.containsKey(u.unit().getType())) {
						visible_neutral_types.get(u.unit().getType()).add(u);
					} else {
						visible_neutral_types.put(u.unit().getType(), new ArrayList<>(List.of(u)));
					}
				}
			}

		}
	}
	static void on_frame() {}
	public static void end_frame() {}
	
	public static int count_friendly(UnitType type) {
		if (Game.is_worker(type)) return Game.worker_count();
		int result = visible_friendly_types.getOrDefault(type, new ArrayList<>()).size();
		if (Game.is_structure(type)) result -= in_progress(type);
		return result;
	}
	
	public static int count_enemy(UnitType type) {
		return visible_enemy_types.getOrDefault(type, new ArrayList<>()).size();
	}
	
	public static int count(UnitType u) {
		return count_friendly(u) + in_progress(u);
	}
	
	public static ArrayList<UnitInPool> get_units(UnitType type) {
		ArrayList<UnitInPool> units = new ArrayList<>();
		for (UnitInPool u: Game.get_units()) {
			if (u.unit().getType() == type) units.add(u);
		}
		return units;
	}
	
	public static ArrayList<UnitInPool> get_units(Alliance team) {
		if (team == Alliance.SELF) return new ArrayList<>(visible_friendly.values());
		if (team == Alliance.NEUTRAL) return new ArrayList<>(visible_neutral.values());
		return new ArrayList<>(visible_enemy.values());
	}
	
	public static ArrayList<UnitInPool> get_units(Alliance team, UnitType type) {
		if (team == Alliance.SELF) {
			return (ArrayList<UnitInPool>) visible_friendly_types.getOrDefault(type, new ArrayList<>());
		} else if (team == Alliance.ENEMY){
			return (ArrayList<UnitInPool>) visible_enemy_types.getOrDefault(type, new ArrayList<>());
		} else {
			return (ArrayList<UnitInPool>) visible_neutral_types.getOrDefault(type, new ArrayList<>());
		}
	}
	
	public static boolean geyser_is_free(UnitInPool u) {
		for (UnitInPool e : get_units(Alliance.SELF, Units.ZERG_EXTRACTOR)) {
			if (e.unit().getPosition().toPoint2d().distance(u.unit().getPosition().toPoint2d()) < 1) {
				return false;
			}
		}
		return !claimed_gases.contains(u.getTag());
	}
	
	public static int in_progress(UnitType t) {
		return production.getOrDefault(Game.get_unit_type_data().get(t).getAbility().orElse(Abilities.INVALID), 0);
	}
	
	public static int last_seen(Tag t) {
		return last_seen_frame.get(t);
		
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
		for (UnitInPool u: get_units(Alliance.SELF)) {
			if (u.unit().getBuildProgress() > 0.99 && Game.is_combat(u.unit().getType()) && !(!queens_count && u.unit().getType() == Units.ZERG_QUEEN)) {
				result += Game.get_unit_type_data().get(u.unit().getType()).getFoodRequired().orElse(0f);
			}
		}
		aas_frame = Game.get_frame();
		aas_value = result;
		return result;
	}
	
}

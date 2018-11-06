package com.hjax.kagamine;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.UnitControllers.Drone;

import java.util.ArrayList;

public class Scouting {
	public static UnitInPool scout = null;
	public static UnitInPool patrol_scout = null;
	public static ArrayList<Point2d> spawns = new ArrayList<>();
	public static int patrol_base = 2;
	public static int overlord_base = 5;
	
	public static boolean scared = false;
	public static boolean has_pulled_back = false;
	
	public static void start_frame() {
		
	}
	public static void on_frame() {
		if (spawns.size() == 0) {
			Game.get_game_info().getStartRaw().ifPresent(StartRaw -> spawns = new ArrayList<>(StartRaw.getStartLocations()));
		}
		if (spawns.size() > 1) {
			for (UnitInPool u: GameInfoCache.get_units(Alliance.ENEMY)) {
				if (Game.is_structure(u.unit().getType())) {
					spawns = new ArrayList<>();
					spawns.add(closest_enemy_spawn(u.unit().getPosition().toPoint2d()));
					break;
				}
			}
		}
		if (spawns.size() > 1) {
			outer: for (UnitInPool u: GameInfoCache.get_units(Alliance.SELF)) {
				for (Point2d s: spawns) {
					if (s.distance(u.unit().getPosition().toPoint2d()) < 8) {
						spawns.remove(s);
						break outer;
					}
				}
			}
		}
		if (scout == null && GameInfoCache.count_friendly(Units.ZERG_DRONE) > 16 && Build.scout) {
			assign_scout();
		}
		if (scout == null && GameInfoCache.count_friendly(Units.ZERG_DRONE) > 12 && spawns.size() >= 3 && Build.scout) {
			assign_scout();
		}
		if (Wisdom.confused() && Game.army_supply() < 20 && patrol_base < 7) {
			if (patrol_scout == null) {
				assign_patrol_scout();
			}
		} else {
			if (patrol_scout != null) {
				if (patrol_scout.isAlive()) {
					Game.unit_command(patrol_scout, Abilities.STOP);
				}
				patrol_scout = null;
			}
		}
		
		if (scout != null && scout.isAlive()) {
			if (scout.unit().getOrders().size() == 0 || scout.unit().getOrders().get(0).getAbility() != Abilities.MOVE) {
				Game.unit_command(scout, Abilities.MOVE, BaseManager.get_placement_location(Units.PROTOSS_PYLON, closest_enemy_spawn(scout.unit().getPosition().toPoint2d()), 5, 15));
			}
		}
		
		if (patrol_scout != null && patrol_scout.isAlive()) {
			Point2d target = BaseManager.get_base(patrol_base);
			if (target.distance(patrol_scout.unit().getPosition().toPoint2d()) < 4) {
				patrol_base++;
			} else if (patrol_scout.unit().getOrders().size() == 0 || patrol_scout.unit().getOrders().get(0).getAbility() != Abilities.MOVE) {
				Game.unit_command(patrol_scout, Abilities.MOVE, target);
			}
		}

		if (!has_pulled_back) {
			if (Wisdom.proxy_detected() || Wisdom.all_in_detected() || Wisdom.air_detected()) {
				has_pulled_back = true;
				for (UnitInPool overlord: GameInfoCache.get_units(Alliance.SELF, Units.ZERG_OVERLORD)) {
					Game.unit_command(overlord, Abilities.MOVE, BaseManager.main_base().location);
				}
			}
		}
		
	}
	public static void end_frame() {
		
	}
	
	public static Point2d closest_enemy_spawn(Point2d s) {
		Point2d best = null;
		for (Point2d p: spawns) {
			if (best == null || s.distance(p) < s.distance(best)) {
				best = p;
			}
		} 
		return best;
	}
	
	private static Point2d mclosest_enemy_spawn = null;
	private static int mclosest_enemy_spawn_frame = -1;
	public static Point2d closest_enemy_spawn() {
		if (Game.get_frame() != mclosest_enemy_spawn_frame) {
			mclosest_enemy_spawn_frame = (int) Game.get_frame();
			if (BaseManager.main_base() != null) {
				 mclosest_enemy_spawn = closest_enemy_spawn(BaseManager.main_base().location);
			}
			 mclosest_enemy_spawn = closest_enemy_spawn(Point2d.of(0, 0));
		}
		return  mclosest_enemy_spawn;
	}
	
	public static void assign_scout() {
		for (UnitInPool unit: GameInfoCache.get_units(Alliance.SELF, Units.ZERG_DRONE)) {
			if (Drone.can_build(unit)) {
				scout = unit;
				return;
			}
		}
	}
	
	public static void assign_patrol_scout() {
		for (UnitInPool unit: GameInfoCache.get_units(Alliance.SELF, Units.ZERG_DRONE)) {
			if (Drone.can_build(unit)) {
				patrol_scout = unit;
				return;
			}
		}
	}
	
	public static boolean is_scout(UnitInPool a) {
		return (scout != null && a.getTag().equals(scout.getTag())) || (patrol_scout != null && a.getTag().equals(patrol_scout.getTag()));
	}
	
	
}

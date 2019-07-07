package com.hjax.kagamine.knowledge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.data.Upgrades;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Tag;
import com.hjax.kagamine.Constants;
import com.hjax.kagamine.build.Build;
import com.hjax.kagamine.economy.Base;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.game.HjaxUnit;
import com.hjax.kagamine.game.RaceInterface;
import com.hjax.kagamine.unitcontrollers.Worker;

public class Scouting {
	public static HjaxUnit scout = null;
	public static HjaxUnit patrol_scout = null;
	public static ArrayList<Point2d> spawns = new ArrayList<>();
	public static int patrol_base = 2;
	public static int overlord_count = 0;
	
	public static boolean scared = false;
	public static boolean has_pulled_back = false;
	
	public static Map<Tag, Base> overlords = new HashMap<>();
	
	public static void start_frame() {
		
	}
	public static void on_frame() {
		if (spawns.size() == 0) {
			Game.get_game_info().getStartRaw().ifPresent(StartRaw -> spawns = new ArrayList<>(StartRaw.getStartLocations()));
		}
		if (spawns.size() > 1) {
			for (HjaxUnit unit: GameInfoCache.get_units(Alliance.ENEMY)) {
				if (unit.is_structure()) {
					Point2d spawn = closest_enemy_spawn(unit.location());
					spawns = new ArrayList<>();
					spawns.add(spawn);
					break;
				}
			}
		}
		if (spawns.size() > 1) {
			outer: for (HjaxUnit unit: GameInfoCache.get_units(Alliance.SELF)) {
				for (Point2d spawn: spawns) {
					if (unit.distance(spawn) < 8) {
						spawns.remove(spawn);
						break outer;
					}
				}
			}
		}
		if (scout == null && GameInfoCache.count_friendly(RaceInterface.get_race_worker()) > 16 && Build.scout) {
			assign_scout();
		}
		if (scout == null && GameInfoCache.count_friendly(RaceInterface.get_race_worker()) > 12 && spawns.size() >= 3 && Build.scout) {
			assign_scout();
		}
		if (Wisdom.confused() && Game.army_supply() < 20 && patrol_base < 7) {
			if (patrol_scout == null) {
				assign_patrol_scout();
			}
		} else {
			if (patrol_scout != null) {
				if (patrol_scout.alive()) {
					patrol_scout.stop();
				}
				patrol_scout = null;
			}
		}
		
		if (scout != null && scout.alive()) {
			if (scout.idle() || scout.orders().get(0).getAbility() != Abilities.MOVE) {
				scout.move(BaseManager.get_placement_location(Units.PROTOSS_PYLON, closest_enemy_spawn(scout.location()), 5, 15));
			}
		}
		
		if (patrol_scout != null && patrol_scout.alive()) {
			Point2d target = BaseManager.get_base(patrol_base);
			if (patrol_scout.distance(target) < 4) {
				patrol_base++;
			} else if (patrol_scout.idle() || patrol_scout.orders().get(0).getAbility() != Abilities.MOVE) {
				patrol_scout.move(target);
			}
		}

		if (!has_pulled_back && !Game.has_upgrade(Upgrades.OVERLORD_SPEED)) {
			if (Wisdom.proxy_detected() || Wisdom.all_in_detected() || Wisdom.air_detected()) {
				has_pulled_back = true;
				for (HjaxUnit overlord: GameInfoCache.get_units(Alliance.SELF, Units.ZERG_OVERLORD)) {
					overlord.move(BaseManager.main_base().location);
				}
			}
		}
		
		for (Tag t: overlords.keySet()) {
			if (!GameInfoCache.visible_friendly.containsKey(t)) {
				overlords.remove(t);
				break;
			}
			if (GameInfoCache.visible_friendly.get(t).distance(overlords.get(t).location) < 4) {
				overlords.remove(t);
				break;
			}
		}
		
		if (Game.has_upgrade(Upgrades.OVERLORD_SPEED)) {
			Base best_base = null;
			for (Base b: BaseManager.bases) {
				if (Game.get_frame() - b.last_seen_frame > Constants.FPS * 45) {
					if (!overlords.containsValue(b)) {
						if (best_base == null || best_base.location.distance(closest_enemy_spawn()) > b.location.distance(closest_enemy_spawn())) {
							best_base = b;
						}
					}
				}
			}
			if (best_base != null) {
				HjaxUnit best_scout = null;
				for (HjaxUnit overlord : GameInfoCache.get_units(Alliance.SELF, Units.ZERG_OVERLORD)) {
					if (!overlords.containsKey(overlord.tag())) {
						if (best_scout == null || overlord.distance(best_base.location) < best_scout.distance(best_base.location)) {
							best_scout = overlord;
						}
					}
				}
				if (best_scout != null) {
					overlords.put(best_scout.tag(), best_base);
				}
			}
		} else {
			Base best_base = null;
			for (Base b : BaseManager.bases) {
				if (Game.get_frame() - b.last_seen_frame > Constants.FPS * 20) {
					if (!overlords.containsValue(b)) {
						if (best_base == null || best_base.location.distance(BaseManager.main_base().location) > b.location.distance(BaseManager.main_base().location)) {
							best_base = b;
						}
					}
				}
			}
			if (best_base != null) {
				HjaxUnit best_scout = null;
				for (HjaxUnit overlord : GameInfoCache.get_units(Alliance.SELF, Units.ZERG_OVERLORD)) {
					if (!overlords.containsKey(overlord.tag())) {
						if (best_scout == null || overlord.distance(best_base.location) < best_scout.distance(best_base.location)) {
							best_scout = overlord;
						}
					}
				}
				if (best_scout != null) {
					overlords.put(best_scout.tag(), best_base);
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
		for (HjaxUnit unit: GameInfoCache.get_units(Alliance.SELF, RaceInterface.get_race_worker())) {
			if (Worker.can_build(unit)) {
				scout = unit;
				return;
			}
		}
	}
	
	public static void assign_patrol_scout() {
		for (HjaxUnit unit: GameInfoCache.get_units(Alliance.SELF, RaceInterface.get_race_worker())) {
			if (Worker.can_build(unit)) {
				patrol_scout = unit;
				return;
			}
		}
	}
	
	public static boolean is_scout(HjaxUnit ally) {
		return (scout != null && ally.tag().equals(scout.tag())) || (patrol_scout != null && ally.tag().equals(patrol_scout.tag()));
	}
	
	
}

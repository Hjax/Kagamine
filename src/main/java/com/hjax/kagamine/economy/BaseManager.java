package com.hjax.kagamine.economy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.debug.Color;
import com.github.ocraft.s2client.protocol.spatial.Point;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.Utilities;
import com.hjax.kagamine.Vector2d;
import com.hjax.kagamine.army.EnemySquadManager;
import com.hjax.kagamine.army.ThreatManager;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.game.HjaxUnit;
import com.hjax.kagamine.game.RaceInterface;
import com.hjax.kagamine.knowledge.Scouting;
import com.hjax.kagamine.unitcontrollers.Worker;

public class BaseManager {
	// the index of bases must never change
	public static ArrayList<Base> bases = new ArrayList<>();
	static ArrayList<Point2d> expos = new ArrayList<>();
	private static Map<Pair<Integer, Integer>, Float> distances = new HashMap<>();
	
	public static void start_game() {
		bases.clear();
		
		for (Point p : Game.expansions()) {
			bases.add(new Base(p.toPoint2d()));
		}
		
		HjaxUnit main = GameInfoCache.get_units(Alliance.SELF, RaceInterface.get_race_command_structure()).get(0);
		
		bases.add(new Base(main.location()));

		for (Base base : bases) {
			if (main.distance(base) < 10) {
				base.location = main.location();
			}
		}
		
		
		for (int i = 0; i < bases.size(); i++) {
			for (int j = 0; j < bases.size(); j++) {
				Point2d first = bases.get(i).location;
				Point2d second = bases.get(j).location;
				float dist = Game.pathing_distance(first, second);
				if (i != j) {
					while (Math.abs(dist) < 0.1) {
						first = Point2d.of(first.getX() + 1, first.getY());
						second = Point2d.of(second.getX() + 1, second.getY());
						dist = Game.pathing_distance(first, second);
					}
				}
				distances.put(new ImmutablePair<>(i, j), dist);
				distances.put(new ImmutablePair<>(j, i), dist);
			}
		}
		
		main.use_ability(Abilities.RALLY_HATCHERY_WORKERS, main);
	}
	
	public static void on_unit_created(HjaxUnit u) {
		if (u.type()== Units.ZERG_QUEEN) {
			if (inject_queen_count() > 3) return;
			Base best = null;
			for (Base base: bases) {
				if (!base.has_queen() && base.has_friendly_command_structure()) {
					if (base.command_structure.done()) {
						if (best == null || u.distance(best) > u.distance(base)) {
							best = base;
						}
					}
				}
			}
			if (best == null) {
				for (Base base: bases) {
					if (!base.has_queen() && base.has_friendly_command_structure()) {
						if (best == null || u.distance(best) > u.distance(base)) {
							best = base;
						}
					}
				}
			}
			if (best != null) best.set_queen(u);
		}
	}
	
	public static void on_frame() {
		for (HjaxUnit unit: GameInfoCache.get_units()) {
			if (unit.is_command()) {
				for (Base base: bases) {
					if (unit.distance(base) < 7) {
						base.set_command_structure(unit);
						base.set_walking_drone(null);
					}
				}
			}
		}
		for (Base b: bases) {
			b.update();
			if (b.has_walking_drone() && b.walking_drone.distance(b.location) > 4 && (b.walking_drone.ability() != Game.production_ability(RaceInterface.get_race_command_structure()))) {
				b.walking_drone.move(b.location);
			}
			if (b.has_walking_drone() && !b.walking_drone.alive()) b.walking_drone = null;
		}
		for (HjaxUnit ling : GameInfoCache.get_units(Alliance.SELF, Units.ZERG_ZERGLING_BURROWED)) {
			for (HjaxUnit drone: GameInfoCache.get_units(Alliance.SELF, Units.ZERG_DRONE)) {
				if (drone.distance(ling) < 10) {
					ling.use_ability(Abilities.BURROW_UP);
				}
			}
		}
	}
	
	static boolean is_walking_drone(HjaxUnit u) {
		for (Base b: bases) {
			if (b.walking_drone == u) {
				return true;
			}
		}
		return false;
	}
	
	public static Base main_base = null;
	public static Base main_base() {
		if (main_base == null || !main_base.has_friendly_command_structure()) {
			Base best = null;
			for (Base b: bases) {
				if (b.has_friendly_command_structure() && b.command_structure.done()) {
					if (best == null || best.location.distance(Scouting.closest_enemy_spawn(best.location)) < b.location.distance(Scouting.closest_enemy_spawn(b.location))) {
						best = b;
					}
				}
			}
			if (best == null) best = bases.get(0);
			main_base = best;
		}
		
		return main_base;
	}
	
	public static float get_distance(Base b1, Base b2) {
		return distances.get(new ImmutablePair<>(bases.indexOf(b1), bases.indexOf(b2)));
	}
	
	public static long next_base_frame = -1;
	public static Base next_base = null;
	public static Base get_next_base() {
		if (next_base_frame != Game.get_frame()) {
			Base best = null;
			double best_dist = 9999;
			for (Base b: bases) {
				if (b.has_command_structure()) continue;
				if (!ThreatManager.is_safe(b.location)) continue;
				if (best == null || (get_distance(main_base(), b) - get_distance(closest_base(Scouting.closest_enemy_spawn()), b)) < best_dist) {
					best = b;
					best_dist = (get_distance(main_base(), b) - get_distance(closest_base(Scouting.closest_enemy_spawn()), b));
				}
			}
			next_base = best;
			next_base_frame = Game.get_frame();
		}
		return next_base;
	}
	
	public static int inject_queen_count() {
		int result = 0;
		for (Base b : bases) {
			if (b.has_friendly_command_structure() && b.has_queen()) result++;
		}
		return result;
	}
	
	public static int base_count() {
		int result = 0;
		for (Base b: bases) {
			if (b.has_command_structure() && b.has_friendly_command_structure()) result++;
		}
		return result;
	}
	
	public static Point2d get_placement_location(UnitType structure, Point2d base, int min_dist, int max_dist) {
		Point2d result = Point2d.of(0, 0);
		int limit = 0;
		while (!Game.can_place(Game.get_unit_type_data().get(structure).getAbility().orElse(Abilities.INVALID), result) || base.distance(result) < min_dist) {
			float rx = (float) Math.random() * 2 - 1;
			float ry = (float) Math.random() * 2 - 1;
			result = Point2d.of(base.getX() + rx * max_dist, base.getY() + ry * max_dist);
			if (++limit == 100) break;
		}
		return result;	
	}
	
	public static void build(UnitType structure) {
		if (Game.is_town_hall(structure)) {
			if (get_next_base().has_walking_drone()) {
				if (get_next_base().walking_drone.ability() != Game.production_ability(RaceInterface.get_race_command_structure())) {
					get_next_base().walking_drone.use_ability(Game.production_ability(structure), get_next_base().location);
				}
			} else {
				HjaxUnit worker = get_free_worker(get_next_base().location);
				if (worker != null) {
					worker.use_ability(Game.production_ability(RaceInterface.get_race_command_structure()), get_next_base().location);
					return;
				}
			}
		} else if (Game.is_gas_structure(structure)) {
			// try to build at safe bases first
			for (Base b: BaseManager.bases) {
				if (ThreatManager.is_safe(b.location)) {
					if (b.has_friendly_command_structure() && b.command_structure.done()) {
						for (HjaxUnit gas: b.gases) {
							if (GameInfoCache.geyser_is_free(gas)) {
								HjaxUnit worker = get_free_worker(get_next_base().location);
								if (worker != null) {
									worker.use_ability(Game.production_ability(RaceInterface.get_race_gas()), gas);
									return;
								}
							}
						}
					}
				}
			}
			for (Base b: BaseManager.bases) {
				if (b.has_friendly_command_structure() && b.command_structure.done()) {
					for (HjaxUnit gas: b.gases) {
						if (GameInfoCache.geyser_is_free(gas)) {
							HjaxUnit worker = get_free_worker(get_next_base().location);
							if (worker != null) {
								worker.use_ability(Abilities.BUILD_EXTRACTOR, gas);
								return;
							}
						}
					}
				}
			}
		} else if (structure == Units.ZERG_SPINE_CRAWLER) {
			Point2d location = get_spine_placement_location(get_forward_base());
			HjaxUnit worker = get_free_worker(location);
			if (worker != null) {
				worker.use_ability(Abilities.BUILD_SPINE_CRAWLER, location);
				return;
			}
		} else {
			Point2d location = get_placement_location(structure, main_base().location, 6, 15);
			HjaxUnit worker = get_free_worker(location);
			if (worker != null) {
				worker.use_ability(Game.production_ability(structure), location);
				return;
			}
		}
	}
	
	public static HjaxUnit get_free_worker(Point2d location) {
		HjaxUnit best = null;
		unitloop: for (HjaxUnit unit : GameInfoCache.get_units(Alliance.SELF, RaceInterface.get_race_worker())) {
			for (Base b: bases) {
				if (b.walking_drone == unit) continue unitloop;
			}
			if (Worker.can_build(unit)) {
				if (best == null || unit.distance(location) < best.distance(location)) {
					best = unit;
				}
			}
		}
		return best;
	}
	
	public static int active_gases() {
		int total = 0;
		for (HjaxUnit unit: GameInfoCache.get_units(Alliance.SELF, RaceInterface.get_race_gas())) {
			if (unit.gas() > 0) {
				total++;
			}
		}
		return total;
	}
	
	private static Map<Integer, Point2d> get_numbers = new HashMap<>();
	public static Point2d get_base(int n) {
		if (!get_numbers.containsKey(n)) {
			ArrayList<Point2d> found = new ArrayList<>();
			for (int i = 0; i < 20; i++) {
				Base best = null;
				for (Base b: bases) {
					if (best == null|| (get_distance(main_base(), b) - get_distance(closest_base(Scouting.closest_enemy_spawn()), b)) < (get_distance(main_base(), best) - (get_distance(closest_base(Scouting.closest_enemy_spawn()), best)))) {
						if (!found.contains(b.location)) {
							best = b;
						}
					}
				}
				found.add(best.location);
				if (found.size() >= n) break;
			}
			get_numbers.put(n, found.get(found.size() - 1));
		}
		return get_numbers.get(n);
	}
	
	public static Base closest_base(Point2d p) {
		Base best = null;
		for (Base b: bases) {
			if (best == null || p.distance(best.location) > b.location.distance(p)) {
				best = b;
			}
		}
		return best;
	}
	
	public static Base closest_friendly_base(Point2d p) {
		Base best = null;
		for (Base b: bases) {
			if (b.has_friendly_command_structure()) {
				if (best == null || p.distance(best.location) > b.location.distance(p)) {
					best = b;
				}
			}
		}
		return best;
	}
	
	static Point2d get_spine_placement_location(Base b) {
		Point2d target = Scouting.closest_enemy_spawn();
		target = Point2d.of(target.getX() + 4, target.getY());
		Point2d result = null;
		for (int i = 0; i < 200; i++) {
			double rx = Math.random() * 2 - 1;
			double ry = Math.random() * 2 - 1;
			Point2d test = Point2d.of((float) (b.location.getX() + rx * 10), (float) (b.location.getY() + ry * 10));
			if (Game.can_place(Abilities.MORPH_SPINE_CRAWLER_ROOT, test)) {
				if (result == null || Game.pathing_distance(result,  target) > Game.pathing_distance(test, target)) {
					result = test;
				}
			}
		}
		return result;
	}
	
	public static long forward_base_frame = -1;
	public static Base forward_base = null;
	public static Base get_forward_base() {
		if (forward_base_frame != Game.get_frame()) {
			Base best = null;
			Point2d target = closest_base(Scouting.closest_enemy_spawn()).location;
			int best_size = 0;
			for (Set<HjaxUnit> squad : EnemySquadManager.enemy_squads) {
				if (squad.size() > best_size) {
					target = EnemySquadManager.average_point(new ArrayList<>(squad));
					best_size = squad.size();
				}
			}
			for (Base b: bases) {
				if (b.has_friendly_command_structure() && b.command_structure.done()) {
					if (best == null || b.location.distance(target) < best.location.distance(target)) {
						best = b;
					}
				}
			}

			if (best == null) best = bases.get(0);
			forward_base = best;
			forward_base_frame = Game.get_frame();
		}
		return forward_base;
	}
	
	public static Base closest_occupied_base(Point2d p) {
		Base best = null;
		for (Base b : bases) {
			if (b.has_command_structure()) {
				if (best == null || b.location.distance(p) < best.location.distance(p)) {
					best = b;
				}
			}
		}
		return best;
	}
	
	static Point2d get_spore_placement_location(Base b) {
		float x = 0;
		float y = 0;
		int total = 0;
		for (HjaxUnit min: GameInfoCache.get_units(Alliance.NEUTRAL)) {
			if (min.minerals() > 0 && min.distance(b.location) < 8) {
				x += min.location().getX();
				y += min.location().getY();
				total++;
			}
		}
		x /= total;
		y /= total;
		for (HjaxUnit spore : GameInfoCache.get_units(Alliance.SELF, Units.ZERG_SPORE_CRAWLER)) {
			if (spore.distance(Point2d.of(x, y)) < 4) {
				return null;
			}
		}
		float new_x = b.location.getX() - x;
		float new_y = b.location.getY() - y;
		Vector2d offset = Utilities.normalize(new Vector2d(new_x, new_y));
		for (int i = 0; i < 20; i++) {
			Point2d p = Point2d.of((float) (x + (2.5 + 0.1 * i) * offset.x), (float) (y + (2.5 * 0.1 * i) * offset.y));
			if (Game.can_place(Abilities.MORPH_SPORE_CRAWLER_ROOT, p)) {
				return p;
			}
		}
		return null;
	}
	
	private static Point2d get_spore_placement(Base b, Point2d slider) {
		
		for (HjaxUnit spore : GameInfoCache.get_units(Alliance.SELF, Units.ZERG_SPORE_CRAWLER)) {
			if (spore.distance(slider) < 3) {
				return null;
			}
		}
		
		Vector2d offset = Utilities.normalize(Utilities.direction_to(Vector2d.of(b.location), Vector2d.of(slider)));
		for (int i = 0; i < 20; i++) {
			Point2d p = Point2d.of((float) (slider.getX() - (2.5 + 0.1 * i) * offset.x), (float) (slider.getY() - (2.5 * 0.1 * i) * offset.y));
			if (Game.can_place(Abilities.MORPH_SPORE_CRAWLER_ROOT, p)) {
				return p;
			}
		}
		return null;
	}
	
	public static void build_defensive_spores() {
		for (Base b: bases) {
			if (Game.minerals() < 75) {
				return;
			}
			if (b.has_friendly_command_structure() && b.command_structure.done()) {
				Point2d spore = get_spore_placement_location(b);
				if (spore != null) {
					HjaxUnit worker = get_free_worker(spore);
					if (worker != null) {
						worker.use_ability(Abilities.BUILD_SPORE_CRAWLER, spore);
						Game.spend(75, 0);
						return;
					}
				}
			}
		}
	}
	
	public static void build_triangle_spores() {
		for (Base b: bases) {
			if (Game.minerals() < 75) {
				return;
			}
			if (b.has_friendly_command_structure() && b.command_structure.done()) {
				Point2d[] spore = get_spore_triangle_placement_locations(b);
				for (Point2d p : spore) {
					if (p != null) {
						HjaxUnit worker = get_free_worker(p);
						if (worker != null) {
							worker.use_ability(Abilities.BUILD_SPORE_CRAWLER, p);
							Game.spend(75, 0);
							return;
						}
					}
				}
			}
		}
	}
	
	static Point2d[] get_spore_triangle_placement_locations(Base b) {
		
		Point2d[] results = new Point2d[2];
		
		HjaxUnit first = null;
		HjaxUnit second = null;
		double best = -1;
		List<HjaxUnit> resources = new ArrayList<>();
		resources.addAll(b.minerals);
		resources.addAll(b.gases);
		for (HjaxUnit res1 : resources) {
			for (HjaxUnit res2: resources) {
				if (res1.distance(res2) > best) {
					first = res1;
					second = res2;
					best = res1.distance(res2);
				}
			}
		}
		results[0] = get_spore_placement(b, first.location());
		results[1] = get_spore_placement(b, second.location());
		
		return results;
	}
	
	static void calculate_expansions() {
		expos.clear();
		ArrayList<Set<HjaxUnit>> mineral_lines = new ArrayList<>();
		outer: for (HjaxUnit unit: GameInfoCache.get_units(Alliance.NEUTRAL)) {
			if (unit.type().toString().toLowerCase().contains("mineral") || unit.type().toString().toLowerCase().contains("geyser")) {
				for (Set<HjaxUnit> lines : mineral_lines) {
					for (HjaxUnit patch : lines) {
						if (patch.distance(unit) < 14 && Math.abs(Game.height(patch.location()) - Game.height(unit.location())) < .1) {
							lines.add(unit);
							continue outer;
						}
					}
				}
				Set<HjaxUnit> adder = new HashSet<>();
				adder.add(unit);
				mineral_lines.add(adder);
			}
		}
		for (Set<HjaxUnit> line : mineral_lines) {
			HjaxUnit first = line.iterator().next();
			for (HjaxUnit u : line) {
				Game.draw_line(first.location(), u.location(), Color.GREEN);
			}
		}
		
		for (Set<HjaxUnit> line : mineral_lines) {
			float x = 0;
			float y = 0;
			int count = 0;
			for (HjaxUnit patch : new ArrayList<>(line)) {
				x += patch.location().getX();
				y += patch.location().getY();
				count++;
			}
			x = x/count;
			y = y/count;
			Vector2d average = new Vector2d(x, y);
			
			Point2d best = null;
			
			List<Point2d> points = new ArrayList<>();
			for (int x_offset = -10; x_offset < 11; x_offset++) {
				for (int y_offset = -10; y_offset < 11; y_offset++) {
					if ((average.x + x_offset) > 0 && (average.y + y_offset) > 0) {
						Point2d current = Point2d.of((float) (average.x + x_offset), (float) (average.y + y_offset));
						points.add(current);
					}
				}
			}
			List<Boolean> results = Game.can_place(Abilities.BUILD_HATCHERY, points);
			int skipped = 0;
			for (int x_offset = -10; x_offset < 11; x_offset++) {
				for (int y_offset = -10; y_offset < 11; y_offset++) {
					if ((average.x + x_offset) > 0 && (average.y + y_offset) > 0) {
						Point2d current = Point2d.of((float) (average.x + x_offset), (float) (average.y + y_offset));
						if (best == null || average.toPoint2d().distance(current) < average.toPoint2d().distance(best)) {
							if (results.get((x_offset + 10) * 21 + (y_offset + 10) - skipped)) {
								best = current;
							}
						}
					} else {
						skipped++;
					}
				}
			}

			if (best != null) {
				expos.add(best);
			}
		}
	}

}
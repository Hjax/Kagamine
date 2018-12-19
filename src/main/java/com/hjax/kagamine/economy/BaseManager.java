package com.hjax.kagamine.economy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.debug.Color;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.Utilities;
import com.hjax.kagamine.Vector2d;
import com.hjax.kagamine.army.ThreatManager;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.knowledge.Scouting;
import com.hjax.kagamine.unitcontrollers.Drone;

public class BaseManager {
	// the index of bases must never change
	public static ArrayList<Base> bases = new ArrayList<>();
	static ArrayList<Point2d> expos = new ArrayList<>();
	private static Map<Pair<Integer, Integer>, Float> distances = new HashMap<>();
	
	public static void start_game() {
		bases.clear();
		calculate_expansions();
		
		for (Point2d e: expos) bases.add(new Base(e));
		UnitInPool main = GameInfoCache.get_units(Alliance.SELF, Units.ZERG_HATCHERY).get(0);
		// Fix the placement for our main base
		for (Base b : bases) {
			if (b.location.distance(main.unit().getPosition().toPoint2d()) < 10) {
				b.location = main.unit().getPosition().toPoint2d();
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
		on_unit_created(main);
		Game.unit_command(main, Abilities.RALLY_HATCHERY_WORKERS, main.unit());
	}
	
	public static void on_unit_created(UnitInPool u) {
		if (u.unit().getType() == Units.ZERG_QUEEN) {
			Base best = null;
			for (Base b: bases) {
				if (!b.has_queen() && b.has_friendly_command_structure()) {
					if (b.command_structure.unit().getBuildProgress() > 0.999) {
						if (best == null || best.location.distance(u.unit().getPosition().toPoint2d()) > b.location.distance(u.unit().getPosition().toPoint2d())) {
							best = b;
						}
					}
				}
			}
			if (best == null) {
				for (Base b: bases) {
					if (!b.has_queen() && b.has_friendly_command_structure()) {
						if (best == null || best.location.distance(u.unit().getPosition().toPoint2d()) > b.location.distance(u.unit().getPosition().toPoint2d())) {
							best = b;
						}
					}
				}
			}
			if (best != null) best.set_queen(u);
		}
	}
	
	public static void on_frame() {
		for (UnitInPool u: Game.get_units()) {
			if (Game.is_town_hall(u.unit().getType())) {
				for (Base b: bases) {
					if (b.location.distance(u.unit().getPosition().toPoint2d()) < 5) b.set_command_structure(u);
				}
			}
		}
		for (Base b: bases) {
			b.update();
			if (b.has_walking_drone() && b.walking_drone.unit().getPosition().toPoint2d().distance(b.location) > 4 && (b.walking_drone.unit().getOrders().size() == 0 || b.walking_drone.unit().getOrders().get(0).getAbility() != Abilities.BUILD_HATCHERY)) {
				Game.unit_command(b.walking_drone, Abilities.MOVE, b.location);
			}
			if (b.has_walking_drone() && !b.walking_drone.isAlive()) b.walking_drone = null;
		}
		for (UnitInPool ling : GameInfoCache.get_units(Alliance.SELF, Units.ZERG_ZERGLING_BURROWED)) {
			for (UnitInPool drone: GameInfoCache.get_units(Alliance.SELF, Units.ZERG_DRONE)) {
				if (drone.unit().getPosition().distance(ling.unit().getPosition()) < 10) {
					Game.unit_command(ling, Abilities.BURROW_UP);
				}
			}
		}
	}
	
	static boolean is_walking_drone(UnitInPool u) {
		for (Base b: bases) {
			if (b.walking_drone == u) {
				return true;
			}
		}
		return false;
	}
	
	public static long main_base_frame = -1;
	public static Base main_base = null;
	public static Base main_base() {
		if (main_base_frame != Game.get_frame()) {
			main_base_frame = Game.get_frame();
			Base best = null;
			for (Base b: bases) {
				if (b.has_friendly_command_structure() && b.command_structure.unit().getBuildProgress() > 0.999) {
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
				if (b.has_friendly_command_structure()) continue;
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
	
	public static int base_count(Alliance a) {
		int result = 0;
		for (Base b: bases) {
			if (b.has_command_structure() && b.command_structure.unit().getAlliance() == a) result++;
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
			if (++limit == 500) break;
		}
		return result;	
	}
	
	public static void build(UnitType structure) {
		if (Game.is_town_hall(structure)) {
			if (get_next_base().has_walking_drone()) {
				if (get_next_base().walking_drone.unit().getOrders().size() == 0 || get_next_base().walking_drone.unit().getOrders().get(0).getAbility() != Abilities.BUILD_HATCHERY) {
					Game.unit_command(get_next_base().walking_drone, Game.get_unit_type_data().get(structure).getAbility().orElse(Abilities.INVALID), get_next_base().location);
				}
			} else {
				UnitInPool worker = get_free_worker(get_next_base().location);
				if (worker != null) {
					Game.unit_command(worker, Abilities.BUILD_HATCHERY, get_next_base().location);
					return;
				}
			}
		} else if (structure == Units.ZERG_EXTRACTOR) {
			// try to build at safe bases first
			for (Base b: BaseManager.bases) {
				if (ThreatManager.is_safe(b.location)) {
					if (b.has_friendly_command_structure() && b.command_structure.unit().getBuildProgress() > 0.999) {
						for (UnitInPool gas: b.gases) {
							if (GameInfoCache.geyser_is_free(gas)) {
								UnitInPool worker = get_free_worker(get_next_base().location);
								if (worker != null) {
									Game.unit_command(worker, Abilities.BUILD_EXTRACTOR, gas.unit());
									return;
								}
							}
						}
					}
				}
			}
			for (Base b: BaseManager.bases) {
				if (b.has_friendly_command_structure() && b.command_structure.unit().getBuildProgress() > 0.999) {
					for (UnitInPool gas: b.gases) {
						if (GameInfoCache.geyser_is_free(gas)) {
							UnitInPool worker = get_free_worker(get_next_base().location);
							if (worker != null) {
								Game.unit_command(worker, Abilities.BUILD_EXTRACTOR, gas.unit());
								return;
							}
						}
					}
				}
			}
		} else if (structure == Units.ZERG_SPINE_CRAWLER) {
			Point2d location = get_spine_placement_location(get_forward_base());
			UnitInPool worker = get_free_worker(location);
			if (worker != null) {
				Game.unit_command(worker, Abilities.BUILD_SPINE_CRAWLER, location);
				return;
			}
		} else {
			Point2d location = get_placement_location(structure, main_base().location, 6, 15);
			UnitInPool worker = get_free_worker(location);
			if (worker != null) {
				Game.unit_command(worker, Game.get_unit_type_data().get(structure).getAbility().get(), location);
				return;
			}
		}
	}
	
	public static UnitInPool get_free_worker(Point2d location) {
		UnitInPool best = null;
		unitloop: for (UnitInPool u : GameInfoCache.get_units(Alliance.SELF, Units.ZERG_DRONE)) {
			for (Base b: bases) {
				if (b.walking_drone == u) continue unitloop;
			}
			if (Drone.can_build(u)) {
				if (best == null || location.distance(u.unit().getPosition().toPoint2d()) < location.distance(best.unit().getPosition().toPoint2d())) {
					best = u;
				}
			}
		}
		return best;
	}
	
	public static int active_extractors() {
		int total = 0;
		for (UnitInPool u : GameInfoCache.get_units(Alliance.SELF, Units.ZERG_EXTRACTOR)) {
			if (u.unit().getVespeneContents().orElse(0) > 0) {
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
	
	public static void build_defensive_spores() {
		outer: for (Base b: bases) {
			if (Game.minerals() < 75) {
				return;
			}
			if (b.has_friendly_command_structure() && !(b.command_structure.unit().getBuildProgress() < .999)) {
				for (UnitInPool spore: GameInfoCache.get_units(Alliance.SELF, Units.ZERG_SPORE_CRAWLER)) {
					if (spore.unit().getPosition().toPoint2d().distance(b.location) <= 9) {
						continue outer;
					}
				}
				Point2d spore = get_spore_placement_location(b);
				if (spore.distance(Point2d.of(0, 0)) < 5) continue outer;
				UnitInPool worker = get_free_worker(spore);
				if (worker != null) {
					Game.unit_command(worker, Abilities.BUILD_SPORE_CRAWLER, spore);
					Game.spend(75, 0);
					return;
				}
			}
		}
	}
	
	static Base closest_base(Point2d p) {
		Base best = null;
		for (Base b: bases) {
			if (best == null || p.distance(best.location) > b.location.distance(p)) {
				best = b;
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
			Base target = closest_base(Scouting.closest_enemy_spawn());
			for (Base b: bases) {
				if (b.has_friendly_command_structure() && !(b.command_structure.unit().getBuildProgress() < 0.999)) {
					if (best == null || get_distance(b, target) < get_distance(best, target)) {
						best = b;
					}
				}
			}

			if (best == null) best = bases.get(0);
			Game.draw_box(best.location, Color.GREEN);
			forward_base = best;
			forward_base_frame = Game.get_frame();
		}
		return forward_base;
	}
	
	static Point2d get_spore_placement_location(Base b) {
		float x = 0;
		float y = 0;
		int total = 0;
		for (UnitInPool min: GameInfoCache.get_units(Alliance.NEUTRAL)) {
			if (min.unit().getMineralContents() .orElse(0)> 0 && min.unit().getPosition().toPoint2d().distance(b.location) < 8) {
				x += min.unit().getPosition().getX();
				y += min.unit().getPosition().getY();
				total++;
			}
		}
		x /= total;
		y /= total;
		x = b.location.getX() - x;
		y = b.location.getY() - y;
		Vector2d offset = Utilities.normalize(new Vector2d(x, y));
		for (int i = 0; i < 20; i++) {
			Point2d p = Point2d.of((float) (b.location.getX() - (2.5 + 0.1 * i) * offset.x), (float) (b.location.getY() - (2.5 * 0.1 * i) * offset.y));
			if (Game.can_place(Abilities.MORPH_SPORE_CRAWLER_ROOT, p)) {
				return p;
			}
		}
		return Point2d.of(0, 0);
	}
	
	static void calculate_expansions() {
		expos.clear();
		ArrayList<Set<UnitInPool>> mineral_lines = new ArrayList<>();
		outer: for (UnitInPool unit: GameInfoCache.get_units(Alliance.NEUTRAL)) {
			if (unit.unit().getType().toString().toLowerCase().contains("mineral") || unit.unit().getType().toString().toLowerCase().contains("geyser")) {
				for (Set<UnitInPool> lines : mineral_lines) {
					for (UnitInPool patch : lines) {
						if (patch.unit().getPosition().distance(unit.unit().getPosition()) < 14) {
							lines.add(unit);
							continue outer;
						}
					}
				}
				Set<UnitInPool> adder = new HashSet<>();
				adder.add(unit);
				mineral_lines.add(adder);
			}
		}
		for (Set<UnitInPool> line : mineral_lines) {
			UnitInPool first = line.iterator().next();
			for (UnitInPool u : line) {
				Game.draw_line(first.unit().getPosition().toPoint2d(), u.unit().getPosition().toPoint2d(), Color.GREEN);
			}
		}

		for (Set<UnitInPool> line : mineral_lines) {
			float x = 0;
			float y = 0;
			int count = 0;
			for (UnitInPool patch : new ArrayList<>(line)) {
				x += patch.unit().getPosition().getX();
				y += patch.unit().getPosition().getY();
				count++;
			}
			x = x/count;
			y = y/count;
			Vector2d average = new Vector2d(x, y);
			
			Point2d best = null;
			
			List<Point2d> points = new ArrayList<>();
			for (int x_offset = -10; x_offset < 11; x_offset++) {
				for (int y_offset = -10; y_offset < 11; y_offset++) {
					Point2d current = Point2d.of((float) (average.x + x_offset), (float) (average.y + y_offset));
					points.add(current);
				}
			}
			List<Boolean> results = Game.can_place(Abilities.BUILD_HATCHERY, points);
			for (int x_offset = -10; x_offset < 11; x_offset++) {
				for (int y_offset = -10; y_offset < 11; y_offset++) {
					Point2d current = Point2d.of((float) (average.x + x_offset), (float) (average.y + y_offset));
					if (best == null || average.toPoint2d().distance(current) < average.toPoint2d().distance(best)) {
						if (results.get((x_offset + 10) * 21 + (y_offset + 10))) {
							best = current;
						}
					}
				}
			}
			
			if (best != null) {
				expos.add(best);
			}
		}
	}
	
	public static boolean needs_expand() {
		int patches = 0;
		int gases = BaseManager.active_extractors();
		for (Base b : BaseManager.bases) {
			if (b.has_friendly_command_structure()) {
				patches += b.minerals.size();
			}
		}
		return GameInfoCache.count_friendly(Units.ZERG_DRONE) > (3 * gases + 2 * patches);
	}

}
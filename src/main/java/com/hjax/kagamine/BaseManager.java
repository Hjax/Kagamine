package com.hjax.kagamine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.UnitControllers.Drone;


public class BaseManager {
	public static ArrayList<Base> bases;
	static ArrayList<Point2d> expos;
	static {
		bases = new ArrayList<>();
		expos = new ArrayList<>();
	}
	
	static void start_game() {
		bases.clear();
		calculate_expansions();
		for (Point2d e: expos) {
			bases.add(new Base(e));
		}
		
		UnitInPool main = GameInfoCache.get_units(Alliance.SELF, Units.ZERG_HATCHERY).get(0);
		
		// Fix the placement for our main base
		for (Base b : bases) {
			if (b.location.distance(main.unit().getPosition().toPoint2d()) < 10) {
				b.location = main.unit().getPosition().toPoint2d();
			}
		}
		
		on_unit_created(main);
	}
	
	static void on_unit_created(UnitInPool u) {
		if (u.unit().getType() == Units.ZERG_HATCHERY) {
			for (Base b : bases) {
				if (b.location.distance(u.unit().getPosition().toPoint2d()) < 1) {
					b.set_command_structure(u);
				}
			}
		}
		if (u.unit().getType() == Units.ZERG_QUEEN) {
			Base best = null;
			for (Base b: bases) {
				if (!b.has_queen() && b.has_command_structure()) {
					if (b.command_structure.unit().getBuildProgress() > 0.999) {
						if (best == null || best.location.distance(u.unit().getPosition().toPoint2d()) > b.location.distance(u.unit().getPosition().toPoint2d())) {
							best = b;
						}
					}
				}
			}
			if (best == null) {
				for (Base b: bases) {
					if (!b.has_queen() && b.has_command_structure()) {
						if (best == null || best.location.distance(u.unit().getPosition().toPoint2d()) > b.location.distance(u.unit().getPosition().toPoint2d())) {
							best = b;
						}
					}
				}
			}
			if (best != null) {
				best.set_queen(u);
			}
		}
	}
	
	static void on_unit_destroyed(UnitInPool u) {
		if (u.unit().getType() == Units.ZERG_HATCHERY || u.unit().getType() == Units.ZERG_LAIR|| u.unit().getType() == Units.ZERG_HIVE) {
			for (Base b : bases) {
				if (b.has_command_structure() && b.command_structure.getTag() == u.getTag()) {
					b.set_command_structure(null);
				}
			}
		}
		if (u.unit().getType() == Units.ZERG_QUEEN) {
			for (Base b : bases) {
				if (b.has_queen() && b.queen.getTag() == u.getTag()) {
					b.set_queen(null);
				}
			}
		}
	}
	
	static void on_frame() {
		for (Base b: bases) {
			b.update();
			if (b.has_walking_drone() && Drone.can_build(b.walking_drone)) {
				Game.unit_command(b.walking_drone, Abilities.MOVE, b.location);
			}
		}
		for (UnitInPool ling : GameInfoCache.get_units(Alliance.SELF, Units.ZERG_ZERGLING_BURROWED)) {
			for (UnitInPool drone: GameInfoCache.get_units(Alliance.SELF, Units.ZERG_DRONE)) {
				if (drone.unit().getPosition().distance(ling.unit().getPosition()) < 10) {
					Game.unit_command(ling, Abilities.BURROW_UP);
				}
			}
		}
		// worker transfer code
		for (Base b : bases) {
			if (!b.has_command_structure()) continue;
			if (b.command_structure.unit().getAssignedHarvesters().orElse(0) > 16) {
				for (Base target: bases) {
					if (!target.has_command_structure()) continue;
					if (target.minerals.size() == 0) continue;
					if (target.command_structure.unit().getAssignedHarvesters().orElse(0) + GameInfoCache.in_progress(Units.ZERG_DRONE) < 16) {
						for (UnitInPool worker : GameInfoCache.get_units(Alliance.SELF, Units.ZERG_DRONE)) {
							if (worker.unit().getPosition().toPoint2d().distance(b.location) < 10) {
								// TODO remove try catch, fix crashing
								try {
									if (worker.unit().getOrders().get(0).getAbility() == Abilities.HARVEST_GATHER && Game.get_unit(worker.unit().getOrders().get(0).getTargetedUnitTag().get()).unit().getMineralContents().orElse(0) > 0) {
										Game.unit_command(worker, Abilities.SMART, target.minerals.get(0).unit());
										return;
									}
								} catch (Exception e) {
									Game.unit_command(worker, Abilities.SMART, target.minerals.get(0).unit());
									return;
								}
							}
						}
					}
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
	
	// TODO this will cause crashes if all of our bases get sniped 
	static Base main_base() {
		Base best = null;
		for (Base b: bases) {
			if (b.has_command_structure() && b.command_structure.unit().getBuildProgress() > 0.999) {
				if (best == null || best.location.distance(Scouting.closest_enemy_spawn(best.location)) < b.location.distance(Scouting.closest_enemy_spawn(b.location))) {
					best = b;
				}
			}
		}
		return best;
	}
	
	public static void assign_worker(UnitInPool u) {
		for (Base b : bases) {
			if (b.has_command_structure() && b.command_structure.unit().getBuildProgress() > 0.999) {
				if (b.command_structure.unit().getAssignedHarvesters().orElse(0) < b.command_structure.unit().getIdealHarvesters().orElse(0)) {
					if (b.minerals.size() > 0) {
						Game.unit_command(u, Abilities.SMART, b.minerals.get(0).unit());
						return;
					}
				}
			}
		}
		for (Base b : bases) {
			if (b.has_command_structure() && b.command_structure.unit().getBuildProgress() > 0.999) {
				if (b.command_structure.unit().getAssignedHarvesters().orElse(0) < b.command_structure.unit().getIdealHarvesters().orElse(0) * 1.5) {
					if (b.minerals.size() > 0) {
						Game.unit_command(u, Abilities.SMART, b.minerals.get(0).unit());
						return;
					}
				}
			}
		}
	}
	
	public static float larva_rate() {
		int total = 0;
		for (Base b: bases) {
			if (b.has_command_structure()) {
				total++;
				if (b.has_queen()) total++;
			}
		}
		return total;
	}
	
	
	public static long next_base_frame = -1;
	public static Base next_base = null;
	public static Base get_next_base() {
		if (next_base_frame != Game.get_frame()) {
			Base best = null;
			double best_dist = -1;
			for (Base b: bases) {
				if (b.has_command_structure()) continue;
				if (best == null || (main_base().location.distance(b.location) - Scouting.closest_enemy_spawn().distance(b.location)) < best_dist) {
					best = b;
					best_dist = main_base().location.distance(b.location) - Scouting.closest_enemy_spawn().distance(b.location);
				}
			}
			next_base = best;
			next_base_frame = Game.get_frame();
		}
		return next_base;
	}
	
	public static int base_count() {
		int result = 0;
		for (Base b: bases) {
			if (b.has_command_structure()) result++;
		}
		return result;
	}
	
	static Point2d get_placement_location(UnitType structure, Point2d base, int min_dist, int max_dist) {
		Point2d result = Point2d.of(0, 0);
		int limit = 0;
		while (!Game.can_place(Game.get_unit_type_data().get(structure).getAbility().orElse(Abilities.INVALID), result) || base.distance(result) < min_dist) {
			float rx = (float) Math.random();
			float ry = (float) Math.random();
			result = Point2d.of(base.getX() + rx * max_dist, base.getY() + ry * max_dist);
			if (++limit == 500) break;
		}
		return result;	
	}
	
	static void build(UnitType structure) {
		if (Game.is_town_hall(structure)) {
			if (get_next_base().has_walking_drone()) {
				Game.unit_command(get_next_base().walking_drone, Game.get_unit_type_data().get(structure).getAbility().orElse(Abilities.INVALID), get_next_base().location);
			} else {
				UnitInPool worker = get_free_worker(get_next_base().location);
				if (worker != null) {
					Game.unit_command(worker, Abilities.BUILD_HATCHERY, get_next_base().location);
					return;
				}
			}
		} else if (structure == Units.ZERG_EXTRACTOR) {
			for (Base b: BaseManager.bases) {
				if (b.has_command_structure() && b.command_structure.unit().getBuildProgress() > 0.999) {
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
			Point2d location = get_placement_location(structure, main_base().location, 5, 15);
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
	
	static int active_extractors() {
		int total = 0;
		for (UnitInPool u : GameInfoCache.get_units(Alliance.SELF, Units.ZERG_EXTRACTOR)) {
			if (u.unit().getVespeneContents().orElse(0) > 0) {
				total++;
			}
		}
		return total;
	}
	
	static Point2d get_base(int n) {
		ArrayList<Point2d> found = new ArrayList<>();
		for (int i = 0; i < 20; i++) {
			Point2d best = Point2d.of(0, 0);
			double best_dist = -1;
			for (Base b: bases) {
				if (best_dist < 0 || main_base().location.distance(b.location) - Scouting.closest_enemy_spawn().distance(b.location) < best_dist) {
					if (!found.contains(b.location)) {
						best = b.location;
						best_dist = main_base().location.distance(b.location) - Scouting.closest_enemy_spawn().distance(b.location);
					}
				}
			}
			found.add(best);
			if (found.size() >= n) break;
		}
		return found.get(found.size() - 1);
	}
	
	static void build_defensive_spores() {
		outer: for (Base b: bases) {
			if (Game.minerals() < 75) {
				return;
			}
			if (b.has_command_structure() && !(b.command_structure.unit().getBuildProgress() < .999)) {
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
	
	static Point2d get_spine_placement_location(Base b) {
		Point2d target = Scouting.closest_enemy_spawn();
		target = Point2d.of(target.getX() + 4, target.getY());
		Point2d result = Point2d.of(0, 0);
		for (int i = 0; i < 200; i++) {
			double rx = Math.random();
			double ry = Math.random();
			Point2d test = Point2d.of((float) (b.location.getX() + rx * 10), (float) (b.location.getY() + ry * 10));
			if (Game.can_place(Abilities.BUILD_SPINE_CRAWLER, test)) {
				if (result == Point2d.of(0, 0) || Game.pathing_distance(result,  target) > Game.pathing_distance(test, target)) {
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
			Point2d target = Scouting.closest_enemy_spawn();
			target = Point2d.of(target.getX() + 5, target.getY() + 5);
			for (Base b: bases) {
				if (b.has_command_structure() && !(b.command_structure.unit().getBuildProgress() < 0.999)) {
					if (best == null || Game.pathing_distance(b.location, target) < Game.pathing_distance(best.location, target)) {
						best = b;
					}
				}
			}
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
			if (Game.can_place(Abilities.BUILD_SPORE_CRAWLER, p)) {
				return p;
			}
		}
		return Point2d.of(0, 0);
	}
	
	static void calculate_expansions() {
		expos.clear();
		ArrayList<Set<UnitInPool>> mineral_lines = new ArrayList<>();
		outer: for (UnitInPool unit: GameInfoCache.get_units(Alliance.NEUTRAL)) {
			if (unit.unit().getType().toString().toLowerCase().contains("mineral")) {
				for (Set<UnitInPool> lines : mineral_lines) {
					for (UnitInPool patch : lines) {
						if (patch.unit().getPosition().distance(unit.unit().getPosition()) < 10) {
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
			
			
			for (int x_offset = -10; x_offset < 11; x_offset++) {
				for (int y_offset = -10; y_offset < 11; y_offset++) {
					Point2d current = Point2d.of((float) (average.x + x_offset), (float) (average.y + y_offset));
					if (best == null || average.toPoint2d().distance(current) < average.toPoint2d().distance(best)) {
						if (Game.query.placement(Abilities.BUILD_HATCHERY, current)) {
							best = current;
							//Game.debug.debugBoxOut(Point.of(current.getX(), current.getY(), (float) (Game.height(current) + .5)), Point.of((float) (current.getX() + .5), (float) (current.getY() + .5), (float) (Game.height(current) + .5)), Color.GREEN);
						}
						else {
							//Game.debug.debugBoxOut(Point.of(current.getX(), current.getY(), (float) (Game.height(current) + .5)), Point.of((float) (current.getX() + .5), (float) (current.getY() + .5), (float) (Game.height(current) + .5)), Color.RED);
						}
					}
				}
			}
			
			if (best != null) {
				expos.add(best);
			}
		}
	}

}

package com.hjax.kagamine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;


public class BaseManager {
	static ArrayList<Base> bases;
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
			best.set_queen(u);
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
					if (target.command_structure.unit().getAssignedHarvesters().orElse(0) + GameInfoCache.production.get(Units.ZERG_DRONE) < 16) {
						for (UnitInPool worker : GameInfoCache.get_units(Alliance.SELF, Units.ZERG_DRONE)) {
							if (worker.unit().getPosition().toPoint2d().distance(b.location) < 10) {
								if (worker.unit().getOrders().get(0).getAbility() == Abilities.HARVEST_GATHER && Game.get_unit(worker.unit().getOrders().get(0).getTargetedUnitTag().get()).unit().getMineralContents().orElse(0) > 0) {
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
			if (best == null || best.location.distance(Scouting.closest_enemy_spawn()) < b.location.distance(Scouting.closest_enemy_spawn())) {
				best = b;
			}
		}
		return best;
	}
	
	static void assign_worker(UnitInPool u) {
		for (Base b : bases) {
			if (b.has_command_structure()) {
				// TODO dont use hard coded 16
				if (b.command_structure.unit().getAssignedHarvesters().orElse(0) < 16) {
					if (b.minerals.size() > 0) {
						Game.unit_command(u, Abilities.SMART, b.minerals.get(0).unit());
						return;
					}
				}
			}
		}
		for (Base b : bases) {
			if (b.has_command_structure()) {
				// TODO dont use hard coded 16
				if (b.command_structure.unit().getAssignedHarvesters().orElse(0) < 24) {
					if (b.minerals.size() > 0) {
						Game.unit_command(u, Abilities.SMART, b.minerals.get(0).unit());
						return;
					}
				}
			}
		}
	}
	
	static Base get_next_base() {
		Base best = null;
		double best_dist = -1;
		for (Base b: bases) {
			if (b.has_command_structure()) continue;
			if (best == null || (main_base().location.distance(b.location) - Scouting.closest_enemy_spawn().distance(b.location)) < best_dist) {
				best = b;
				best_dist = main_base().location.distance(b.location) - Scouting.closest_enemy_spawn().distance(b.location);
			}
		}
		return best;
	}
	
	static int base_count() {
		int result = 0;
		for (Base b: bases) {
			if (b.has_command_structure()) result++;
		}
		return result;
	}
	
	static Point2d get_placement_location(UnitType structure, Point2d base, int max_dist, int min_dist) {
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
				
			}
		}
	}
	
	static UnitInPool get_free_worker(Point2d location) {
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
	
	static Base get_forward_base() {
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
		return best;
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
		Point2d offset = Utilities.normalize(Point2d.of(x, y));
		for (int i = 0; i < 20; i++) {
			Point2d p = Point2d.of((float) (b.location.getX() - (2.5 + 0.1 * i) * offset.getX()), (float) (b.location.getY() - (2.5 * 0.1 * i) * offset.getY()));
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
			if (unit.unit().getDisplayType().toString().toLowerCase().contains("mineral")) {
				for (Set<UnitInPool> lines : mineral_lines) {
					if (lines.iterator().next().unit().getPosition().distance(unit.unit().getPosition()) < 10) {
						lines.add(unit);
						continue outer;
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
			float offset_x = 0;
			float offset_y = 0;
			count = 0;
			for (UnitInPool patch : new ArrayList<>(line)) {
				offset_x += patch.unit().getPosition().getX() - x;
				offset_y += patch.unit().getPosition().getY() - y;
				count++;
			}
			offset_x /= count;
			offset_y /= count;
			for (int i = 0; i < 10; i++) {
				Point2d current = Point2d.of(x + i * offset_x, y + i * offset_y);
				if (Game.query.placement(Abilities.BUILD_HATCHERY, current)) {
					expos.add(current);
				}
			}
		}
	}
}

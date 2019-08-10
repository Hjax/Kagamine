package com.hjax.kagamine.unitcontrollers.zerg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.debug.Color;
import com.github.ocraft.s2client.protocol.observation.AvailableAbility;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Tag;
import com.hjax.kagamine.Utilities;
import com.hjax.kagamine.Vector2d;
import com.hjax.kagamine.economy.Base;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.game.HjaxUnit;
import com.hjax.kagamine.knowledge.Scouting;
import com.hjax.kagamine.Constants;

public class Creep {
	private static final Map<ImmutablePair<Integer, Integer>, Integer> reserved = new HashMap<>();
	private static int[][] terrain = new int[1000][1000];
	private static final int[][] bases = new int[1000][1000];
	private static final Map<Tag, Integer> used = new HashMap<>();
	private static List<Point2d> creep_points = new ArrayList<>();
	static {
		Point2d min = Game.get_game_info().getStartRaw().get().getPlayableArea().getP0().toPoint2d();
		Point2d max = Game.get_game_info().getStartRaw().get().getPlayableArea().getP1().toPoint2d();
		for (int x = (int) min.getX(); x < max.getX(); x += Constants.CREEP_RESOLUTION) {
			for (int y = (int) min.getY(); y < max.getY(); y += Constants.CREEP_RESOLUTION) {
				for (Base b : BaseManager.bases) {
					if (b.location.distance(Point2d.of(x, y)) < 6) {
						bases[x][y] = 1;
					}
				}
			}
		}
	}
	
	public static void start_frame() {
		calculate();
	}
	
	private static void calculate() {
		ArrayList<ImmutablePair<Integer, Integer>> to_erase = new ArrayList<>();
		for (ImmutablePair<Integer, Integer> item : reserved.keySet()) {
			if (reserved.get(item) < Game.get_frame() - Constants.FPS * 10) {
				to_erase.add(item);
			}
		}
		for (ImmutablePair<Integer, Integer> item : to_erase) reserved.remove(item);
		terrain = new int[1000][1000];
		creep_points = new ArrayList<>();
		List<Point2d> alt = new ArrayList<>();
		
		Point2d min = Game.get_game_info().getStartRaw().get().getPlayableArea().getP0().toPoint2d();
		Point2d max = Game.get_game_info().getStartRaw().get().getPlayableArea().getP1().toPoint2d();
		for (int x = (int) min.getX(); x <= max.getX(); x += Constants.CREEP_RESOLUTION) {
			for (int y = (int) min.getY(); y <= max.getY(); y += Constants.CREEP_RESOLUTION) {
				if (Game.pathable(Point2d.of(x, y)) && bases[x][y] == 0) {
					if (Game.on_creep(Point2d.of(x, y)) && Game.is_visible(Point2d.of(x, y))) {
						terrain[x][y] = 1;
					}
				} else {
					terrain[x][y] = -1;
				}
			}
		}
		for (int x = (int) min.getX(); x <= max.getX(); x += Constants.CREEP_RESOLUTION) {
			for (int y = (int) min.getY(); y <= max.getY(); y += Constants.CREEP_RESOLUTION) {
				if (terrain[x][y] == 1) {
					for (Point2d p : around(Point2d.of(x, y))) {
						if (terrain[(int) p.getX()][(int) p.getY()] == 0) {
							Base best = BaseManager.bases.get(0);
							for (Base b: BaseManager.bases) {
								if (b.location.distance(p) < best.location.distance(p)) {
									best = b;
								}
							}
							boolean first3 = false;
							for (int i = 0; i < 3; i++) {
								if (best.location.distance(BaseManager.get_base(i)) < 1) {
									first3 = true;
									break;
								}
							}
							if (!first3 || Scouting.closest_enemy_spawn().distance(best.location) > Scouting.closest_enemy_spawn().distance(p)) {
								if (Game.height(Point2d.of(x,  y)) == Game.height(BaseManager.main_base().location)) {
									alt.add(Point2d.of(x, y));
								} else {
									Game.draw_box(Point2d.of(x, y), Color.PURPLE);
									creep_points.add(Point2d.of(x, y));
								}
							}
						}
					}
				}
			}
		}
		if (creep_points.size() == 0) creep_points.addAll(alt);
		
		for (HjaxUnit u : GameInfoCache.get_units(Alliance.SELF, Units.ZERG_CREEP_TUMOR_BURROWED)) {
			for (int i = creep_points.size() - 1; i > 0; i--) {
				if (u.distance(creep_points.get(i)) <= 4) {
					creep_points.remove(i);
				}
			}
		}
		
		for (HjaxUnit u : GameInfoCache.get_units(Alliance.SELF, Units.ZERG_CREEP_TUMOR_QUEEN)) {
			for (int i = creep_points.size() - 1; i > 0; i--) {
				if (u.distance(creep_points.get(i)) <= 4) {
					creep_points.remove(i);
				}
			}
		}
		
		for (HjaxUnit u : GameInfoCache.get_units(Alliance.SELF, Units.ZERG_CREEP_TUMOR)) {
			for (int i = creep_points.size() - 1; i > 0; i--) {
				if (u.distance(creep_points.get(i)) <= 4) {
					creep_points.remove(i);
				}
			}
		}
		
	}

	public static void on_frame(HjaxUnit u) {
		if (used.getOrDefault(u.tag(), 0) < 1000 && u.done()) {
			Game.write_text("Attemping to spread", u.location());
			boolean found = false;
			for (AvailableAbility x : Game.availible_abilities(u).getAbilities()) {
				if (x.getAbility() == Abilities.BUILD_CREEP_TUMOR) {
					found = true;
					spread_towards(u, Scouting.closest_enemy_spawn());
					break;
				}
			}
			if (!found && used.getOrDefault(u.tag(), 0) > 0) {
				used.put(u.tag(), used.getOrDefault(u.tag(), 0) + 1);
			}
		}
	}
	
	private static void spread_towards(HjaxUnit u, Point2d p) {
		
		Point2d best = null;
		for (Point2d point: creep_points) {
			if (point.distance(u.location()) < 10) {
				if (best == null || best.distance(p) > point.distance(p)) {
					if (Game.can_place(Abilities.BUILD_CREEP_TUMOR_QUEEN, point)) {
						best = point;
					}
				}
			}
		}
		if (best != null) {
			Game.draw_line(u.location(), best, Color.PURPLE);
			u.use_ability(Abilities.BUILD_CREEP_TUMOR, best);
			used.put(u.tag(), used.getOrDefault(u.tag(), 0) + 1);
		}

	}
	
	private static double score(Point2d p) {
		return p.distance(BaseManager.main_base().location);
	}
	
	public static Point2d get_creep_point() {
		Point2d best = null;
		for (Point2d p : creep_points) {
			if (!reserved.containsKey(new ImmutablePair<>((int) p.getX(), (int) p.getY()))) {
				if (best == null || score(best) > score(p)) {
					best = p;
				}
			}
		}
		if (best != null) {
			reserved.put(new ImmutablePair<>((int) best.getX(), (int) best.getY()), (int) Game.get_frame());
		}
		return best;
	}
	
	private static Point2d[] around(Point2d p) {
		Point2d[] result = new Point2d[4];
		result[0] = Point2d.of(p.getX() + Constants.CREEP_RESOLUTION, p.getY());
		result[1] = Point2d.of(p.getX() - Constants.CREEP_RESOLUTION, p.getY());
		result[2] = Point2d.of(p.getX(), p.getY() + Constants.CREEP_RESOLUTION);
		result[3] = Point2d.of(p.getX(), p.getY() - Constants.CREEP_RESOLUTION);
		return result;
	}
}

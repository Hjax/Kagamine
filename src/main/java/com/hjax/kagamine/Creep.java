package com.hjax.kagamine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Tag;

import javafx.util.Pair;

public class Creep {
	public static Map<Pair<Integer, Integer>, Integer> reserved = new HashMap<>();
	static int[][] terrain = new int[1000][1000];	
	static int[][] bases = new int[1000][1000];
	static Set<Tag> used = new HashSet<>();
	static List<Point2d> creep_points = new ArrayList<>();
	static {
		Point2d min = Game.get_game_info().getStartRaw().get().getPlayableArea().getP0().toPoint2d();
		Point2d max = Game.get_game_info().getStartRaw().get().getPlayableArea().getP1().toPoint2d();
		for (int x = (int) min.getX(); x < max.getX(); x += Constants.CREEP_RESOLUTION) {
			for (int y = (int) min.getY(); x < max.getY(); y += Constants.CREEP_RESOLUTION) {
				for (Base b : BaseManager.bases) {
					if (b.location.distance(Point2d.of(x, y)) < 6) {
						bases[x][y] = 1;
					}
				}
			}
		}
	}
	static void calculate() {
		
	}
}

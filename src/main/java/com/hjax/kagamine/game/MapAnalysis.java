package com.hjax.kagamine.game;

import java.util.ArrayList;

import com.github.ocraft.s2client.protocol.spatial.Point2d;

public class MapAnalysis {
	static boolean[][] pathable = new boolean[1000][1000];
	static float[][] height = new float[1000][1000];
	static boolean[][] air_safe = new boolean[1000][1000];
	public static ArrayList<Point2d> overlord_spots = new ArrayList<>();
	static {
		Point2d min = Game.get_game_info().getStartRaw().get().getPlayableArea().getP0().toPoint2d();
		Point2d max = Game.get_game_info().getStartRaw().get().getPlayableArea().getP1().toPoint2d();
		for (int x = (int) min.getX(); x < max.getX(); x ++) {
			for (int y = (int) min.getY(); y < max.getY(); y ++) {
				pathable[x][y] = Game.pathable(Point2d.of(x, y));
				height[x][y] = Game.height(Point2d.of(x, y));
			}
		}
		for (int x = (int) min.getX(); x < max.getX(); x ++) {
			for (int y = (int) min.getY(); y < max.getY(); y ++) {
				boolean safe = true;
				scan: for (int x_offset = -8; x_offset < 8; x_offset++) {
					for (int y_offset = -8; y_offset < 8; y_offset++) {
						if ((x_offset * x_offset + y_offset * y_offset) > 196) continue;
						if (x + x_offset > max.getX() || x + x_offset < min.getX()) continue;
						if (y + y_offset > max.getY() || y + y_offset < min.getY()) continue;
						if (pathable[x + x_offset][y + y_offset] && height[x + x_offset][y + y_offset] > height[x][y] - 2) {
							safe = false;
							break scan;
						}
					}
				}
				air_safe[x][y] = safe;
			}
		}
		
		for (int x = (int) min.getX(); x < max.getX(); x ++) {
			inner: for (int y = (int) min.getY(); y < max.getY(); y ++) {
				if (air_safe[x][y]) {
					for (int x_offset = -1; x_offset < 1; x_offset++) {
						for (int y_offset = -1; y_offset < 1; y_offset++) {
							if (x_offset != 0 && y_offset != 0) continue;
							if (Math.abs(x_offset) + Math.abs(y_offset) == 2) continue;
							if (!air_safe[x + x_offset][y + y_offset]) continue inner;
						}
					}
					for (Point2d p : overlord_spots) {
						if (p.distance(Point2d.of(x, y)) < 5) {
							continue inner;
						}
					}
					overlord_spots.add(Point2d.of(x, y));
				}
			}
		}
	}	
	
	public static void on_frame() {
		
	}
	
	
}

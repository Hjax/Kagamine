package com.hjax.kagamine.game;

import java.util.ArrayList;

import com.hjax.kagamine.Vector2d;

public class MapAnalysis {
	private static final boolean[][] pathable = new boolean[1000][1000];
	private static final float[][] height = new float[1000][1000];
	private static final boolean[][] air_safe = new boolean[1000][1000];
	private static final ArrayList<Vector2d> overlord_spots = new ArrayList<>();
	static {
		Vector2d min = Game.min_point();
		Vector2d max = Game.max_point();
		for (int x = (int) min.getX(); x < max.getX() * 2; x ++) {
			for (int y = (int) min.getY(); y < max.getY() * 2; y ++) {
				pathable[x][y] = Game.pathable(Vector2d.of((float) (x / 2.0), (float) (y / 2.0)));
				height[x][y] = Game.height(Vector2d.of((float) (x / 2.0), (float) (y / 2.0)));
			}
		}
		for (int x = (int) min.getX(); x < max.getX() * 2; x ++) {
			for (int y = (int) min.getY(); y < max.getY() * 2; y ++) {
				boolean safe = true;
				scan: for (int x_offset = -8; x_offset < 8; x_offset++) {
					for (int y_offset = -8; y_offset < 8; y_offset++) {
						if ((x_offset * x_offset + y_offset * y_offset) > 196) continue;
						if (x + x_offset > max.getX() * 2 || x + x_offset < min.getX()) continue;
						if (y + y_offset > max.getY() * 2 || y + y_offset < min.getY()) continue;
						if (pathable[x + x_offset][y + y_offset] && height[x + x_offset][y + y_offset] > height[x][y] - 1) {
							safe = false;
							break scan;
						}
					}
				}
				air_safe[x][y] = safe;

			}
		}
		
		for (int x = (int) min.getX(); x < max.getX() * 2; x ++) {
			inner: for (int y = (int) min.getY(); y < max.getY() * 2; y ++) {
				if (air_safe[x][y]) {
					for (int x_offset = -2; x_offset <= 2; x_offset++) {
						for (int y_offset = -2; y_offset <= 2; y_offset++) {
							if (y_offset * y_offset + x_offset * x_offset > 5) continue;
							if (x + x_offset < 0 || y + y_offset < 0) continue;
							if (!air_safe[x + x_offset][y + y_offset]) continue inner;
						}
					}
					for (Vector2d p : overlord_spots) {
						if (p.distance(Vector2d.of((float) (x / 2.0), (float) (y / 2.0))) < 5) {
							continue inner;
						}
					}
					for (int x_offset = -2; x_offset <= 2; x_offset += 4) {
						for (int y_offset = -2; y_offset <= 2; y_offset += 4) {
							if (x + x_offset > 0 && y + y_offset > 0) {
								if (pathable[x + x_offset][y + y_offset]) {
									overlord_spots.add(Vector2d.of((float) (x / 2.0), (float) (y / 2.0)));
									continue inner;
								}
							}
						}
					}
					
				}
			}
		}
	}


}

package com.hjax.kagamine.game;

import com.hjax.kagamine.Vector2d;

class Pathfinding {
	
	private static final boolean[][] pathable_grid = new boolean[1000][1000];
	
	public static void start_game() {
		
		Vector2d min = Game.min_point();
		Vector2d max = Game.max_point();
		
		for (int x = (int) min.getX(); x < max.getX(); x++) {
			for (int y = (int) min.getY(); y < max.getY(); y++) {
				pathable_grid[x][y] = Game.pathable(Vector2d.of(x, y));
			}
		}
	}
	
	
}

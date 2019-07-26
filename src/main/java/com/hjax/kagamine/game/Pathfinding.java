package com.hjax.kagamine.game;

import com.github.ocraft.s2client.protocol.spatial.Point2d;

class Pathfinding {
	
	private static final boolean[][] pathable_grid = new boolean[1000][1000];
	
	public static void start_game() {
		Point2d min = Game.get_game_info().getStartRaw().get().getPlayableArea().getP0().toPoint2d();
		Point2d max = Game.get_game_info().getStartRaw().get().getPlayableArea().getP1().toPoint2d();
		for (int x = (int) min.getX(); x < max.getX(); x++) {
			for (int y = (int) min.getY(); y < max.getY(); y++) {
				pathable_grid[x][y] = Game.pathable(Point2d.of(x, y));
			}
		}
	}
	
	
}

package com.hjax.kagamine.knowledge;

import java.util.ArrayList;

import com.github.ocraft.s2client.protocol.debug.Color;
import com.hjax.kagamine.Vector2d;
import com.hjax.kagamine.game.Game;

public class WallFinder {
	
	public static ArrayList<Vector2d> cannon_spots = new ArrayList<>();

	
	public static void onFrame() {
		
		int count = 0;
		
		for (float x =  Game.min_point().getX(); x < Game.max_point().getX(); x += 1) {
			for (float y = Game.min_point().getY(); y < Game.max_point().getY(); y += 1) {
				
				Vector2d current = new Vector2d(x, y);
				
				
				if (canMakeStandardDownWall(current)) {
					count++;
					Game.draw_box(current, Color.GREEN);
				}
				
				if (canMakeStandardUpWall(current)) {
					count++;
					Game.draw_box(current, Color.RED);
				}
				
				if (canMakeStandardLeftWall(current)) {
					count++;
					Game.draw_box(current, Color.BLUE);
				}

				if (canMakeStandardRightWall(current)) {
					count++;
				    Game.draw_box(current, Color.YELLOW);
				}
				
			}
		}
		System.out.println(count);
		
	}

	public static boolean canMakeStandardDownWall(Vector2d cannon) {
		Vector2d pylon1 = cannon.add(new Vector2d(0, -3));
		Vector2d pylon2 = cannon.add(new Vector2d(2, -2));
		Vector2d pylon3 = cannon.add(new Vector2d(-2, -2));

		if (canBuild(cannon)) {
			if (canBuild(pylon1) && canBuild(pylon2) && canBuild(pylon3)) {
				if (is_blocked(cannon.add(new Vector2d((float) 2.9f, -1))) && is_blocked(cannon.add(new Vector2d((float) -2.9f, -1))) &&
					is_blocked(cannon.add(new Vector2d((float) 2.9f, -.5f))) && is_blocked(cannon.add(new Vector2d((float) -2.9f, -.5f)))) {

					return true;

				}
			}
		}

		return false;
	}
	
	public static boolean canMakeStandardUpWall(Vector2d cannon) {
		Vector2d pylon1 = cannon.add(new Vector2d(0, 3));
		Vector2d pylon2 = cannon.add(new Vector2d(2, 2));
		Vector2d pylon3 = cannon.add(new Vector2d(-2, 2));

		if (canBuild(cannon)) {
			if (canBuild(pylon1) && canBuild(pylon2) && canBuild(pylon3)) {
				
				
				if (is_blocked(cannon.add(new Vector2d((float) 2.9f, 0f))) && is_blocked(cannon.add(new Vector2d((float) -2.9f, 0f)))) {
					
					return true;

				}
			}
		}

		return false;
	}
	
	public static boolean canMakeStandardRightWall(Vector2d cannon) {
		Vector2d pylon1 = cannon.add(new Vector2d(3, 0));
		Vector2d pylon2 = cannon.add(new Vector2d(2, 2));
		Vector2d pylon3 = cannon.add(new Vector2d(2, -2));

		if (canBuild(cannon)) {
			if (canBuild(pylon1) && canBuild(pylon2) && canBuild(pylon3)) {
				
				if (is_blocked(cannon.add(new Vector2d(0, 2.9f))) && is_blocked(cannon.add(new Vector2d(0, -2.9f)))) {

					return true;

				}
			}
		}

		return false;
	}
	
	public static boolean canMakeStandardLeftWall(Vector2d cannon) {
		Vector2d pylon1 = cannon.add(new Vector2d(-3, 0));
		Vector2d pylon2 = cannon.add(new Vector2d(-2, 2));
		Vector2d pylon3 = cannon.add(new Vector2d(-2, -2));

		if (canBuild(cannon)) {
			if (canBuild(pylon1) && canBuild(pylon2) && canBuild(pylon3)) {
				
				if (is_blocked(cannon.add(new Vector2d(-1, 2.9f))) && is_blocked(cannon.add(new Vector2d(-1, -2.9f))) &&
					is_blocked(cannon.add(new Vector2d(-.5f, 2.9f))) && is_blocked(cannon.add(new Vector2d(-.5f, -2.9f)))) {

					return true;

				}
			}
		}

		return false;
	}

	
	public static boolean canBuild(Vector2d point) {
		return Game.is_placeable(point.add(new Vector2d(0.9f, 0.9f))) &&
				Game.is_placeable(point.add(new Vector2d(-0.9f, 0.9f))) &&
				Game.is_placeable(point.add(new Vector2d(0.9f, -0.9f))) &&
				Game.is_placeable(point.add(new Vector2d(-0.9f, -0.9f)));
	}
	
	public static boolean is_blocked(Vector2d point) {
		return !Game.is_placeable(point) && !Game.pathable(point);
	}
	
}

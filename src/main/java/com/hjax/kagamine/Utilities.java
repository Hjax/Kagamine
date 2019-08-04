package com.hjax.kagamine;

import com.github.ocraft.s2client.protocol.spatial.Point2d;

public class Utilities {
	public static Vector2d normalize(Vector2d v) {
		
		double length = Math.sqrt(v.x * v.x + v.y * v.y);
		
		return new Vector2d((float) (v.x / length), (float) (v.y / length));
	}
	
	public static Vector2d direction_to(Vector2d a, Vector2d b) {
		return normalize(new Vector2d(b.x - a.x, b.y - a.y));
	}
	
}
 
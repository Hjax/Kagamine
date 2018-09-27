package com.hjax.kagamine;

import com.github.ocraft.s2client.protocol.spatial.Point2d;

public class Vector2d {
	public float x;
	public float y;
	public Vector2d(float mx, float my) {
		x = mx;
		y = my;
	}
	public static Vector2d of(Point2d p) {
		return new Vector2d(p.getX(), p.getY());
	}
	public Point2d toPoint2d() {
		return Point2d.of(x, y);
	}
	
	public Vector2d add(Vector2d v) {
		return new Vector2d(x + v.x, y + v.y);
	}
	
	public Vector2d scale(float i) {
		return new Vector2d(x * i, y * i);
	}
}

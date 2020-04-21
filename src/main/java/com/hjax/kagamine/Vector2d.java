package com.hjax.kagamine;

import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.hjax.kagamine.Vector2d;

public class Vector2d {
	
	private final float x;
	private final float y;
	
	public Vector2d(float mx, float my) {
		x = mx;
		y = my;
	}
	
	public float getX() {
		return x;
	}
	
	public float getY() {
		return y;
	}
	
	public static Vector2d of(Point2d p) {
		return new Vector2d(p.getX(), p.getY());
	}
	
	public static Vector2d of(float x, float y) {
		return new Vector2d(x, y);
	}
	public Point2d toPoint2d() {
		return Point2d.of(x, y);
	}
	
	public Vector2d add(Vector2d v) {
		return new Vector2d(x + v.x, y + v.y);
	}
	
	public Vector2d scale(double d) {
		return new Vector2d((float) (x * d), (float) (y * d));
	}
	
	public double distance(Vector2d other) {
		return Math.sqrt(Math.pow(x - other.x, 2) + Math.pow(y - other.y, 2));
	}
	
	public Vector2d normalized() {
		
		double length = Math.sqrt(x * x + y * y);
		
		return new Vector2d((float) (x / length), (float) (y / length));
	}
	
	public Vector2d directionTo(Vector2d b) {
		
		return new Vector2d(b.x - x, b.y - y).normalized();
		
	}
	
	
}

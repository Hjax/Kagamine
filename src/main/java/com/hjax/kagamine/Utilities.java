package com.hjax.kagamine;

import com.github.ocraft.s2client.protocol.spatial.Point2d;

public class Utilities {
	public static Point2d normalize(Point2d v) {
		double length = Math.sqrt(v.getX() * v.getX() + v.getY() * v.getY());
		return Point2d.of((float) (v.getX() / length), (float) (v.getY() / length));
	}
	
	public static Point2d direction_to(Point2d a, Point2d b) {
		return normalize(Point2d.of(b.getX() - a.getX(), b.getY() - a.getY()));
	}
}
 
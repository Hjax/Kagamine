package com.hjax.kagamine.knowledge;

public class Bound {
	public Bound(int i, boolean b) {
		frame = i;
		exact = b;
	}
	public int frame = -1;
	public boolean exact = false;
	public Bound update(int i, boolean b) {
		if (i < frame) {
			return new Bound(i, b);
		}
		return this;
	}
}

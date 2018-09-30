package com.hjax.kagamine;

import java.util.HashMap;
import java.util.Map;

public class Counter {
	public static Map<String, Integer> values = new HashMap<>();
	public static void increment(String s) {
		values.put(s, values.getOrDefault(s, 0) + 1);
	}
	public static void print() {
		for (String s : values.keySet()) {
			System.out.println(s + " " + values.get(s));
		}
		values.clear();
	}
}

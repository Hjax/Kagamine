package com.hjax.kagamine;

import java.util.HashMap;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.unit.Tag;

public class Reservation {
	public static HashMap<Tag, String> res;
	public static boolean is_reserved(UnitInPool u) {
		return res.containsKey(u.getTag());
	}
	public static void reserve(UnitInPool u) {
		assert !res.containsKey(u.getTag());
		res.put(u.getTag(), Thread.currentThread().getStackTrace()[1].getClassName());
	}
	public static void free(UnitInPool u) {
		assert Thread.currentThread().getStackTrace()[1].getClassName() == res.get(u.getTag());
		res.remove(u.getTag());
	}
}

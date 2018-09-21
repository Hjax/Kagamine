package com.hjax.kagamine.UnitControllers;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.hjax.kagamine.BaseManager;
import com.hjax.kagamine.Reservation;

public class Drone {
	public static void on_frame(UnitInPool u) {
		if (u.unit().getOrders().size() == 0 && !Reservation.is_reserved(u)) {
			BaseManager.assign_worker(u);
		}
	}
	
	public static boolean can_build(UnitInPool u) {
		return !(Reservation.is_reserved(u) && (u.unit().getOrders().size() == 0 || (u.unit().getOrders().get(0).getAbility() == Abilities.HARVEST_GATHER || u.unit().getOrders().get(0).getAbility() == Abilities.HARVEST_RETURN)));
	}
}
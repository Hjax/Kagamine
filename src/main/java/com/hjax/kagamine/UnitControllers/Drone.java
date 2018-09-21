package com.hjax.kagamine.UnitControllers;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.hjax.kagamine.BaseManager;
import com.hjax.kagamine.Game;
import com.hjax.kagamine.Reservation;
import com.hjax.kagamine.Scouting;

public class Drone {
	public static void on_frame(UnitInPool u) {
		if (u.unit().getOrders().size() == 0 && can_build(u)) {
			BaseManager.assign_worker(u);
		}
	}
	
	public static boolean can_build(UnitInPool u) {
		return !(Scouting.scout == u || Scouting.patrol_scout == u) && (u.unit().getOrders().size() == 0 || (u.unit().getOrders().get(0).getAbility() == Abilities.HARVEST_GATHER && Game.get_unit(u.unit().getOrders().get(0).getTargetedUnitTag().get()).unit().getMineralContents().orElse(0) > 0));
	}
}
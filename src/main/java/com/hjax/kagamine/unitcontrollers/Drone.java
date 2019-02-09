package com.hjax.kagamine.unitcontrollers;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.hjax.kagamine.economy.Base;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.economy.EconomyManager;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.knowledge.Scouting;

public class Drone {
	public static void on_frame(UnitInPool u) {
		if (u.unit().getOrders().size() == 0 && can_build(u)) {
			EconomyManager.assign_worker(u);
		}
	}
	
	public static boolean can_build(UnitInPool u) {	
		for (Base b : BaseManager.bases) {
			if (u == b.walking_drone) return false;
		}
		try {
			return !(Scouting.scout == u || Scouting.patrol_scout == u) && (u.unit().getOrders().size() == 0 || (u.unit().getOrders().get(0).getTargetedUnitTag().isPresent() && u.unit().getOrders().get(0).getAbility() == Abilities.HARVEST_GATHER && Game.get_unit(u.unit().getOrders().get(0).getTargetedUnitTag().get()).unit().getMineralContents().orElse(0) > 0));
		} catch (Exception e) {
			return false;
		}
		
	}
}
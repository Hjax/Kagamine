package com.hjax.kagamine.unitcontrollers;

import com.github.ocraft.s2client.protocol.data.Abilities;
import com.hjax.kagamine.economy.Base;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.economy.EconomyManager;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.game.HjaxUnit;
import com.hjax.kagamine.knowledge.Scouting;

public class Worker {
	public static void on_frame(HjaxUnit u) {
		
		if (u.idle() && can_build(u)) {
			EconomyManager.assign_worker(u);
		}
	}
	
	public static boolean can_build(HjaxUnit u) {	
		for (Base b : BaseManager.bases) {
			if (u == b.walking_drone) return false;
		}
		try {
			return !(Scouting.scout == u || Scouting.patrol_scout == u) && (u.orders().size() == 0 || (u.orders().get(0).getTargetedUnitTag().isPresent() && u.orders().get(0).getAbility() == Abilities.HARVEST_GATHER && GameInfoCache.get_unit(u.orders().get(0).getTargetedUnitTag().get()).minerals() > 0));
		} catch (Exception e) {
			return false;
		}
		
	}
}
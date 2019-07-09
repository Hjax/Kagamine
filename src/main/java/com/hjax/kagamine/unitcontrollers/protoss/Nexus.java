package com.hjax.kagamine.unitcontrollers.protoss;

import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.game.HjaxUnit;

public class Nexus {
	public static void on_frame(HjaxUnit u) {
		if (u.energy() > 50) {
			for (HjaxUnit struct : GameInfoCache.get_units(Alliance.SELF)) {
				if (struct.is_structure() && !struct.idle() && struct.done() && !struct.is_chronoed()) {
					u.use_ability(Abilities.EFFECT_CHRONO_BOOST_ENERGY_COST, struct);
				}
			}
		}
	}
}

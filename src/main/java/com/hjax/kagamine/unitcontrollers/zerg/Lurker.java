package com.hjax.kagamine.unitcontrollers.zerg;

import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.game.HjaxUnit;
import com.hjax.kagamine.unitcontrollers.GenericUnit;

public class Lurker {
	public static void on_frame(HjaxUnit u) {
		boolean near = false;
		for (HjaxUnit enemy: GameInfoCache.get_units(Alliance.ENEMY)) {
			if (enemy.distance(u) < 10 && !enemy.flying()) {
				near = true;
			}
		} 
		if (near && !u.burrowed()) {
			u.use_ability(Abilities.BURROW_DOWN);
			return;
		} else if (!near && u.burrowed()) {
			u.use_ability(Abilities.BURROW_UP);
			return;
		} else if (!u.burrowed()){
			GenericUnit.on_frame(u, true);
			return;
		}
	}
}

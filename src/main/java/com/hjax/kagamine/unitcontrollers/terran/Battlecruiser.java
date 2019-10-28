package com.hjax.kagamine.unitcontrollers.terran;

import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.observation.AvailableAbility;
import com.hjax.kagamine.army.ArmyManager;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.game.HjaxUnit;

public class Battlecruiser {

	
	public static void on_frame(HjaxUnit u) {
		for (AvailableAbility a: Game.availible_abilities(u).getAbilities()) {
			if (a.getAbility() == Abilities.EFFECT_YAMATO_GUN) {
				for (HjaxUnit ub : GameInfoCache.get_units(Units.TERRAN_BATTLECRUISER)) {
					if (u != ub) {
						u.use_ability(Abilities.EFFECT_YAMATO_GUN, ub);
					}
				}
			}
			if (a.getAbility() == Abilities.EFFECT_TACTICAL_JUMP) {
				u.use_ability(Abilities.EFFECT_TACTICAL_JUMP, ArmyManager.army_center);
			}
		}
	}
	
}

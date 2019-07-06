package com.hjax.kagamine.unitcontrollers.zerg;

import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.observation.AvailableAbility;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.Utilities;
import com.hjax.kagamine.Vector2d;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.game.HjaxUnit;
import com.hjax.kagamine.knowledge.Scouting;
import com.hjax.kagamine.unitcontrollers.GenericUnit;

public class Ravager {
	public static void on_frame(HjaxUnit u2) {
		HjaxUnit best = null;
		for (HjaxUnit u: GameInfoCache.get_units(Alliance.ENEMY, Units.PROTOSS_PHOTON_CANNON)) {
			if (best == null || u.distance(u2.location()) < best.distance(u2.location())) {
				best = u;
			}
		}
		if (best == null) {
			for (HjaxUnit u: GameInfoCache.get_units(Alliance.ENEMY)) {
				if (Game.is_structure(u.type()) && u.distance(u2.location()) < 9) {
					best = u;
					break;
				}
			}
		}
		if (best != null) {
			for (AvailableAbility ab : Game.availible_abilities(u2).getAbilities()) {
				if (ab.getAbility() == Abilities.EFFECT_CORROSIVE_BILE) {
					u2.use_ability(Abilities.EFFECT_CORROSIVE_BILE, best.location());
					return;
				}
			}
			if (best.type() == Units.PROTOSS_PHOTON_CANNON) {
				if (best.distance(BaseManager.main_base().location) < best.distance(Scouting.closest_enemy_spawn())) {
					Vector2d diff = Utilities.direction_to(Vector2d.of(best.location()), Vector2d.of(u2.location()));
					u2.move(Point2d.of(best.location().getX() + diff.x * 15, best.location().getY() + diff.y * 15));
					return;
				}
			}
		}
		if (u2.ability() != Abilities.EFFECT_CORROSIVE_BILE) GenericUnit.on_frame(u2, true);
	}
}

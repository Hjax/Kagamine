package com.hjax.kagamine.unitcontrollers.zerg;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
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
import com.hjax.kagamine.knowledge.Scouting;
import com.hjax.kagamine.unitcontrollers.GenericUnit;

public class Ravager {
	public static void on_frame(UnitInPool rav) {
		UnitInPool best = null;
		for (UnitInPool u: GameInfoCache.get_units(Alliance.ENEMY, Units.PROTOSS_PHOTON_CANNON)) {
			if (best == null || u.unit().getPosition().toPoint2d().distance(rav.unit().getPosition().toPoint2d()) < best.unit().getPosition().toPoint2d().distance(rav.unit().getPosition().toPoint2d())) {
				best = u;
			}
		}
		if (best == null) {
			for (UnitInPool u: GameInfoCache.get_units(Alliance.ENEMY)) {
				if (Game.is_structure(u.unit().getType()) && u.unit().getPosition().toPoint2d().distance(rav.unit().getPosition().toPoint2d()) < 9) {
					best = u;
					break;
				}
			}
		}
		if (best != null) {
			for (AvailableAbility ab : Game.availible_abilities(rav).getAbilities()) {
				if (ab.getAbility() == Abilities.EFFECT_CORROSIVE_BILE) {
					Game.unit_command(rav, Abilities.EFFECT_CORROSIVE_BILE, best.unit().getPosition().toPoint2d());
					return;
				}
			}
			if (best.unit().getType() == Units.PROTOSS_PHOTON_CANNON) {
				if (best.unit().getPosition().toPoint2d().distance(BaseManager.main_base().location) < best.unit().getPosition().toPoint2d().distance(Scouting.closest_enemy_spawn())) {
					Vector2d diff = Utilities.direction_to(Vector2d.of(best.unit().getPosition().toPoint2d()), Vector2d.of(rav.unit().getPosition().toPoint2d()));
					Game.unit_command(rav, Abilities.MOVE, Point2d.of(best.unit().getPosition().getX() + diff.x * 15, best.unit().getPosition().getY() + diff.y * 15));
					return;
				}
			}
		}
		if (rav.unit().getOrders().size() == 0 || rav.unit().getOrders().get(0).getAbility() != Abilities.EFFECT_CORROSIVE_BILE) GenericUnit.on_frame(rav, true);
	}
}

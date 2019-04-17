package com.hjax.kagamine.unitcontrollers.zerg;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.unitcontrollers.GenericUnit;

public class Lurker {
	public static void on_frame(UnitInPool u) {
		boolean near = false;
		for (UnitInPool enemy: GameInfoCache.get_units(Alliance.ENEMY)) {
			if (enemy.unit().getPosition().toPoint2d().distance(u.unit().getPosition().toPoint2d()) < 10 && !enemy.unit().getFlying().orElse(false)) {
				near = true;
			}
		} 
		if (near && !u.unit().getBurrowed().orElse(false)) {
			Game.unit_command(u, Abilities.BURROW_DOWN);
			return;
		} else if (!near && u.unit().getBurrowed().orElse(false)) {
			Game.unit_command(u, Abilities.BURROW_UP);
			return;
		} else if (!u.unit().getBurrowed().orElse(false)){
			GenericUnit.on_frame(u, true);
			return;
		}
	}
}

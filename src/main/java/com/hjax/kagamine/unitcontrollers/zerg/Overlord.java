package com.hjax.kagamine.unitcontrollers.zerg;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.knowledge.Scouting;

public class Overlord {
	public static void on_frame(UnitInPool u) {
		if (Scouting.overlords.containsKey(u.getTag())) {
			Game.unit_command(u, Abilities.MOVE, Scouting.overlords.get(u.getTag()).location);
		} else if (u.unit().getPosition().toPoint2d().distance(BaseManager.main_base().location) > 10) {
			Game.unit_command(u, Abilities.MOVE, BaseManager.main_base().location);
		}
	}
}

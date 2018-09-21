package com.hjax.kagamine.UnitControllers;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Units;
import com.hjax.kagamine.Base;
import com.hjax.kagamine.BaseManager;
import com.hjax.kagamine.Game;
import com.hjax.kagamine.GameInfoCache;

public class Queen {
	public static void on_frame(UnitInPool u) {
		int tumors = GameInfoCache.count_friendly(Units.ZERG_CREEP_TUMOR) + GameInfoCache.count_friendly(Units.ZERG_CREEP_TUMOR_BURROWED) + GameInfoCache.count_friendly(Units.ZERG_CREEP_TUMOR_QUEEN);
		if (tumors != 0 || GameInfoCache.count_friendly(Units.ZERG_QUEEN) != 2) {
			for (Base b : BaseManager.bases) {
				if (GameInfoCache.count_friendly(Units.ZERG_LARVA) < BaseManager.base_count() * 3) {
					if (b.has_queen() && b.queen == u && b.has_command_structure() && b.command_structure.unit().getBuildProgress() > 0.999) {
						if (u.unit().getEnergy().get() >= 25) {
							Game.unit_command(u, Abilities.EFFECT_INJECT_LARVA, b.command_structure.unit());
						}
					}
				}
			}
		}
	}
}

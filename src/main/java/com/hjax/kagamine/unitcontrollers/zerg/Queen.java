package com.hjax.kagamine.unitcontrollers.zerg;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.army.ThreatManager;
import com.hjax.kagamine.build.Build;
import com.hjax.kagamine.economy.Base;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.knowledge.Wisdom;
import com.hjax.kagamine.unitcontrollers.GenericUnit;

public class Queen {
	public static void on_frame(UnitInPool u) {
		int tumors = GameInfoCache.count_friendly(Units.ZERG_CREEP_TUMOR) + GameInfoCache.count_friendly(Units.ZERG_CREEP_TUMOR_BURROWED) + GameInfoCache.count_friendly(Units.ZERG_CREEP_TUMOR_QUEEN);
		if (tumors != 0 || GameInfoCache.count_friendly(Units.ZERG_QUEEN) != 2 || GameInfoCache.count_friendly(Units.ZERG_HATCHERY) != 2 || Wisdom.all_in_detected() || Wisdom.proxy_detected() || Build.ideal_workers < 30) {
			for (Base b : BaseManager.bases) {
				if (GameInfoCache.count_friendly(Units.ZERG_LARVA) < BaseManager.base_count(Alliance.SELF) * 3) {
					if (b.has_queen() && b.queen == u && b.has_friendly_command_structure() && b.command_structure.unit().getBuildProgress() > 0.999) {
						if (u.unit().getEnergy().get() >= 25) {
							Game.unit_command(u, Abilities.EFFECT_INJECT_LARVA, b.command_structure.unit());
						}
					}
				}
			}
		}
		if (u.unit().getEnergy().get() > 50) {
			for (UnitInPool a: GameInfoCache.get_units(Alliance.SELF)) {
				if (a.unit().getPosition().toPoint2d().distance(u.unit().getPosition().toPoint2d()) <= 7) {
					if (a.unit().getHealthMax().get() - a.unit().getHealth().get() >= 125) {
						Game.unit_command(u, Abilities.EFFECT_TRANSFUSION, a.unit());
					}
				}
			}
		}
		if (!Wisdom.cannon_rush() && !ThreatManager.under_attack()) {
			if (u.unit().getOrders().size() == 0 && ((u.unit().getEnergy().get() >= 25 && GameInfoCache.count_friendly(Units.ZERG_CREEP_TUMOR) < 25) || u.unit().getEnergy().get() >= 75)) {
				Point2d p = Creep.get_creep_point();
				if (p != null) {
					Game.unit_command(u, Abilities.BUILD_CREEP_TUMOR, p);
					return;
				}
			}
		} 
		if (u.unit().getOrders().size() == 0) {
			GenericUnit.on_frame(u, false);
		}
	}
}

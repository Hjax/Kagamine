package com.hjax.kagamine.UnitControllers;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.ArmyManager;
import com.hjax.kagamine.Base;
import com.hjax.kagamine.BaseManager;
import com.hjax.kagamine.Creep;
import com.hjax.kagamine.Game;
import com.hjax.kagamine.GameInfoCache;
import com.hjax.kagamine.ThreatManager;
import com.hjax.kagamine.Wisdom;

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
		if (Wisdom.proxy_detected() || Wisdom.all_in_detected() && GameInfoCache.count_friendly(Units.ZERG_SPINE_CRAWLER) > 0 && BaseManager.base_count() < 2 && Game.army_supply() < ThreatManager.seen.size() * 4 && Game.army_supply() < 25) {
			for (UnitInPool s: GameInfoCache.get_units(Alliance.SELF, Units.ZERG_SPINE_CRAWLER)) {
				if (s.unit().getPosition().toPoint2d().distance(u.unit().getPosition().toPoint2d()) <= 7) {
					Game.unit_command(u, Abilities.ATTACK, ArmyManager.defend);
					return;
				}
			}
			Base forward = BaseManager.get_forward_base();
			if (forward.location.distance(u.unit().getPosition().toPoint2d()) > 10) {
				Game.unit_command(u, Abilities.MOVE, forward.location);
				return;
			}
		} else if (Game.army_supply() > ThreatManager.seen.size() * 2){
			if (ArmyManager.defend != null) {
				Game.unit_command(u, Abilities.ATTACK, ArmyManager.defend);
				return;
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
		if (u.unit().getOrders().size() == 0 && u.unit().getEnergy().get() >= 25) {
			Point2d p = Creep.get_creep_point();
			if (p != null) {
				Game.unit_command(u, Abilities.BUILD_CREEP_TUMOR, p);
			}
		}
	}
}

package com.hjax.kagamine.UnitControllers;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.ArmyManager;
import com.hjax.kagamine.Base;
import com.hjax.kagamine.BaseManager;
import com.hjax.kagamine.Build;
import com.hjax.kagamine.Game;
import com.hjax.kagamine.GameInfoCache;
import com.hjax.kagamine.ThreatManager;
import com.hjax.kagamine.Wisdom;

public class GenericUnit {
	public static void on_frame(UnitInPool u) {
		if (Wisdom.proxy_detected() || Wisdom.all_in_detected() && GameInfoCache.count_friendly(Units.ZERG_SPINE_CRAWLER) > 0 && BaseManager.base_count() < 2 && Game.army_supply() < ThreatManager.seen.size() * 4 && Game.army_supply() < 25) {
			for (UnitInPool e: GameInfoCache.get_units(Alliance.ENEMY)) {
				if (e.unit().getType() != Units.PROTOSS_ADEPT_PHASE_SHIFT) {
					for (UnitInPool s: GameInfoCache.get_units(Alliance.SELF, Units.ZERG_SPINE_CRAWLER)) {
						if (s.unit().getPosition().toPoint2d().distance(u.unit().getPosition().toPoint2d()) <= 7) {
							Game.unit_command(u, Abilities.ATTACK, e.unit().getPosition().toPoint2d());
							return;
						}
					}
				}
			}
			Base forward = BaseManager.get_forward_base();
			if (forward.location.distance(u.unit().getPosition().toPoint2d()) > 10) {
				Game.unit_command(u, Abilities.MOVE, forward.location);
				return;
			}
		} else if (Game.army_supply() > ThreatManager.seen.size() * 2){
			for (UnitInPool e: GameInfoCache.get_units(Alliance.ENEMY)) {
				for (Base b: BaseManager.bases) {
					if (b.has_command_structure() || b.location.distance(BaseManager.get_next_base().location) < 5 && !Wisdom.confused()) {
						if (e.unit().getPosition().toPoint2d().distance(b.location) < 20) {
							if (u.unit().getOrders().size() == 0 || u.unit().getOrders().get(0).getAbility() == Abilities.MOVE) {
								Game.unit_command(u, Abilities.ATTACK, e.unit().getPosition().toPoint2d());
								return;
							}
						}
					}
				}
			}
		}
		if (Game.supply() >= Build.push_supply) {
			if (ArmyManager.has_target) {
				if (u.unit().getOrders().size() == 0) {
					Game.unit_command(u, Abilities.ATTACK, ArmyManager.target);
				}
			} else {
				if (u.unit().getOrders().size() == 0) {
					Game.unit_command(u, Abilities.ATTACK, Game.get_game_info().findRandomLocation());
				}
			}
			
		}
		if (!ThreatManager.under_attack() && Game.supply() < Build.push_supply) {
			Base front = BaseManager.get_forward_base();
			if (u.unit().getOrders().size() == 0) {
				if (u.unit().getPosition().toPoint2d().distance(front.location) > 12) {
					Game.unit_command(u, Abilities.ATTACK, front.location);
				}
			}
		}
	}
}

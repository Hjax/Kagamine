package com.hjax.kagamine.unitcontrollers;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.hjax.kagamine.army.ArmyManager;
import com.hjax.kagamine.army.BaseDefense;
import com.hjax.kagamine.army.ThreatManager;
import com.hjax.kagamine.build.Build;
import com.hjax.kagamine.economy.Base;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.knowledge.Wisdom;

public class Baneling {
	public static void on_frame(UnitInPool u) {
		if (BaseDefense.assignments.containsKey(u.unit().getTag())) {
			Game.unit_command(u, Abilities.MOVE, BaseDefense.assignments.get(u.getTag()));
		}
		
		if (u.unit().getOrders().size() != 0) return;
		if (Wisdom.cannon_rush()) return;
		
		if ((Game.supply() >= Build.push_supply || Wisdom.ahead())) {
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
					Game.unit_command(u, Abilities.MOVE, front.location);
				}
			}
		}
	}
}

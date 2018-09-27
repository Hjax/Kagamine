package com.hjax.kagamine.UnitControllers;

import java.util.ArrayList;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.ArmyManager;
import com.hjax.kagamine.Base;
import com.hjax.kagamine.BaseManager;
import com.hjax.kagamine.Build;
import com.hjax.kagamine.Game;
import com.hjax.kagamine.GameInfoCache;
import com.hjax.kagamine.ThreatManager;
import com.hjax.kagamine.Utilities;
import com.hjax.kagamine.Vector2d;
import com.hjax.kagamine.Wisdom;

public class GenericUnit {
	public static void on_frame(UnitInPool u) {
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
		if (u.unit().getWeaponCooldown().orElse((float) 0) > 0.1) {
			for (UnitInPool e: GameInfoCache.get_units(Alliance.ENEMY)) {
				if (Game.get_unit_type_data().get(e.unit().getType()).getWeapons().size() > 0) {
					if (u.unit().getPosition().toPoint2d().distance(e.unit().getPosition().toPoint2d()) < new ArrayList<>(Game.get_unit_type_data().get(u.unit().getType()).getWeapons()).get(0).getRange()) {
						if (new ArrayList<>(Game.get_unit_type_data().get(e.unit().getType()).getWeapons()).get(0).getRange() < new ArrayList<>(Game.get_unit_type_data().get(u.unit().getType()).getWeapons()).get(0).getRange()) {
							Vector2d offset = Utilities.direction_to(Vector2d.of(u.unit().getPosition().toPoint2d()), Vector2d.of(e.unit().getPosition().toPoint2d()));
							Point2d target = Point2d.of(u.unit().getPosition().getX() - offset.x, u.unit().getPosition().getY() - offset.y);
							Game.unit_command(u, Abilities.MOVE, target);
						}
					}
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

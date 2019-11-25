package com.hjax.kagamine.unitcontrollers;

import java.util.ArrayList;

import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.data.Upgrades;
import com.github.ocraft.s2client.protocol.data.Weapon;
import com.github.ocraft.s2client.protocol.data.Weapon.TargetType;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.Utilities;
import com.hjax.kagamine.Vector2d;
import com.hjax.kagamine.army.ArmyManager;
import com.hjax.kagamine.army.UnitMovementManager;
import com.hjax.kagamine.economy.Base;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.game.HjaxUnit;
import com.hjax.kagamine.knowledge.Wisdom;

public class GenericUnit {
	public static void on_frame(HjaxUnit u, boolean moveOut) {

		for (HjaxUnit disruptor_shot : GameInfoCache.get_units(Units.PROTOSS_DISRUPTOR_PHASED)) {
			if (disruptor_shot.distance(u) < 4) {
				u.move(Utilities.direction_to(Vector2d.of(disruptor_shot.location()), Vector2d.of(u.location())).scale(2).add(Vector2d.of(u.location())).toPoint2d());
				return;
			}
		}
		
		if (!u.is_melee() && u.ability() == Abilities.ATTACK && u.orders().get(0).getTargetedUnitTag().isPresent() && Game.army_supply() < 100) {
			HjaxUnit target = GameInfoCache.get_unit(u.orders().get(0).getTargetedUnitTag().get());
			if (target != null && (Game.is_town_hall(target.type()) || target.type() == Units.TERRAN_BUNKER)) {
				for (HjaxUnit scv: GameInfoCache.get_units(Alliance.ENEMY, Units.TERRAN_SCV)) {
					if (scv.distance(target) < 5) {
						u.attack(scv);
						return;
					}
				}
			}
		}
		
		if (u.cooldown() > 0.1 & u.ability() == Abilities.ATTACK) {
			for (HjaxUnit e: GameInfoCache.get_units(Alliance.ENEMY)) {
				if (e.type() == Units.PROTOSS_INTERCEPTOR) continue;
				if (Game.is_changeling(e.type())) continue;
				if (Game.get_unit_type_data().get(e.type()).getWeapons().size() > 0) {
					Weapon best = null;
					for (Weapon w: Game.get_unit_type_data().get(u.type()).getWeapons()) {
						if (w.getTargetType() == TargetType.ANY || (w.getTargetType() == TargetType.AIR && e.flying()) || ( (w.getTargetType() == TargetType.GROUND && !e.flying()))) {
							best = w;
						}
					}
					if (best != null) {
						if (u.location().distance(e.location()) < best.getRange()) {
							if (new ArrayList<>(Game.get_unit_type_data().get(e.type()).getWeapons()).get(0).getRange() < best.getRange()) {
								Vector2d offset = Utilities.direction_to(Vector2d.of(u.location()), Vector2d.of(e.location()));
								Point2d target = Point2d.of(u.location().getX() - offset.x, u.location().getY() - offset.y);
								u.move(target);
								return;
							}
						}
					}
				}
			}
			return;
		}
		else if (UnitMovementManager.assignments.containsKey(u.tag())) {
			u.attack(UnitMovementManager.assignments.get(u.tag()));
			return;
		}
				
		if (Wisdom.cannon_rush()) return;
		
		if ((Wisdom.shouldAttack() && moveOut) || UnitMovementManager.has_defense_point()) {
			if (ArmyManager.has_target) {
				if (UnitMovementManager.has_defense_point()) {
					u.attack(UnitMovementManager.defense_point());
				} else if (u.location().distance(ArmyManager.army_center) > 15) {
					u.attack(ArmyManager.army_center);
				} else {
					u.attack(ArmyManager.get_target());
				}
			} else {
				if (u.idle()) {
					u.attack(Game.get_game_info().findRandomLocation());
					return;
				}
			}
			return;

		}
		if (moveOut && !Wisdom.shouldAttack()) {
			Base front = BaseManager.get_forward_base();
			if (u.location().distance(front.location) > 8) {
				u.move(front.location);
			}
		}
	}
}

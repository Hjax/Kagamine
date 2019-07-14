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
import com.hjax.kagamine.army.BaseDefense;
import com.hjax.kagamine.build.Build;
import com.hjax.kagamine.economy.Base;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.game.HjaxUnit;
import com.hjax.kagamine.knowledge.Wisdom;

public class GenericUnit {
	public static void on_frame(HjaxUnit u, boolean moveOut) {
		if (Game.hits_air(u.type())) {
			for (HjaxUnit medi: GameInfoCache.get_units(Alliance.ENEMY)) {
				if (medi.flying() && medi.distance(u) < 6) {
					u.attack(medi);
					return;
				}
			}
		}
		
		if (u.ability() == Abilities.ATTACK && u.orders().get(0).getTargetedUnitTag().isPresent()) {
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
		
		if (u.cooldown() > 0.1) {
			for (HjaxUnit e: GameInfoCache.get_units(Alliance.ENEMY)) {
				if (e.type() == Units.PROTOSS_INTERCEPTOR) continue;
				if (Game.is_changeling(e.type())) continue;
				if (Game.get_unit_type_data().get(e.type()).getWeapons().size() > 0) {
					float range = new ArrayList<>(Game.get_unit_type_data().get(u.type()).getWeapons()).get(0).getRange();
					if (u.type() == Units.ZERG_HYDRALISK && Game.has_upgrade(Upgrades.EVOLVE_GROOVED_SPINES)) range++;
					if (u.location().distance(e.location()) < range) {
						Weapon best = null;
						for (Weapon w: Game.get_unit_type_data().get(u.type()).getWeapons()) {
							if (w.getTargetType() == TargetType.ANY || (w.getTargetType() == TargetType.AIR && e.flying()) || ( (w.getTargetType() == TargetType.GROUND && !e.flying()))) {
								best = w;
							}
						}
						if (best != null) {
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
		} else if (BaseDefense.assignments.containsKey(u.tag())) {
			u.attack(BaseDefense.assignments.get(u.tag()));
			return;
		}
				
		if (Wisdom.cannon_rush()) return;
		
		if ((Wisdom.shouldAttack() && moveOut) || BaseDefense.has_defense_point()) {
			if (ArmyManager.has_target) {
				if (BaseDefense.has_defense_point()) {
					u.attack(BaseDefense.defense_point());
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
		if (moveOut && !(Wisdom.shouldAttack() || Game.supply() >= Build.push_supply)) {
			Base front = BaseManager.get_forward_base();
			if (u.location().distance(front.location) > 8) {
				if (!BaseManager.closest_base(u.location()).has_enemy_command_structure()) {
					u.attack(front.location);
				} else {
					u.move(BaseManager.closest_friendly_base(u.location()).location);
				}
			}
		}
	}
}

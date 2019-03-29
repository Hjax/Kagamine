package com.hjax.kagamine.unitcontrollers;

import java.util.ArrayList;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
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
import com.hjax.kagamine.army.ThreatManager;
import com.hjax.kagamine.build.Build;
import com.hjax.kagamine.economy.Base;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.knowledge.Wisdom;

public class GenericUnit {
	public static void on_frame(UnitInPool u, boolean moveOut) {
		if (Game.hits_air(u.unit().getType())) {
			for (UnitInPool medi: GameInfoCache.get_units(Alliance.ENEMY, Units.TERRAN_MEDIVAC)) {
				if (medi.unit().getPosition().toPoint2d().distance(u.unit().getPosition().toPoint2d()) < 6) {
					Game.unit_command(u, Abilities.ATTACK, medi.unit());
					return;
				}
			}
		}
		
		if (u.unit().getOrders().size() > 0 && u.unit().getOrders().get(0).getAbility() == Abilities.ATTACK && u.unit().getOrders().get(0).getTargetedUnitTag().isPresent()) {
			UnitInPool target = Game.get_unit(u.unit().getOrders().get(0).getTargetedUnitTag().get());
			if (target != null && (Game.is_town_hall(target.unit().getType()) || target.unit().getType() == Units.TERRAN_BUNKER)) {
				for (UnitInPool scv: GameInfoCache.get_units(Alliance.ENEMY, Units.TERRAN_SCV)) {
					if (scv.unit().getPosition().toPoint2d().distance(target.unit().getPosition().toPoint2d()) < 7) {
						Game.unit_command(u, Abilities.ATTACK, scv.unit());
						return;
					}
				}
			}
		}
		
		if (u.unit().getWeaponCooldown().orElse((float) 0) > 0.1) {
			for (UnitInPool e: GameInfoCache.get_units(Alliance.ENEMY)) {
				if (e.unit().getType() == Units.PROTOSS_INTERCEPTOR) continue;
				if (Game.is_changeling(e.unit().getType())) continue;
				if (Game.get_unit_type_data().get(e.unit().getType()).getWeapons().size() > 0) {
					float range = new ArrayList<>(Game.get_unit_type_data().get(u.unit().getType()).getWeapons()).get(0).getRange();
					if (u.unit().getType() == Units.ZERG_HYDRALISK && Game.has_upgrade(Upgrades.EVOLVE_GROOVED_SPINES)) range++;
					if (u.unit().getPosition().toPoint2d().distance(e.unit().getPosition().toPoint2d()) < range) {
						Weapon best = null;
						for (Weapon w: Game.get_unit_type_data().get(u.unit().getType()).getWeapons()) {
							if (w.getTargetType() == TargetType.ANY || (w.getTargetType() == TargetType.AIR && e.unit().getFlying().orElse(false)) || ( (w.getTargetType() == TargetType.GROUND && !e.unit().getFlying().orElse(false)))) {
								best = w;
							}
						}
						if (best != null) {
							if (new ArrayList<>(Game.get_unit_type_data().get(e.unit().getType()).getWeapons()).get(0).getRange() < best.getRange()) {
								Vector2d offset = Utilities.direction_to(Vector2d.of(u.unit().getPosition().toPoint2d()), Vector2d.of(e.unit().getPosition().toPoint2d()));
								Point2d target = Point2d.of(u.unit().getPosition().getX() - offset.x, u.unit().getPosition().getY() - offset.y);
								Game.unit_command(u, Abilities.MOVE, target);
								return;
							}
						}
					}
				}
			}
		} else if (BaseDefense.assignments.containsKey(u.unit().getTag())) {
			Game.unit_command(u, Abilities.ATTACK, BaseDefense.assignments.get(u.getTag()));
			return;
		}
				
		if (Wisdom.cannon_rush()) return;
		
		if ((Game.supply() >= Build.push_supply || Wisdom.shouldAttack()) && moveOut) {
			if (ArmyManager.has_target) {
				Game.unit_command(u, Abilities.ATTACK, ArmyManager.target);
				return;
			} else {
				if (u.unit().getOrders().size() == 0) {
					Game.unit_command(u, Abilities.ATTACK, Game.get_game_info().findRandomLocation());
					return;
				}
			}

		}
		if (moveOut && !ThreatManager.under_attack() && !(Wisdom.shouldAttack() || Game.supply() >= Build.push_supply)) {
			Base front = BaseManager.get_forward_base();
			if (u.unit().getPosition().toPoint2d().distance(front.location) > 12) {
				if (BaseManager.closest_base(u.unit().getPosition().toPoint2d()).has_friendly_command_structure()) {
					Game.unit_command(u, Abilities.ATTACK, front.location);
				} else {
					Game.unit_command(u, Abilities.MOVE, BaseManager.closest_friendly_base(u.unit().getPosition().toPoint2d()).location);
				}
			}
		}
	}
}

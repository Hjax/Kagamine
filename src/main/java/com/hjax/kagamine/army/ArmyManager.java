package com.hjax.kagamine.army;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Tag;
import com.hjax.kagamine.economy.Base;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.economy.EconomyManager;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.knowledge.Scouting;
import com.hjax.kagamine.knowledge.Wisdom;
import com.hjax.kagamine.unitcontrollers.Worker;

public class ArmyManager {
	public static Point2d target;
	public static boolean has_target = false;
	static {
		target = Scouting.closest_enemy_spawn();
		has_target = true;
	}
	
	public static void on_frame() {
		outer: for (UnitInPool u: GameInfoCache.get_units(Alliance.SELF)) {
			if (target.distance(u.unit().getPosition().toPoint2d()) < 4) {
				for (UnitInPool e: GameInfoCache.get_units(Alliance.ENEMY)) {
					if (e.unit().getType() != Units.PROTOSS_ADEPT_PHASE_SHIFT && !Game.is_changeling(e.unit().getType())) {
						if (!e.unit().getFlying().orElse(false) || GameInfoCache.count_friendly(Units.ZERG_MUTALISK) > 0) {
							if (u.unit().getPosition().distance(e.unit().getPosition()) < 4) {
								break outer;
							}
						}
					}
				}
				has_target = false;
				break outer;
			}
		}
		if (!has_target) {
			for (UnitInPool e: GameInfoCache.get_units(Alliance.ENEMY)) {
				if (e.unit().getType() != Units.PROTOSS_ADEPT_PHASE_SHIFT) {
					if (!e.unit().getFlying().orElse(false) || GameInfoCache.count_friendly(Units.ZERG_MUTALISK) > 0) {
						target = e.unit().getPosition().toPoint2d();
						has_target = true;
						break;
					}
				}
			}
		}
		
		for (UnitInPool e: GameInfoCache.get_units(Alliance.ENEMY)) {
			if (Game.is_structure(e.unit().getType())) {
				if (!has_target || target.distance(BaseManager.main_base().location) > e.unit().getPosition().toPoint2d().distance(BaseManager.main_base().location)) {
					has_target = true;
					target = e.unit().getPosition().toPoint2d();
				}
			}
		}

		if (Game.completed_army_supply() < 2) {
			if (!Wisdom.worker_rush()) {
				if (!Wisdom.cannon_rush() && !Wisdom.proxy_detected()) {
					enemy_loop: for (UnitInPool u: GameInfoCache.get_units(Alliance.ENEMY)) {
						for (Base b : BaseManager.bases) {
							if (b.has_friendly_command_structure() && u.unit().getPosition().toPoint2d().distance(b.location) < 12) {
								int attackers = 0;
								for (UnitInPool ally: GameInfoCache.get_units(Alliance.SELF, Units.ZERG_DRONE)) {
									if (!(ally.unit().getOrders().size() == 0) && (ally.unit().getOrders().get(0).getAbility() == Abilities.ATTACK || ally.unit().getOrders().get(0).getAbility() == Abilities.ATTACK_ATTACK)) {
										if (ally.unit().getOrders().get(0).getTargetedUnitTag().orElse(Tag.of((long) 0)).equals(u.unit().getTag())) {
											attackers++;
										}
									}
								}
								if (attackers >= 1) continue enemy_loop;
								for (UnitInPool ally: GameInfoCache.get_units(Alliance.SELF, Units.ZERG_DRONE)) {
									if (Worker.can_build(ally) && ally.unit().getHealth().orElse((float) 0) > 15) {
										Game.unit_command(ally, Abilities.ATTACK, u.unit());
										continue enemy_loop;
									}
								}
							}
						}
					}
				}
			} else {
				for (UnitInPool enemy: GameInfoCache.get_units(Alliance.ENEMY)) {
					if (Game.is_worker(enemy.unit().getType())) {
						if (enemy.unit().getPosition().toPoint2d().distance(BaseManager.main_base().location) <= 20) {
							for (UnitInPool ally: GameInfoCache.get_units(Alliance.SELF, Units.ZERG_DRONE)) {
								if (ally.unit().getHealth().orElse((float) 0) > 10) {
									if (Worker.can_build(ally)) {
										Game.unit_command(ally, Abilities.ATTACK, enemy.unit().getPosition().toPoint2d());
									}
								}
							}
						}
					}
				}
			}
		}
		
		for (UnitInPool ally: GameInfoCache.get_units(Alliance.SELF, Units.ZERG_DRONE)) {
			if (Scouting.is_scout(ally)) continue;
			double best = 9999;
			if (!(ally.unit().getOrders().size() == 0)) {
				if (ally.unit().getOrders().get(0).getAbility() == Abilities.ATTACK) {
					if (Game.completed_army_supply() >= 2) {
						EconomyManager.assign_worker(ally);
						continue;
					}
 					for (Base b : BaseManager.bases) {
						if (b.has_friendly_command_structure()) {
							if (b.location.distance(ally.unit().getPosition().toPoint2d()) < best) best = b.location.distance(ally.unit().getPosition().toPoint2d());
						}
					}
					if (best > 20) EconomyManager.assign_worker(ally);
					if (ally.unit().getHealth().orElse((float) 0) <= 5) EconomyManager.assign_worker(ally);
				}
			}
		}
		
	}
}

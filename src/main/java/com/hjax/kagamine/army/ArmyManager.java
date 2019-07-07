package com.hjax.kagamine.army;

import java.util.ArrayList;
import java.util.List;

import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.debug.Color;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Tag;
import com.hjax.kagamine.Constants;
import com.hjax.kagamine.economy.Base;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.economy.EconomyManager;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.RaceInterface;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.game.HjaxUnit;
import com.hjax.kagamine.knowledge.Scouting;
import com.hjax.kagamine.knowledge.Wisdom;
import com.hjax.kagamine.unitcontrollers.Worker;
import com.github.ocraft.s2client.protocol.game.Race;

public class ArmyManager {
	private static Point2d target;
	
	public static Point2d army_center = Point2d.of(0, 0);
	public static List<HjaxUnit> main_army = new ArrayList<>();
	public static boolean has_target = false;
	static {
		target = Scouting.closest_enemy_spawn();
		has_target = true;
	}
	
	public static void on_frame() {
		
		main_army.clear();
		
		for (HjaxUnit u : UnitRoleManager.get(UnitRoleManager.UnitRole.ARMY)) {
			 List<HjaxUnit> main_army_candidate = new ArrayList<>();
			for (HjaxUnit second : UnitRoleManager.get(UnitRoleManager.UnitRole.ARMY)) {
				if (u.distance(second) < Constants.REGROUP_RADIUS) {
					main_army_candidate.add(second);
				}
			}
			if (main_army_candidate.size() > main_army.size()) {
				main_army.clear();
				main_army.addAll(main_army_candidate);
			}
		}
		
		army_center = EnemySquadManager.average_point(main_army);
		
		outer: for (HjaxUnit unit: GameInfoCache.get_units(Alliance.SELF)) {
			if (unit.distance(target) < 4) {
				for (HjaxUnit enemy: GameInfoCache.get_units(Alliance.ENEMY)) {
					if (enemy.type() != Units.PROTOSS_ADEPT_PHASE_SHIFT && !Game.is_changeling(enemy.type())) {
						if (!enemy.flying() || GameInfoCache.count_friendly(Units.ZERG_MUTALISK) > 0) {
							if (unit.distance(enemy) < 4) {
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
			for (HjaxUnit enemy: GameInfoCache.get_units(Alliance.ENEMY)) {
				if (enemy.type() != Units.PROTOSS_ADEPT_PHASE_SHIFT) {
					if (!enemy.flying() || GameInfoCache.count_friendly(Units.ZERG_MUTALISK) > 0) {
						target = enemy.location();
						has_target = true;
						break;
					}
				}
			}
		}
		
		for (HjaxUnit enemy: GameInfoCache.get_units(Alliance.ENEMY)) {
			if (Game.is_structure(enemy.type())) {
				if (!has_target || target.distance(BaseManager.main_base().location) > enemy.distance(BaseManager.main_base().location)) {
					has_target = true;
					target = enemy.location();
				}
			}
		}

		if (Game.completed_army_supply() * 2 < (ThreatManager.seen.size() + 1) && ThreatManager.seen.size() < 15) {
			if (!Wisdom.worker_rush()) {
				if (GameInfoCache.get_opponent_race() == Race.ZERG || (!Wisdom.cannon_rush() && !Wisdom.proxy_detected())) {
					enemy_loop: for (HjaxUnit enemy: GameInfoCache.get_units(Alliance.ENEMY)) {
						for (Base b : BaseManager.bases) {
							if (b.has_friendly_command_structure() && enemy.distance(b.location) < 12) {
								int attackers = 0;
								for (HjaxUnit ally: GameInfoCache.get_units(Alliance.SELF, RaceInterface.get_race_worker())) {
									if (ally.ability() == Abilities.ATTACK || ally.ability() == Abilities.ATTACK_ATTACK) {
										
										if (ally.orders().get(0).getTargetedUnitTag().orElse(Tag.of((long) 0)).equals(enemy.tag())) {
											attackers++;
										}
									}
								}
								if (Game.is_structure(enemy.type()) && attackers >= 4) continue enemy_loop;
								else if (attackers >= 2) continue enemy_loop;
								for (HjaxUnit ally: GameInfoCache.get_units(Alliance.SELF, RaceInterface.get_race_worker())) {
									if (Worker.can_build(ally) && ally.health() > 15) {
										ally.attack(enemy);
										continue enemy_loop;
									}
								}
							}
						}
					}
				}
			} else {
				for (HjaxUnit enemy: GameInfoCache.get_units(Alliance.ENEMY)) {
					if (enemy.is_worker()) {
						if (enemy.distance(BaseManager.main_base().location) <= 20) {
							for (HjaxUnit ally: GameInfoCache.get_units(Alliance.SELF, RaceInterface.get_race_worker())) {
								if (ally.health() > 10) {
									if (Worker.can_build(ally)) {
										ally.attack(enemy.location());
									}
								}
							}
						}
					}
				}
			}
		}

		for (HjaxUnit ally: GameInfoCache.get_units(Alliance.SELF, RaceInterface.get_race_worker())) {
			if (Scouting.is_scout(ally)) continue;
			double best = 9999;
			if (ally.ability() == Abilities.ATTACK) {
				if (Game.completed_army_supply() >= 2) {
					EconomyManager.assign_worker(ally);
					continue;
				}
				for (Base b : BaseManager.bases) {
					if (b.has_friendly_command_structure()) {
						if (ally.distance(b.location) < best) best = ally.distance(b.location);
					}
				}
				if (best > 20) EconomyManager.assign_worker(ally);
				if (ally.health() <= 10) EconomyManager.assign_worker(ally);
			}
		}

	}
	
	public static Point2d get_target() {
		return target;
	}
}

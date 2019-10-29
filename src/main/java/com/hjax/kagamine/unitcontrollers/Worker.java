package com.hjax.kagamine.unitcontrollers;

import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.data.Upgrades;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.army.ThreatManager;
import com.hjax.kagamine.economy.Base;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.economy.EconomyManager;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.game.HjaxUnit;
import com.hjax.kagamine.knowledge.Scouting;

public class Worker {
	public static void on_frame(HjaxUnit u) {
		
		if (u.idle() && can_build(u)) {
			EconomyManager.assign_worker(u);
		}
		int threat = 0;
		int burrow_threat = 0;
		boolean nearby_spore = false;
		for (HjaxUnit spore: GameInfoCache.get_units(Alliance.SELF, Units.ZERG_SPORE_CRAWLER)) {
			if (spore.distance(u) < 6) {
				nearby_spore = true;
			}
		}
		
		for (HjaxUnit enemy: GameInfoCache.get_units(Alliance.ENEMY)) {
			if (enemy.distance(u.location()) < 10) {
				if (enemy.is_combat() && (!enemy.flying() || !nearby_spore)) {
					threat += Game.get_unit_type_data().get(enemy.type()).getFoodRequired().orElse(0f);
				}
				burrow_threat += Game.get_unit_type_data().get(enemy.type()).getFoodRequired().orElse(0f);;
			}
		}
		
		if (burrow_threat > 0 && !u.is_burrowed() && u.health() <= 26) {
			if (Game.has_upgrade(Upgrades.BURROW) && u.type() == Units.ZERG_DRONE) {
				u.use_ability(Abilities.BURROW_DOWN);
				return;
			}
		}
		
		if (threat > 5 && !u.is_burrowed()) {
			if (Game.has_upgrade(Upgrades.BURROW) && u.type() == Units.ZERG_DRONE) {
				u.use_ability(Abilities.BURROW_DOWN);
				return;
			}
			for (Base b : BaseManager.bases) {
				if (b.has_friendly_command_structure() && ThreatManager.is_safe(b.location) && b.minerals.size() > 0) {
					u.use_ability(Abilities.HARVEST_GATHER, b.minerals.get(0));
				}
			}
		}
		
		if (threat == 0 && u.is_burrowed()) {
			u.use_ability(Abilities.BURROW_UP);
			return;
		}
		
	}
	
	public static boolean can_build(HjaxUnit u) {	
		for (Base b : BaseManager.bases) {
			if (u == b.walking_drone) return false;
		}
		try {
			return !(Scouting.scout == u || Scouting.patrol_scout == u) && (u.orders().size() == 0 || (u.orders().get(0).getTargetedUnitTag().isPresent() && u.orders().get(0).getAbility() == Abilities.HARVEST_GATHER && GameInfoCache.get_unit(u.orders().get(0).getTargetedUnitTag().get()).minerals() > 0));
		} catch (Exception e) {
			return false;
		}
		
	}
}
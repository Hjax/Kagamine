package com.hjax.kagamine.unitcontrollers.zerg;

import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.army.UnitMovementManager;
import com.hjax.kagamine.army.ThreatManager;
import com.hjax.kagamine.build.Composition;
import com.hjax.kagamine.economy.Base;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.game.HjaxUnit;
import com.hjax.kagamine.knowledge.Wisdom;
import com.hjax.kagamine.unitcontrollers.GenericUnit;

public class Queen {
	public static void on_frame(HjaxUnit u) {
		
		if (u.energy() > 50) {
			for (HjaxUnit a: GameInfoCache.get_units(Alliance.SELF)) {
				if (a.distance(u) <= 7) {
					if (a.health_max() - a.health() >= 125) {
						u.use_ability(Abilities.EFFECT_TRANSFUSION, a);
					}
				}
			}
		}
		
		if (UnitMovementManager.assignments.containsKey(u.tag())) { 
			GenericUnit.on_frame(u, false);
			return;
		}
		
		int tumors = GameInfoCache.count_friendly(Units.ZERG_CREEP_TUMOR) + GameInfoCache.count_friendly(Units.ZERG_CREEP_TUMOR_BURROWED) + GameInfoCache.count_friendly(Units.ZERG_CREEP_TUMOR_QUEEN);
		boolean inject = false;
		
		
		if (tumors != 0 || GameInfoCache.count_friendly(Units.ZERG_QUEEN) > 3 || GameInfoCache.count_friendly(Units.ZERG_HATCHERY) < 2 || Wisdom.all_in_detected() || Wisdom.proxy_detected()) {
			for (Base b : BaseManager.bases) {
				if (GameInfoCache.count_friendly(Units.ZERG_LARVA) < BaseManager.base_count() * 3) {
					if (b.has_queen() && b.queen == u && b.has_friendly_command_structure() && b.command_structure.done()) {
						inject = true;
						if (u.energy() >= 25) {
							u.use_ability(Abilities.EFFECT_INJECT_LARVA, b.command_structure);
						} if (u.distance(b) > 8) {
							u.move(b.location);
						}
					}
				}
			}
		}
		
		if (u.ability() == Abilities.ATTACK || u.ability() == Abilities.ATTACK_ATTACK) {
			if (!u.orders().get(0).getTargetedUnitTag().isPresent()) {
				if (!UnitMovementManager.assignments.containsKey(u.tag())) { 
					u.stop();
					return;
				}
			}
		}
		
		if (inject) return;
		
		if (tumors > 4 && Composition.full_comp().contains(Units.ZERG_QUEEN) && !inject) {
			GenericUnit.on_frame(u, true);
			return;
		}

		if (tumors == 0 || GameInfoCache.count_enemy(Units.TERRAN_REAPER) < 4 || Game.army_supply() > 30) {
			if (!Wisdom.cannon_rush() && (ThreatManager.attacking_supply() < GameInfoCache.attacking_army_supply())) {
				if (u.idle() && ((u.energy() >= 25 && GameInfoCache.count_friendly(Units.ZERG_CREEP_TUMOR) < 25) || u.energy() >= 75)) {
					Point2d p = Creep.get_creep_point();
					if (p != null) {
						u.use_ability(Abilities.BUILD_CREEP_TUMOR, p);
						return;
					}
				}
			} 
		}
		
		if (u.idle() || u.ability() == Abilities.ATTACK || u.ability() == Abilities.ATTACK_ATTACK) {
			GenericUnit.on_frame(u, false);
		}
	}
	
	public static Base get_base(HjaxUnit queen) {
		for (Base b : BaseManager.bases) {
			if (b.has_queen() && b.queen == queen && b.has_friendly_command_structure() && b.command_structure.done()) {
				return b;
			}
		}
		return null;
	}
}

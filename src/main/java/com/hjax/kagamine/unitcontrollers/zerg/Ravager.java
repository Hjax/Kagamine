package com.hjax.kagamine.unitcontrollers.zerg;

import java.util.HashMap;
import java.util.Map;

import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.observation.AvailableAbility;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.Constants;
import com.hjax.kagamine.Utilities;
import com.hjax.kagamine.Vector2d;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.game.HjaxUnit;
import com.hjax.kagamine.knowledge.Scouting;
import com.hjax.kagamine.unitcontrollers.GenericUnit;

public class Ravager {
	
	public static Map<Point2d, Long> ff_biles = new HashMap<>();
	
	static UnitType[] bile_targets = {	Units.NEUTRAL_FORCE_FIELD, Units.PROTOSS_WARP_PRISM_PHASING, Units.TERRAN_SIEGE_TANK_SIEGED, Units.PROTOSS_PHOTON_CANNON, Units.ZERG_SPINE_CRAWLER, Units.TERRAN_BUNKER};
	
	public static void on_frame(HjaxUnit u2) {
		
		HjaxUnit best = null;
		
		for (UnitType target_type : bile_targets) {
			for (HjaxUnit u : GameInfoCache.get_units(target_type)) {
				if (u.distance(u2) < 9) {
					if (!ff_biles.containsKey(u.location()) || ff_biles.get(u.location()) < Game.get_frame() - (3 * Constants.FPS)) {
						best = u;
					}
				}
			}
			if (best != null) break;
		}
		
		if (best != null) {
			for (AvailableAbility ab : Game.availible_abilities(u2).getAbilities()) {
				if (ab.getAbility() == Abilities.EFFECT_CORROSIVE_BILE) {
					
					if (best.type() == Units.NEUTRAL_FORCE_FIELD) {
						ff_biles.put(best.location(), Game.get_frame());
					}
					
					u2.use_ability(Abilities.EFFECT_CORROSIVE_BILE, best.location());
					return;
				}
			}
			if (best.type() == Units.PROTOSS_PHOTON_CANNON || best.type() == Units.TERRAN_BUNKER || best.type() == Units.ZERG_SPINE_CRAWLER) {
				if (best.distance(BaseManager.main_base().location) < best.distance(Scouting.closest_enemy_spawn())) {
					Vector2d diff = Utilities.direction_to(Vector2d.of(best.location()), Vector2d.of(u2.location()));
					u2.move(Point2d.of(best.location().getX() + diff.x * 15, best.location().getY() + diff.y * 15));
					return;
				}
			}
		}
		if (u2.ability() != Abilities.EFFECT_CORROSIVE_BILE) GenericUnit.on_frame(u2, true);
	}
}

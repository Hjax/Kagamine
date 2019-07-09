package com.hjax.kagamine.unitcontrollers.protoss;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Upgrades;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Tag;
import com.hjax.kagamine.Constants;
import com.hjax.kagamine.Utilities;
import com.hjax.kagamine.Vector2d;
import com.hjax.kagamine.army.EnemySquadManager;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.game.HjaxUnit;
import com.hjax.kagamine.unitcontrollers.GenericUnit;

public class Stalker {
	
	private static Map<Tag, Long> last_blink_frame = new HashMap<>();
	
	public static void on_frame(HjaxUnit u) {
		if (Game.has_upgrade(Upgrades.BLINK_TECH) && Game.get_frame() - last_blink_frame.getOrDefault(u.tag(), (long) 0) > 10 * Constants.FPS) {
			if (u.shields() < 1) {
				List<HjaxUnit> result = new ArrayList<>();
				for (HjaxUnit enemy : GameInfoCache.get_units(Alliance.ENEMY)) {
					if (Game.is_combat(enemy.type()) && enemy.distance(u) < 12) {
						result.add(enemy);
					}
				}
				if (result.size() > 0) {
					last_blink_frame.put(u.tag(), Game.get_frame());
					u.use_ability(Abilities.EFFECT_BLINK, Vector2d.of(u.location()).add(Utilities.direction_to(Vector2d.of(u.location()), Vector2d.of(EnemySquadManager.average_point(result))).scale(-8)).toPoint2d());
					return;
				}
			}
		}
		
		GenericUnit.on_frame(u, true);
	}
}

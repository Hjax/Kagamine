package com.hjax.kagamine.army;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Tag;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;

public class BanelingAvoidance {
	public static Map<Tag, Tag> banelingAssignments = new HashMap<>();
	public static void on_frame() {
		
		
		List<Tag> to_remove = new ArrayList<>();
		for (Tag t: banelingAssignments.keySet()) {
			if (Game.get_unit(t) == null) {
				to_remove.add(t);
			}
		}
		for (Tag t: banelingAssignments.keySet()) {
			if (Game.get_unit(banelingAssignments.get(t)) == null) {
				to_remove.add(t);
			}
		}
		for (Tag t: to_remove) {
			banelingAssignments.remove(t);
		}
		
		for (UnitInPool enemyBane: GameInfoCache.get_units(Alliance.ENEMY, Units.ZERG_BANELING)) {
			if (Collections.frequency(banelingAssignments.values(), enemyBane.getTag()) < 2) {
				for (UnitInPool allyLing : GameInfoCache.get_units(Alliance.SELF, Units.ZERG_ZERGLING)) {
					if (banelingAssignments.containsKey(allyLing.getTag())) continue;
					if (allyLing.unit().getPosition().toPoint2d().distance(enemyBane.unit().getPosition().toPoint2d()) < 10) {
						banelingAssignments.put(allyLing.getTag(), enemyBane.getTag());
						break;
					}
				}
			}
		}
	}
}

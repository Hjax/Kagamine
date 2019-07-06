package com.hjax.kagamine.army;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Tag;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.game.HjaxUnit;

public class BanelingAvoidance {
	public static Map<Tag, Tag> banelingAssignments = new HashMap<>();
	public static void on_frame() {
		
		
		List<Tag> to_remove = new ArrayList<>();
		for (Tag t: banelingAssignments.keySet()) {
			if (GameInfoCache.get_unit(t) == null) {
				to_remove.add(t);
			}
		}
		for (Tag t: banelingAssignments.keySet()) {
			if (GameInfoCache.get_unit(banelingAssignments.get(t)) == null) {
				to_remove.add(t);
			}
		}
		for (Tag t: to_remove) {
			banelingAssignments.remove(t);
		}
		
		for (HjaxUnit enemyBane: GameInfoCache.get_units(Alliance.ENEMY, Units.ZERG_BANELING)) {
			if (Collections.frequency(banelingAssignments.values(), enemyBane.tag()) < 2) {
				for (HjaxUnit allyLing : GameInfoCache.get_units(Alliance.SELF, Units.ZERG_ZERGLING)) {
					if (banelingAssignments.containsKey(allyLing.tag())) continue;
					if (allyLing.distance(enemyBane) < 10) {
						banelingAssignments.put(allyLing.tag(), enemyBane.tag());
						break;
					}
				}
			}
		}
	}
}

package com.hjax.kagamine.army;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.unitcontrollers.*;

public class UnitManager {
	public static void on_frame() {
		for (UnitInPool u: GameInfoCache.get_units(Alliance.SELF)) {
			if (u.unit().getType() == Units.ZERG_QUEEN) {
				Queen.on_frame(u);
			} else if (u.unit().getType() == Units.ZERG_DRONE) {
				Drone.on_frame(u);
			} else if (u.unit().getType() == Units.ZERG_ZERGLING) {
				Zergling.on_frame(u);
			} else if (u.unit().getType() == Units.ZERG_LARVA) {
				Larva.on_frame(u);
			} else if (u.unit().getType() == Units.ZERG_EXTRACTOR) {
				Extractor.on_frame(u);
			} else if (u.unit().getType() == Units.ZERG_OVERLORD) {
				Overlord.on_frame(u);
			} else if (u.unit().getType() == Units.ZERG_MUTALISK) {
				Mutalisk.on_frame(u);
			} else if (u.unit().getType() == Units.ZERG_RAVAGER) {
				Ravager.on_frame(u);
			} else if (u.unit().getType() == Units.ZERG_BANELING) {
				Baneling.on_frame(u);
			} else if (u.unit().getType() == Units.ZERG_EGG) {
			} else if (u.unit().getType() == Units.ZERG_CREEP_TUMOR) {
			} else if (u.unit().getType() == Units.ZERG_CREEP_TUMOR_QUEEN) {
			} else if (u.unit().getType() == Units.ZERG_CREEP_TUMOR_BURROWED) {
				Creep.on_frame(u);
			} else if (!Game.is_structure(u.unit().getType())) {
				GenericUnit.on_frame(u, true);
			}
		}
	}
}

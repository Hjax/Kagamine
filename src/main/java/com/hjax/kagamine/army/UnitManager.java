package com.hjax.kagamine.army;

import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.game.HjaxUnit;
import com.hjax.kagamine.unitcontrollers.*;
import com.hjax.kagamine.unitcontrollers.zerg.*;

public class UnitManager {
	public static void on_frame() {
		for (HjaxUnit u: GameInfoCache.get_units(Alliance.SELF)) {
			if (u.type() == Units.ZERG_QUEEN) {
				Queen.on_frame(u);
			} else if (u.is_worker()) {
				Worker.on_frame(u);
			} else if (u.type() == Units.ZERG_ZERGLING) {
				Zergling.on_frame(u);
			} else if (u.type() == Units.ZERG_LARVA) {
				Larva.on_frame(u);
			} else if (u.type() == Units.ZERG_LURKER_MP || u.type() == Units.ZERG_LURKER_MP_BURROWED) {
				Lurker.on_frame(u);
			} else if (u.is_gas()) {
				Extractor.on_frame(u);
			} else if (u.type() == Units.ZERG_OVERLORD) {
				Overlord.on_frame(u);
			} else if (u.type() == Units.ZERG_MUTALISK) {
				Mutalisk.on_frame(u);
			} else if (u.type() == Units.ZERG_INFESTOR) {
				Infestor.on_frame(u);
			} else if (u.type() == Units.ZERG_VIPER) {
				Viper.on_frame(u);
			} else if (u.type() == Units.ZERG_RAVAGER) {
				Ravager.on_frame(u);
			} else if (u.type() == Units.ZERG_EGG) {
			} else if (u.type() == Units.ZERG_CREEP_TUMOR) {
			} else if (u.type() == Units.ZERG_CREEP_TUMOR_QUEEN) {
			} else if (u.type() == Units.ZERG_BROODLING) {
			} else if (u.type() == Units.ZERG_CREEP_TUMOR_BURROWED) {
				Creep.on_frame(u);
			} else if (!Game.is_structure(u.type())) {
				GenericUnit.on_frame(u, true);
			}
		}
	}
}

package com.hjax.kagamine.unitcontrollers.zerg;

import java.util.ArrayList;
import java.util.List;

import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.game.HjaxUnit;

public class Larva {
	private static final List<HjaxUnit> larva = new ArrayList<>();
	private static int larva_index = 0;
	public static void start_frame() {
		larva_index = 0;
		larva.clear();
		larva.addAll(GameInfoCache.get_units(Alliance.SELF, Units.ZERG_LARVA));
	}
	public static boolean has_larva() {
		return larva_index < larva.size();
	}

    public static void produce_unit(UnitType type) {
    	if (has_larva() && type != Units.INVALID) {
    		larva.get(larva_index).use_ability(Game.get_unit_type_data().get(type).getAbility().get());
    		larva_index++;
    	}
	}
}

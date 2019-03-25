package com.hjax.kagamine.build;

import java.util.Arrays;
import java.util.List;

import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.game.Race;
import com.hjax.kagamine.game.Game;

public class Composition {
	public static List<UnitType> comp() {
		if (Game.get_opponent_race() == Race.TERRAN) {
			if (Game.army_supply() < 15) {
				return Arrays.asList(Units.ZERG_ZERGLING);
			}
			if (Game.army_supply() < 80) {
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_HYDRALISK);
			}
			return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_HYDRALISK, Units.ZERG_CORRUPTOR, Units.ZERG_BROODLORD);
		}
		return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_ROACH, Units.ZERG_HYDRALISK);
	}
}

package com.hjax.kagamine.build;

import java.util.Arrays;
import java.util.List;

import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.game.Race;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.knowledge.EnemyModel;

public class Composition {
	public static List<UnitType> comp() {
		if (Game.get_opponent_race() == Race.TERRAN) {
			if (Game.army_supply() < 10 || GameInfoCache.count_friendly(Units.ZERG_DRONE) < 40) {
				return Arrays.asList(Units.ZERG_ZERGLING);
			}
			if (Game.army_supply() < 80) {
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_HYDRALISK);
			}
			return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_HYDRALISK, Units.ZERG_CORRUPTOR, Units.ZERG_BROODLORD);
		}
		if (Game.get_opponent_race() == Race.ZERG) {
			if (Game.army_supply() < 20) {
				return Arrays.asList(Units.ZERG_ZERGLING);
			} else {
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_ROACH, Units.ZERG_HYDRALISK);
			}
		}
		if (Game.get_opponent_race() == Race.PROTOSS) {
			if (EnemyModel.counts.getOrDefault(Units.PROTOSS_CARRIER, 0) > 0) {
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_HYDRALISK);
			}
			if (Game.army_supply() < 15) {
				return Arrays.asList(Units.ZERG_ZERGLING);
			}
			if (Game.army_supply() < 80) {
				if (EnemyModel.counts.getOrDefault(Units.PROTOSS_COLOSSUS, 0) > 0) {
					return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_ROACH, Units.ZERG_HYDRALISK, Units.ZERG_CORRUPTOR);
				}
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_ROACH, Units.ZERG_HYDRALISK);
			}
			return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_ROACH, Units.ZERG_HYDRALISK, Units.ZERG_CORRUPTOR, Units.ZERG_BROODLORD);
		}
		return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_ROACH, Units.ZERG_HYDRALISK);
	}
}

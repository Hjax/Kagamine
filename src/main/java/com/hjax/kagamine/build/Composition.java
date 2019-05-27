package com.hjax.kagamine.build;

import java.util.Arrays;
import java.util.List;

import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.game.Race;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.knowledge.EnemyModel;
import com.hjax.kagamine.knowledge.Wisdom;

public class Composition {
	public static List<UnitType> comp() {
		if (GameInfoCache.get_opponent_race() == Race.TERRAN) {
			if (GameInfoCache.count_friendly(Units.ZERG_DRONE) > 50 && GameInfoCache.count_friendly(Units.ZERG_GREATER_SPIRE) > 0) {
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_HYDRALISK, Units.ZERG_CORRUPTOR, Units.ZERG_BROODLORD);
			}
			if (BuildPlanner.is_all_in && (Wisdom.cannon_rush() || Wisdom.proxy_detected())) {
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_ROACH, Units.ZERG_RAVAGER);
			}
			if (Game.army_supply() < 10 || GameInfoCache.count_friendly(Units.ZERG_DRONE) < 35) {
				return Arrays.asList(Units.ZERG_ZERGLING);
			}
			if (Game.army_supply() < 80) {
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_HYDRALISK);
			}
			return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_HYDRALISK, Units.ZERG_CORRUPTOR, Units.ZERG_BROODLORD);
		}
		if (GameInfoCache.get_opponent_race() == Race.ZERG) {
			if (Game.army_supply() < 30) {
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_ROACH);
			}
			return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_ROACH, Units.ZERG_HYDRALISK, Units.ZERG_LURKER_MP);
		}
		if (GameInfoCache.get_opponent_race() == Race.PROTOSS) {
			if (BuildPlanner.is_all_in && (Wisdom.cannon_rush() || Wisdom.proxy_detected())) {
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_ROACH, Units.ZERG_RAVAGER);
			}
			if (EnemyModel.counts.getOrDefault(Units.PROTOSS_CARRIER, 0) > 0) {
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_HYDRALISK);
			}
			if (EnemyModel.counts.getOrDefault(Units.PROTOSS_TEMPEST, 0) > 0) {
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_HYDRALISK, Units.ZERG_CORRUPTOR);
			}
			if (Game.army_supply() < 15) {
				return Arrays.asList(Units.ZERG_ZERGLING);
			}
			if (Game.army_supply() < 80) {
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_ROACH, Units.ZERG_HYDRALISK, Units.ZERG_LURKER_MP);
			}
			return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_ROACH, Units.ZERG_HYDRALISK, Units.ZERG_LURKER_MP, Units.ZERG_CORRUPTOR, Units.ZERG_BROODLORD);
		}
		return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_ROACH, Units.ZERG_HYDRALISK);
	}
}

package com.hjax.kagamine.build;

import java.util.Arrays;
import java.util.List;

import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.game.Race;
import com.hjax.kagamine.enemymodel.EnemyModel;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.knowledge.Wisdom;

public class Composition {
	public static List<UnitType> comp() {
		if (GameInfoCache.get_opponent_race() == Race.TERRAN) {
			if (GameInfoCache.count_friendly(Units.ZERG_DRONE) > 50 && GameInfoCache.count_friendly(Units.ZERG_GREATER_SPIRE) > 0) {
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_INFESTOR, Units.ZERG_CORRUPTOR, Units.ZERG_BROODLORD);
			}
			if (EnemyModel.enemy_floated()) {
				return Arrays.asList(Units.ZERG_MUTALISK);
			}
			if (BuildPlanner.is_all_in && (Wisdom.cannon_rush() || Wisdom.proxy_detected())) {
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_ROACH, Units.ZERG_RAVAGER);
			}
			if (Game.army_supply() < 10 || GameInfoCache.count_friendly(Units.ZERG_DRONE) < 35) {
				return Arrays.asList(Units.ZERG_ZERGLING);
			}
			if (EnemyModel.counts.getOrDefault(Units.TERRAN_BANSHEE, 0) > 2) {
				return Arrays.asList(Units.ZERG_HYDRALISK);
			}
			if (EnemyModel.counts.getOrDefault(Units.TERRAN_BATTLECRUISER, 0) >= 2 && GameInfoCache.count_friendly(Units.ZERG_SPIRE) > 0) {
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_CORRUPTOR);
			}
			if (EnemyModel.counts.getOrDefault(Units.TERRAN_BATTLECRUISER, 0) >= 2) {
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_HYDRALISK, Units.ZERG_CORRUPTOR);
			}
			if (Game.army_supply() < 80) {
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_HYDRALISK);
			}
			return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_HYDRALISK,  Units.ZERG_INFESTOR, Units.ZERG_CORRUPTOR, Units.ZERG_BROODLORD);
		}
		if (GameInfoCache.get_opponent_race() == Race.ZERG) {
			if (Wisdom.cannon_rush() || Wisdom.proxy_detected()) {
				return Arrays.asList(Units.ZERG_ZERGLING);
			}
			if (Game.army_supply() < 30) {
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_ROACH);
			}
			if (Wisdom.cannon_rush() || Wisdom.proxy_detected()) {
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_ROACH, Units.ZERG_RAVAGER);
			}
			if (EnemyModel.counts.getOrDefault(Units.ZERG_MUTALISK, 0) > 0) {
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_HYDRALISK);
			}
			return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_ROACH, Units.ZERG_RAVAGER);
		}
		if (GameInfoCache.get_opponent_race() == Race.PROTOSS) {
			if (GameInfoCache.count_friendly(Units.ZERG_DRONE) > 50 && GameInfoCache.count_friendly(Units.ZERG_GREATER_SPIRE) > 0) {
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_INFESTOR, Units.ZERG_CORRUPTOR, Units.ZERG_BROODLORD);
			}
			if (BuildPlanner.is_all_in && (Wisdom.cannon_rush() || Wisdom.proxy_detected())) {
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_ROACH, Units.ZERG_RAVAGER);
			}
			if (EnemyModel.counts.getOrDefault(Units.PROTOSS_CARRIER, 0) > 0) {
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_HYDRALISK);
			}
			if (EnemyModel.counts.getOrDefault(Units.PROTOSS_VOIDRAY, 0) > 0) {
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_HYDRALISK);
			}
			if (EnemyModel.counts.getOrDefault(Units.PROTOSS_TEMPEST, 0) > 2) {
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_CORRUPTOR);
			}
			if (Game.army_supply() < 15) {
				return Arrays.asList(Units.ZERG_ZERGLING);
			}
			if (Wisdom.all_in_detected() && Game.army_supply() < 50) {
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_ROACH, Units.ZERG_QUEEN);
			}
			if (Game.army_supply() < 60) {
				return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_ROACH, Units.ZERG_HYDRALISK);
			}
			return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_ROACH, Units.ZERG_HYDRALISK, Units.ZERG_VIPER);
			//return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_HYDRALISK, Units.ZERG_LURKER_MP, Units.ZERG_CORRUPTOR, Units.ZERG_BROODLORD);
		}
		return Arrays.asList(Units.ZERG_ZERGLING, Units.ZERG_ROACH, Units.ZERG_HYDRALISK);
	}
}

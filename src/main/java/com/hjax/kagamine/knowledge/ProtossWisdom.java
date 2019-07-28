package com.hjax.kagamine.knowledge;

import com.hjax.kagamine.build.Build;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.economy.EconomyManager;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.game.RaceInterface;

class ProtossWisdom {

	public static boolean should_build_workers() {
		return GameInfoCache.count(RaceInterface.get_race_worker()) < EconomyManager.total_minerals() + BaseManager.active_gases() * 3;
	}
	
	public static boolean should_expand() {
		if (Game.army_supply() < 20 * (BaseManager.base_count() - 1)) return false;
		return GameInfoCache.count(RaceInterface.get_race_worker()) + 6 >= EconomyManager.total_minerals() + BaseManager.active_gases() * 3;
	}
	
	public static boolean should_build_army() {
		return true;
	}
}

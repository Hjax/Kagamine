package com.hjax.kagamine.knowledge;

import com.hjax.kagamine.build.Build;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.economy.EconomyManager;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.game.RaceInterface;

public class ProtossWisdom {

	public static boolean should_build_workers() {
		if (GameInfoCache.count(RaceInterface.get_race_worker()) > Build.ideal_workers) return false;
		return GameInfoCache.count(RaceInterface.get_race_worker()) < EconomyManager.total_minerals() + BaseManager.active_gases() * 3;
	}
	
	public static boolean should_expand() {
		if (Game.army_supply() < 20 * (BaseManager.base_count() - 1)) return false;
		if (BaseManager.base_count() >= Build.ideal_bases && Build.ideal_bases > 0) return false;
		return GameInfoCache.count(RaceInterface.get_race_worker()) + 6 >= EconomyManager.total_minerals() + BaseManager.active_gases() * 3;
	}
	
	public static boolean should_build_army() {
		return true;
	}
}

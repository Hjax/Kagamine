package com.hjax.kagamine.knowledge;

import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.economy.EconomyManager;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.game.RaceInterface;

public class ProtossWisdom {

	public static boolean should_build_workers() {
		return GameInfoCache.count(RaceInterface.get_race_worker()) < EconomyManager.total_minerals() * 2 + BaseManager.active_gases() * 3;
	}
	
	public static boolean should_expand() {
		return GameInfoCache.count(RaceInterface.get_race_worker()) >= EconomyManager.total_minerals() * 2 + BaseManager.active_gases() * 3;
	}
	
	public static boolean should_build_army() {
		return true;
	}
}

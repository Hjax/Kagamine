package com.hjax.kagamine;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;

public class ArmyManager {
	public static Point2d target;
	static {
		target = Game.get_game_info().findRandomLocation();
	}
	
	public static void start_frame() {
		
	}
	
	public static void on_frame() {
		if (Game.army_supply() < 2) {
			if (!Wisdom.cannon_rush() && !Wisdom.proxy_detected() && !ThreatManager.under_attack()) {
				for (UnitInPool unit: GameInfoCache.get_units(Alliance.ENEMY)) {
					if (Game.is_worker(unit.unit().getType())) {
						
					}
				}
			}
		}
	}
	
	public static void end_frame() {
		
	}
}

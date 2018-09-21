package com.hjax.kagamine;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;

public class ArmyManager {
	public static Point2d target;
	public static boolean has_target = false;
	static {
		target = Game.get_game_info().findRandomLocation();
	}
	
	public static void start_frame() {
		
	}
	
	public static void on_frame() {
		outer: for (UnitInPool u: GameInfoCache.get_units(Alliance.SELF)) {
			if (target.distance(u.unit().getPosition().toPoint2d()) < 4) {
				for (UnitInPool e: GameInfoCache.get_units(Alliance.ENEMY)) {
					if (e.unit().getType() != Units.PROTOSS_ADEPT_PHASE_SHIFT) {
						if (!e.unit().getFlying().orElse(false) || GameInfoCache.count_friendly(Units.ZERG_MUTALISK) > 0) {
							if (u.unit().getPosition().distance(e.unit().getPosition()) < 4) {
								break outer;
							}
						}
					}
				}
				has_target = false;
				break outer;
			}
		}
		if (!has_target) {
			for (UnitInPool e: GameInfoCache.get_units(Alliance.ENEMY)) {
				if (e.unit().getType() != Units.PROTOSS_ADEPT_PHASE_SHIFT) {
					if (!e.unit().getFlying().orElse(false) || GameInfoCache.count_friendly(Units.ZERG_MUTALISK) > 0) {
						target = e.unit().getPosition().toPoint2d();
						has_target = true;
						break;
					}
				}
			}
		}
		
	}
	
	public static void end_frame() {
		
	}
}

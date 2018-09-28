package com.hjax.kagamine;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;

public class ArmyManager {
	public static Point2d target;
	public static boolean has_target = false;
	public static Point2d defend = null;
	static {
		target = Scouting.closest_enemy_spawn();
		has_target = true;
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
		defend = null;
		for (UnitInPool e: GameInfoCache.get_units(Alliance.ENEMY)) {
			for (Base b: BaseManager.bases) {
				if (b.has_command_structure() || b.location.distance(BaseManager.get_next_base().location) < 5 && !Wisdom.confused()) {
					if (e.unit().getPosition().toPoint2d().distance(b.location) < 20) {
						defend = e.unit().getPosition().toPoint2d();
					}
				}
			}
		}

	}
	
	public static void end_frame() {
		
	}
}

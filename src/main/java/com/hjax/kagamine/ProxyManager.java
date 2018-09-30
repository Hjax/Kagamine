package com.hjax.kagamine;

import java.util.HashSet;
import java.util.Set;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.unit.Alliance;

public class ProxyManager {
	public static Set<Base> proxy_bases = new HashSet<>();
	public static void on_frame() {
		proxy_bases = new HashSet<>();
		for (UnitInPool u: GameInfoCache.get_units(Alliance.ENEMY)) {
			if (Game.is_structure(u.unit().getType())) {
				if (u.unit().getPosition().toPoint2d().distance(BaseManager.main_base().location) < u.unit().getPosition().toPoint2d().distance(Scouting.closest_enemy_spawn())) {
					Base best = null;
					for (Base b: BaseManager.bases) {
						if (best == null || best.location.distance(u.unit().getPosition().toPoint2d()) > b.location.distance(u.unit().getPosition().toPoint2d())) {
							best = b;
						}
					}
					proxy_bases.add(best);
				}
			}
		}
	}
	
	public static boolean can_take_base(Base b) {
		return !proxy_bases.contains(b);
	}
	

	
}

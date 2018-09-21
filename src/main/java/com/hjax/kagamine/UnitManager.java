package com.hjax.kagamine;

import java.util.HashMap;
import java.util.Map;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.UnitControllers.*;

public class UnitManager {
	public interface Function  {
		   public void apply(UnitInPool u);
		}
	static Map<UnitType, Function> controllers = new HashMap<>();
	static {
		controllers.put(Units.ZERG_QUEEN, (UnitInPool u) -> Queen.on_frame(u));
		controllers.put(Units.ZERG_DRONE, (UnitInPool u) -> Drone.on_frame(u));
		controllers.put(Units.ZERG_LARVA, (UnitInPool u) -> Larva.on_frame(u));
		controllers.put(Units.ZERG_EXTRACTOR, (UnitInPool u) -> Extractor.on_frame(u));
		controllers.put(Units.INVALID, (UnitInPool u) -> GenericUnit.on_frame(u));
	}
	public static void on_frame() {
		for (UnitInPool u: GameInfoCache.get_units(Alliance.SELF)) {
			if (controllers.containsKey(u.unit().getType())) {
				controllers.get(u.unit().getType()).apply(u);
			} else if (!Game.is_structure(u.unit().getType())){
				controllers.get(Units.INVALID).apply(u);
			}
		}
	}
}

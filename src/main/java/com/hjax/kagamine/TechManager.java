package com.hjax.kagamine;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Upgrade;
import com.github.ocraft.s2client.protocol.unit.Alliance;

public class TechManager {
	public static void on_frame() {
		for (Upgrade u : Build.upgrades) {
			if (Game.can_afford(u) && !(Game.has_upgrade(u))) {
				for (UnitType t: Game.get_unit_type_data().keySet()) {
					if (t.getAbilities().contains(Game.get_upgrade_data().get(u).getAbility().orElse(Abilities.INVALID))) {
						for (UnitInPool up: GameInfoCache.get_units(Alliance.SELF, t)) {
							if (up.unit().getOrders().size() == 0) {
								Game.purchase(u);
								Game.unit_command(up, Game.get_upgrade_data().get(u).getAbility().get());
								return;
							}
						}
					}
				}
			}
		}
	}
}

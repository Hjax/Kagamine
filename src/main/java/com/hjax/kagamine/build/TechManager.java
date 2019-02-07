package com.hjax.kagamine.build;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Upgrade;
import com.github.ocraft.s2client.protocol.observation.AvailableAbility;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;

public class TechManager {
	public static void on_frame() {
		outer: for (Upgrade u : Build.upgrades) {
			if (!(Game.has_upgrade(u)) && !GameInfoCache.is_researching(u)) {
				for (UnitType t: Game.get_unit_type_data().keySet()) {
					if (t.getAbilities().contains(Game.get_upgrade_data().get(u).getAbility().orElse(Abilities.INVALID)) || t.getAbilities().contains(Game.get_ability_data().get(Game.get_upgrade_data().get(u).getAbility().orElse(Abilities.INVALID)).getRemapsToAbility().orElse(Abilities.INVALID))) {
						for (UnitInPool up: GameInfoCache.get_units(Alliance.SELF, t)) {
							if (up.unit().getOrders().size() == 0 && up.unit().getBuildProgress() > 0.999) {
								for (AvailableAbility aa: Game.availible_abilities(up).getAbilities()) {
									if (aa.getAbility() == Game.get_ability_data().get(Game.get_upgrade_data().get(u).getAbility().get()).getRemapsToAbility().orElse(Game.get_upgrade_data().get(u).getAbility().get())) {
										if (Game.can_afford(u)) {
											Game.unit_command(up, Game.get_upgrade_data().get(u).getAbility().get());
										}
										Game.purchase(u);
										continue outer;
									}
								}

							}
						}
					}
				}
			}
		}
	}
}

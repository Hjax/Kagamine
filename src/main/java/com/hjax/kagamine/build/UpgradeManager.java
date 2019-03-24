package com.hjax.kagamine.build;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.data.Upgrade;
import com.github.ocraft.s2client.protocol.data.Upgrades;
import com.github.ocraft.s2client.protocol.observation.AvailableAbility;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;

public class UpgradeManager {
	
	public static Map<UnitType, List<Upgrade>> upgrades = new HashMap<>();
	static {
		upgrades.put(Units.ZERG_ZERGLING, Arrays.asList(Upgrades.ZERG_MELEE_WEAPONS_LEVEL1, Upgrades.ZERG_MELEE_WEAPONS_LEVEL2, Upgrades.ZERG_MELEE_WEAPONS_LEVEL3, Upgrades.ZERG_GROUND_ARMORS_LEVEL1, Upgrades.ZERG_GROUND_ARMORS_LEVEL2, Upgrades.ZERG_GROUND_ARMORS_LEVEL3, Upgrades.ZERGLING_ATTACK_SPEED, Upgrades.ZERGLING_MOVEMENT_SPEED));
		upgrades.put(Units.ZERG_BANELING, Arrays.asList(Upgrades.ZERG_MELEE_WEAPONS_LEVEL1, Upgrades.ZERG_MELEE_WEAPONS_LEVEL2, Upgrades.ZERG_MELEE_WEAPONS_LEVEL3, Upgrades.ZERG_GROUND_ARMORS_LEVEL1, Upgrades.ZERG_GROUND_ARMORS_LEVEL2, Upgrades.ZERG_GROUND_ARMORS_LEVEL3, Upgrades.CENTRIFICAL_HOOKS));
		upgrades.put(Units.ZERG_ULTRALISK, Arrays.asList(Upgrades.ZERG_MELEE_WEAPONS_LEVEL1, Upgrades.ZERG_MELEE_WEAPONS_LEVEL2, Upgrades.ZERG_MELEE_WEAPONS_LEVEL3, Upgrades.ZERG_GROUND_ARMORS_LEVEL1, Upgrades.ZERG_GROUND_ARMORS_LEVEL2, Upgrades.ZERG_GROUND_ARMORS_LEVEL3, Upgrades.CHITINOUS_PLATING, Upgrades.ANABOLIC_SYNTHESIS));
		upgrades.put(Units.ZERG_ROACH, Arrays.asList(Upgrades.ZERG_MISSILE_WEAPONS_LEVEL1, Upgrades.ZERG_MISSILE_WEAPONS_LEVEL2, Upgrades.ZERG_MISSILE_WEAPONS_LEVEL3, Upgrades.ZERG_GROUND_ARMORS_LEVEL1, Upgrades.ZERG_GROUND_ARMORS_LEVEL2, Upgrades.ZERG_GROUND_ARMORS_LEVEL3, Upgrades.GLIALRE_CONSTITUTION, Upgrades.TUNNELING_CLAWS));
		upgrades.put(Units.ZERG_RAVAGER, Arrays.asList(Upgrades.ZERG_MISSILE_WEAPONS_LEVEL1, Upgrades.ZERG_MISSILE_WEAPONS_LEVEL2, Upgrades.ZERG_MISSILE_WEAPONS_LEVEL3, Upgrades.ZERG_GROUND_ARMORS_LEVEL1, Upgrades.ZERG_GROUND_ARMORS_LEVEL2, Upgrades.ZERG_GROUND_ARMORS_LEVEL3));
		upgrades.put(Units.ZERG_HYDRALISK, Arrays.asList(Upgrades.ZERG_MISSILE_WEAPONS_LEVEL1, Upgrades.ZERG_MISSILE_WEAPONS_LEVEL2, Upgrades.ZERG_MISSILE_WEAPONS_LEVEL3, Upgrades.ZERG_GROUND_ARMORS_LEVEL1, Upgrades.ZERG_GROUND_ARMORS_LEVEL2, Upgrades.ZERG_GROUND_ARMORS_LEVEL3));
		upgrades.put(Units.ZERG_LURKER_MP, Arrays.asList(Upgrades.ZERG_MISSILE_WEAPONS_LEVEL1, Upgrades.ZERG_MISSILE_WEAPONS_LEVEL2, Upgrades.ZERG_MISSILE_WEAPONS_LEVEL3, Upgrades.ZERG_GROUND_ARMORS_LEVEL1, Upgrades.ZERG_GROUND_ARMORS_LEVEL2, Upgrades.ZERG_GROUND_ARMORS_LEVEL3));
		upgrades.put(Units.ZERG_QUEEN, Arrays.asList(Upgrades.ZERG_MISSILE_WEAPONS_LEVEL1, Upgrades.ZERG_MISSILE_WEAPONS_LEVEL2, Upgrades.ZERG_MISSILE_WEAPONS_LEVEL3, Upgrades.ZERG_GROUND_ARMORS_LEVEL1, Upgrades.ZERG_GROUND_ARMORS_LEVEL2, Upgrades.ZERG_GROUND_ARMORS_LEVEL3));
		
		upgrades.put(Units.ZERG_MUTALISK, Arrays.asList(Upgrades.ZERG_FLYER_WEAPONS_LEVEL1, Upgrades.ZERG_FLYER_WEAPONS_LEVEL2, Upgrades.ZERG_FLYER_WEAPONS_LEVEL3, Upgrades.ZERG_FLYER_ARMORS_LEVEL1, Upgrades.ZERG_FLYER_ARMORS_LEVEL2, Upgrades.ZERG_FLYER_ARMORS_LEVEL3));
		upgrades.put(Units.ZERG_CORRUPTOR, Arrays.asList(Upgrades.ZERG_FLYER_WEAPONS_LEVEL1, Upgrades.ZERG_FLYER_WEAPONS_LEVEL2, Upgrades.ZERG_FLYER_WEAPONS_LEVEL3, Upgrades.ZERG_FLYER_ARMORS_LEVEL1, Upgrades.ZERG_FLYER_ARMORS_LEVEL2, Upgrades.ZERG_FLYER_ARMORS_LEVEL3));
		upgrades.put(Units.ZERG_BROODLORD, Arrays.asList(Upgrades.ZERG_FLYER_WEAPONS_LEVEL1, Upgrades.ZERG_FLYER_WEAPONS_LEVEL2, Upgrades.ZERG_FLYER_WEAPONS_LEVEL3, Upgrades.ZERG_FLYER_ARMORS_LEVEL1, Upgrades.ZERG_FLYER_ARMORS_LEVEL2, Upgrades.ZERG_FLYER_ARMORS_LEVEL3));
		
	}
	
	
	public static void on_frame() {
		for (UnitType ut : Build.composition) {
			outer: for (Upgrade u : upgrades.getOrDefault(ut, Arrays.asList())) {
				if (!(Game.has_upgrade(u)) && !GameInfoCache.is_researching(u)) {
					for (UnitType t: Game.get_unit_type_data().keySet()) {
						if (t.getAbilities().contains(Game.get_upgrade_data().get(u).getAbility().orElse(Abilities.INVALID)) || t.getAbilities().contains(Game.get_ability_data().get(Game.get_upgrade_data().get(u).getAbility().orElse(Abilities.INVALID)).getRemapsToAbility().orElse(Abilities.INVALID))) {
							if ((t.equals(Units.ZERG_EVOLUTION_CHAMBER) || t.equals(Units.ZERG_SPIRE)) && BuildExecutor.count(t) < 2 && (BuildExecutor.count(Units.ZERG_DRONE) > 45)) {
								if (Game.can_afford(t)) {
									BaseManager.build(t);
								}
								Game.purchase(t);
								continue outer;
							}
							for (UnitInPool up: GameInfoCache.get_units(Alliance.SELF, t)) {
								if (up.unit().getOrders().size() == 0 && up.unit().getBuildProgress() > 0.999) {
									for (AvailableAbility aa: Game.availible_abilities(up, true).getAbilities()) {
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
}

package com.hjax.kagamine.build;

import java.util.ArrayList;
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
import com.github.ocraft.s2client.protocol.game.Race;
import com.github.ocraft.s2client.protocol.observation.AvailableAbility;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;

public class UpgradeManager {
	
	public static Map<UnitType, List<Upgrade>> upgrades = new HashMap<>();
	public static Map<Upgrade, List<UnitType>> upgraders = new HashMap<>();
	static {
		upgrades.put(Units.ZERG_ZERGLING, Arrays.asList(Upgrades.ZERG_MELEE_WEAPONS_LEVEL1, Upgrades.ZERG_MELEE_WEAPONS_LEVEL2, Upgrades.ZERG_MELEE_WEAPONS_LEVEL3, Upgrades.ZERG_GROUND_ARMORS_LEVEL1, Upgrades.ZERG_GROUND_ARMORS_LEVEL2, Upgrades.ZERG_GROUND_ARMORS_LEVEL3, Upgrades.ZERGLING_ATTACK_SPEED, Upgrades.ZERGLING_MOVEMENT_SPEED, Upgrades.OVERLORD_SPEED));
		upgrades.put(Units.ZERG_BANELING, Arrays.asList(Upgrades.ZERG_MELEE_WEAPONS_LEVEL1, Upgrades.ZERG_MELEE_WEAPONS_LEVEL2, Upgrades.ZERG_MELEE_WEAPONS_LEVEL3, Upgrades.ZERG_GROUND_ARMORS_LEVEL1, Upgrades.ZERG_GROUND_ARMORS_LEVEL2, Upgrades.ZERG_GROUND_ARMORS_LEVEL3, Upgrades.CENTRIFICAL_HOOKS));
		upgrades.put(Units.ZERG_ULTRALISK, Arrays.asList(Upgrades.ZERG_MELEE_WEAPONS_LEVEL1, Upgrades.ZERG_MELEE_WEAPONS_LEVEL2, Upgrades.ZERG_MELEE_WEAPONS_LEVEL3, Upgrades.ZERG_GROUND_ARMORS_LEVEL1, Upgrades.ZERG_GROUND_ARMORS_LEVEL2, Upgrades.ZERG_GROUND_ARMORS_LEVEL3, Upgrades.CHITINOUS_PLATING, Upgrades.ANABOLIC_SYNTHESIS));
		upgrades.put(Units.ZERG_ROACH, Arrays.asList(Upgrades.ZERG_MISSILE_WEAPONS_LEVEL1, Upgrades.ZERG_MISSILE_WEAPONS_LEVEL2, Upgrades.ZERG_MISSILE_WEAPONS_LEVEL3, Upgrades.ZERG_GROUND_ARMORS_LEVEL1, Upgrades.ZERG_GROUND_ARMORS_LEVEL2, Upgrades.ZERG_GROUND_ARMORS_LEVEL3, Upgrades.GLIALRE_CONSTITUTION, Upgrades.TUNNELING_CLAWS));
		upgrades.put(Units.ZERG_RAVAGER, Arrays.asList(Upgrades.ZERG_MISSILE_WEAPONS_LEVEL1, Upgrades.ZERG_MISSILE_WEAPONS_LEVEL2, Upgrades.ZERG_MISSILE_WEAPONS_LEVEL3, Upgrades.ZERG_GROUND_ARMORS_LEVEL1, Upgrades.ZERG_GROUND_ARMORS_LEVEL2, Upgrades.ZERG_GROUND_ARMORS_LEVEL3));
		upgrades.put(Units.ZERG_HYDRALISK, Arrays.asList(Upgrades.ZERG_MISSILE_WEAPONS_LEVEL1, Upgrades.ZERG_MISSILE_WEAPONS_LEVEL2, Upgrades.ZERG_MISSILE_WEAPONS_LEVEL3, Upgrades.ZERG_GROUND_ARMORS_LEVEL1, Upgrades.ZERG_GROUND_ARMORS_LEVEL2, Upgrades.ZERG_GROUND_ARMORS_LEVEL3, Upgrades.EVOLVE_GROOVED_SPINES, Upgrades.EVOLVE_MUSCULAR_AUGMENTS));
		upgrades.put(Units.ZERG_LURKER_MP, Arrays.asList(Upgrades.ZERG_MISSILE_WEAPONS_LEVEL1, Upgrades.ZERG_MISSILE_WEAPONS_LEVEL2, Upgrades.ZERG_MISSILE_WEAPONS_LEVEL3, Upgrades.ZERG_GROUND_ARMORS_LEVEL1, Upgrades.ZERG_GROUND_ARMORS_LEVEL2, Upgrades.ZERG_GROUND_ARMORS_LEVEL3, Upgrades.DIGGING_CLAWS));
		upgrades.put(Units.ZERG_QUEEN, Arrays.asList(Upgrades.ZERG_MISSILE_WEAPONS_LEVEL1, Upgrades.ZERG_MISSILE_WEAPONS_LEVEL2, Upgrades.ZERG_MISSILE_WEAPONS_LEVEL3, Upgrades.ZERG_GROUND_ARMORS_LEVEL1, Upgrades.ZERG_GROUND_ARMORS_LEVEL2, Upgrades.ZERG_GROUND_ARMORS_LEVEL3));
		
		upgrades.put(Units.ZERG_MUTALISK, Arrays.asList(Upgrades.ZERG_FLYER_WEAPONS_LEVEL1, Upgrades.ZERG_FLYER_WEAPONS_LEVEL2, Upgrades.ZERG_FLYER_WEAPONS_LEVEL3, Upgrades.ZERG_FLYER_ARMORS_LEVEL1, Upgrades.ZERG_FLYER_ARMORS_LEVEL2, Upgrades.ZERG_FLYER_ARMORS_LEVEL3));
		upgrades.put(Units.ZERG_CORRUPTOR, Arrays.asList(Upgrades.ZERG_FLYER_WEAPONS_LEVEL1, Upgrades.ZERG_FLYER_WEAPONS_LEVEL2, Upgrades.ZERG_FLYER_WEAPONS_LEVEL3, Upgrades.ZERG_FLYER_ARMORS_LEVEL1, Upgrades.ZERG_FLYER_ARMORS_LEVEL2, Upgrades.ZERG_FLYER_ARMORS_LEVEL3));
		upgrades.put(Units.ZERG_BROODLORD, Arrays.asList(Upgrades.ZERG_FLYER_WEAPONS_LEVEL1, Upgrades.ZERG_FLYER_WEAPONS_LEVEL2, Upgrades.ZERG_FLYER_WEAPONS_LEVEL3, Upgrades.ZERG_FLYER_ARMORS_LEVEL1, Upgrades.ZERG_FLYER_ARMORS_LEVEL2, Upgrades.ZERG_FLYER_ARMORS_LEVEL3));
		
	}
	
	public static void start_game() {
		for (UnitType ut : upgrades.keySet()) {
			for (Upgrade u : upgrades.getOrDefault(ut, Arrays.asList())) {
				for (UnitType t: Game.get_unit_type_data().keySet()) {
					if (t.getAbilities().contains(Game.get_upgrade_data().get(u).getAbility().orElse(Abilities.INVALID)) || t.getAbilities().contains(Game.get_ability_data().get(Game.get_upgrade_data().get(u).getAbility().orElse(Abilities.INVALID)).getRemapsToAbility().orElse(Abilities.INVALID))) {
						if (upgraders.containsKey(u)) {
							upgraders.get(u).add(t);
						} else {
							upgraders.put(u, new ArrayList<>(List.of(t)));
						}
					}
				}
			}
		}
		upgraders.put(Upgrades.DIGGING_CLAWS, new ArrayList<>(List.of(Units.ZERG_LURKER_DEN_MP)));
	}
	
	
	public static void on_frame() {
		for (UnitType ut : Composition.comp()) {
			outer: for (Upgrade u : upgrades.getOrDefault(ut, Arrays.asList())) {
				if (!(Game.has_upgrade(u)) && !GameInfoCache.is_researching(u)) {
					if (u.toString().toLowerCase().contains("melee") && !Game.has_upgrade(Upgrades.ZERG_MISSILE_WEAPONS_LEVEL3) && (Composition.comp().contains(Units.ZERG_ROACH) || Composition.comp().contains(Units.ZERG_HYDRALISK))) continue;
					for (UnitType t: upgraders.get(u)) {
						if ((t.equals(Units.ZERG_EVOLUTION_CHAMBER) && GameInfoCache.count(t) < 1 && (GameInfoCache.count(Units.ZERG_DRONE) > 35) && GameInfoCache.get_opponent_race() == Race.ZERG) || (t.equals(Units.ZERG_EVOLUTION_CHAMBER) && GameInfoCache.count(t) < 2 && (GameInfoCache.count(Units.ZERG_DRONE) > 60))) {
							if (Game.can_afford(t)) {
								BaseManager.build(t);
							}
							Game.purchase(t);
							return;
						}
						if (t == Units.ZERG_SPIRE && GameInfoCache.count(Units.ZERG_GREATER_SPIRE) < 1 && Composition.comp().contains(Units.ZERG_BROODLORD)) continue outer;
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

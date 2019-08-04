package com.hjax.kagamine.build;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.hjax.kagamine.game.HjaxUnit;
import com.hjax.kagamine.game.RaceInterface;
import com.hjax.kagamine.knowledge.Balance;
import com.hjax.kagamine.knowledge.Wisdom;

public class UpgradeManager {
	
	private static final Map<UnitType, List<Upgrade>> upgrades = new HashMap<>();
	private static final Map<Upgrade, List<UnitType>> upgraders = new HashMap<>();
	static {
		upgrades.put(Units.ZERG_ZERGLING, Arrays.asList(Upgrades.ZERG_MELEE_WEAPONS_LEVEL1, Upgrades.ZERG_MELEE_WEAPONS_LEVEL2, Upgrades.ZERG_MELEE_WEAPONS_LEVEL3, Upgrades.ZERG_GROUND_ARMORS_LEVEL1, Upgrades.ZERG_GROUND_ARMORS_LEVEL2, Upgrades.ZERG_GROUND_ARMORS_LEVEL3, Upgrades.ZERGLING_ATTACK_SPEED, Upgrades.ZERGLING_MOVEMENT_SPEED, Upgrades.OVERLORD_SPEED));
		upgrades.put(Units.ZERG_BANELING, Arrays.asList(Upgrades.ZERG_MELEE_WEAPONS_LEVEL1, Upgrades.ZERG_MELEE_WEAPONS_LEVEL2, Upgrades.ZERG_MELEE_WEAPONS_LEVEL3, Upgrades.ZERG_GROUND_ARMORS_LEVEL1, Upgrades.ZERG_GROUND_ARMORS_LEVEL2, Upgrades.ZERG_GROUND_ARMORS_LEVEL3, Upgrades.CENTRIFICAL_HOOKS));
		upgrades.put(Units.ZERG_ULTRALISK, Arrays.asList(Upgrades.ZERG_MELEE_WEAPONS_LEVEL1, Upgrades.ZERG_MELEE_WEAPONS_LEVEL2, Upgrades.ZERG_MELEE_WEAPONS_LEVEL3, Upgrades.ZERG_GROUND_ARMORS_LEVEL1, Upgrades.ZERG_GROUND_ARMORS_LEVEL2, Upgrades.ZERG_GROUND_ARMORS_LEVEL3, Upgrades.CHITINOUS_PLATING, Upgrades.ANABOLIC_SYNTHESIS));
		upgrades.put(Units.ZERG_ROACH, Arrays.asList(Upgrades.ZERG_MISSILE_WEAPONS_LEVEL1, Upgrades.ZERG_MISSILE_WEAPONS_LEVEL2, Upgrades.ZERG_MISSILE_WEAPONS_LEVEL3, Upgrades.ZERG_GROUND_ARMORS_LEVEL1, Upgrades.ZERG_GROUND_ARMORS_LEVEL2, Upgrades.ZERG_GROUND_ARMORS_LEVEL3, Upgrades.GLIALRE_CONSTITUTION, Upgrades.TUNNELING_CLAWS));
		upgrades.put(Units.ZERG_RAVAGER, Arrays.asList(Upgrades.ZERG_MISSILE_WEAPONS_LEVEL1, Upgrades.ZERG_MISSILE_WEAPONS_LEVEL2, Upgrades.ZERG_MISSILE_WEAPONS_LEVEL3, Upgrades.ZERG_GROUND_ARMORS_LEVEL1, Upgrades.ZERG_GROUND_ARMORS_LEVEL2, Upgrades.ZERG_GROUND_ARMORS_LEVEL3));
		upgrades.put(Units.ZERG_HYDRALISK, Arrays.asList(Upgrades.BURROW, Upgrades.ZERG_MISSILE_WEAPONS_LEVEL1, Upgrades.ZERG_MISSILE_WEAPONS_LEVEL2, Upgrades.ZERG_MISSILE_WEAPONS_LEVEL3, Upgrades.ZERG_GROUND_ARMORS_LEVEL1, Upgrades.ZERG_GROUND_ARMORS_LEVEL2, Upgrades.ZERG_GROUND_ARMORS_LEVEL3, Upgrades.EVOLVE_GROOVED_SPINES, Upgrades.EVOLVE_MUSCULAR_AUGMENTS));
		upgrades.put(Units.ZERG_LURKER_MP, Arrays.asList(Upgrades.ZERG_MISSILE_WEAPONS_LEVEL1, Upgrades.ZERG_MISSILE_WEAPONS_LEVEL2, Upgrades.ZERG_MISSILE_WEAPONS_LEVEL3, Upgrades.ZERG_GROUND_ARMORS_LEVEL1, Upgrades.ZERG_GROUND_ARMORS_LEVEL2, Upgrades.ZERG_GROUND_ARMORS_LEVEL3, Upgrades.DIGGING_CLAWS));
		upgrades.put(Units.ZERG_QUEEN, Arrays.asList(Upgrades.ZERG_MISSILE_WEAPONS_LEVEL1, Upgrades.ZERG_MISSILE_WEAPONS_LEVEL2, Upgrades.ZERG_MISSILE_WEAPONS_LEVEL3, Upgrades.ZERG_GROUND_ARMORS_LEVEL1, Upgrades.ZERG_GROUND_ARMORS_LEVEL2, Upgrades.ZERG_GROUND_ARMORS_LEVEL3));
		upgrades.put(Units.ZERG_INFESTOR, Arrays.asList(Upgrades.INFESTOR_ENERGY_UPGRADE, Upgrades.NEURAL_PARASITE));
		
		upgrades.put(Units.ZERG_MUTALISK, Arrays.asList(Upgrades.ZERG_FLYER_WEAPONS_LEVEL1, Upgrades.ZERG_FLYER_WEAPONS_LEVEL2, Upgrades.ZERG_FLYER_WEAPONS_LEVEL3, Upgrades.ZERG_FLYER_ARMORS_LEVEL1, Upgrades.ZERG_FLYER_ARMORS_LEVEL2, Upgrades.ZERG_FLYER_ARMORS_LEVEL3));
		upgrades.put(Units.ZERG_CORRUPTOR, Arrays.asList(Upgrades.ZERG_FLYER_WEAPONS_LEVEL1, Upgrades.ZERG_FLYER_WEAPONS_LEVEL2, Upgrades.ZERG_FLYER_WEAPONS_LEVEL3, Upgrades.ZERG_FLYER_ARMORS_LEVEL1, Upgrades.ZERG_FLYER_ARMORS_LEVEL2, Upgrades.ZERG_FLYER_ARMORS_LEVEL3));
		upgrades.put(Units.ZERG_BROODLORD, Arrays.asList(Upgrades.ZERG_FLYER_WEAPONS_LEVEL1, Upgrades.ZERG_FLYER_WEAPONS_LEVEL2, Upgrades.ZERG_FLYER_WEAPONS_LEVEL3, Upgrades.ZERG_FLYER_ARMORS_LEVEL1, Upgrades.ZERG_FLYER_ARMORS_LEVEL2, Upgrades.ZERG_FLYER_ARMORS_LEVEL3));
		
		upgrades.put(Units.PROTOSS_ZEALOT, Arrays.asList(Upgrades.PROTOSS_GROUND_WEAPONS_LEVEL1, Upgrades.PROTOSS_GROUND_WEAPONS_LEVEL2, Upgrades.PROTOSS_GROUND_WEAPONS_LEVEL3, Upgrades.PROTOSS_GROUND_ARMORS_LEVEL1, Upgrades.PROTOSS_GROUND_ARMORS_LEVEL2, Upgrades.PROTOSS_GROUND_ARMORS_LEVEL3, Upgrades.BLINK_TECH, Upgrades.CHARGE));
		
		
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
		upgraders.put(Upgrades.ANABOLIC_SYNTHESIS, new ArrayList<>(List.of(Units.ZERG_ULTRALISK_CAVERN)));
	}
	
	
	public static void on_frame() {
		
		if (GameInfoCache.get_opponent_race() == Race.ZERG && Game.army_supply() < 10 && (Wisdom.all_in_detected() || Wisdom.cannon_rush())) {
			return;
		}
		
		for (UnitType ut : Composition.full_comp()) {
			outer: for (Upgrade u : upgrades.getOrDefault(ut, Arrays.asList())) {
				if (u == Upgrades.OVERLORD_SPEED && !(Game.has_upgrade(Upgrades.ZERGLING_MOVEMENT_SPEED) || GameInfoCache.is_researching(Upgrades.ZERGLING_MOVEMENT_SPEED))) continue;
				if (!(Game.has_upgrade(u)) && !GameInfoCache.is_researching(u)) {
					if (u.toString().toLowerCase().contains("melee") && !Game.has_upgrade(Upgrades.ZERG_MISSILE_WEAPONS_LEVEL3) && (Composition.full_comp().contains(Units.ZERG_ROACH) || Composition.full_comp().contains(Units.ZERG_HYDRALISK))) continue;
					for (UnitType t: upgraders.get(u)) {
						if ((t.equals(Units.ZERG_EVOLUTION_CHAMBER) && GameInfoCache.count(t) < 1 && (GameInfoCache.count(Units.ZERG_DRONE) > 35) && GameInfoCache.get_opponent_race() == Race.ZERG) || (t.equals(Units.ZERG_EVOLUTION_CHAMBER) && GameInfoCache.count(t) < 2 && (GameInfoCache.count(Units.ZERG_DRONE) > 60))) {
							if (Game.can_afford(t)) {
								BaseManager.build(t);
							}
							Game.purchase(t);
							return;
						}
						if ((t.equals(Units.PROTOSS_FORGE) && GameInfoCache.count(t) < 2) && GameInfoCache.count(RaceInterface.get_race_worker()) > 50) {
							if (Game.can_afford(t)) {
								BaseManager.build(t);
							}
							Game.purchase(t);
							return;
						}
						if ((t.equals(Units.PROTOSS_TWILIGHT_COUNCIL) && GameInfoCache.count(t) < 1) && GameInfoCache.count(RaceInterface.get_race_worker()) > 30 && !Balance.has_tech_requirement(t)) {
							if (Game.can_afford(t)) {
								BaseManager.build(t);
							}
							Game.purchase(t);
							return;
						}
						if (t == Units.ZERG_SPIRE && GameInfoCache.count(Units.ZERG_GREATER_SPIRE) < 1 && Composition.full_comp().contains(Units.ZERG_BROODLORD)) continue outer;
						for (HjaxUnit up: GameInfoCache.get_units(Alliance.SELF, t)) {
							if (up.idle() && up.done()) {
								for (AvailableAbility aa: Game.availible_abilities(up, true).getAbilities()) {
									if (aa.getAbility() == Game.get_ability_data().get(Game.get_upgrade_data().get(u).getAbility().get()).getRemapsToAbility().orElse(Game.get_upgrade_data().get(u).getAbility().get())) {
										if (Game.can_afford(u)) {
											up.use_ability(Game.get_upgrade_data().get(u).getAbility().get());
										}
										Game.purchase(u);
										return;
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

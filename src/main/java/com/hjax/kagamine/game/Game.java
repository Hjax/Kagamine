package com.hjax.kagamine.game;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.ocraft.s2client.bot.gateway.ActionInterface;
import com.github.ocraft.s2client.bot.gateway.DebugInterface;
import com.github.ocraft.s2client.bot.gateway.ObservationInterface;
import com.github.ocraft.s2client.bot.gateway.QueryInterface;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.action.ActionChat.Channel;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Ability;
import com.github.ocraft.s2client.protocol.data.AbilityData;
import com.github.ocraft.s2client.protocol.data.Effect;
import com.github.ocraft.s2client.protocol.data.EffectData;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.UnitTypeData;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.data.Upgrade;
import com.github.ocraft.s2client.protocol.data.UpgradeData;
import com.github.ocraft.s2client.protocol.data.Weapon;
import com.github.ocraft.s2client.protocol.data.Weapon.TargetType;
import com.github.ocraft.s2client.protocol.debug.Color;
import com.github.ocraft.s2client.protocol.game.PlayerInfo;
import com.github.ocraft.s2client.protocol.game.Race;
import com.github.ocraft.s2client.protocol.observation.raw.EffectLocations;
import com.github.ocraft.s2client.protocol.observation.raw.Visibility;
import com.github.ocraft.s2client.protocol.query.AvailableAbilities;
import com.github.ocraft.s2client.protocol.query.QueryBuildingPlacement;
import com.github.ocraft.s2client.protocol.response.ResponseGameInfo;
import com.github.ocraft.s2client.protocol.spatial.Point;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Tag;
import com.github.ocraft.s2client.protocol.unit.Unit;
import com.hjax.kagamine.Constants;
import com.hjax.kagamine.Counter;

public class Game {
	
	static ActionInterface action;
	static ObservationInterface observation;
	static QueryInterface query;
	public static DebugInterface debug;
	
	static ResponseGameInfo game_info = null;
	static Map<UnitType, UnitTypeData> unit_type_data = null;
	static Map<Upgrade, UpgradeData> upgrade_data = null;
	static Map<Ability, AbilityData> ability_data = null;
	static Map<Effect, EffectData> effect_data = null;
	
	static Map<Ability, Set<Tag>> normal_abilities = new HashMap<>();
	static Map<Ability, Map<Point2d, Set<Tag>>> point_target_abilities = new HashMap<>();
	static Map<Ability, Map<Unit, Set<Tag>>> unit_target_abilities = new HashMap<>();

	static int lines = 0;
	
	/**
	 * The money that has been spent this frame
	 * index 0 is minerals
	 * index 1 is gas
	 */
	static int[] spending = new int[2];
	
	public static void start_frame(ObservationInterface o, ActionInterface a, QueryInterface q, DebugInterface d) {
		observation = o;
		action = a;
		query = q;
		debug = d;
		
		normal_abilities.clear();
		point_target_abilities.clear();
		unit_target_abilities.clear();

		spending = new int[2];
		
		lines = 0;
	}
	
	public static void on_frame() {

	}
	
	public static void end_frame() {
		
		for (Ability a : normal_abilities.keySet()) {
			action.unitCommand(normal_abilities.get(a), a, false);
		}
		
		for (Ability a : point_target_abilities.keySet()) {
			for (Point2d p : point_target_abilities.get(a).keySet()) {
				action.unitCommand(point_target_abilities.get(a).get(p), a, p, false);
			}
		}
		
		for (Ability a : unit_target_abilities.keySet()) {
			for (Unit t : unit_target_abilities.get(a).keySet()) {
				action.unitCommand(unit_target_abilities.get(a).get(t), a, t, false);
			}
		}
		
		action.sendActions();
	}
	
	public static void unit_command(Unit u, Ability a, Unit t, boolean queued) {
		if (u.getOrders().size() > 0 && !queued) {
			if (u.getOrders().get(0).getAbility() == a) {
				if (u.getOrders().get(0).getTargetedUnitTag().isPresent()) {
					if (t.getTag() == u.getOrders().get(0).getTargetedUnitTag().get()) {
						return;
					}
				}
			}
		}
		Counter.increment(u.getType().toString());
		
		if (!queued) {
			if (!unit_target_abilities.containsKey(a)) {
				unit_target_abilities.put(a, new HashMap<>());
			}
			if (!unit_target_abilities.get(a).containsKey(t)) {
				unit_target_abilities.get(a).put(t, new HashSet<>());
			}
			unit_target_abilities.get(a).get(t).add(u.getTag());
		} else {
			action.unitCommand(u, a, t, queued);
		}

	}
	
	public static void unit_command(Unit u, Ability a, Point2d p, boolean queued) {
		if (u.getOrders().size() > 0 && !queued) {
			if (u.getOrders().get(0).getAbility() == a) {
				if (u.getOrders().get(0).getTargetedWorldSpacePosition().isPresent()) {
					if (p.distance(u.getOrders().get(0).getTargetedWorldSpacePosition().get().toPoint2d()) < 1) {
						return;
					}
				}
			}
		}
		Counter.increment(u.getType().toString());

		if (!queued) {
			if (!point_target_abilities.containsKey(a)) {
				point_target_abilities.put(a, new HashMap<>());
			}
			if (!point_target_abilities.get(a).containsKey(p)) {
				point_target_abilities.get(a).put(p, new HashSet<>());
			}
			point_target_abilities.get(a).get(p).add(u.getTag());
		} else {
			action.unitCommand(u, a, p, queued);
		}
		
	}
	
	public static void unit_command(Unit u, Ability a, boolean queued) {
		Counter.increment(u.getType().toString());
		
		if (!queued) {
			if (!normal_abilities.containsKey(a)) {
				normal_abilities.put(a, new HashSet<>());
			}
			normal_abilities.get(a).add(u.getTag());
		} else {
			action.unitCommand(u, a, queued);
		}
	}
	
	
	public static void unit_command(List<UnitInPool> u, Ability a, Unit target) {
		List<Unit> list = new ArrayList<>();
		for (UnitInPool unit : u) list.add(unit.unit());
		action.unitCommand(list, a, target, false);
		
	}
	
	public static void unit_command(List<UnitInPool> u, Ability a, Point2d target) {
		List<Unit> list = new ArrayList<>();
		for (UnitInPool unit : u) list.add(unit.unit());
		action.unitCommand(list, a, target, false);
		
	}
	
	
	public static List<EffectLocations> get_effects() {
		return observation.getEffects();
	}
	
	public static Map<Effect, EffectData> get_effect_data() {
		if (effect_data == null) {
			effect_data = observation.getEffectData(false);
		}
		return effect_data;
	}
	
	public static void unit_command(Unit u, Ability a, Unit t) {
		unit_command(u, a, t, false);
	}
	
	public static void unit_command(Unit u, Ability a, Point2d p) {
		unit_command(u, a, p, false);
	}
	
	public static void unit_command(Unit u, Ability a) {
		unit_command(u, a, false);
	}
	
	public static void unit_command(UnitInPool u, Ability a, Unit t, boolean queued) {
		unit_command(u.unit(), a, t, queued);
	}
	
	public static void unit_command(UnitInPool u, Ability a, Point2d p, boolean queued) {
		unit_command(u.unit(), a, p, queued);
	}
	
	public static void unit_command(UnitInPool u, Ability a, boolean queued) {
		unit_command(u.unit(), a, queued);
	}
	
	public static void unit_command(UnitInPool u, Ability a, Unit t) {
		unit_command(u.unit(), a, t, false);
	}
	
	public static void unit_command(UnitInPool u, Ability a, Point2d p) {
		unit_command(u.unit(), a, p, false);
	}
	
	public static void unit_command(UnitInPool u, Ability a) {
		unit_command(u.unit(), a, false);
	}
	
	public static boolean has_upgrade(Upgrade u) {
		return observation.getUpgrades().contains(u);
	}
	
	public static boolean on_creep(Point2d p ) {
		return observation.hasCreep(p);
	}
	
	public static List<UnitInPool> get_units() {
		return observation.getUnits();
	}

	protected static UnitInPool get_unit(Tag t) {
		return observation.getUnit(t);
	}
	
	public static void chat(String s) {
		if (Constants.CHAT) {
			action.sendChat(s, Channel.BROADCAST);
		}
	}
	
	public static boolean isVisible(Point2d p) {
		return observation.getVisibility(p) == Visibility.VISIBLE;
	}
	
	public static double get_game_time() {
		return observation.getGameLoop() / Constants.FPS;
	}
	
	public static long get_frame() {
		return observation.getGameLoop();
	}
	
	public static boolean can_place(Ability a, Point2d p) {
		return query.placement(a, p);
	}
	
	public static List<Boolean> can_place(Ability a, List<Point2d> p) {
		List<QueryBuildingPlacement> queries = new ArrayList<>();
		for (Point2d point : p) queries.add(QueryBuildingPlacement.placeBuilding().useAbility(a).on(point).build());
		return query.placement(queries);
	}
	
	public static int supply() {
		return observation.getFoodUsed();
	}
	
	public static int supply_cap() {
		return observation.getFoodCap();
	}
	
	public static int army_supply() {
		return observation.getFoodArmy();
	}
	
	public static float completed_army_supply() {
		float result = 0;
		for (UnitInPool u:  get_units()) {
			if (u.unit().getAlliance() == Alliance.SELF && is_combat(u.unit().getType()) && u.unit().getBuildProgress() > 0.999) {
				result += supply(u.unit().getType());
			}
		}
		return result;
	}
	
	public static int minerals() {
		return observation.getMinerals() - spending[0];
	}
	
	public static int gas() {
		return observation.getVespene() - spending[1];
	}
	
	public static void spend(int m, int g) {
		spending[0] += m;
		spending[1] += g;
	}
	
	public static void purchase(Upgrade u) {
		int minerals = Game.get_upgrade_data().get(u).getMineralCost().orElse(0);
		int gas = Game.get_upgrade_data().get(u).getVespeneCost().orElse(0);
		spend(minerals, gas);
	}
	
	// TODO deal with morphs
	public static void purchase(UnitType u) {
		int minerals = Game.get_unit_type_data().get(u).getMineralCost().orElse(0);
		int gas = Game.get_unit_type_data().get(u).getVespeneCost().orElse(0);
		if (get_unit_type_data().get(u).getRace().orElse(Race.NO_RACE) == Race.ZERG && is_structure(u)) {
			minerals = Math.max(minerals - 50, 0);
		}
		if (u == Units.ZERG_LAIR) minerals -= 300;
		spend(minerals, gas);
	}
	
	public static Map<UnitType, UnitTypeData> get_unit_type_data() {
		if (unit_type_data == null) {
			unit_type_data = observation.getUnitTypeData(false);
		}
		return unit_type_data;
	}
	
	public static Map<Upgrade, UpgradeData> get_upgrade_data() {
		if (upgrade_data == null) {
			upgrade_data = observation.getUpgradeData(false);
		}
		return upgrade_data;
	}
	
	public static Map<Ability, AbilityData> get_ability_data() {
		if (ability_data == null) {
			ability_data = observation.getAbilityData(false);
		}
		return ability_data; 
	}
	
	public static ResponseGameInfo get_game_info() {
		if (game_info == null) {
			game_info = observation.getGameInfo();
		}
		return game_info;
	}
	
	public static int get_player_id() {
		return observation.getPlayerId();
	}
	
	public static boolean pathable(Point2d p) {
		return observation.isPathable(p);
	}
	
	public static boolean buildable(Point2d p) {
		return observation.isPathable(p);
	}
	
	public static float height(Point2d p) {
		return observation.terrainHeight(p);
	}
	
	public static float pathing_distance(Point2d a, Point2d b) {
		return query.pathingDistance(a, b);
	}
	
	public static boolean can_afford(UnitType u) {
		int minerals = get_unit_type_data().get(u).getMineralCost().orElse(0);
		int gas = get_unit_type_data().get(u).getVespeneCost().orElse(0);
		if (u == Units.ZERG_LAIR) minerals -= get_unit_type_data().get(Units.ZERG_HATCHERY).getMineralCost().orElse(0);
		else if (u == Units.ZERG_HIVE) {
			minerals -= get_unit_type_data().get(Units.ZERG_LAIR).getMineralCost().orElse(0);
			gas -= get_unit_type_data().get(Units.ZERG_LAIR).getVespeneCost().orElse(0);
		}
		else if (get_unit_type_data().get(u).getRace().orElse(Race.NO_RACE) == Race.ZERG && is_structure(u)) {
			minerals = Math.max(minerals - 50, 0);
		}
		else if (u == Units.ZERG_GREATER_SPIRE) {
			minerals -= get_unit_type_data().get(Units.ZERG_SPIRE).getMineralCost().orElse(0);
			gas -= get_unit_type_data().get(Units.ZERG_SPIRE).getVespeneCost().orElse(0);
		}
		return (minerals <= minerals() || minerals == 0) && (gas <= gas() || gas == 0);
	}
	
	public static boolean can_afford(Upgrade u) {
		int minerals = get_upgrade_data().get(u).getMineralCost().orElse(0);
		int gas = get_upgrade_data().get(u).getVespeneCost().orElse(0);
		return (minerals <= minerals() || minerals == 0) && (gas <= gas() || gas == 0);
	}
	
	public static boolean is_town_hall(UnitType u) {
		return u.equals(Units.PROTOSS_NEXUS) ||
				u.equals(Units.TERRAN_COMMAND_CENTER) ||
				u.equals(Units.TERRAN_COMMAND_CENTER_FLYING) || 
				u.equals(Units.TERRAN_ORBITAL_COMMAND) ||
				u.equals(Units.TERRAN_ORBITAL_COMMAND_FLYING) ||
				u.equals(Units.TERRAN_PLANETARY_FORTRESS) ||
				u.equals(Units.ZERG_HATCHERY) ||
				u.equals(Units.ZERG_LAIR) ||
				u.equals(Units.ZERG_HIVE);
	}
	
	public static boolean is_worker(UnitType u) {
		return u.equals(Units.ZERG_DRONE) ||
				u.equals(Units.TERRAN_SCV) ||
				u.equals(Units.PROTOSS_PROBE);
	}
	
	
	public static boolean is_structure(UnitType u) {
		if (is_town_hall(u)) return true;
		return (get_unit_type_data().get(u).getFoodRequired().orElse((float) 0) == 0 && 
				get_unit_type_data().get(u).getFoodProvided().orElse((float) 0) == 0 &&
				(get_unit_type_data().get(u).getMineralCost().orElse(0) > 0 || get_unit_type_data().get(u).getVespeneCost().orElse(0) > 0)) ||
				(get_unit_type_data().get(u).getRace().orElse(Race.NO_RACE) != Race.ZERG && get_unit_type_data().get(u).getFoodProvided().orElse((float) 0) > 0);
	}

	public static boolean is_placeable(Point2d p) {
		return Game.observation.isPlacable(p);
	}
	
	public static AvailableAbilities availible_abilities(UnitInPool u) {
		return query.getAbilitiesForUnit(u.unit(), false);
	}
	
	public static AvailableAbilities availible_abilities(UnitInPool u, boolean ignore) {
		return query.getAbilitiesForUnit(u.unit(), ignore);
	}
	
	public static AvailableAbilities availible_abilities(HjaxUnit u, boolean ignore) {
		return query.getAbilitiesForUnit(u.unit(), ignore);
	}
	
	public static AvailableAbilities availible_abilities(HjaxUnit u) {
		return query.getAbilitiesForUnit(u.unit(), false);
	}
	
	public static List<AvailableAbilities> availible_abilities(List<UnitInPool> u) {
		List<Unit> parsed = new ArrayList<>();
		for (UnitInPool up: u) parsed.add(up.unit());
		return query.getAbilitiesForUnits(parsed, false);
	}
	
	public static void draw_box(Point2d current, Color c) {
		if (Constants.DEBUG) {
			//debug.debugBoxOut(Point.of(current.getX(), current.getY(), (float) (Math.max(Game.height(current) + .5, 0))), Point.of((float) (current.getX() + .5), (float) (current.getY() + .5), (float) (Math.max(Game.height(current) + .5, 0))), c);
			debug.debugBoxOut(Point.of(current.getX(), current.getY(), 15), Point.of((float) (current.getX() + .5), (float) (current.getY() + .5), (float) 15), c);
		}
	}
	
	public static void draw_line(Point2d a, Point2d b, Color c) {
		if (Constants.DEBUG) {
			debug.debugLineOut(Point.of(a.getX(), a.getY(), (float) (Game.height(a) + 1)), Point.of((float) (b.getX()), (float) (b.getY()), (float) (Game.height(b) + 1)), c);
		}
	}
	
	public static float army_killed() { 
		return Game.observation.getScore().getDetails().getKilledMinerals().getArmy() + Game.observation.getScore().getDetails().getKilledVespene().getArmy();
	}
	
	public static float army_lost() {
		return Game.observation.getScore().getDetails().getLostMinerals().getArmy() + Game.observation.getScore().getDetails().getLostVespene().getArmy();
	}
	
	public static int minerals_killed() {
		return (int) (Game.observation.getScore().getDetails().getKilledMinerals().getArmy() + Game.observation.getScore().getDetails().getKilledMinerals().getUpgrade() + Game.observation.getScore().getDetails().getKilledMinerals().getNone() + Game.observation.getScore().getDetails().getKilledMinerals().getEconomy() + Game.observation.getScore().getDetails().getKilledMinerals().getTechnology()) ;
	}
	
	public static int gas_killed() {
		return (int) (Game.observation.getScore().getDetails().getKilledVespene().getArmy() + Game.observation.getScore().getDetails().getKilledVespene().getUpgrade() + Game.observation.getScore().getDetails().getKilledVespene().getNone() + Game.observation.getScore().getDetails().getKilledVespene().getEconomy() + Game.observation.getScore().getDetails().getKilledVespene().getTechnology()) ;
	}

	public static boolean hits_air(UnitType u) {
		for (Weapon w: get_unit_type_data().get(u).getWeapons()) {
			if (w.getTargetType().equals(TargetType.AIR) || w.getTargetType().equals(TargetType.ANY)) return true;
		}
		return false;
	}
	
	public static boolean is_combat(UnitType u) {
		if (u == Units.ZERG_LURKER_MP || u == Units.ZERG_LURKER_MP_BURROWED) return true;
		if (u == Units.ZERG_INFESTOR || u == Units.ZERG_VIPER) return true;
		return (get_unit_type_data().get(u).getWeapons().size() > 0 && !is_worker(u)) || u == Units.ZERG_BANELING;
	}
	
	public static boolean is_changeling(UnitType u) {
		return u.toString().toLowerCase().contains("changeling");
	}
	
	public static boolean is_gas(UnitType u) {
		return u.toString().toLowerCase().contains("geyser");
	}
	
	public static boolean is_mineral(UnitType u) {
		return u.toString().toLowerCase().contains("mineral");
	}
	
	public static boolean is_resource(UnitType u) {
		return is_mineral(u) || is_gas(u);
	}
	
	public static List<Point> expansions() {
		return query.calculateExpansionLocations(observation);
	}
	
	public static void write_text(String text) {
		debug.debugTextOut(text, Point2d.of((float) 0.1, (float) ((100.0 + 20.0 * lines++) / 1080.0)), Color.WHITE, 15);
	}
	
	public static void write_text(String text, Point2d location) {
		debug.debugTextOut(text, Point.of(location.getX(), location.getY()), Color.WHITE, 15);
	}
	
	public static int worker_count() {
		return observation.getFoodWorkers();
	}
	
	public static double supply(UnitType u) {
		return get_unit_type_data().get(u).getFoodRequired().orElse(0.0f);
	}
	
	public static Ability production_ability(UnitType u) {
		return Game.get_unit_type_data().get(u).getAbility().orElse(Abilities.INVALID);
	}
	
	private static Map<Ability, List<UnitType>> uwa_cache = new HashMap<>();
	public static List<UnitType> unit_with_ability(Ability a) {
		if (uwa_cache.containsKey(a)) return uwa_cache.get(a);
		List<UnitType> result = new ArrayList<>();
		for (UnitType u : get_unit_type_data().keySet()) {
			if (u.getAbilities().contains(a)) {
				result.add(u);
			}
		}
		uwa_cache.put(a, result);
		return result;
	}
	 
	public static Race race() {
		for (PlayerInfo p : get_game_info().getPlayersInfo()) {
			if (p.getPlayerId() == get_player_id()) {
				return p.getRequestedRace();
			}
		}
		return Race.NO_RACE;
	}
}

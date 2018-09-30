package com.hjax.kagamine;
import java.util.List;
import java.util.Map;

import com.github.ocraft.s2client.bot.gateway.ActionInterface;
import com.github.ocraft.s2client.bot.gateway.DebugInterface;
import com.github.ocraft.s2client.bot.gateway.ObservationInterface;
import com.github.ocraft.s2client.bot.gateway.QueryInterface;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.action.ActionChat.Channel;
import com.github.ocraft.s2client.protocol.data.Ability;
import com.github.ocraft.s2client.protocol.data.AbilityData;
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
import com.github.ocraft.s2client.protocol.query.AvailableAbilities;
import com.github.ocraft.s2client.protocol.response.ResponseGameInfo;
import com.github.ocraft.s2client.protocol.spatial.Point;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Tag;
import com.github.ocraft.s2client.protocol.unit.Unit;

public class Game {
	
	static ActionInterface action;
	static ObservationInterface observation;
	static QueryInterface query;
	static DebugInterface debug;
	
	static ResponseGameInfo game_info = null;
	static Map<UnitType, UnitTypeData> unit_type_data = null;
	static Map<Upgrade, UpgradeData> upgrade_data = null;
	static Map<Ability, AbilityData> ability_data = null;

	
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

		spending = new int[2];
		o.getGameInfo();
	}
	
	public static void on_frame() {
		
	}
	
	public static void end_frame() {
		action.sendActions();
	}
	
	public static void unit_command(Unit u, Ability a, Unit t, boolean queued) {
		action.unitCommand(u, a, t, queued);
	}
	
	public static void unit_command(Unit u, Ability a, Point2d p, boolean queued) {
		action.unitCommand(u, a, p, queued);
	}
	
	public static void unit_command(Unit u, Ability a, boolean queued) {
		action.unitCommand(u, a, queued);
	}
	
	public static void unit_command(Unit u, Ability a, Unit t) {
		action.unitCommand(u, a, t, false);
	}
	
	public static void unit_command(Unit u, Ability a, Point2d p) {
		action.unitCommand(u, a, p, false);
	}
	
	public static void unit_command(Unit u, Ability a) {
		action.unitCommand(u, a, false);
	}
	
	public static void unit_command(UnitInPool u, Ability a, Unit t, boolean queued) {
		action.unitCommand(u.unit(), a, t, queued);
	}
	
	public static void unit_command(UnitInPool u, Ability a, Point2d p, boolean queued) {
		action.unitCommand(u.unit(), a, p, queued);
	}
	
	public static void unit_command(UnitInPool u, Ability a, boolean queued) {
		action.unitCommand(u.unit(), a, queued);
	}
	
	public static void unit_command(UnitInPool u, Ability a, Unit t) {
		action.unitCommand(u.unit(), a, t, false);
	}
	
	public static void unit_command(UnitInPool u, Ability a, Point2d p) {
		action.unitCommand(u.unit(), a, p, false);
	}
	
	public static void unit_command(UnitInPool u, Ability a) {
		action.unitCommand(u.unit(), a, false);
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

	public static UnitInPool get_unit(Tag t) {
		return observation.getUnit(t);
	}
	
	public static void chat(String s) {
		if (Constants.CHAT) {
			action.sendChat(s, Channel.BROADCAST);
		}
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
	
	public static int supply() {
		return observation.getFoodUsed();
	}
	
	public static int supply_cap() {
		return observation.getFoodCap();
	}
	
	public static int army_supply() {
		return observation.getFoodArmy();
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
	
	public static Map<Ability, AbilityData> get_abliity_data() {
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
	
	public static Race get_opponent_race() {
		for (PlayerInfo player: get_game_info().getPlayersInfo()) {
			if (player.getPlayerId() != get_player_id()) {
				return player.getRequestedRace();
			}
		}
		return Race.RANDOM;
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
		return minerals <= minerals() && gas <= gas();
	}
	
	public static boolean can_afford(Upgrade u) {
		int minerals = get_upgrade_data().get(u).getMineralCost().orElse(0);
		int gas = get_upgrade_data().get(u).getVespeneCost().orElse(0);
		return minerals <= minerals() && gas <= gas();
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
	
	public static void draw_box(Point2d current, Color c) {
		debug.debugBoxOut(Point.of(current.getX(), current.getY(), (float) (Game.height(current) + .5)), Point.of((float) (current.getX() + .5), (float) (current.getY() + .5), (float) (Game.height(current) + .5)), c);
	}
	
	public static float army_killed() { 
		return Game.observation.getScore().getDetails().getKilledMinerals().getArmy() + Game.observation.getScore().getDetails().getKilledVespene().getArmy();
	}
	
	public static float army_lost() {
		return Game.observation.getScore().getDetails().getLostMinerals().getArmy() + Game.observation.getScore().getDetails().getLostVespene().getArmy();
	}

	public static boolean hits_air(UnitType u) {
		for (Weapon w: get_unit_type_data().get(u).getWeapons()) {
			if (w.getTargetType().equals(TargetType.AIR) || w.getTargetType().equals(TargetType.ANY)) return true;
		}
		return false;
	}
}

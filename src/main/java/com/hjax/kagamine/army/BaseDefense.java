package com.hjax.kagamine.army;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.debug.Color;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Tag;
import com.hjax.kagamine.Constants;
import com.hjax.kagamine.economy.Base;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.game.HjaxUnit;

public class BaseDefense {
	private static final Set<Tag> used = new HashSet<>();
	public static final Map<Tag, Point2d> assignments = new HashMap<>();
	public static final Map<Tag, Point2d> surroundCenter = new HashMap<>();
	
	private static Map<Set<HjaxUnit>, Boolean> assigned = new HashMap<>();
	
	private static Point2d defense_point;
	public static int detection_points;
	
	public static void on_frame() {
		defense_point = null;
		used.clear();
		assignments.clear();
		surroundCenter.clear();
		detection_points = 0;

		assigned.clear();
		
		
		for (Set<HjaxUnit> enemy_squad : EnemySquadManager.enemy_squads) {
			
			assigned.put(enemy_squad, false);
			
			Point2d average = EnemySquadManager.average_point(new ArrayList<>(enemy_squad));
			
			for (Base b : BaseManager.bases) {
				try {
					if (((b.has_friendly_command_structure() || b.equals(BaseManager.get_next_base())) && 
						 b.location.distance(average) < Constants.THREAT_DISTANCE)) {
						assign_defense(enemy_squad);
						assigned.put(enemy_squad, true);

					} 
				} catch (Exception ignored) {
					
				}
			}
		}
		
		for (Set<HjaxUnit> enemy_squad : EnemySquadManager.enemy_squads) {
			if (assigned.get(enemy_squad)) continue;
			
			Point2d average = EnemySquadManager.average_point(new ArrayList<>(enemy_squad));
			
			if ((Game.closest_invisible(average).distance(average) > 12 && 
					BaseManager.closest_base(average).has_friendly_command_structure())) {
						
				assign_defense(enemy_squad);
				assigned.put(enemy_squad, true);
				
			}
		}
		
		for (Set<HjaxUnit> enemy_squad : EnemySquadManager.enemy_squads) {
			if (assigned.get(enemy_squad)) continue;
			
			Point2d average = EnemySquadManager.average_point(new ArrayList<>(enemy_squad));
			
			if ((Game.closest_invisible(average).distance(average) > 12 && 
					ThreatManager.threat() * 1.1 < Game.army_supply() && 
					!BaseManager.closest_base(average).has_enemy_command_structure())) {
						
				assign_defense(enemy_squad);
				assigned.put(enemy_squad, true);
				
			}
		}
		
		for (HjaxUnit ally : UnitRoleManager.get(UnitRoleManager.UnitRole.ARMY)) {
			if (used.contains(ally.tag())) {
				UnitRoleManager.add(ally, UnitRoleManager.UnitRole.DEFENDER);
			}
		}
	}
	
	private static void assign_defense(Set<HjaxUnit> enemy_squad) {
		float ground_supply = 0;
		float flyer_supply = 0;
		boolean needs_detection = false;
		
		Point2d average = EnemySquadManager.average_point(new ArrayList<>(enemy_squad));
		
		for (HjaxUnit enemy : enemy_squad) {
			if (enemy.flying()) {
				flyer_supply += Game.supply(enemy.type());
			} else {
				ground_supply += Game.supply(enemy.type());
			}
			needs_detection = needs_detection || enemy.cloaked();
		}
		
		if (needs_detection) detection_points++;
		
		ArrayList<HjaxUnit> assigned = new ArrayList<>();
		if (ground_supply > 70 || flyer_supply > 70) {
			defense_point = average;
			return;
		}
		
		float assigned_supply = 0;
		while (assigned_supply < ground_supply * 1.5) {
			HjaxUnit current = closest_free(average, false);
			if (current == null) current = closest_free(average, true);
			if (current == null) break;
			assigned_supply += Game.supply(current.type());
			assigned.add(current);
		}
		assigned_supply = 0;
		while (assigned_supply < flyer_supply) {
			HjaxUnit current = closest_free(average, true);
			if (current == null) break;
			assigned_supply += Game.supply(current.type());
			assigned.add(current);
		}
		if (needs_detection) {
			HjaxUnit current = closest_free_detection(average);
			if (current == null) return;
			assigned.add(current);
		}
		Point2d center = average_point_zergling(assigned, average);
		for (HjaxUnit u: assigned) {
			assignments.put(u.tag(), average);
			if (center.distance(Point2d.of(0, 0)) > 1 && enemy_squad.size() * 2 <= assigned.size()) {
				surroundCenter.put(u.tag(), center);
			}
			Game.draw_line(average, u.location(), Color.GREEN);
		}
	}
	
	private static HjaxUnit closest_free_detection(Point2d p) {
		HjaxUnit best = null;
		for (HjaxUnit ally : GameInfoCache.get_units(Alliance.SELF, Units.ZERG_OVERSEER)) {
			if (!assignments.containsKey(ally.tag())) {
				if (best == null || best.distance(p) > ally.distance(p)) {
					best = ally;
				}
			}
		}
		return best;
	}
	
	private static HjaxUnit closest_free(Point2d p, boolean aa) {
		HjaxUnit best = null;
		for (HjaxUnit ally : UnitRoleManager.get(UnitRoleManager.UnitRole.ARMY)) {
			if (aa != Game.hits_air(ally.type())) continue;
			if (Game.is_spellcaster(ally.type())) continue;
			if (!Game.is_structure(ally.type()) && Game.is_combat(ally.type())) {
				if (!used.contains(ally.tag())) {
					if (best == null || 
							(best.distance(p) / Game.get_unit_type_data().get(best.type()).getMovementSpeed().orElse((float) 1)) > 
							(ally.distance(p)) / Game.get_unit_type_data().get(ally.type()).getMovementSpeed().orElse((float) 1)) {
						best = ally;
					}
				}
			}
		}
		if (best != null) {
			used.add(best.tag());
		}
		return best;
	}
	
	public static boolean has_defense_point() {
		return defense_point != null;
	}
	
	public static Point2d defense_point() {
		return defense_point;
	}
	
	private static Point2d average_point_zergling(List<HjaxUnit> l, Point2d target_center) {
		float x = 0;
		float y = 0;
		int n = 0;
		for (HjaxUnit u : l) {
			if (u.type() == Units.ZERG_ZERGLING) {
				if (u.distance(target_center) < 5) {
					x += u.location().getX();
					y += u.location().getY();
					n++;
				}
			}
		}
		return Point2d.of(x / n, y / n);
	}
}

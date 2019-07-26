package com.hjax.kagamine.game;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Ability;
import com.github.ocraft.s2client.protocol.data.Buffs;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.data.Weapon;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.CloakState;
import com.github.ocraft.s2client.protocol.unit.DisplayType;
import com.github.ocraft.s2client.protocol.unit.Tag;
import com.github.ocraft.s2client.protocol.unit.Unit;
import com.github.ocraft.s2client.protocol.unit.UnitOrder;
import com.hjax.kagamine.Constants;
import com.hjax.kagamine.economy.Base;

public class HjaxUnit {

	private final UnitInPool contained;
	private static final Map<Tag, HjaxUnit> cache = new HashMap<>();
	
	private HjaxUnit(UnitInPool unit) {
		contained = unit;
	}
	
	public static HjaxUnit getInstance(UnitInPool unit) {
		if (!cache.containsKey(unit.getTag())) {
			cache.put(unit.getTag(), new HjaxUnit(unit));
		}
		return cache.get(unit.getTag());
	}
	
	public Point2d location() {
		return get_unit().getPosition().toPoint2d();
	}
	
	public double distance(HjaxUnit other) {
		return location().distance(other.location());
	}
	
	public double distance(Point2d other) {
		return location().distance(other);
	}
	
	public double distance(Base other) {
		return location().distance(other.location);
	}
	
	public Tag tag() {
		return contained.getTag();
	}
	
	private long type_frame = -1;
	private UnitType type = Units.INVALID;
	public UnitType type() {
		if (type_frame != Game.get_frame()) {
			type_frame = Game.get_frame();
			type = get_unit().getType();
		}
		return type;
	}
	
	public boolean alive() {
		return contained.isAlive();
	}
	
	public Alliance alliance() {
		return get_unit().getAlliance();
	}
	
	public boolean friendly() {
		return get_unit().getAlliance() == Alliance.SELF;
	}
	
	public boolean done() {
		return get_unit().getBuildProgress() > Constants.DONE;
	}
	
	public double progress() {
		return get_unit().getBuildProgress();
	}
	
	private long unit_frame = -1;
	private Unit unit = null;
	private Unit get_unit() {
		if (unit_frame != Game.get_frame()) {
			unit_frame = Game.get_frame();
			unit = contained.unit();
		}
		return unit;
	}
	
	public List<UnitOrder> orders() {
		return get_unit().getOrders();
	}
	
	public long last_seen() {
		return contained.getLastSeenGameLoop();
	}
	
	public boolean flying() {
		return get_unit().getFlying().orElse(false);
	}
	
	public boolean cloaked() {
		return get_unit().getCloakState().orElse(CloakState.NOT_CLOAKED) != CloakState.NOT_CLOAKED;
	}
	
	public boolean idle() {
		return orders().size() == 0;
	}
	
	public double health() {
		return get_unit().getHealth().orElse((float) 0.0);
	}
	
	public double health_max() {
		return get_unit().getHealthMax().orElse((float) 0.0);
	}
	
	public void attack(Point2d point) {
		Game.unit_command(get_unit(), Abilities.ATTACK, point);
	}
	
	public void attack(HjaxUnit unit) {
		Game.unit_command(get_unit(), Abilities.ATTACK, unit.unit());
	}
	
	public void move(Point2d point) {
		Game.unit_command(get_unit(), Abilities.MOVE, point);
	}
	
	public void use_ability(Ability ability) {
		Game.unit_command(get_unit(), ability);
	}
	
	public void use_ability(Ability ability, Point2d point) {
		Game.unit_command(get_unit(), ability, point);
	}
	
	public void use_ability(Ability ability, HjaxUnit unit) {
		Game.unit_command(get_unit(), ability, unit.unit());
	}
	
	public boolean is_worker() {
		return Game.is_worker(type());
	}
	
	public boolean is_structure() {
		return Game.is_structure(type());
	}
	
	public boolean is_combat() {
		return Game.is_combat(type());
	}
	
	public boolean is_command() {
		return Game.is_town_hall(type());
	}
	
	public boolean is_halluc() {
		return get_unit().getHallucination().orElse(false);
	}
	
	public boolean is_not_snapshot() {
		return get_unit().getDisplayType() != DisplayType.SNAPSHOT;
	}
	
	public void stop() {
		use_ability(Abilities.STOP);
	}
	
	public void cancel() {
		use_ability(Abilities.CANCEL);
	}
	
	public Ability ability() {
		if (orders().size() == 0) {
			return Abilities.INVALID;
		}
		return orders().get(0).getAbility();
	}
	
	public int minerals() {
		return get_unit().getMineralContents().orElse(0);
	}
	
	public int gas() {
		return get_unit().getVespeneContents().orElse(0);
	}
	
	public int assigned_workers() {
		return get_unit().getAssignedHarvesters().orElse(0);
	}
	
	public int ideal_workers() {
		return get_unit().getIdealHarvesters().orElse(0);
	}
	
	Unit unit() {
		return get_unit();
	}
	
	public boolean is_gas() {
		return type() == Units.PROTOSS_ASSIMILATOR || type() == Units.TERRAN_REFINERY || type() == Units.ZERG_EXTRACTOR;
	}
	
	public double cooldown() {
		return get_unit().getWeaponCooldown().orElse(0.0f);
	}
	
	public boolean burrowed() {
		return get_unit().getBurrowed().orElse(false);
	}
	
	public double energy() {
		return get_unit().getEnergy().orElse(0.0f);
	}
	
	public double shields() {
		return get_unit().getShield().orElse(0.0f);
	}
	
	public boolean is_chronoed() {
		return get_unit().getBuffs().contains(Buffs.CHRONOBOOST_ENERGY_COST);
	}
	
	public boolean is_burrowed() {
		return get_unit().getBurrowed().orElse(false);
	}
	
	public boolean is_melee() {
		for (Weapon w : Game.get_unit_type_data().get(get_unit().getType()).getWeapons()) {
			if (w.getRange() > 4) {
				return false;
			}
		}
		return true;
	}
}


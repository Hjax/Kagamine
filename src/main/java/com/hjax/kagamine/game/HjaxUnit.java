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

	private UnitInPool contained;
	private static Map<Tag, HjaxUnit> cache = new HashMap<>();
	
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
		return contained.unit().getPosition().toPoint2d();
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
	
	public UnitType type() {
		return contained.unit().getType();
	}
	
	public boolean alive() {
		return contained.isAlive();
	}
	
	public Alliance alliance() {
		return contained.unit().getAlliance();
	}
	
	public boolean friendly() {
		return contained.unit().getAlliance() == Alliance.SELF;
	}
	
	public boolean done() {
		return contained.unit().getBuildProgress() > Constants.DONE;
	}
	
	public List<UnitOrder> orders() {
		return contained.unit().getOrders();
	}
	
	public long last_seen() {
		return contained.getLastSeenGameLoop();
	}
	
	public boolean flying() {
		return contained.unit().getFlying().orElse(false);
	}
	
	public boolean cloaked() {
		return contained.unit().getCloakState().orElse(CloakState.NOT_CLOAKED) != CloakState.NOT_CLOAKED;
	}
	
	public boolean idle() {
		return orders().size() == 0;
	}
	
	public double health() {
		return contained.unit().getHealth().orElse((float) 0.0);
	}
	
	public double health_max() {
		return contained.unit().getHealthMax().orElse((float) 0.0);
	}
	
	public void attack(Point2d point) {
		Game.unit_command(contained.unit(), Abilities.ATTACK, point);
	}
	
	public void attack(HjaxUnit unit) {
		Game.unit_command(contained.unit(), Abilities.ATTACK, unit.unit());
	}
	
	public void move(Point2d point) {
		Game.unit_command(contained.unit(), Abilities.MOVE, point);
	}
	
	public void use_ability(Ability ability) {
		Game.unit_command(contained.unit(), ability);
	}
	
	public void use_ability(Ability ability, Point2d point) {
		Game.unit_command(contained.unit(), ability, point);
	}
	
	public void use_ability(Ability ability, HjaxUnit unit) {
		Game.unit_command(contained.unit(), ability, unit.unit());
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
		return contained.unit().getHallucination().orElse(false);
	}
	
	public boolean is_snapshot() {
		return contained.unit().getDisplayType() == DisplayType.SNAPSHOT;
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
		return contained.unit().getMineralContents().orElse(0);
	}
	
	public int gas() {
		return contained.unit().getVespeneContents().orElse(0);
	}
	
	public int assigned_workers() {
		return contained.unit().getAssignedHarvesters().orElse(0);
	}
	
	public int ideal_workers() {
		return contained.unit().getIdealHarvesters().orElse(0);
	}
	
	protected Unit unit() {
		return contained.unit();
	}
	
	public boolean is_gas() {
		return type() == Units.PROTOSS_ASSIMILATOR || type() == Units.TERRAN_REFINERY || type() == Units.ZERG_EXTRACTOR;
	}
	
	public double cooldown() {
		return contained.unit().getWeaponCooldown().orElse(0.0f);
	}
	
	public boolean burrowed() {
		return contained.unit().getBurrowed().orElse(false);
	}
	
	public double energy() {
		return contained.unit().getEnergy().orElse(0.0f);
	}
	
	public double shields() {
		return contained.unit().getShield().orElse(0.0f);
	}
	
	public boolean is_chronoed() {
		return contained.unit().getBuffs().contains(Buffs.CHRONOBOOST_ENERGY_COST);
	}
	
	public boolean is_burrowed() {
		return contained.unit().getBurrowed().orElse(false);
	}
}


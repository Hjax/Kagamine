package com.hjax.kagamine;

import java.io.InputStream;
import java.util.Scanner;

import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.army.ArmyManager;
import com.hjax.kagamine.army.BanelingAvoidance;
import com.hjax.kagamine.army.UnitMovementManager;
import com.hjax.kagamine.army.UnitRoleManager;
import com.hjax.kagamine.army.EnemySquadManager;
import com.hjax.kagamine.army.ThreatManager;
import com.hjax.kagamine.army.UnitManager;
import com.hjax.kagamine.build.ZergBuildExecutor;
import com.hjax.kagamine.build.BuildExecutor;
import com.hjax.kagamine.build.BuildPlanner;
import com.hjax.kagamine.build.Composition;
import com.hjax.kagamine.build.TechLevelManager;
import com.hjax.kagamine.build.UpgradeManager;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.economy.EconomyManager;
import com.hjax.kagamine.economy.MiningOptimizer;
import com.hjax.kagamine.enemymodel.EnemyBaseDefense;
import com.hjax.kagamine.enemymodel.EnemyModel;
import com.hjax.kagamine.enemymodel.ResourceTracking;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.game.HjaxUnit;
import com.hjax.kagamine.game.MapAnalysis;
import com.hjax.kagamine.knowledge.Scouting;
import com.hjax.kagamine.knowledge.Wisdom;
import com.hjax.kagamine.knowledge.ZergWisdom;
import com.hjax.kagamine.unitcontrollers.zerg.Creep;
import com.hjax.kagamine.unitcontrollers.zerg.Larva;
import com.hjax.kagamine.unitcontrollers.zerg.Mutalisk;

class Kagamine extends S2Agent {

	private static double time_sum;
	private static int frame;
	private static double max = -1;

	public void onGameFullStart() {
		long startTime = System.nanoTime();
		Game.start_frame(observation(), actions(), query(), debug());
		GameInfoCache.start_frame();
		BaseManager.start_game();
		BuildPlanner.decide_build();
		UpgradeManager.start_game();
		Chat.sendMessage("Hey I'm Kagamine! Good luck and have fun :)");

		try {
			InputStream is = this.getClass().getResourceAsStream("commit.txt");
			Scanner dateScanner = new Scanner(is);
			String date = dateScanner.nextLine();
			dateScanner.close();
			Chat.sendMessage("This version of Kagamine was built on:");
			Chat.sendMessage(date);
		} catch (Exception ignored) {}


		System.out.println("Start game took " + ((System.nanoTime() - startTime) / 1000000.0) + " ms");
	}

	public void onStep() {
		long startTime = System.nanoTime();
		if (Game.get_true_frame() % Constants.FRAME_SKIP == 0) {
			Game.start_frame(observation(), actions(), query(), debug());
			GameInfoCache.start_frame();
			UnitRoleManager.on_frame();
			ResourceTracking.on_frame();
			EnemyModel.on_frame();
			EnemySquadManager.on_frame();
			ThreatManager.on_frame();
			UnitMovementManager.on_frame();
			MiningOptimizer.on_frame();
			BanelingAvoidance.on_frame();
			EnemyBaseDefense.on_frame();
			Mutalisk.on_frame();
			Larva.start_frame();
			Scouting.on_frame();
			ArmyManager.on_frame();
			BaseManager.on_frame();
			EconomyManager.on_frame();
			Creep.start_frame();
			BuildPlanner.on_frame();
			BuildExecutor.on_frame();
			UnitManager.on_frame();
			Game.end_frame();
			
			if (Wisdom.cannon_rush()) {
				Game.write_text("Enemy Strategy: Cannon Rush");
			} else if (Wisdom.worker_rush()) {
				Game.write_text("Enemy Strategy: Worker Rush");
			} else if (Wisdom.proxy_detected()) {
				Game.write_text("Enemy Strategy: Proxy Cheese");
			} else if (Wisdom.all_in_detected()) {
				Game.write_text("Enemy Strategy: All-in");
			} else {
				Game.write_text("Enemy Strategy: Macro");
			}
			
			int[] resources = EnemyModel.resourceEstimate();
			Game.write_text("Enemy Supply: " + EnemyModel.enemySupply());
			Game.write_text("Enemy Army: " + EnemyModel.enemyArmy());
			Game.write_text("Enemy Workers: " + EnemyModel.enemyWorkers());
			Game.write_text("Enemy Bases: " + EnemyModel.enemyBaseCount());
			Game.write_text("Enemy resources : " + resources[0] + " " + resources[1]);
			
			Game.write_text("My army: " + GameInfoCache.attacking_army_supply());
			Game.write_text("Ahead: " + Wisdom.ahead());
			Game.write_text("Should Attack: " + Wisdom.shouldAttack());
			Game.write_text("Next Army Unit: " + ZergBuildExecutor.next_army_unit());
			Game.write_text("Army target: " + ZergWisdom.army_target());
			Game.write_text("Should make units: " + ZergWisdom.should_build_army());
			Game.write_text("Should make drones: " + ZergWisdom.should_build_workers());
			Game.write_text("Should make expand (and defense it): " + ZergWisdom.should_expand());
			Game.write_text("Queens " + GameInfoCache.count(Units.ZERG_QUEEN) + "/" + ZergWisdom.queen_target());
			Game.write_text("Tech Level " + TechLevelManager.getTechLevel().toString());
			Game.write_text("Spending: " + Game.minerals() + " " + Game.gas());
			Game.write_text("Comp: " + Composition.comp().toString());
			
		}

		
		
		if (Constants.DEBUG) {
			Game.debug.sendDebug();
			time_sum += ((System.nanoTime() - startTime) / 1000000.0);
			if (((System.nanoTime() - startTime) / 1000000.0) > max) {
				max = (System.nanoTime() - startTime) / 1000000.0;
			}
			frame++;
			//System.out.println("Average " + (time_sum / frame));
			//System.out.println("Max " + max);
			//System.out.println("----------------------------------");
		}
		
	}

	public void onUnitCreated(UnitInPool unit) {
		BaseManager.on_unit_created(HjaxUnit.getInstance(unit));
	}

	public void onUnitDestroyed(UnitInPool unit) {
		if (unit.unit().getAlliance() == Alliance.ENEMY) {
			EnemyModel.removeFromModel(HjaxUnit.getInstance(unit));
		}
	}

	public void onGameEnd() {
		Counter.print();
	}

}
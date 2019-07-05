package com.hjax.kagamine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.army.ArmyManager;
import com.hjax.kagamine.army.BanelingAvoidance;
import com.hjax.kagamine.army.BaseDefense;
import com.hjax.kagamine.army.EnemySquadManager;
import com.hjax.kagamine.army.ThreatManager;
import com.hjax.kagamine.army.UnitManager;
import com.hjax.kagamine.build.BuildExecutor;
import com.hjax.kagamine.build.BuildPlanner;
import com.hjax.kagamine.build.UpgradeManager;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.economy.EconomyManager;
import com.hjax.kagamine.economy.MiningOptimizer;
import com.hjax.kagamine.enemymodel.EnemyBaseDefense;
import com.hjax.kagamine.enemymodel.EnemyModel;
import com.hjax.kagamine.enemymodel.ResourceTracking;
import com.hjax.kagamine.game.ControlGroups;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.game.MapAnalysis;
import com.hjax.kagamine.knowledge.Scouting;
import com.hjax.kagamine.knowledge.Wisdom;
import com.hjax.kagamine.unitcontrollers.zerg.Creep;
import com.hjax.kagamine.unitcontrollers.zerg.Larva;
import com.hjax.kagamine.unitcontrollers.zerg.Mutalisk;

public class Kagamine extends S2Agent{

	public static double time_sum = 0;
	public static int frame = 0;
	public static double max = -1;
	
	@Override
	public void onGameFullStart() {
		long startTime = System.nanoTime();
		Game.start_frame(observation(), actions(), query(), debug());
		GameInfoCache.start_frame();
		BaseManager.start_game();
		BuildPlanner.decide_build();
		UpgradeManager.start_game();
		Chat.sendMessage("Hey I'm Kagamine! Good luck and have fun :)");
		
		try {
			String date = new String(Files.readAllBytes(Paths.get("commit.txt")));
			Chat.sendMessage("This version of Kagamine was built on:");
			Chat.sendMessage(date);
		} catch (IOException e) {}
		
		System.out.println("Start game took " + ((System.nanoTime() - startTime) / 1000000.0) + " ms");
	}

	@Override
	public void onStep() {
		long startTime = System.nanoTime();
		Game.start_frame(observation(), actions(), query(), debug());
		
		if (Game.get_frame() % Constants.FRAME_SKIP == 0) {
			GameInfoCache.start_frame();
			ResourceTracking.on_frame();
			EnemyModel.on_frame();
			ControlGroups.on_frame();
			MiningOptimizer.on_frame();
			BanelingAvoidance.on_frame();
			EnemyBaseDefense.on_frame();
			Mutalisk.on_frame();
			Larva.start_frame();
			Scouting.on_frame();
			ArmyManager.on_frame();
			ThreatManager.on_frame();
			BaseManager.on_frame();
			EconomyManager.on_frame();
			Creep.start_frame();
			BuildPlanner.on_frame();
			EnemySquadManager.on_frame();
			BaseDefense.on_frame();
			BuildExecutor.on_frame();
			UnitManager.on_frame();
			MapAnalysis.on_frame();
			GameInfoCache.end_frame();
			
			
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
			Game.write_text("Enemy Supply: " + EnemyModel.enemySupply());;
			Game.write_text("Enemy Army: " + EnemyModel.enemyArmy());
			Game.write_text("Enemy Workers: " + EnemyModel.enemyWorkers());
			Game.write_text("Enemy Bases: " + EnemyModel.enemyBaseCount());
			Game.write_text("Enemy resources : " + resources[0] + " " + resources[1]);
			
			Game.write_text("My army: " + GameInfoCache.attacking_army_supply());
			Game.write_text("Ahead: " + Wisdom.ahead());
			Game.write_text("Should Attack: " + Wisdom.shouldAttack());
		}

		
		
		if (Constants.DEBUG) {
			Game.debug.sendDebug();
			time_sum += ((System.nanoTime() - startTime) / 1000000.0);
			if (((System.nanoTime() - startTime) / 1000000.0) > max) {
				max = (System.nanoTime() - startTime) / 1000000.0;
			}
			frame++;
			System.out.println("Average " + (time_sum / frame));
			System.out.println("Max " + max);
			System.out.println("----------------------------------");
		}
		
		Game.end_frame();
	}
	
	@Override
	public void onUnitCreated(UnitInPool u) {
		BaseManager.on_unit_created(u);
	}
	
	@Override
	public void onUnitDestroyed(UnitInPool u) {
		if (u.unit().getAlliance() == Alliance.ENEMY) {
			EnemyModel.removeFromModel(u);
		}
	}
	
	@Override
	public void onGameEnd() {
		Counter.print();
	}
	
	

}
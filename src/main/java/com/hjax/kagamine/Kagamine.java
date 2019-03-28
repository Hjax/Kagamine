package com.hjax.kagamine;

import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.army.ArmyManager;
import com.hjax.kagamine.army.BaseDefense;
import com.hjax.kagamine.army.EnemySquadManager;
import com.hjax.kagamine.army.ThreatManager;
import com.hjax.kagamine.army.UnitManager;
import com.hjax.kagamine.build.BuildExecutor;
import com.hjax.kagamine.build.BuildPlanner;
import com.hjax.kagamine.economy.BaseManager;
import com.hjax.kagamine.economy.EconomyManager;
import com.hjax.kagamine.economy.MiningOptimizer;
import com.hjax.kagamine.game.ControlGroups;
import com.hjax.kagamine.game.Game;
import com.hjax.kagamine.game.GameInfoCache;
import com.hjax.kagamine.game.MapAnalysis;
import com.hjax.kagamine.knowledge.EnemyModel;
import com.hjax.kagamine.knowledge.ResourceTracking;
import com.hjax.kagamine.knowledge.Scouting;
import com.hjax.kagamine.unitcontrollers.zerg.Creep;
import com.hjax.kagamine.unitcontrollers.zerg.Larva;

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
		Scouting.start_game();
		BuildPlanner.decide_build();
		Chat.sendMessage("Hey I'm Kagamine! Good luck and have fun :)");
		Chat.sendMessage("You are playing vs 032519");
		System.out.println("Start game took " + ((System.nanoTime() - startTime) / 1000000.0) + " ms");
	}

	@Override
	public void onStep() {
		long startTime = System.nanoTime();
		Game.start_frame(observation(), actions(), query(), debug());
		
		if ((Game.get_frame() % Constants.FRAME_SKIP) == 0) {
			GameInfoCache.start_frame();
			MiningOptimizer.on_frame();
			ResourceTracking.on_frame();
			EnemyModel.on_frame();
			Larva.start_frame();
			ControlGroups.on_frame();
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
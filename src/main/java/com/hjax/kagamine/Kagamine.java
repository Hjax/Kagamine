package com.hjax.kagamine;

import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.hjax.kagamine.UnitControllers.Creep;
import com.hjax.kagamine.UnitControllers.Larva;

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
		System.out.println("Start game took " + ((System.nanoTime() - startTime) / 1000000.0) + " ms");
	}

	@Override
	public void onStep() {
		long startTime = System.nanoTime();
		//Inference.on_frame();
		Game.start_frame(observation(), actions(), query(), debug());
		if ((Game.get_frame() % Constants.FRAME_SKIP) == 0) {
			GameInfoCache.start_frame();
			Larva.start_frame();
			Scouting.on_frame();
			ArmyManager.on_frame();
			ThreatManager.on_frame();
			BaseManager.on_frame();
			EconomyManager.on_frame();
			MiningOptimizer.on_frame();
			Creep.start_frame();
			BuildPlanner.on_frame();
			EnemySquadManager.on_frame();
			BaseDefense.on_frame();
			BuildExecutor.on_frame();
			UnitManager.on_frame();
			GameInfoCache.end_frame();
		}
		if (!Main.ladder && Constants.DEBUG) {
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
	}
	
	@Override
	public void onUnitCreated(UnitInPool u) {
		BaseManager.on_unit_created(u);
	}
	
	@Override
	public void onGameEnd() {
		Counter.print();
	}

}
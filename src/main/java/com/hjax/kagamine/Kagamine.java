package com.hjax.kagamine;

import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.hjax.kagamine.UnitControllers.Larva;

public class Kagamine extends S2Agent{

	@Override
	public void onGameFullStart() {
		Game.start_frame(observation(), actions(), query(), debug());
		GameInfoCache.start_frame();
		Game.chat("Kagamine 1.0 BETA");
		BaseManager.start_game();
		BuildPlanner.decide_build();
	}

	@Override
	public void onStep() {
		Game.start_frame(observation(), actions(), query(), debug());
		if ((Game.get_frame() % Constants.FRAME_SKIP) == 0) {
			System.out.println("---------------------------");
			long startTime = System.nanoTime();
			GameInfoCache.start_frame();
			System.out.println("GameInfoCache " + (System.nanoTime() - startTime) / 1000000);
			startTime = System.nanoTime();
			Larva.start_frame();
			System.out.println("Larva " + (System.nanoTime() - startTime) / 1000000);
			startTime = System.nanoTime();
			Scouting.on_frame();
			System.out.println("Scouting " + (System.nanoTime() - startTime) / 1000000);
			startTime = System.nanoTime();
			ArmyManager.on_frame();
			System.out.println("ArmyManager " + (System.nanoTime() - startTime) / 1000000);
			startTime = System.nanoTime();
			ThreatManager.on_frame();
			System.out.println("ThreatManager " + (System.nanoTime() - startTime) / 1000000);
			startTime = System.nanoTime();
			BaseManager.on_frame();
			System.out.println("BaseManager " + (System.nanoTime() - startTime) / 1000000);
			startTime = System.nanoTime();
			BuildPlanner.on_frame();
			System.out.println("BuildPlanner " + (System.nanoTime() - startTime) / 1000000);
			startTime = System.nanoTime();
			UnitManager.on_frame();
			System.out.println("UnitManager " + (System.nanoTime() - startTime) / 1000000);
			startTime = System.nanoTime();
			GameInfoCache.end_frame();
			System.out.println("GameInfoCache end " + (System.nanoTime() - startTime) / 1000000);
		}
		
		Game.debug.sendDebug();
		
	}
	
	@Override
	public void onUnitCreated(UnitInPool u) {
		BaseManager.on_unit_created(u);
	}
	
	@Override
	public void onUnitDestroyed(UnitInPool u) {
		BaseManager.on_unit_destroyed(u);
	}
}
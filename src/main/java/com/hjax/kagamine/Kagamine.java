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
		long startTime = System.nanoTime();
		Game.start_frame(observation(), actions(), query(), debug());
		if ((Game.get_frame() % Constants.FRAME_SKIP) == 0) {
			GameInfoCache.start_frame();
			Larva.start_frame();
			Scouting.on_frame();
			ArmyManager.on_frame();
			ThreatManager.on_frame();
			BaseManager.on_frame();
			BuildPlanner.on_frame();
			UnitManager.on_frame();
			GameInfoCache.end_frame();
		}
		
		Game.debug.sendDebug();
		System.out.println((System.nanoTime() - startTime) / 1000000);
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
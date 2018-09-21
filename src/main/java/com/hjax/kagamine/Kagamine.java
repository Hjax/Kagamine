package com.hjax.kagamine;

import com.github.ocraft.s2client.bot.S2Agent;

public class Kagamine extends S2Agent{

	@Override
	public void onGameStart() {
		Game.start_frame(observation(), actions(), query(), debug());
		Game.chat("Kagamine 1.0 BETA");
		BuildPlanner.decide_build();
	}

	@Override
	public void onStep() {
		Game.start_frame(observation(), actions(), query(), debug());
		if ((Game.get_frame() % Constants.FRAME_SKIP) == 0) {
			GameInfoCache.start_frame();
			Scouting.on_frame();
			ArmyManager.on_frame();
			ThreatManager.on_frame();
			BaseManager.on_frame();
			BuildPlanner.on_frame();
			UnitManager.on_frame();
			GameInfoCache.end_frame();
		}
	}

}
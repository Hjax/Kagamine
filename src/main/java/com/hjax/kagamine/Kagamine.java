package com.hjax.kagamine;

import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;

public class Kagamine extends S2Agent{

	@Override
	public void onGameStart() {
		System.out.println("Hello world of Starcraft II bots!");
	}

	@Override
	public void onStep() {
		
		Game.start_frame(observation(), actions(), query(), debug());
		GameInfoCache.start_frame();
		
		for (UnitInPool u: GameInfoCache.all_units.values()) {
			if (!u.isAlive()) {
				System.out.println("Dead " + u.unit().getType().toString());
			}
		}
	}

}
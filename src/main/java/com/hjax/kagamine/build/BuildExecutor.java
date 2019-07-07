package com.hjax.kagamine.build;

import com.github.ocraft.s2client.protocol.game.Race;
import com.hjax.kagamine.game.Game;

public class BuildExecutor {
	public static void on_frame() {
		if (Game.race() == Race.ZERG) {
			ZergBuildExecutor.on_frame();
		}
	}
}

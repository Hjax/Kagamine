package com.hjax.kagamine;

import java.nio.file.Paths;

import com.github.ocraft.s2client.bot.S2Coordinator;
import com.github.ocraft.s2client.protocol.game.Difficulty;
import com.github.ocraft.s2client.protocol.game.LocalMap;
import com.github.ocraft.s2client.protocol.game.Race;
import com.hjax.kagamine.Kagamine;

public class Main {
		public static boolean ladder = true;
	    public static void main(String[] args) {
	        Kagamine bot = new Kagamine();
	        Nothing bot2 = new Nothing();
	        S2Coordinator s2Coordinator;
	        if (ladder) {
	        	System.out.println("Starting ladder game");
		        s2Coordinator = S2Coordinator.setup()
		                .loadLadderSettings(args)
		                .setParticipants(S2Coordinator.createParticipant(Race.ZERG, bot))
		                .connectToLadder()
		                .joinGame();
	        } else {
	        	System.out.println("Starting regular game");
		        s2Coordinator = S2Coordinator.setup()
		                .loadSettings(args)
		                .setRealtime(false)
		                .setParticipants(
		                        //S2Coordinator.createParticipant(Race.TERRAN, bot2),
		                        S2Coordinator.createParticipant(Race.ZERG, bot),
		                        S2Coordinator.createComputer(Race.ZERG, Difficulty.CHEAT_INSANE))
		                .launchStarcraft()
		                .startGame(LocalMap.of(Paths.get("C:\\Program Files (x86)\\StarCraft II\\Maps\\CeruleanFallLE.SC2Map")));
	        }
	        while (s2Coordinator.update()) {
	        }
	        s2Coordinator.quit();
	    }

	}
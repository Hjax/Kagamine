package com.hjax.kagamine;

import com.github.ocraft.s2client.bot.S2Coordinator;
import com.github.ocraft.s2client.protocol.game.Race;

public class LadderMain {
    public static void main(String[] args) {
    	Constants.CHAT = false;
    	Constants.DEBUG = false;
        Kagamine bot = new Kagamine();
        S2Coordinator s2Coordinator;
            System.out.println("Starting ladder game");
            s2Coordinator = S2Coordinator.setup()
                    .loadLadderSettings(args)
                    .setParticipants(S2Coordinator.createParticipant(Race.ZERG, bot))
                    .connectToLadder()
                    .joinGame();

        while (s2Coordinator.update()) {
        }
        s2Coordinator.quit();
    }

}
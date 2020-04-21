package com.hjax.kagamine;

import java.nio.file.Paths;
import java.util.Scanner;

import com.github.ocraft.s2client.bot.S2Coordinator;
import com.github.ocraft.s2client.protocol.game.AiBuild;
import com.github.ocraft.s2client.protocol.game.Difficulty;
import com.github.ocraft.s2client.protocol.game.LocalMap;
import com.github.ocraft.s2client.protocol.game.Race;

public class Main {

		public static void main(String[] args) {
			Kagamine bot = new Kagamine();
			Nothing bot2 = new Nothing();
			S2Coordinator s2Coordinator;
			System.out.println("Starting regular game");
			System.out.println("Enter the race you would like the human to play");
			Scanner input = new Scanner(System.in);
			Race choice = Race.NO_RACE;
			while (choice == Race.NO_RACE) {
				String current = input.nextLine();
				switch (current.toLowerCase()) {
					case "terran":
						choice = Race.TERRAN;
						break;
					case "protoss":
						choice = Race.PROTOSS;
						break;
					case "zerg":
						choice = Race.ZERG;
						break;
					case "random":
						choice = Race.RANDOM;
						break;
				}
			}
			input.close();
			s2Coordinator = S2Coordinator.setup()
					.loadSettings(args)
					.setRealtime(false)
					.setNeedsSupportDir(true)
					.setRawAffectsSelection(true)
					.setTimeoutMS(600 * 1000)
					//.setProcessPath(Paths.get("C:\\Ladder\\4.8.4\\StarCraft II\\Versions\\Base73286\\SC2_x64.exe"))
					.setParticipants(
							//S2Coordinator.createParticipant(choice, bot2),
							S2Coordinator.createParticipant(Race.ZERG, bot),
							S2Coordinator.createComputer(choice, Difficulty.VERY_HARD, AiBuild.MACRO))
					.launchStarcraft()
					.startGame(LocalMap.of(Paths.get("KingsCoveLE.SC2Map")));
			while (s2Coordinator.update()) {
			}
			s2Coordinator.quit();
		}

}
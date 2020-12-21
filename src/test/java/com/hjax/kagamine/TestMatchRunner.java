package com.hjax.kagamine;

import com.github.ocraft.s2client.bot.S2Coordinator;
import com.github.ocraft.s2client.protocol.game.AiBuild;
import com.github.ocraft.s2client.protocol.game.Difficulty;
import com.github.ocraft.s2client.protocol.game.LocalMap;
import com.github.ocraft.s2client.protocol.game.Race;

import java.nio.file.Paths;

public class TestMatchRunner {

    public static void main(String[] args) {
        new TestMatchRunner().run();
    }

    private String replayFolder = "./replays/";
    private String map = "EverDreamLE.SC2Map";
    private Race opponentRace = Race.RANDOM;
    private Difficulty opponentDifficulty = Difficulty.VERY_HARD;
    private AiBuild opponentBuild = AiBuild.RANDOM_BUILD;
    private String[] loadSettingsArgs = new String[]{};

    public TestMatchRunner setReplayFolder(String replayFolder) {
        this.replayFolder = replayFolder;
        return this;
    }

    public TestMatchRunner setMap(String map) {
        this.map = map;
        return this;
    }

    public TestMatchRunner setOpponentRace(Race opponentRace) {
        this.opponentRace = opponentRace;
        return this;
    }

    public TestMatchRunner setOpponentDifficulty(Difficulty opponentDifficulty) {
        this.opponentDifficulty = opponentDifficulty;
        return this;
    }

    public TestMatchRunner setOpponentBuild(AiBuild opponentBuild) {
        this.opponentBuild = opponentBuild;
        return this;
    }

    public TestMatchRunner setLoadSettingsArgs(String[] loadSettingsArgs) {
        this.loadSettingsArgs = loadSettingsArgs;
        return this;
    }

    public void run() {
        Kagamine bot = new Kagamine();
        S2Coordinator s2Coordinator;
        s2Coordinator = S2Coordinator.setup()
                                     .loadSettings(loadSettingsArgs)
                                     .setRealtime(false)
                                     .setNeedsSupportDir(true)
                                     .setRawAffectsSelection(true)
                                     .setTimeoutMS(600 * 1000)
                                     .setParticipants(
                                             S2Coordinator.createParticipant(Race.ZERG, bot),
                                             S2Coordinator.createComputer(this.opponentRace, this.opponentDifficulty, this.opponentBuild))
                                     .launchStarcraft()
                                     .startGame(LocalMap.of(Paths.get(map)));
        while (s2Coordinator.update()) {
        }

        try {
            // Is this the right way to save a replay?
            s2Coordinator.saveReplayList(Paths.get(Util.getReplayFilename(this.replayFolder)));
        } catch (Exception e) {
            System.out.println("Failed to save replay file!");
            e.printStackTrace();
        }

        s2Coordinator.quit();
    }
}

package com.hjax.kagamine.UnitControllers;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.hjax.kagamine.Game;
import com.hjax.kagamine.GameInfoCache;

import java.util.ArrayList;
import java.util.List;

public class Larva {
    private static List<UnitInPool> larva = new ArrayList<>();
    private static int larva_index = 0;

    public static void start_frame() {
        larva_index = 0;
        larva.clear();
        larva.addAll(GameInfoCache.get_units(Alliance.SELF, Units.ZERG_LARVA));
    }

    public static boolean has_larva() {
        return larva_index < larva.size();
    }

    public static void on_frame(UnitInPool u) {

    }

    public static void produce_unit(UnitType type) {
        Game.unit_command(larva.get(larva_index), Game.get_unit_type_data().get(type).getAbility().get());
        larva_index++;
    }
}

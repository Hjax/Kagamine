package com.hjax.kagamine.build;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import com.github.ocraft.s2client.protocol.data.UnitType;

public class Build {
	public static int build_index = 0;
	public static int ideal_gases = 8;
	public static boolean pull_off_gas = false;
	public static List<Pair<Integer, UnitType>> build = new ArrayList<>();
}

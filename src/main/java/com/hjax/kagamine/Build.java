package com.hjax.kagamine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Upgrade;


public class Build {
	public static int build_index = 0;
	public static int ideal_hatches = 0;
	public static int ideal_workers = 20;
	public static int ideal_gases = 6;
	public static int push_supply = 20;
	public static int max_queens = -1;
	public static int tech_drones = 30;
	public static boolean scout = true;
	public static boolean pull_off_gas = false;
	public static List<UnitType> composition = new ArrayList<>();
	public static Set<Upgrade> upgrades = new HashSet<>();
	public static List<Pair<Integer, UnitType>> build = new ArrayList<>();
}

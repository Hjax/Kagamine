package com.hjax.kagamine.build;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import com.github.ocraft.s2client.protocol.data.UnitType;

public class Build {
	public static int build_index = 0;
	public static int ideal_hatches = 0;
	public static int ideal_workers = 20;
	public static int ideal_gases = 6;
	public static int push_supply = 20;
	public static int max_queens = -1;
	public static int tech_drones = 30;
	public static boolean two_base_tech = false;
	public static boolean scout = true;
	public static boolean pull_off_gas = false;
	//public static List<UnitType> composition = new ArrayList<>();
	public static List<Pair<Integer, UnitType>> build = new ArrayList<>();
}

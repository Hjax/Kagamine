package com.hjax.kagamine;

import java.util.HashSet;
import java.util.Set;

import com.github.ocraft.s2client.protocol.data.UnitType;

public class Build {
	public static int build_index = 0;
	public static int ideal_hatches = 0;
	public static int ideal_workers = 20;
	public static int push_supply = 20;
	public static int max_queens = 0;
	public static boolean scout = true;
	public static boolean pull_off_gas = true;
	public static Set<UnitType> composition = new HashSet<>();
}

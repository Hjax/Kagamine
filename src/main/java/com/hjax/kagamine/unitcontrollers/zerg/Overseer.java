package com.hjax.kagamine.unitcontrollers.zerg;

import com.hjax.kagamine.army.ArmyManager;
import com.hjax.kagamine.army.BaseDefense;
import com.hjax.kagamine.game.HjaxUnit;

public class Overseer {

	public static void on_frame(HjaxUnit u) {
		if (BaseDefense.assignments.containsKey(u.tag())) {
			u.move(BaseDefense.assignments.get(u.tag()));
		} else if (u.distance(ArmyManager.army_center) > 5) {
			u.move(ArmyManager.army_center);
		}
	}
}

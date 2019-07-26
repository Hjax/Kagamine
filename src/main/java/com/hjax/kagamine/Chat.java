package com.hjax.kagamine;

import java.util.HashSet;
import java.util.Set;

import com.hjax.kagamine.game.Game;

public class Chat {
	
	private static final Set<String> sent = new HashSet<>();
	
	public static void sendMessage(String message) {
		if (!sent.contains(message)) {
			sent.add(message);
			Game.chat(message);
		}
	}
}

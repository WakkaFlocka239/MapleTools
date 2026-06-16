package me.wakka.mapletools;

public class Debug {
	private static boolean enabled = false;

	public static void debug(Object message) {
		debug(String.valueOf(message));
	}

	public static void debug(String message) {
		if (!enabled)
			return;

		System.out.println(message);
	}

	public static void force(Object message) {
		force(String.valueOf(message));
	}

	public static void force(String message) {
		System.out.println(message);
	}

	public static void toggle(boolean bool) {
		enabled = bool;
	}
}

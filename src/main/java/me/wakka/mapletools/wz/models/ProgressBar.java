package me.wakka.mapletools.wz.models;

public class ProgressBar {
	private final int total;
	private int current = 0;
	private int lastFilled = 0;

	public ProgressBar(int total) {
		this.total = total;
	}

	public void step(String label) {
		current++;

		int width = 30;
		int filled = (int) ((current / (double) total) * width);
		int percent = (int) ((current / (double) total) * 100);

		if (lastFilled == filled && current < total)
			return;

		lastFilled= filled;

		String bar = "[" +
			"=".repeat(filled) +
			" ".repeat(width - filled) +
			"]";

		System.out.println(bar + " " + percent + "% " + current + "/" + total + " " + label);

		if (current >= total) {
			System.out.println();
		}
	}
}

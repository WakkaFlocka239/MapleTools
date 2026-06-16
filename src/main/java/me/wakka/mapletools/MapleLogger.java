package me.wakka.mapletools;

import me.wakka.mapletools.feature.ui.panels.LogPanel;

public class MapleLogger {

	private LogPanel logPanel;

	public void attach(LogPanel panel) {
		this.logPanel = panel;
	}

	public void log(String message) {
		if (logPanel != null) {
			logPanel.log(message);
		}
	}

	public void info(String message) {
		if (logPanel != null)
			logPanel.info(message);
	}

	public void warn(String message) {
		if (logPanel != null)
			logPanel.warn(message);
	}

	public void error(String message) {
		if (logPanel != null)
			logPanel.error(message);
	}
}

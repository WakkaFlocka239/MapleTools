package me.wakka.mapletools.feature.ui.panels;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import me.wakka.mapletools.data.MapleSession;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class LogPanel extends MapleToolPanel {

	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

	@Override
	public String getPanelId() {
		return "log";
	}

	private final TextArea logArea = new TextArea();


	public LogPanel(MapleSession session) {
		super("Log", session);

		setPrefSize(500, 250);
		setMinSize(250, 120);

		logArea.setEditable(false);
		logArea.setWrapText(true);

		logArea.setStyle("""
    -fx-control-inner-background: #1e1e1e;
    -fx-background-color: transparent;
    -fx-text-fill: white;
    -fx-font-size: 14px;
    -fx-highlight-fill: #444;
    -fx-highlight-text-fill: white;
""");

		getChildren().add(logArea);

		VBox.setVgrow(logArea, Priority.ALWAYS);
		logArea.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
	}

	public void log(String message) {
		Platform.runLater(() -> {
			String timestamp = LocalTime.now().format(TIME_FORMAT);

			logArea.appendText(
				"[" + timestamp + "] " + message + "\n"
			);

			logArea.setScrollTop(Double.MAX_VALUE);
		});
	}

	private void log(String level, String message) {
		if (level != null && !level.isEmpty()) {
			level = "[" + level + "] ";
		}

		String finalLevel = level;
		Platform.runLater(() -> {
			String timestamp = LocalTime.now().format(TIME_FORMAT);

			logArea.appendText(
				"[" + timestamp + "] " + finalLevel + message + "\n"
			);

			logArea.setScrollTop(Double.MAX_VALUE);
		});
	}

	public void info(String message) {
		log("INFO", message);
	}

	public void warn(String message) {
		log("WARN", message);
	}

	public void error(String message) {
		log("ERROR", message);
	}
}

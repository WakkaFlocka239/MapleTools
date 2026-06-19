package me.wakka.mapletools;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import lombok.SneakyThrows;
import me.wakka.mapletools.feature.ui.panels.LogPanel;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class MapleLogger {

	public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

	private LogPanel logPanel;
	private final Path logFile;

	public MapleLogger() {
		this.logFile = Path.of("logs", "mapletools-" + LocalDate.now() + ".log");

		try {
			Files.createDirectories(logFile.getParent());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void attach(LogPanel panel) {
		this.logPanel = panel;
	}

	public void log(String message) {
		write(null, message);
	}

	public void info(String message) {
		write(LogLevel.INFO, message);
	}

	public void warn(String message) {
		write(LogLevel.WARN, message);
	}

	public void error(String message) {
		write(LogLevel.ERROR, message);
	}

	public void note(String message) {
		write(LogLevel.NOTE, message);
	}

	public void clear(){
		logPanel.clear();
	}

	public void copy(){
		ClipboardContent content = new ClipboardContent();
		content.putString(logPanel.getLogArea().getText());

		Clipboard.getSystemClipboard().setContent(content);
	}

	@SneakyThrows
	public void openFolder(){
		Desktop.getDesktop().open(Path.of("logs").toFile());
	}

	private void write(LogLevel level, String message) {
		String timestamp = LocalTime.now().format(TIME_FORMAT);
		String levelText = level == null ? "" : "[" + level.name() + "] ";

		String line = "[" + timestamp + "] " + levelText + message;

		writeToFile(line);

		if (logPanel != null)
			logPanel.append(line);
	}

	private void writeToFile(String line) {
		try {
			Files.writeString(logFile, line + System.lineSeparator(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public enum LogLevel {
		INFO,
		WARN,
		ERROR,
		NOTE,
		;
	}
}

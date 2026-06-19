package me.wakka.mapletools;

import lombok.SneakyThrows;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MapleLogger {

	public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

	private final List<LogListener> listeners = new ArrayList<>();
	private final Path logFile;

	public MapleLogger() {
		this.logFile = Path.of("logs", "mapletools-" + LocalDate.now() + ".log");

		try {
			Files.createDirectories(logFile.getParent());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void addListener(LogListener listener) {
		listeners.add(listener);
	}

	public void removeListener(LogListener listener) {
		listeners.remove(listener);
	}

	private void notifyListeners(String line) {
		for (LogListener listener : listeners)
			listener.onLog(line);
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

	@SneakyThrows
	public void openFolder(){
		Desktop.getDesktop().open(Path.of("logs").toFile());
	}

	private void write(LogLevel level, String message) {
		String timestamp = LocalTime.now().format(TIME_FORMAT);
		String levelText = level == null ? "" : "[" + level.name() + "] ";

		String line = "[" + timestamp + "] " + levelText + message;

		writeToFile(line);
		notifyListeners(line);
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

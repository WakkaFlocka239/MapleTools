package me.wakka.mapletools.feature.ui.panels;

import javafx.application.Platform;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import lombok.Getter;
import me.wakka.mapletools.LogListener;
import me.wakka.mapletools.data.MapleSession;

import java.util.List;

public class LogPanel extends MapleToolPanel implements LogListener {

	@Getter
	private final TextArea logArea = new TextArea();
	private final TextField inputField = new TextField();

	@Override
	public String getPanelId() {
		return "log";
	}

	@Override
	public boolean isPanelResizable() {
		return true;
	}

	@Override
	public List<MenuItem> getSettings() {
		return List.of(
			menuItem("Clear Log", e -> clear()),
			menuItem("Copy Log", e -> copy()),
			menuItem("Open Folder", e -> session.getLogger().openFolder())
		);
	}

	public LogPanel(MapleSession session) {
		super("Log", session);

		session.getLogger().addListener(this);

		setPrefSize(500, 250);
		setMinSize(250, 120);

		logArea.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		logArea.setEditable(false);
		logArea.setWrapText(true);
		logArea.getStyleClass().add("log-area");

		inputField.setPromptText("Add note...");
		inputField.setOnAction(e -> submitInput());
		inputField.getStyleClass().add("log-input");

		content.getChildren().addAll(logArea, inputField);
	}

	private void submitInput() {
		String text = inputField.getText().trim();

		if (text.isEmpty())
			return;

		session.getLogger().note(text);

		inputField.clear();
	}

	public void append(String line) {
		Platform.runLater(() -> {
			logArea.appendText(line + "\n");
			logArea.setScrollTop(Double.MAX_VALUE);
		});
	}

	@Override
	public void onLog(String line) {
		Platform.runLater(() -> append(line));
	}

	public void clear() {
		logArea.clear();
	}

	public void copy() {
		ClipboardContent content = new ClipboardContent();
		content.putString(logArea.getText());

		Clipboard.getSystemClipboard().setContent(content);
	}
}

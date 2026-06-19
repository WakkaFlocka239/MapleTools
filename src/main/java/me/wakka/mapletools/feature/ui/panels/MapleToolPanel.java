package me.wakka.mapletools.feature.ui.panels;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import me.wakka.mapletools.data.MapleSession;

import java.util.Collections;
import java.util.List;

public abstract class MapleToolPanel extends BorderPane {

	protected final MapleSession session;
	protected final VBox content = new VBox(8);
	protected final Button settingsButton = new Button("⚙");
	protected final ContextMenu settingsMenu = new ContextMenu();

	public abstract String getPanelId();

	public boolean isResizeable() {
		return false;
	}

	public List<MenuItem> getSettings() {
		return Collections.emptyList();
	}

	public MapleToolPanel(String title, MapleSession session) {
		this.session = session;

		HBox header = new HBox();

		Label titleLabel = new Label(title);
		titleLabel.getStyleClass().add("title");

		setupSettingsButton();

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		setPadding(new Insets(10));
		getStyleClass().add("panel");

		header.getChildren().addAll(titleLabel, spacer, settingsButton);
		setTop(header);
		setCenter(content);
	}

	private void setupSettingsButton() {
		List<MenuItem> settings = getSettings();

		settingsButton.setOnMouseEntered(e -> settingsButton.setStyle("-fx-border-color: #66aaff; m-fx-border-width: 1;"));
		settingsButton.setOnMouseExited(e -> settingsButton.setStyle("-fx-border-width: 1;"));

		settingsButton.setOnAction(e -> {
			if (settingsMenu.isShowing()) {
				settingsMenu.hide();
				return;
			}
			settingsMenu.getItems().setAll(settings);
			settingsMenu.show(settingsButton, Side.BOTTOM, 0, 0);
		});

		settingsButton.setVisible(!settings.isEmpty());
		settingsButton.setManaged(!settings.isEmpty());

		settingsMenu.setAutoHide(true);

	}

	protected MenuItem menuItem(String title, EventHandler<ActionEvent> action) {
		MenuItem item = new MenuItem(title);
		item.setOnAction(action);
		return item;
	}
}

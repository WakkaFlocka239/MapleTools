package me.wakka.mapletools.feature.ui.panels;

import javafx.scene.control.Label;
import me.wakka.mapletools.data.MapleSession;

public class HealthPanel extends MapleToolPanel {

	@Override
	public String getPanelId() {
		return "health";
	}

	private final Label label = new Label("HP: -- / --");

	public HealthPanel(MapleSession session) {
		super("Health", session);

		setPrefSize(250, 100);

		label.setStyle("-fx-text-fill: white;");
		getChildren().add(label);
	}

	public void update(int current, int max) {
		label.setText("HP: " + current + " / " + max);
	}
}

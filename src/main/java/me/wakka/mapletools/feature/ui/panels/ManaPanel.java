package me.wakka.mapletools.feature.ui.panels;

import javafx.scene.control.Label;
import me.wakka.mapletools.data.MapleSession;

public class ManaPanel extends MapleToolPanel {

	@Override
	public String getPanelId() {
		return "mana";
	}

	private final Label label = new Label("MP: -- / --");

	public ManaPanel(MapleSession session) {
		super("Mana", session);

		setPrefSize(250, 100);

		label.setStyle("-fx-text-fill: white;");
		getChildren().add(label);
	}

	public void update(int current, int max) {
		label.setText("MP: " + current + " / " + max);
	}
}

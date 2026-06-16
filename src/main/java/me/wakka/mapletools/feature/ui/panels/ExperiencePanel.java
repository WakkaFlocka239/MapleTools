package me.wakka.mapletools.feature.ui.panels;

import javafx.scene.control.Label;
import me.wakka.mapletools.data.MapleSession;

public class ExperiencePanel extends MapleToolPanel {

	@Override
	public String getPanelId() {
		return "experience";
	}

	private final Label label = new Label("XP: --");

	public ExperiencePanel(MapleSession session) {
		super("Experience", session);

		setPrefSize(250, 100);

		label.setStyle("-fx-text-fill: white;");
		getChildren().add(label);
	}

	public void update(double current) {
		label.setText("EXP: " + current);
	}
}

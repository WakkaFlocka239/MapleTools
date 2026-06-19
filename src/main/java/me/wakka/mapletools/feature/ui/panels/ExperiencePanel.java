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

		content.getChildren().add(label);

		session.getCurrentExpPercent().addListener((obs, old, current) -> update());

		update();
	}

	public void update() {
		label.setText("EXP: " + session.getCurrentExpPercent().get());
	}
}

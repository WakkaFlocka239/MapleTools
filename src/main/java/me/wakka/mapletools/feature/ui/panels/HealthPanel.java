package me.wakka.mapletools.feature.ui.panels;

import javafx.scene.control.Label;
import me.wakka.mapletools.data.MapleSession;

public class HealthPanel extends MapleToolPanel {

	@Override
	public String getPanelId() {
		return "health";
	}

	private final Label label = valueLabel("HP: -- / --");

	public HealthPanel(MapleSession session) {
		super("Health", session);

		setPrefSize(250, 100);

		session.getCurrentHp().addListener((obs, old, current) -> update());
		session.getCurrentMaxHp().addListener((obs, old, current) -> update());

		update();
	}

	public void update() {
		label.setText("HP: " + session.getCurrentHp().get() + " / " + session.getCurrentMaxHp().get());
	}
}

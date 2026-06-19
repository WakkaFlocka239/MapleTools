package me.wakka.mapletools.feature.ui.panels;

import javafx.scene.control.Label;
import me.wakka.mapletools.data.MapleSession;

public class ManaPanel extends MapleToolPanel {

	@Override
	public String getPanelId() {
		return "mana";
	}

	private final Label label = valueLabel("MP: -- / --");

	public ManaPanel(MapleSession session) {
		super("Mana", session);

		setPrefSize(250, 100);

		session.getCurrentMp().addListener((obs, old, current) -> update());
		session.getCurrentMaxMp().addListener((obs, old, current) -> update());

		update();
	}

	public void update() {
		label.setText("MP: " + session.getCurrentMp().get() + " / " + session.getCurrentMaxMp().get());
	}
}

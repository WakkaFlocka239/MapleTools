package me.wakka.mapletools.feature.ui.panels;

import javafx.scene.control.Label;
import me.wakka.mapletools.data.MapleSession;
import me.wakka.mapletools.models.MapleMap;

public class LocationPanel extends MapleToolPanel {

	@Override
	public String getPanelId() {
		return "location";
	}

	private final Label streetLabel = new Label("Street: ???");
	private final Label mapLabel = new Label("Map: ???");
	private final Label mapIdLabel = new Label("ID: ???");

	public LocationPanel(MapleSession session) {
		super("Location", session);

		setPrefSize(300, 100);

		content.getChildren().addAll(streetLabel, mapLabel, mapIdLabel);

		session.getCurrentMap().addListener((obs, old, current) -> update());

		update();
	}

	public void update() {
		MapleMap currentMap = session.getCurrentMap().get();
		if (currentMap == null)
			return;

		streetLabel.setText("Street: " + currentMap.getStreetName());
		mapLabel.setText("Map: " + currentMap.getMapName());
		mapIdLabel.setText("ID: " + currentMap.getMapId());
	}
}

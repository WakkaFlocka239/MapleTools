package me.wakka.mapletools.feature.ui.panels;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import me.wakka.mapletools.data.MapleSession;

public abstract class MapleToolPanel extends VBox {

	protected final MapleSession session;

	public abstract String getPanelId();

	public MapleToolPanel(String title, MapleSession session) {
		this.session = session;

		setPadding(new Insets(10));
		setSpacing(10);

		setStyle("""
            -fx-background-color: #2b2b2b;
            -fx-background-radius: 8;
            -fx-border-color: #444;
            -fx-border-radius: 8;
        """);

		Label titleLabel = new Label(title);
		titleLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

		getChildren().add(titleLabel);
	}
}

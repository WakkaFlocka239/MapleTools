package me.wakka.mapletools.feature.ui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import me.wakka.mapletools.data.MapleSession;
import me.wakka.mapletools.feature.ui.panels.ExperiencePanel;
import me.wakka.mapletools.feature.ui.panels.HealthPanel;
import me.wakka.mapletools.feature.ui.panels.LocationPanel;
import me.wakka.mapletools.feature.ui.panels.LogPanel;
import me.wakka.mapletools.feature.ui.panels.ManaPanel;

import java.util.Objects;

public class MapleToolsWindow extends BorderPane {

	private final MapleSession session;

	public MapleToolsWindow(MapleSession session) {
		this.session = session;
	}

	public void start(Stage stage) {
		Scene scene = new Scene(this, 1000, 750);
		scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/mapletools.css")).toExternalForm());

		stage.setTitle("MapleTools");
		stage.setScene(scene);
		stage.setResizable(true);
		stage.show();

		FlowPane dashboard = new FlowPane();
		dashboard.setHgap(10);
		dashboard.setVgap(10);
		dashboard.setPadding(new Insets(10));

		Pane dragLayer = new Pane();
		dragLayer.setMouseTransparent(true);
		dragLayer.setPickOnBounds(false);
		dragLayer.prefWidthProperty().bind(dashboard.widthProperty());
		dragLayer.prefHeightProperty().bind(dashboard.heightProperty());

		HealthPanel healthPanel = new HealthPanel(session);
		ManaPanel manaPanel = new ManaPanel(session);
		ExperiencePanel expPanel = new ExperiencePanel(session);
		LocationPanel locationPanel = new LocationPanel(session);
		LogPanel logPanel = new LogPanel(session);

		PanelDragController dragController = new PanelDragController(scene, dashboard, dragLayer);
		dragController.makeDraggable(healthPanel);
		dragController.makeDraggable(manaPanel);
		dragController.makeDraggable(expPanel);
		dragController.makeDraggable(locationPanel);
		dragController.makeDraggable(logPanel);

		dashboard.getChildren().addAll(healthPanel, manaPanel, expPanel, locationPanel, logPanel);

		StackPane root = new StackPane();
		root.getChildren().addAll(dashboard, dragLayer);
		setCenter(root);

		session.getLogger().info("MapleTools started");
	}
}

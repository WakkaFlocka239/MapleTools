package me.wakka.mapletools.feature.ui;

import javafx.animation.ScaleTransition;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import me.wakka.mapletools.data.MapleSession;
import me.wakka.mapletools.feature.ui.panels.ExperiencePanel;
import me.wakka.mapletools.feature.ui.panels.HealthPanel;
import me.wakka.mapletools.feature.ui.panels.LocationPanel;
import me.wakka.mapletools.feature.ui.panels.LogPanel;
import me.wakka.mapletools.feature.ui.panels.ManaPanel;

public class MapleToolsWindow extends BorderPane {

	private final MapleSession session;
	private ImageView activeGhost;
	private Pane activePanel;
	private Pane activeDragLayer;
	private FlowPane dashboard;
	private double grabX;
	private double grabY;

	public MapleToolsWindow(MapleSession session) {
		this.session = session;
	}

	public void start(Stage stage) {
		Scene scene = new Scene(this, 1000, 750);

		scene.setOnMouseDragged(e -> {
			if (activeGhost == null || activePanel == null)
				return;

			moveGhost(activeGhost, activeDragLayer, grabX, grabY, e.getSceneX(), e.getSceneY());
			reorderPanel(activePanel, dashboard, e.getSceneX(), e.getSceneY());
			moveGhost(activeGhost, activeDragLayer, grabX, grabY, e.getSceneX(), e.getSceneY());

			e.consume();
		});

		scene.setOnMouseReleased(e -> {
			if (activePanel != null)
				activePanel.setOpacity(1.0);

			if (activeGhost != null && activeDragLayer != null)
				activeDragLayer.getChildren().remove(activeGhost);

			activeGhost = null;
			activePanel = null;
			dashboard = null;
			activeDragLayer = null;
		});

		stage.setTitle("MapleTools");
		stage.setScene(scene);
		stage.setResizable(true);
		scene.setFill(Color.rgb(43, 43, 43));
		stage.show();

		StackPane root = new StackPane();
		FlowPane dashboard = new FlowPane();
		Pane dragLayer = new Pane();

		dragLayer.setMouseTransparent(true);
		dragLayer.setPickOnBounds(false);
		dragLayer.prefWidthProperty().bind(dashboard.widthProperty());
		dragLayer.prefHeightProperty().bind(dashboard.heightProperty());

		dashboard.setHgap(10);
		dashboard.setVgap(10);
		dashboard.setPadding(new Insets(10));

		HealthPanel healthPanel = new HealthPanel(session);
		ManaPanel manaPanel = new ManaPanel(session);
		ExperiencePanel expPanel = new ExperiencePanel(session);
		LocationPanel locationPanel = new LocationPanel(session);
		LogPanel logPanel = new LogPanel(session);
		session.logger().attach(logPanel);

		makeDraggable(healthPanel, dashboard, dragLayer);
		makeDraggable(manaPanel, dashboard, dragLayer);
		makeDraggable(expPanel, dashboard, dragLayer);
		makeDraggable(locationPanel, dashboard, dragLayer);
		makeDraggable(logPanel, dashboard, dragLayer);

		dashboard.getChildren().addAll(healthPanel, manaPanel, expPanel, locationPanel, logPanel);

		root.getChildren().addAll(dashboard, dragLayer);
		setCenter(root);

		logPanel.info("MapleTools started.");
	}

	private void makeDraggable(Pane panel, FlowPane daskboard, Pane dragLayer) {
		panel.setOnMousePressed(e -> {
			Point2D mouseInPanel = panel.sceneToLocal(e.getSceneX(), e.getSceneY());
			grabX = mouseInPanel.getX();
			grabY = mouseInPanel.getY();

			activePanel = panel;
			dashboard = daskboard;
			activeDragLayer = dragLayer;

			WritableImage image = panel.snapshot(null, null);

			activeGhost = new ImageView(image);
			activeGhost.setOpacity(0.75);
			activeGhost.setMouseTransparent(true);

			dragLayer.getChildren().add(activeGhost);
			panel.setOpacity(0.25);

			moveGhost(activeGhost, dragLayer, grabX, grabY, e.getSceneX(), e.getSceneY());

			e.consume();
		});
	}

	private void reorderPanel(Pane draggedPanel, FlowPane dashboard, double sceneX, double sceneY) {
		for (Node node : dashboard.getChildren()) {
			if (node == draggedPanel)
				continue;

			Bounds bounds = node.localToScene(node.getBoundsInLocal());

			if (bounds.contains(sceneX, sceneY)) {
				node.setStyle("""
					-fx-background-color: #3a3a3a;
					-fx-background-radius: 8;
				""");

				int draggedIndex = dashboard.getChildren().indexOf(draggedPanel);
				int targetIndex = dashboard.getChildren().indexOf(node);

				double centerX = bounds.getMinX() + bounds.getWidth() / 2;
				boolean insertAfter = sceneX > centerX;

				int insertIndex = insertAfter ? targetIndex + 1 : targetIndex;

				if (draggedIndex < insertIndex)
					insertIndex--;

				if (draggedIndex == insertIndex)
					return;

				dashboard.getChildren().remove(draggedPanel);
				dashboard.getChildren().add(insertIndex, draggedPanel);

				animatePanels(dashboard);

				return;
			}
		}
	}

	private void moveGhost(ImageView ghost, Pane dragLayer, double grabX, double grabY, double sceneX, double sceneY) {
		Point2D point = dragLayer.sceneToLocal(sceneX, sceneY);

		ghost.setLayoutX(point.getX() - grabX);
		ghost.setLayoutY(point.getY() - grabY);
	}

	private void animatePanels(FlowPane dashboard) {
		for (Node node : dashboard.getChildren()) {
			node.setScaleX(0.97);
			node.setScaleY(0.97);

			ScaleTransition transition = new ScaleTransition(Duration.millis(100), node);
			transition.setToX(1.0);
			transition.setToY(1.0);
			transition.play();
		}
	}
}

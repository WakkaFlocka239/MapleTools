package me.wakka.mapletools.feature.ui;

import javafx.animation.ScaleTransition;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

public class PanelDragController {

	private FlowPane dashboard;
	private Pane dragLayer;

	private ImageView activeGhost;
	private Pane activePanel;

	private double grabX;
	private double grabY;

	public PanelDragController(Scene scene, FlowPane dashboard, Pane dragLayer) {
		this.dashboard = dashboard;
		this.dragLayer = dragLayer;

		scene.setOnMouseDragged(e -> {
			if (activeGhost == null || activePanel == null)
				return;

			moveGhost(e.getSceneX(), e.getSceneY());
			reorderPanel(e.getSceneX(), e.getSceneY());
			moveGhost(e.getSceneX(), e.getSceneY());

			e.consume();
		});

		scene.setOnMouseReleased(e -> finishDrag());
	}

	public void makeDraggable(Pane panel) {
		panel.setOnMousePressed(e -> {
			Point2D mouseInPanel = panel.sceneToLocal(e.getSceneX(), e.getSceneY());

			grabX = mouseInPanel.getX();
			grabY = mouseInPanel.getY();

			activePanel = panel;

			WritableImage image = panel.snapshot(null, null);
			activeGhost = new ImageView(image);
			activeGhost.setOpacity(0.75);
			activeGhost.setMouseTransparent(true);

			dragLayer.getChildren().add(activeGhost);
			panel.setOpacity(0.25);

			moveGhost(e.getSceneX(), e.getSceneY());

			e.consume();
		});
	}

	private void finishDrag() {
		if (activePanel != null)
			activePanel.setOpacity(1.0);

		if (activeGhost != null)
			dragLayer.getChildren().remove(activeGhost);

		activeGhost = null;
		activePanel = null;
	}

	private void reorderPanel(double sceneX, double sceneY) {
		for (Node node : dashboard.getChildren()) {
			if (node == activePanel)
				continue;

			Bounds bounds = node.localToScene(node.getBoundsInLocal());

			if (!bounds.contains(sceneX, sceneY))
				continue;

			int draggedIndex = dashboard.getChildren().indexOf(activePanel);
			int targetIndex = dashboard.getChildren().indexOf(node);

			double centerX = bounds.getMinX() + bounds.getWidth() / 2;
			boolean insertAfter = sceneX > centerX;

			int insertIndex = insertAfter ? targetIndex + 1 : targetIndex;

			if (draggedIndex < insertIndex)
				insertIndex--;

			if (draggedIndex == insertIndex)
				return;

			dashboard.getChildren().remove(activePanel);
			dashboard.getChildren().add(insertIndex, activePanel);

			animatePanels();
			return;
		}
	}

	private void moveGhost(double sceneX, double sceneY) {
		Point2D point = dragLayer.sceneToLocal(sceneX, sceneY);

		activeGhost.setLayoutX(point.getX() - grabX);
		activeGhost.setLayoutY(point.getY() - grabY);
	}

	private void animatePanels() {
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

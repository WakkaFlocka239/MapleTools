package me.wakka.mapletools.feature.overlay;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import me.wakka.mapletools.data.MapleSession;
import me.wakka.mapletools.feature.overlay.CaptureRegion.CaptureType;
import me.wakka.mapletools.feature.overlay.CaptureRegionPresets.CaptureRegionPreset;

import java.util.ArrayList;
import java.util.List;

public class OverlayWindow {

	private final MapleSession session;
	private Stage stage;
	private Timeline keepOnTop;
	private GlobalKeyboard globalKeyboard;
	private CaptureManager captureManager;
	private CaptureRegion selectedRegion;

	public OverlayWindow(MapleSession session) {
		this.session = session;
	}

	public void stop() {
		if (keepOnTop != null)
			keepOnTop.stop();

		if (captureManager != null)
			captureManager.stop();

		if (globalKeyboard != null)
			globalKeyboard.stop();

		if (stage != null)
			stage.close();
	}

	public void start() throws Exception {
		this.stage = new Stage();

		this.globalKeyboard = new GlobalKeyboard(this);
		this.globalKeyboard.start();

		Rectangle2D bounds = Screen.getPrimary().getBounds();

		Pane root = new Pane();
		root.setStyle("-fx-background-color: transparent;");
		root.setBackground(Background.EMPTY);
		root.setPickOnBounds(false);

		Scene scene = new Scene(root, bounds.getWidth(), bounds.getHeight());
		scene.setFill(Color.TRANSPARENT);

		scene.setOnKeyPressed(event -> {
			double moveAmount = event.isShiftDown() ? 10 : 1;

			switch (event.getCode()) {
				case ESCAPE -> Platform.exit();

				case F5 -> bringOverlayToFront();

				case UP -> moveSelectedRegion(0, -moveAmount);
				case DOWN -> moveSelectedRegion(0, moveAmount);
				case LEFT -> moveSelectedRegion(-moveAmount, 0);
				case RIGHT -> moveSelectedRegion(moveAmount, 0);

				case W -> resizeSelectedRegion(0, -moveAmount);
				case S -> resizeSelectedRegion(0, moveAmount);
				case A -> resizeSelectedRegion(-moveAmount, 0);
				case D -> resizeSelectedRegion(moveAmount, 0);
			}
		});

		stage.initStyle(StageStyle.TRANSPARENT);
		stage.setAlwaysOnTop(true);
		stage.setFullScreen(false);
		stage.setResizable(false);
		stage.setScene(scene);
		stage.setOpacity(1.0);


		stage.setX(bounds.getMinX());
		stage.setY(bounds.getMinY());
		stage.setWidth(bounds.getWidth());
		stage.setHeight(bounds.getHeight());

		stage.show();
		bringOverlayToFront();
		startKeepOnTop();

		List<CaptureRegion> regions = new ArrayList<>();
		for (CaptureRegionPreset preset : CaptureRegionPresets.DEFAULTS) {
			if (!preset.enabled())
				continue;

			addRegion(root, regions, preset.type(), preset.color(), preset.refreshMillis(), preset.x(), preset.y(), preset.width(), preset.height());
		}

		captureManager = new CaptureManager(session, regions);
		captureManager.start();
	}

	private void bringOverlayToFront() {
		Platform.runLater(() -> {
			if (stage == null)
				return;

			stage.setAlwaysOnTop(false);
			stage.setAlwaysOnTop(true);
			stage.toFront();
			stage.requestFocus();
		});
	}

	private void startKeepOnTop() {
		keepOnTop = new Timeline(
			new KeyFrame(Duration.seconds(1), event -> bringOverlayToFront())
		);

		keepOnTop.setCycleCount(Animation.INDEFINITE);
		keepOnTop.play();
	}

	private void addRegion(Pane root, List<CaptureRegion> regions, CaptureType type, Color color, int refreshMS, double x, double y, double width, double height) {
		CaptureRegion region = new CaptureRegion(type, color, refreshMS, x, y, width, height);

		region.setOnMouseEntered(event -> selectRegion(region));

		regions.add(region);
		root.getChildren().add(region);
	}

	private void selectRegion(CaptureRegion region) {
		if (selectedRegion == region)
			return;

		if (selectedRegion != null) {
			selectedRegion.setOpacity(0.5);
		}

		selectedRegion = region;

		selectedRegion.setOpacity(1.0);

		stage.requestFocus();
	}

	public void moveSelectedRegion(double dx, double dy) {
		if (selectedRegion == null)
			return;

		selectedRegion.move(dx, dy);
	}

	public void resizeSelectedRegion(double dw, double dh) {
		if (selectedRegion == null)
			return;

		double newWidth = selectedRegion.getRegionWidth() + dw;
		double newHeight = selectedRegion.getRegionHeight() + dh;

		selectedRegion.setRegionSize(newWidth, newHeight);
	}
}

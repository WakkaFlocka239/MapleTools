package me.wakka.mapletools.feature.overlay;

import javafx.geometry.Bounds;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import lombok.Getter;

public class CaptureRegion extends Pane {
	private final javafx.scene.shape.Rectangle border;
	private final Circle resizeHandle;

	private double dragOffsetX;
	private double dragOffsetY;

	@Getter
	private CaptureType type;

	@Getter
	private final int refreshMillis;

	private long lastCaptureTime;

	public CaptureRegion(CaptureType type, Color color, int refreshMillis, double x, double y, double width, double height) {
		this.type = type;
		this.refreshMillis = refreshMillis;
		this.lastCaptureTime = 0;

		setLayoutX(x);
		setLayoutY(y);
		setPrefSize(width, height);

		this.border = new javafx.scene.shape.Rectangle(width, height);
		border.setFill(Color.color(0, 1, 0, 0.01)); // barely visible for allow better hover handling
		this.border.setStroke(color);
		this.border.setStrokeWidth(1);

		this.resizeHandle = new Circle(6);
		this.resizeHandle.setCenterX(width);
		this.resizeHandle.setCenterY(height);
		this.resizeHandle.setFill(Color.WHITE);
		this.resizeHandle.setStroke(color);
		//
		this.resizeHandle.setVisible(false);
		hoverProperty().addListener((obs, val, hovering) ->
			resizeHandle.setVisible(hovering));
		//
		setupResizing(width, height);

		setupDragging();

		getChildren().addAll(this.border, this.resizeHandle);
	}

	private void setupDragging() {
		border.setOnMousePressed(event -> {
			dragOffsetX = event.getSceneX() - getLayoutX();
			dragOffsetY = event.getSceneY() - getLayoutY();
		});

		border.setOnMouseDragged(event -> {
			setLayoutX(event.getSceneX() - dragOffsetX);
			setLayoutY(event.getSceneY() - dragOffsetY);
		});
	}

	private void setupResizing(double width, double height) {
		resizeHandle.setOnMouseDragged(event -> {
			double _width = event.getSceneX() - localToScene(0, 0).getX();
			double _height = event.getSceneY() - localToScene(0, 0).getY();

			setRegionSize(_width, _height);
		});
	}

	public double getRegionWidth() {
		return border.getWidth();
	}

	public double getRegionHeight() {
		return border.getHeight();
	}

	public void setRegionSize(double width, double height) {
		width = Math.max(5, width);
		height = Math.max(5, height);

		setPrefSize(width, height);
		setMinSize(width, height);
		setMaxSize(width, height);

		border.setWidth(width);
		border.setHeight(height);

		resizeHandle.setCenterX(width);
		resizeHandle.setCenterY(height);
	}

	public java.awt.Rectangle getScreenBounds() {
		Bounds screenBounds = localToScreen(border.getBoundsInLocal());

		return new java.awt.Rectangle(
			(int) screenBounds.getMinX(),
			(int) screenBounds.getMinY(),
			(int) border.getWidth(),
			(int) border.getHeight()
		);
	}

	public boolean shouldCapture(long now){
		return now - lastCaptureTime >= refreshMillis;
	}

	public void markCaptured(long now){
		this.lastCaptureTime = now;
	}

	public void move(double dx, double dy) {
		setLayoutX(getLayoutX() + dx);
		setLayoutY(getLayoutY() + dy);
	}

	public enum CaptureType {
		HEALTH_TEXT,
		MANA_TEXT,
		EXP_TEXT,
		LOCATION_TEXT,
	}
}

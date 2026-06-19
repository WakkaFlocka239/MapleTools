package me.wakka.mapletools.feature.overlay;

import javafx.scene.paint.Color;
import me.wakka.mapletools.feature.overlay.CaptureRegion.CaptureType;

import java.util.List;

public class CaptureRegionPresets {

	public static final List<CaptureRegionPreset> DEFAULTS = List.of(
		new CaptureRegionPreset(CaptureType.LOCATION_TEXT, true, Color.GREEN, 5000, 44, 27, 156, 36),
		new CaptureRegionPreset(CaptureType.HEALTH_TEXT, false, Color.RED, 6000, 238, 733, 80, 18),
		new CaptureRegionPreset(CaptureType.MANA_TEXT, false, Color.BLUE, 7000, 350, 733, 80, 18),
		new CaptureRegionPreset(CaptureType.EXP_TEXT, false, Color.YELLOW, 8000, 503, 733, 40, 18)
	);

	private CaptureRegionPresets() {
	}

	public record CaptureRegionPreset(
		CaptureType type,
		boolean enabled,
		Color color,
		int refreshMillis,
		double x,
		double y,
		double width,
		double height
	) {
	}
}

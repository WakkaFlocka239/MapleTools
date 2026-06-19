package me.wakka.mapletools.feature.overlay.readers;

import me.wakka.mapletools.data.MapleSession;
import me.wakka.mapletools.feature.overlay.CaptureRegion;

import java.awt.*;
import java.awt.image.BufferedImage;

public interface TextReader {

	void read(MapleSession session, CaptureRegion region, BufferedImage image);

	static BufferedImage scale(BufferedImage original, int factor) {
		int width = original.getWidth() * factor;
		int height = original.getHeight() * factor;

		BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = scaled.createGraphics();

		g.setRenderingHint(
			RenderingHints.KEY_INTERPOLATION,
			RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
		);

		g.drawImage(original, 0, 0, width, height, null);
		g.dispose();

		return scaled;
	}

	static BufferedImage threshold(BufferedImage image) {
		BufferedImage result = new BufferedImage(
			image.getWidth(),
			image.getHeight(),
			BufferedImage.TYPE_BYTE_BINARY
		);

		Graphics2D g = result.createGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();

		return result;
	}

	static BufferedImage isolateWhiteText(BufferedImage image) {
		BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_BINARY);

		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int rgb = image.getRGB(x, y);

				int r = (rgb >> 16) & 0xFF;
				int g = (rgb >> 8) & 0xFF;
				int b = rgb & 0xFF;

				int threshold = 150;
				boolean white = r > threshold && g > threshold && b > threshold;

				int brightness = (r + g + b) / 3;

				boolean textPixel = brightness > 140;

				result.setRGB(x, y, textPixel ? 0xFFFFFFFF : 0xFF000000);
			}
		}

		return result;
	}
}

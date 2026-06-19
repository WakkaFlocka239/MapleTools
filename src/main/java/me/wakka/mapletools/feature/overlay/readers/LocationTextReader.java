package me.wakka.mapletools.feature.overlay.readers;

import me.wakka.mapletools.data.MapleData;
import me.wakka.mapletools.data.MapleSession;
import me.wakka.mapletools.feature.overlay.CaptureRegion;
import me.wakka.mapletools.feature.overlay.MapLocationMatcher;
import me.wakka.mapletools.models.MapleMap;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.awt.image.BufferedImage;

public class LocationTextReader implements TextReader {

	private final ITesseract tesseract;
	private final int scaleFactor;

	public LocationTextReader(int pageSegMode, int scaleFactor) {
		this.scaleFactor = scaleFactor;
		this.tesseract = new Tesseract();
		this.tesseract.setDatapath("tessdata");
		this.tesseract.setLanguage("eng");
		this.tesseract.setPageSegMode(pageSegMode);
	}

	@Override
	public void read(MapleSession session, CaptureRegion region, BufferedImage image) {
		try {
			BufferedImage processed = TextReader.scale(image, scaleFactor);
			String rawLoc = tesseract.doOCR(processed).trim();
			session.setRawLocation(rawLoc);

			MapleMap matched = MapLocationMatcher.match(session, rawLoc, MapleData.getMapsByStreet());

			if (matched != null) {
				session.setCurrentMap(matched);
				session.getLogger().info("Matched map: " + matched.getMapName());
			} else {
				session.getLogger().warn("Couldn't find matched map: " + rawLoc);
			}

		} catch (TesseractException e) {
			e.printStackTrace();
		}

	}


}

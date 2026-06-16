package me.wakka.mapletools.feature.overlay.readers;

import lombok.SneakyThrows;
import me.wakka.mapletools.data.MapleSession;
import me.wakka.mapletools.feature.overlay.CaptureRegion;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.awt.image.BufferedImage;

public class StatTextReader implements TextReader {

	public static final String UNKNOWN_VALUE = "???";

	private final ITesseract tesseract;
	private final int scaleFactor;

	public StatTextReader(String whitelist, int pageSegMode, int scaleFactor) {
		this.scaleFactor = scaleFactor;
		this.tesseract = new Tesseract();

		tesseract.setDatapath("tessdata");
		tesseract.setLanguage("eng");
		tesseract.setPageSegMode(pageSegMode);
		tesseract.setVariable("tessedit_char_whitelist", whitelist);
	}

	@SneakyThrows
	public void read(MapleSession session, CaptureRegion region, BufferedImage image) {
		try {
			String raw = tesseract.doOCR(image).trim();

			switch (region.getType()) {
				case HEALTH_TEXT -> MapleSession.get().setRawHp(raw);
				case MANA_TEXT -> MapleSession.get().setRawMp(raw);
				case EXP_TEXT -> MapleSession.get().setRawExp(raw);
			}


		} catch (TesseractException e) {
			e.printStackTrace();
		}

	}
}

package me.wakka.mapletools.feature.overlay.readers;

import lombok.SneakyThrows;
import me.wakka.mapletools.data.MapleSession;
import me.wakka.mapletools.feature.overlay.CaptureRegion;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.awt.image.BufferedImage;

public class StatTextReader implements TextReader {

	private final ITesseract tesseract;
	private final int scaleFactor;

	public StatTextReader(String whitelist, int pageSegMode, int scaleFactor) {
		this.scaleFactor = scaleFactor;
		this.tesseract = new Tesseract();
		this.tesseract.setDatapath("tessdata");
		this.tesseract.setLanguage("eng");
		this.tesseract.setPageSegMode(pageSegMode);
		this.tesseract.setVariable("tessedit_char_whitelist", whitelist);
	}

	@SneakyThrows
	@Override
	public void read(MapleSession session, CaptureRegion region, BufferedImage image) {
		try {
			BufferedImage processed = TextReader.scale(image, scaleFactor);
			String raw = this.tesseract.doOCR(processed).trim();

			switch (region.getType()) {
				case HEALTH_TEXT -> session.setRawHp(raw);
				case MANA_TEXT -> session.setRawMp(raw);
				case EXP_TEXT -> session.setRawExp(raw);
			}


		} catch (TesseractException e) {
			e.printStackTrace();
		}

	}
}

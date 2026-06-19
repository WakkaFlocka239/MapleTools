package me.wakka.mapletools.feature.overlay;

import javafx.application.Platform;
import lombok.SneakyThrows;
import me.wakka.mapletools.data.MapleSession;
import me.wakka.mapletools.feature.overlay.readers.LocationTextReader;
import me.wakka.mapletools.feature.overlay.readers.StatTextReader;
import me.wakka.mapletools.feature.overlay.readers.TextReader;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class CaptureManager {

	private final MapleSession session;
	private final List<CaptureRegion> regions;
	private final Robot robot;
	private volatile boolean running = false;
	private Thread thread;
	private final StatTextReader readerHpMp = new StatTextReader("0123456789/", 7, 1);
	private final StatTextReader readerExp = new StatTextReader("0123456789.%", 7, 1);
	private final LocationTextReader readerLoc = new LocationTextReader(6, 6);


	public CaptureManager(MapleSession session, List<CaptureRegion> regions) throws AWTException {
		this.session = session;
		this.regions = regions;
		this.robot = new Robot();
	}

	public void start(){
		if (running)
			return;

		running = true;

		thread = new Thread(() -> {
			while(running){
				long now = System.currentTimeMillis();

				for (CaptureRegion region : regions) {
					if (!region.shouldCapture(now))
						continue;

					region.markCaptured(now);
					processRegion(region);
				}

				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					running = false;
				}
			}
		}, "CaptureManager");

		thread.setDaemon(true);
		thread.start();
	}

	public void stop(){
		running = false;

		if (thread != null)
			thread.interrupt();
	}

	private void processRegion(CaptureRegion region) {
		try {
			BufferedImage image = captureImage(region);
			getReader(region).read(session, region, image);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private TextReader getReader(CaptureRegion region) {
		return switch (region.getType()) {
			case LOCATION_TEXT -> readerLoc;
			case HEALTH_TEXT, MANA_TEXT -> readerHpMp;
			case EXP_TEXT -> readerExp;
		};
	}

	@SneakyThrows
	private BufferedImage captureImage(CaptureRegion region) {
		setRegionsVisible(false);

		try {
			Thread.sleep(25);
			return robot.createScreenCapture(region.getScreenBounds());
		} finally {
			setRegionsVisible(true);
		}
	}

	private void setRegionsVisible(boolean visible) {
		Platform.runLater(() -> {
			for (CaptureRegion region : regions) {
				region.setVisible(visible);
			}
		});
	}
}

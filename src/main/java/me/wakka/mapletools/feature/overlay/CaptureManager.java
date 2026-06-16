package me.wakka.mapletools.feature.overlay;

import javafx.application.Platform;
import me.wakka.mapletools.data.MapleSession;
import me.wakka.mapletools.feature.overlay.readers.LocationTextReader;
import me.wakka.mapletools.feature.overlay.readers.StatTextReader;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class CaptureManager {

	private final MapleSession session;
	private final List<CaptureRegion> regions;
	private final Robot robot;
	private volatile boolean running = false;
	private Thread thread;
	private final StatTextReader healthManaReader = new StatTextReader("0123456789/", 7, 1);
	private final StatTextReader expReader = new StatTextReader("0123456789.%", 7, 1);
	private final LocationTextReader locationReader = new LocationTextReader(6, 6);


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
			setRegionsVisible(false);
			Thread.sleep(25);

			BufferedImage image = robot.createScreenCapture(region.getScreenBounds());

			setRegionsVisible(true);

			switch (region.getType()) {
				case LOCATION_TEXT -> locationReader.read(session, region, image);
				case HEALTH_TEXT -> healthManaReader.read(session, region, image);
				case MANA_TEXT -> healthManaReader.read(session, region, image);
				case EXP_TEXT -> expReader.read(session, region, image);
			}

		} catch (Exception e) {
			e.printStackTrace();
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

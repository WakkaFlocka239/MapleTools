package me.wakka.mapletools;

import javafx.application.Application;
import javafx.stage.Stage;
import lombok.Getter;
import me.wakka.mapletools.data.MapleSession;
import me.wakka.mapletools.data.MapleData;
import me.wakka.mapletools.feature.mapviewer.MapViewer;
import me.wakka.mapletools.feature.overlay.OverlayWindow;
import me.wakka.mapletools.feature.ui.MapleToolsWindow;

import java.util.List;

public class MapleTools extends Application {

	@Getter
	private static final boolean refreshCacheFromWz = false;
	private static boolean stopping = false;

	private OverlayWindow overlayWindow;
	private MapViewer mapViewer;
	private MapleToolsWindow toolsWindow;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void stop() {
		if (stopping) return;
		stopping = true;

		MapleSession.get().logger().info("MapleTools shutting down...");
		overlayWindow.stop();
		mapViewer.stop();
	}

	@Override
	public void start(Stage stage) throws Exception {
		MapleData.init();
		MapleSession session = MapleSession.get();

		overlayWindow = new OverlayWindow(session);
		// TODO: MAYBE A BUTTON OR REPEATING TASK TO CHECK IT CAN START NOW?
		if (MapleTools.isMapleClientRunning())
			overlayWindow.start();

		mapViewer = new MapViewer(session);
		mapViewer.start();

		stage.hide();

		toolsWindow = new MapleToolsWindow(session);
		toolsWindow.start(stage);
	}

	private static final List<String> MAPLE_PROCESSES = List.of(
		"maplestory.exe",
		"maplelegends.exe"
	);

	public static boolean isMapleClientRunning() {
		return ProcessHandle.allProcesses().anyMatch(process -> {
			String command = process.info()
				.command()
				.orElse("")
				.toLowerCase();

			return MAPLE_PROCESSES.stream()
				.anyMatch(command::contains);
		});
	}


}

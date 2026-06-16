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

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		MapleData.init();
		MapleSession session = MapleSession.get();

		OverlayWindow overlayWindow = new OverlayWindow(session);
		// TODO: MAYBE A BUTTON OR REPEATING TASK TO CHECK IT CAN START NOW?
		if (MapleTools.isMapleClientRunning())
			overlayWindow.start();

		MapViewer mapViewer = new MapViewer(session);
		mapViewer.start();

		stage.hide();

		MapleToolsWindow window = new MapleToolsWindow(session);
		window.start(stage);
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

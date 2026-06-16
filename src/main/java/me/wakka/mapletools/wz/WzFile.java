package me.wakka.mapletools.wz;

import lombok.Getter;
import lombok.SneakyThrows;
import me.wakka.mapletools.Debug;
import me.wakka.mapletools.wz.models.ProgressBar;
import me.wakka.mapletools.models.MapleMapArea;
import me.wakka.mapletools.models.MaplePortal;
import me.wakka.mapletools.models.MapleMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;

@Getter
public class WzFile {
	private final Path path;
	int version;

	public WzFile(Path path, int version) {
		this.path = path;
		this.version = version;
	}

	@SneakyThrows
	public static LinkedHashMap<Integer, List<MaplePortal>> loadPortals() {
		LinkedHashMap<Integer, List<MaplePortal>> mapPortals = new LinkedHashMap<>();

		for (MapleMapArea mapArea : MapleMapArea.values()) {
			mapPortals.putAll(loadPortals(mapArea));
		}

		return mapPortals;
	}

	@SneakyThrows
	public static LinkedHashMap<Integer, List<MaplePortal>> loadPortals(MapleMapArea mapArea) {
		Path mapFolder = Path.of("files", "data", "Map", "Map", mapArea.getEntryName());
		return WzImgReader.loadPortals(mapFolder, mapArea);
	}

	@SneakyThrows
	public static LinkedHashMap<String, MapleMap> loadMapNames() {
		return WzImgReader.loadMapNames();
	}

	@SneakyThrows
	public static void extractAllWzFiles(int version) {
		Path wzFolder = Path.of("files", "v" + version);
		Path outputFolder = Path.of("files/data");

		Files.createDirectories(outputFolder);

		try (var stream = Files.list(wzFolder)) {
			List<Path> wzFiles = stream
				.filter(Files::isRegularFile)
				.filter(path -> path.getFileName().toString().toLowerCase().endsWith(".wz"))
				.sorted()
				.toList();

			ProgressBar progress = new ProgressBar(wzFiles.size());

			for (Path wzFile : wzFiles) {
				if (!WzUtils.isPkg1WzFile(wzFile)) {
					Debug.debug("Skipping non-PKG1 WZ file: " + wzFile.getFileName());
					continue;
				}

				String wzName = WzUtils.stripExtension(wzFile.getFileName().toString());
				Path wzOutputFolder = outputFolder.resolve(wzName);

				Debug.debug("Extracting " + wzFile.getFileName() + " -> " + wzOutputFolder);

				try (WzFileReader reader = new WzFileReader(wzFile, version)) {
					reader.readHeader();
					reader.readRootEntries();
					reader.extractAllImgFiles(wzOutputFolder);
				} catch (IOException e) {
					e.printStackTrace();
				}
				progress.step("Extracting WZ files");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

package me.wakka.mapletools.wz;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import me.wakka.mapletools.Debug;
import me.wakka.mapletools.MapleTools;
import me.wakka.mapletools.wz.models.ProgressBar;
import me.wakka.mapletools.models.MapleMap;
import me.wakka.mapletools.models.MapleMapArea;
import me.wakka.mapletools.models.MaplePortal;
import me.wakka.mapletools.wz.models.WzNode;
import me.wakka.mapletools.wz.models.WzPropertyType;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WzImgReader implements AutoCloseable {
	private static final Path stringCachePath = Path.of("files/cache/string.json");
	private static final String portalCache = "files/cache/portals-<map>.json";

	private static final Charset WZ_STRING_CHARSET = Charset.forName("windows-1252");

	private RandomAccessFile file;
	private final byte[] wzKey;

	private long currentImgBaseOffset = 0;

	public WzImgReader(Path path) throws Exception {
		this(path, WzUtils.GMS_IV);
	}

	public WzImgReader(Path path, byte[] iv) throws Exception {
		this.file = new RandomAccessFile(path.toFile(), "r");
		this.wzKey = WzUtils.generateWzKey(iv);
	}

	public WzNode read() throws IOException {
		return read("root");
	}

	public WzNode read(String rootName) throws IOException {
		currentImgBaseOffset = 0;
		file.seek(0);

		String marker = readPropertyStringBlock();
		Debug.debug("IMG marker: " + marker);

		if (!"Property".equals(marker)) {
			throw new IOException("Expected Property, got: " + marker);
		}

		WzNode root = new WzNode(rootName, null);

		readPropertyList(root, true);

		return root;
	}

	private void readPropertyList(WzNode parent, boolean nested) throws IOException {
		if (nested) {
			file.skipBytes(2);
		}

		int count = WzUtils.readCompressedInt(file);
		Debug.debug("\nProperty count for " + parent.getName() + ": " + count);

		for (int i = 0; i < count; i++) {
			String name = readPropertyStringBlock();
			WzPropertyType type = WzPropertyType.fromId(file.readUnsignedByte());

			WzNode child = readProperty(name, type);
			Debug.debug("Reading property: " + name + " (" + type + ") value=" + child.getValue());

			parent.addChild(child);
		}
	}

	private WzNode readProperty(String name, WzPropertyType type) throws IOException {
		return switch (type) {
			case NULL -> new WzNode(name, null);
			case SHORT -> new WzNode(name, WzUtils.readShortLE(file));
			case INT -> new WzNode(name, WzUtils.readCompressedInt(file));
			case FLOAT -> new WzNode(name, WzUtils.readCompressedFloat(file));
			case DOUBLE -> new WzNode(name, WzUtils.readLongLE(file));
			case STRING -> new WzNode(name, readPropertyStringBlock());

			case EXTENDED -> {
				int blockSize = WzUtils.readIntLE(file);
				long blockStart = file.getFilePointer();

				WzNode complex = readComplexProperty(name);

				file.seek(blockStart + blockSize);
				yield complex;
			}

			case UNKNOWN -> throw new IOException("Unknown property type for " + name);
		};
	}

	private WzNode readComplexProperty(String name) throws IOException {
		String typeName = readComplexTypeName();

		Debug.debug("Complex property: " + name + " -> " + typeName);

		WzNode node = new WzNode(name, typeName);

		switch (typeName) {
			case "Property" -> readPropertyList(node, true);

			case "Canvas" -> readCanvasPlaceholder(node);

			case "Shape2D#Vector2D" -> {
				int x = WzUtils.readCompressedInt(file);
				int y = WzUtils.readCompressedInt(file);

				node.addChild(new WzNode("x", x));
				node.addChild(new WzNode("y", y));
			}

			case "UOL" -> {
				file.skipBytes(1);
				String path = readPropertyStringBlock();
				node.addChild(new WzNode("path", path));
			}

			default -> throw new IOException("Unknown complex property type: " + typeName);
		}

		return node;
	}

	private String readComplexTypeName() throws IOException {
		int first = file.readUnsignedByte();

		while (first == 0x00) {
			first = file.readUnsignedByte();
		}

		if (first == 0x73 || first == 0x1B || first == 0x01) {
			return readPropertyStringBlock(first);
		}

		file.seek(file.getFilePointer() - 1);
		return WzUtils.readWzString(file, wzKey);
	}

	private void readCanvasPlaceholder(WzNode node) throws IOException {
		int marker = file.readUnsignedByte();

		if (marker == 1) {
			file.skipBytes(2);
			readPropertyList(node, false);
		}

		int width = WzUtils.readCompressedInt(file);
		int height = WzUtils.readCompressedInt(file);
		int format1 = WzUtils.readCompressedInt(file);
		int format2 = file.readUnsignedByte();
		int dataLength = WzUtils.readIntLE(file);

		node.addChild(new WzNode("width", width));
		node.addChild(new WzNode("height", height));
		node.addChild(new WzNode("format1", format1));
		node.addChild(new WzNode("format2", format2));
		node.addChild(new WzNode("dataLength", dataLength));

		file.skipBytes(dataLength);
	}

	private String readPropertyStringBlock() throws IOException {
		return readPropertyStringBlock(file.readUnsignedByte());
	}

	private String readPropertyStringBlock(int type) throws IOException {
		return switch (type) {
			case 0x00, 0x73 -> WzUtils.readWzString(file, wzKey);

			case 0x01, 0x1B -> {
				int stringOffset = WzUtils.readIntLE(file);
				long returnPos = file.getFilePointer();

				file.seek(currentImgBaseOffset + stringOffset);

				String value = WzUtils.readWzString(file, wzKey);

				file.seek(returnPos);
				yield value;
			}

			default -> throw new IOException(
				"Unknown property string block type: " + type + " at " + (file.getFilePointer() - 1)
			);
		};
	}

	@Override
	public void close() throws IOException {
		file.close();
	}

	private static final ObjectMapper MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

	private static void saveStringCache(Map<String, MapleMap> mapMetadata, Path stringCache) throws IOException {
		Files.createDirectories(stringCache.getParent());
		MAPPER.writeValue(stringCache.toFile(), mapMetadata);
	}

	private static void savePortalCache(Map<Integer, List<MaplePortal>> portals, Path portalCache) throws IOException {
		Files.createDirectories(portalCache.getParent());
		MAPPER.writeValue(portalCache.toFile(), portals);
	}

	private static Map<Integer, List<MaplePortal>> loadPortalCache(Path path) throws IOException {
		if (!Files.exists(path))
			return null;

		return MAPPER.readValue(path.toFile(), new TypeReference<>() {});
	}

	private static LinkedHashMap<String, MapleMap>  loadMapNamesCache(Path path) throws IOException {
		if (!Files.exists(path))
			return null;

		return MAPPER.readValue(path.toFile(), new TypeReference<>() {});
	}

	public static LinkedHashMap<Integer, List<MaplePortal>> loadPortals(Path mapFolder, MapleMapArea mapArea) throws Exception {
		Path portalCachePath = Path.of(portalCache.replace("<map>", mapArea.name().toLowerCase()));

		if (!MapleTools.isRefreshCacheFromWz()) {
			Map<Integer, List<MaplePortal>> cachedPortals = loadPortalCache(portalCachePath);

			if (cachedPortals != null)
				return (LinkedHashMap<Integer, List<MaplePortal>>) cachedPortals;
		}

		Map<Integer, List<MaplePortal>> mapPortals = new HashMap<>();

		try (var stream = Files.list(mapFolder)) {
			List<Path> imgFiles = stream
				.filter(Files::isRegularFile)
				.filter(path -> path.getFileName().toString().toLowerCase().endsWith(".img"))
				.sorted()
				.toList();

			ProgressBar progress = new ProgressBar(imgFiles.size());

			for (Path imgFile : imgFiles) {
				progress.step("Portals loaded");
				try (WzImgReader reader = new WzImgReader(imgFile)) {
					WzNode root = reader.read(imgFile.getFileName().toString());

					int mapId = Integer.parseInt(
						imgFile.getFileName().toString()
							.replace(".img", "")
							.trim()
					);

					Debug.force("Entries: " + root.getChildren().values().stream()
						.map(WzNode::getName)
						.collect(Collectors.joining(", ")));

					WzNode infoNode = root.child("foothold");
					if (infoNode != null){
						Debug.force("Entries: " + infoNode.getChildren().values().stream()
							.map(WzNode::getName)
							.collect(Collectors.joining(", ")));
						WzTreePrinter.print(infoNode);
//						MapleMap mapleMap = new MapleMap(infoNode);
					}

					WzNode portalNode = root.child("portal");

					if (portalNode != null) {
						List<MaplePortal> portals = new ArrayList<>();

						for (WzNode portalEntry : portalNode.getChildren().values()) {
							MaplePortal portal = new MaplePortal(portalEntry);

							if (portal.hasTargetMap(mapId))
								portals.add(portal);
						}

						if (!portals.isEmpty())
							mapPortals.put(mapId, portals);
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		LinkedHashMap<Integer, List<MaplePortal>> sortedMapPortals = mapPortals.entrySet().stream()
			.sorted(Map.Entry.comparingByKey())
			.collect(Collectors.toMap(
				Map.Entry::getKey,
				Map.Entry::getValue,
				(e1, e2) -> e1,
				LinkedHashMap::new
			));

		savePortalCache(sortedMapPortals, portalCachePath);

		return sortedMapPortals;

	}

	public static LinkedHashMap<String, MapleMap> loadMapNames() throws IOException {
		if (!MapleTools.isRefreshCacheFromWz()) {
			LinkedHashMap<String, MapleMap> cachedMapNames = loadMapNamesCache(stringCachePath);

			if (cachedMapNames != null)
				return cachedMapNames;
		}


		Path mapImgPath = Path.of("files/data/String/Map.img");

		if (!Files.exists(mapImgPath))
			throw new IOException("Could not find extracted Map.img");

		WzNode root;

		try (WzImgReader reader = new WzImgReader(mapImgPath)) {
			root = reader.read("Map.img");

			Map<String, MapleMap> streetData = collectMapNames(root);

			LinkedHashMap<String, MapleMap> sortedStreetData = streetData.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.collect(Collectors.toMap(
					Map.Entry::getKey,
					Map.Entry::getValue,
					(e1, e2) -> e1,
					LinkedHashMap::new
				));

			//WzTreePrinter.print(root);

			saveStringCache(sortedStreetData, stringCachePath);

			return sortedStreetData;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static Map<String, MapleMap> collectMapNames(WzNode root) {
		Map<String, MapleMap> maps = new HashMap<>();
		collectMapNames(root, maps);
		return maps;
	}

	private static void collectMapNames(WzNode node, Map<String, MapleMap> maps) {
		if (node == null)
			return;

		if (isMapMetadataNode(node))
			maps.put(node.getName(), new MapleMap(node));

		for (WzNode child : node.getChildren().values())
			collectMapNames(child, maps);
	}

	private static boolean isMapMetadataNode(WzNode node) {
		if (!node.getName().matches("\\d+"))
			return false;

		return node.child("streetName") != null || node.child("mapName") != null;
	}
}
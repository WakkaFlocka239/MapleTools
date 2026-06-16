package me.wakka.mapletools.data;

import me.wakka.mapletools.models.MapleMap;
import me.wakka.mapletools.models.MaplePortal;
import me.wakka.mapletools.wz.WzFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MapleData {

	private static LinkedHashMap<Integer, List<MaplePortal>> portals;
	private static LinkedHashMap<String, MapleMap> mapNames;
	private static HashMap<String, List<MapleMap>> mapsByStreet;

	public static void init(){
		// WzFile.extractAllWzFiles(62); // only needs to run once
		if (portals == null)
			portals = WzFile.loadPortals();

		if (mapNames == null) {
			mapNames = WzFile.loadMapNames();
			if (mapNames != null && mapsByStreet == null)
				mapsByStreet = buildMapsByStreet();
		}
	}

	public static HashMap<String, List<MapleMap>> buildMapsByStreet() {
		HashMap<String, List<MapleMap>> mapsByStreet = new HashMap<>();

		for (MapleMap map : mapNames.values()) {
			if (map.getStreetName() == null || map.getStreetName().isBlank())
				continue;

			mapsByStreet
				.computeIfAbsent(map.getStreetName(), key -> new ArrayList<>())
				.add(map);
		}

		return mapsByStreet;
	}

	public static HashMap<String, List<MapleMap>> getMapsByStreet() {
		if (mapsByStreet == null && mapNames != null)
			mapsByStreet = buildMapsByStreet();

		return mapsByStreet;
	}

	public static LinkedHashMap<Integer, List<MaplePortal>> getPortals() {
		if (portals == null)
			portals = WzFile.loadPortals();

		return portals;
	}

	public static List<MaplePortal> getPortals(int mapId) {
		return getPortals().getOrDefault(mapId, List.of());
	}

	public static LinkedHashMap<String, MapleMap> getMapNames() {
		if (mapNames == null)
			mapNames = WzFile.loadMapNames();

		return mapNames;
	}

	public static MapleMap getMapByName(String mapName) {
		return getMapNames().get(mapName);
	}
}

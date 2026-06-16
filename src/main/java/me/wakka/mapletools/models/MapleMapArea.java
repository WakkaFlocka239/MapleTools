package me.wakka.mapletools.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum MapleMapArea {
	MAPLE_ISLAND("Map0", 0, 99999999),
	VICTORIA_ISLAND("Map1", 100000000, 199999999),
	ORBIS("Map2", 200000000, 299999999),
	LEAFRE("Map3", 300000000, 399999999),
	EVENT("Map4", 400000000, 499999999),
	ZIPANGU("Map5", 500000000, 599999999),
	FUTURE("Map6", 600000000, 699999999),
	RESERVED("Map7", 700000000, 79999999),
	CASH_SHOP("Map8", 800000000, 899999999),
	INTERNAL("Map9", 900000000, 999999999);

	@Getter
	private final String entryName;
	private final int mapMin;
	private final int mapMax;

	public static MapleMapArea fromMapId(int mapId) {
		for (MapleMapArea area : values()) {
			if (area.contains(mapId)) {
				return area;
			}
		}

		throw new IllegalArgumentException("Unknown map id area: " + mapId);
	}

	public boolean contains(int mapId) {
		return mapId >= mapMin && mapId <= mapMax;
	}
}

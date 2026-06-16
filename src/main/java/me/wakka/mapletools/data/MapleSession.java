package me.wakka.mapletools.data;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import me.wakka.mapletools.MapleLogger;
import me.wakka.mapletools.feature.ui.panels.LogPanel;
import me.wakka.mapletools.models.MapleMap;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public class MapleSession {

	private MapleSession() {}

	private static final MapleSession instance = new MapleSession();

	public static MapleSession get() {
		return instance;
	}

	private final MapleLogger logger = new MapleLogger();

	public MapleLogger logger() {
		return logger;
	}

	// Current
	private MapleMap currentMap;
	private int currentHp;
	private int currentMp;
	private double currentExpPercent;
	private int currentMaxHp;
	private int currentMaxMp;

	// Previous
	private MapleMap previousMap;
	private int previousHp;
	private int previousMp;
	private double previousExpPercent;
	private int previousMaxHp;
	private int previousMaxMp;

	// Raw OCR
	private String rawLocation;
	private String rawStreetName;
	private String rawMapName;
	private String rawHp;
	private String rawMp;
	private String rawExp;

	//

	public void setCurrentMap(@NonNull MapleMap map) {
		if (this.currentMap != null && this.currentMap.getMapId() == map.getMapId())
			return;

		this.previousMap = this.currentMap;
		this.currentMap = map;
	}
}

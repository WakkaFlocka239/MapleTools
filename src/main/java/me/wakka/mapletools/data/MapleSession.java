package me.wakka.mapletools.data;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import me.wakka.mapletools.MapleLogger;
import me.wakka.mapletools.models.MapleMap;

@Getter
public class MapleSession {

	private MapleSession() {}

	private static final MapleSession instance = new MapleSession();

	public static MapleSession get() {
		return instance;
	}

	private final MapleLogger logger = new MapleLogger();

	// Current
	private final ObjectProperty<MapleMap> currentMap = new SimpleObjectProperty<>();
	private final IntegerProperty currentHp = new SimpleIntegerProperty();
	private final IntegerProperty currentMp = new SimpleIntegerProperty();
	private final DoubleProperty currentExpPercent = new SimpleDoubleProperty();
	private final IntegerProperty currentMaxHp = new SimpleIntegerProperty();
	private final IntegerProperty currentMaxMp = new SimpleIntegerProperty();

	// Previous
	private MapleMap previousMap;
	private int previousHp;
	private int previousMp;
	private double previousExpPercent;
	private int previousMaxHp;
	private int previousMaxMp;

	// Raw OCR
	@Setter
	private String rawLocation;
	@Setter
	private String rawStreetName;
	@Setter
	private String rawMapName;
	@Setter
	private String rawHp;
	@Setter
	private String rawMp;
	@Setter
	private String rawExp;

	//

	public void setHealth(int curHp, int maxHp) {
		this.previousHp = this.currentHp.get();
		this.previousMaxHp = this.currentMaxHp.get();
		this.currentHp.set(curHp);
		this.currentMaxHp.set(maxHp);
	}

	public void setCurrentMap(@NonNull MapleMap map) {
		MapleMap currentMap = this.currentMap.get();
		if (currentMap != null && currentMap.getMapId() == map.getMapId())
			return;

		this.previousMap = this.currentMap.get();
		this.currentMap.set(map);
	}
}

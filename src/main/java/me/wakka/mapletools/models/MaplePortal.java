package me.wakka.mapletools.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.wakka.mapletools.wz.models.WzNode;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MaplePortal {
	String name;
	int type;
	int x;
	int y;
	int targetMapId;
	String targetPortalName;

	public MaplePortal(WzNode node) {
		this.name = node.child("pn").asString();
		this.type = node.child("pt").asInt();
		this.x = node.child("x").asInt();
		this.y = node.child("y").asInt();
		this.targetMapId = node.child("tm").asInt();
		this.targetPortalName = node.child("tn").asString();
	}

	@JsonIgnore
	public boolean isValid() {
		return targetMapId != 999999999;
	}

	public boolean hasTargetMap(int currentMapId) {
		return isValid() && targetMapId != currentMapId;
	}
}

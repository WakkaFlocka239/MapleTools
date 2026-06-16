package me.wakka.mapletools.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.wakka.mapletools.wz.models.WzNode;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MapleMap {
	int mapId;
	String streetName;
	String mapName;
	String mapDesc;

	public MapleMap(WzNode node) {
		this.mapId = Integer.parseInt(node.getName());
		this.mapName = node.child("mapName").asString();
		this.streetName = node.child("streetName").asString();

		if (node.hasChild("mapDesc"))
			this.mapDesc = node.child("mapDesc").asString();
	}

	public static class Bounds {
		public int left = Integer.MAX_VALUE;
		public int top = Integer.MAX_VALUE;
		public int right = Integer.MIN_VALUE;
		public int bottom = Integer.MIN_VALUE;

		public Bounds(WzNode root){
			WzNode footHoldNode = root.child("foothold");
			if (footHoldNode != null)
				collectFromFootholds(footHoldNode, this);

			WzNode portalNode = root.child("portal");
			if (portalNode != null)
				collectFromPortals(portalNode, this);
		}

		public void include(int x, int y){
			this.left = Math.min(this.left, x);
			this.right = Math.max(this.right, x);
			this.top = Math.min(this.top, y);
			this.bottom = Math.max(this.bottom, y);
		}

		public boolean isValid(){
			return this.left != Integer.MAX_VALUE;
		}

		public int width(){
			return this.right - this.left;
		}

		public int height(){
			return this.bottom - this.top;
		}

		private static void collectFromFootholds(WzNode footHoldNode, Bounds bounds) {
			if (footHoldNode.child("x1") != null && footHoldNode.child("y1") != null &&
				footHoldNode.child("x2") != null && footHoldNode.child("y2") != null) {

				int x1 = footHoldNode.child("x1").asInt();
				int y1 = footHoldNode.child("y1").asInt();
				int x2 = footHoldNode.child("x2").asInt();
				int y2 = footHoldNode.child("y2").asInt();

				bounds.include(x1, y1);
				bounds.include(x2, y2);
			}

			for (WzNode child : footHoldNode.getChildren().values()) {
				collectFromFootholds(child, bounds);
			}
		}

		private static void collectFromPortals(WzNode portalNode, Bounds bounds) {
			for (WzNode portal : portalNode.getChildren().values()) {
				if (portal.child("x") == null || portal.child("y") == null)
					continue;

				int x = portal.child("x").asInt();
				int y = portal.child("y").asInt();

				bounds.include(x, y);
			}
		}
	}
}

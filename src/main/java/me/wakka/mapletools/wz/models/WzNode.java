package me.wakka.mapletools.wz.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public class WzNode {
	private final String name;
	private final Object value;
	private final Map<String, WzNode> children = new LinkedHashMap<>();

	public void addChild(WzNode child) {
		children.put(child.getName(), child);
	}

	public WzNode child(String name) {
		return children.get(name);
	}

	public String stringValue() {
		return (String) value;
	}

	public int intValue() {
		return (Integer) value;
	}

	public String asString() {
		return (String) value;
	}

	public int asInt() {
		return (Integer) value;
	}


	public boolean hasChild(String name) {
		return children.containsKey(name);
	}
}

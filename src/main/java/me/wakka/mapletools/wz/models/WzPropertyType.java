package me.wakka.mapletools.wz.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WzPropertyType {
	NULL(0),
	SHORT(2),
	INT(3),
	FLOAT(4),
	DOUBLE(5),
	STRING(8),
	EXTENDED(9),
	//
	UNKNOWN(-1);

	private final int id;

	public static WzPropertyType fromId(int id) {
		for (WzPropertyType type : values()) {
			if (type.id == id) {
				return type;
			}
		}

		return UNKNOWN;
	}
}

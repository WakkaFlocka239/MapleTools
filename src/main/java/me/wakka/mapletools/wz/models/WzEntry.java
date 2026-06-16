package me.wakka.mapletools.wz.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class WzEntry {
	String name;
	EntryType type;
	int size;
	int checkSum;
	int offset;

	public WzEntry(String name, int type, int size, int checkSum, int offset) {
		this.name = name;
		this.type = EntryType.fromId(type);
		this.size = size;
		this.checkSum = checkSum;
		this.offset = offset;
	}

	public boolean isDirectory() {
		return type == EntryType.DIRECTORY;
	}

	public boolean isImageFile() {
		return type == EntryType.IMAGE_FILE;
	}

	@NoArgsConstructor
	@AllArgsConstructor
	public enum EntryType {
		UNKNOWN(),
		DIRECTORY(3),
		IMAGE_FILE(4),
		;

		public Integer raw = null;

		public static EntryType fromId(int raw) {
			return switch (raw) {
				case 3 -> DIRECTORY;
				case 4 -> IMAGE_FILE;
				default -> UNKNOWN;
			};
		}
	}
}

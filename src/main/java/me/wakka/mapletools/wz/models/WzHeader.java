package me.wakka.mapletools.wz.models;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WzHeader {
	private String ident;
	private long fileSize;
	private int headerSize;
	private String copyright;
}

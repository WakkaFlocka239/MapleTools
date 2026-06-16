package me.wakka.mapletools.wz;

import lombok.Getter;
import me.wakka.mapletools.Debug;
import me.wakka.mapletools.wz.models.WzEntry;
import me.wakka.mapletools.wz.models.WzEntry.EntryType;
import me.wakka.mapletools.wz.models.WzHeader;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class WzFileReader implements AutoCloseable {
	private RandomAccessFile file;
	private final byte[] wzKey;
	private final int versionHash;

	private final WzHeader header = new WzHeader();
	private final Map<String, WzEntry> rootEntries = new HashMap<>();

	public WzFileReader(Path path, int version) throws Exception {
		this(path, version, WzUtils.GMS_IV);
	}

	public WzFileReader(Path path, int version, byte[] iv) throws Exception {
		this.file = new RandomAccessFile(path.toFile(), "r");
		this.wzKey = WzUtils.generateWzKey(WzUtils.GMS_IV);
		this.versionHash = WzUtils.calculateVersionHash(version);
	}

	public void readHeader() throws IOException {
		file.seek(0);

		header.setIdent(WzUtils.readAscii(file, 4));
		header.setFileSize(WzUtils.readLongLE(file));
		header.setHeaderSize(WzUtils.readIntLE(file));
		header.setCopyright(WzUtils.readAscii(file, header.getHeaderSize() - 16).trim());

		Debug.debug("Ident: " + header.getIdent());
		Debug.debug("File Size: " + header.getFileSize());
		Debug.debug("Header Size: " + header.getHeaderSize());
		Debug.debug("Copyright: " + header.getCopyright());
		Debug.debug("Directory starts at: " + header.getHeaderSize());
	}

	public void readRootEntries() throws IOException {
		file.seek(header.getHeaderSize());

		int encryptedVersion = WzUtils.readShortLE(file) & 0xFFFF;
		int count = WzUtils.readCompressedInt(file);

		Debug.debug("Encrypted Version: " + encryptedVersion);
		Debug.debug("Root Entry Count: " + count);

		for (int i = 0; i < count; i++) {
			int type = file.readUnsignedByte();

			String name = WzUtils.readWzStringBlock(file, wzKey, type, header.getHeaderSize());
			int size = WzUtils.readCompressedInt(file);
			int checksum = WzUtils.readCompressedInt(file);
			int offsetRaw = readWzOffset();

			WzEntry entry = new WzEntry(name, type, size, checksum, offsetRaw);
			rootEntries.put(name, entry);
		}
	}

	public Map<String, WzEntry> readDirectoryAt(WzEntry directory) throws IOException {
		file.seek(directory.getOffset());

		int count = WzUtils.readCompressedInt(file);
		Debug.debug(directory.getName() + " child count: " + count);

		Map<String, WzEntry> entries = new LinkedHashMap<>();

		for (int i = 0; i < count; i++) {
			int type = file.readUnsignedByte();

			String childName = WzUtils.readWzStringBlock(file, wzKey, type, header.getHeaderSize());
			int size = WzUtils.readCompressedInt(file);
			int checksum = WzUtils.readCompressedInt(file);
			int childOffset = readWzOffset();

			WzEntry entry = new WzEntry(childName, type, size, checksum, childOffset);
			entries.put(childName, entry);

			Debug.debug("  " + entry.getName() + " | type=" + entry.getType() + " | offset=" + entry.getOffset());
		}

		return entries;
	}

	private int readWzOffset() throws IOException {
		long offsetPosition = file.getFilePointer();

		int encryptedOffset = WzUtils.readIntLE(file);

		int offset = (int) (offsetPosition - header.getHeaderSize());
		offset ^= 0xFFFFFFFF;
		offset *= versionHash;
		offset -= 0x581C3F6D;

		offset = Integer.rotateLeft(offset, offset & 0x1F);

		offset ^= encryptedOffset;
		offset += header.getHeaderSize() * 2;

		return offset;
	}

	public void extractAllImgFiles(Path outputFolder) throws IOException {
		Files.createDirectories(outputFolder);

		for (WzEntry entry : rootEntries.values()) {
			extractEntry(entry, outputFolder);
		}
	}

	private void extractEntry(WzEntry entry, Path outputFolder) throws IOException {
		if (entry.getType() == EntryType.DIRECTORY) {
			Path folder = outputFolder.resolve(entry.getName());
			Files.createDirectories(folder);

			Map<String, WzEntry> children = readDirectoryAt(entry);

			for (WzEntry child : children.values()) {
				extractEntry(child, folder);
			}

			return;
		}

		if (entry.getType() == EntryType.IMAGE_FILE) {
			Path imgPath = outputFolder.resolve(entry.getName());

			if (!imgPath.toString().toLowerCase().endsWith(".img")) {
				imgPath = outputFolder.resolve(entry.getName() + ".img");
			}

			saveRawEntry(entry, imgPath);
		}
	}

	private void saveRawEntry(WzEntry entry, Path outputPath) throws IOException {
		Files.createDirectories(outputPath.getParent());

		file.seek(entry.getOffset());

		byte[] data = new byte[entry.getSize()];
		file.readFully(data);

		Files.write(outputPath, data);

		Debug.debug("Extracted " + outputPath + " (" + entry.getSize() + " bytes)");
	}

	@Override
	public void close() throws Exception {
		file.close();
	}
}

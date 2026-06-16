package me.wakka.mapletools.wz;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class WzUtils {
	public static final byte[] GMS_IV = {0x4D, 0x23, (byte) 0xC7, 0x2B};
	public static final byte[] EMS_IV = {(byte) 0xB9, 0x7D, 0x63, (byte) 0xE9};
	public static final byte[] ZERO_IV = {0x00, 0x00, 0x00, 0x00};
	public static final byte[] KMS_IV = {(byte) 0xB9, 0x7D, 0x63, (byte) 0xE9};
	public static final byte[] MSEA_IV = {(byte) 0x4D, 0x23, (byte) 0xC7, 0x2B};
	private static final int KEY_LENGTH = 65536;
	public static final Charset WZ_STRING_CHARSET = Charset.forName("windows-1252");

	public static int calculateVersionHash(int versionNum) {
		String version = String.valueOf(versionNum);
		int hash = 0;

		for (char c : version.toCharArray()) {
			hash = (hash * 32) + c + 1;
		}

		return hash;
	}

	public static byte[] generateWzKey() throws Exception {
		return generateWzKey(GMS_IV);
	}

	public static byte[] generateWzKey(byte[] iv) throws Exception {
		byte[] aesUserKey = {
			0x13, 0x00, 0x00, 0x00,
			0x08, 0x00, 0x00, 0x00,
			0x06, 0x00, 0x00, 0x00,
			(byte) 0xB4, 0x00, 0x00, 0x00,
			0x1B, 0x00, 0x00, 0x00,
			0x0F, 0x00, 0x00, 0x00,
			0x33, 0x00, 0x00, 0x00,
			0x52, 0x00, 0x00, 0x00
		};

		Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
		cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(aesUserKey, "AES"));

		byte[] key = new byte[KEY_LENGTH];
		byte[] block = new byte[16];

		for (int i = 0; i < 16; i++) {
			block[i] = iv[i % 4];
		}

		int position = 0;

		while (position < KEY_LENGTH) {
			block = cipher.doFinal(block);

			int copyLength = Math.min(block.length, KEY_LENGTH - position);
			System.arraycopy(block, 0, key, position, copyLength);

			position += copyLength;
		}

		return key;
	}

	public static String readAscii(RandomAccessFile file, int length) throws IOException {
		byte[] bytes = new byte[length];
		file.readFully(bytes);
		return new String(bytes, StandardCharsets.US_ASCII);
	}

	public static short readShortLE(RandomAccessFile file) throws IOException {
		int b1 = file.read();
		int b2 = file.read();

		if (b1 < 0 || b2 < 0) {
			throw new IOException("Unexpected EOF");
		}

		return (short) (b1 | (b2 << 8));
	}

	public static int readIntLE(RandomAccessFile file) throws IOException {
		int b1 = file.read();
		int b2 = file.read();
		int b3 = file.read();
		int b4 = file.read();

		if ((b1 | b2 | b3 | b4) < 0) {
			throw new IOException("Unexpected EOF");
		}

		return b1 | (b2 << 8) | (b3 << 16) | (b4 << 24);
	}

	public static long readLongLE(RandomAccessFile file) throws IOException {
		return ((long) readIntLE(file) & 0xFFFFFFFFL)
			| (((long) readIntLE(file) & 0xFFFFFFFFL) << 32);
	}

	public static int readCompressedInt(RandomAccessFile file) throws IOException {
		int b = file.readByte();

		if (b == -128) {
			return readIntLE(file);
		}

		return b;
	}

	public static float readCompressedFloat(RandomAccessFile file) throws IOException {
		int b = file.readByte();

		if (b == -128) {
			return Float.intBitsToFloat(readIntLE(file));
		}

		return 0.0f;
	}

	public static String readWzString(RandomAccessFile file, byte[] wzKey) throws IOException {
		int lengthByte = file.readByte();

		if (lengthByte == 0) {
			return "";
		}

		if (lengthByte < 0) {
			int length = -lengthByte;

			if (lengthByte == -128) {
				length = readIntLE(file);
			}

			byte[] bytes = new byte[length];

			for (int i = 0; i < length; i++) {
				int encrypted = file.readUnsignedByte();
				int mask = 0xAA + i;
				int key = wzKey[i] & 0xFF;

				bytes[i] = (byte) (encrypted ^ mask ^ key);
			}

			return new String(bytes, WZ_STRING_CHARSET);
		}

		int length = lengthByte;

		if (lengthByte == 127) {
			length = readIntLE(file);
		}

		char[] chars = new char[length];

		for (int i = 0; i < length; i++) {
			int encrypted = readShortLE(file) & 0xFFFF;
			int mask = 0xAAAA + i;

			int keyLow = wzKey[i * 2] & 0xFF;
			int keyHigh = wzKey[i * 2 + 1] & 0xFF;
			int key = keyLow | (keyHigh << 8);

			chars[i] = (char) (encrypted ^ mask ^ key);
		}

		return new String(chars);
	}

	public static String readWzStringBlock(RandomAccessFile file, byte[] wzKey, int type, int headerSize) throws IOException {
		// 0x02 = referenced string
		if (type == 0x02) {
			int stringOffset = readIntLE(file);
			long returnPos = file.getFilePointer();

			file.seek(headerSize + stringOffset);
			String value = readWzString(file, wzKey);

			file.seek(returnPos);
			return value;
		}

		// 0x03 / 0x04 = inline string
		if (type == 0x03 || type == 0x04) {
			return readWzString(file, wzKey);
		}

		return "UNKNOWN_TYPE_" + type;
	}


	public static String stripExtension(String fileName) {
		int dot = fileName.lastIndexOf('.');
		return dot == -1 ? fileName : fileName.substring(0, dot);
	}

	public static boolean isPkg1WzFile(Path path) {
		try (RandomAccessFile file = new RandomAccessFile(path.toFile(), "r")) {
			if (file.length() < 4)
				return false;

			byte[] ident = new byte[4];
			file.readFully(ident);

			return "PKG1".equals(new String(ident, StandardCharsets.US_ASCII));
		} catch (IOException e) {
			return false;
		}
	}
}

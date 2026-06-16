package me.wakka.mapletools.feature.overlay.readers;

import me.wakka.mapletools.data.MapleData;
import me.wakka.mapletools.data.MapleSession;
import me.wakka.mapletools.feature.overlay.CaptureRegion;
import me.wakka.mapletools.models.MapleMap;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LocationTextReader implements TextReader {

	public static final String UNKNOWN_LOCATION = "???";

	private final ITesseract tesseract;
	private final int scaleFactor;

	public LocationTextReader(int pageSegMode, int scaleFactor) {
		this.scaleFactor = scaleFactor;
		this.tesseract = new Tesseract();

		tesseract.setDatapath("tessdata");
		tesseract.setLanguage("eng");
		tesseract.setPageSegMode(pageSegMode);
	}

	public void read(MapleSession session, CaptureRegion region, BufferedImage image) {
		try {
			BufferedImage processed = TextReader.scale(image, scaleFactor);
			String rawLoc = tesseract.doOCR(processed).trim();
			MapleSession.get().setRawLocation(rawLoc);

			MapleMap matched = matchLocation(session, rawLoc, MapleData.getMapsByStreet());

			if (matched != null) {
				MapleSession.get().setCurrentMap(matched);
				session.logger().info("Matched map: " + matched.getMapName());
			} else {
				session.logger().warn("Couldn't find matched map: " + rawLoc);
			}

		} catch (TesseractException e) {
			e.printStackTrace();
		}

	}

	@Nullable
	public MapleMap matchLocation(MapleSession session, String rawLocation, Map<String, List<MapleMap>> mapsByStreet) {
		String[] lines = rawLocation
			.replace("\r", "")
			.strip()
			.split("\\R");

		String rawStreetName = lines.length > 0 ? lines[0].trim() : "";
		String rawMapName = lines.length > 1 ? lines[1].trim() : "";

		debugStreetMatches(session, rawStreetName, mapsByStreet.keySet());

		String bestStreet = findBestStreet(rawStreetName, mapsByStreet.keySet());

		if (bestStreet == null)
			return null;

		List<MapleMap> possibleMaps = mapsByStreet.get(bestStreet);

		if (possibleMaps == null || possibleMaps.isEmpty())
			return null;

		return findBestMap(rawMapName, possibleMaps);
	}

	private String findBestStreet(String guess, Set<String> streets) {
		String bestStreet = null;
		int bestScore = -1;

		for (String street : streets) {
			int score = scoreMatch(guess, street);

			if (score > bestScore) {
				bestScore = score;
				bestStreet = street;
			}
		}

		return bestScore >= 25 ? bestStreet : null;
	}

	private MapleMap findBestMap(String guess, List<MapleMap> maps) {
		MapleMap bestMap = null;
		int bestScore = -1;

		for (MapleMap map : maps) {
			int score = scoreMatch(guess, map.getMapName());

			if (score > bestScore) {
				bestScore = score;
				bestMap = map;
			}
		}

		return bestScore >= 50 ? bestMap : null;
	}

	private int scoreMatch(String guess, String official) {
		String a = normalizeForMatch(guess);
		String b = normalizeForMatch(official);

		return similarity(a, b);
	}

	private String normalizeForMatch(String text) {
		return text.toLowerCase()
			.replace('|', 'l')
			.replace('=', '<')
			.replace('6', 's')
			.replaceAll("\\s+", " ")
			.trim();
	}


	private int similarity(String a, String b) {
		if (a.isEmpty() || b.isEmpty())
			return 0;

		int distance = levenshtein(a, b);
		int maxLength = Math.max(a.length(), b.length());

		return (int) Math.round((1.0 - ((double) distance / maxLength)) * 100);
	}

	private int levenshtein(String a, String b) {
		int[][] dp = new int[a.length() + 1][b.length() + 1];

		for (int i = 0; i <= a.length(); i++)
			dp[i][0] = i;

		for (int j = 0; j <= b.length(); j++)
			dp[0][j] = j;

		for (int i = 1; i <= a.length(); i++) {
			for (int j = 1; j <= b.length(); j++) {
				int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
				int deleteCost = dp[i - 1][j] + 1;
				int insertCost = dp[i][j - 1] + 1;
				int replaceCost = dp[i - 1][j - 1] + cost;

				dp[i][j] = Math.min(deleteCost, Math.min(insertCost, replaceCost));
			}
		}

		return dp[a.length()][b.length()];
	}

	private void debugStreetMatches(MapleSession session, String guess, Set<String> streets) {
		session.logger().log("Street Guess: [" + guess + "]");
		session.logger().log("Normalized Guess: [" + normalizeForMatch(guess) + "]");

		streets.stream()
			.map(street -> new StreetScore(street, scoreMatch(guess, street)))
			.sorted((a, b) -> Integer.compare(b.score(), a.score()))
			.limit(10)
			.forEach(result ->
				session.logger().log(result.score() + " -> " + result.street())
			);
	}

	private record StreetScore(String street, int score) {}


}

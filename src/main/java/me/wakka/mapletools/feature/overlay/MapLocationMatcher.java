package me.wakka.mapletools.feature.overlay;

import me.wakka.mapletools.data.MapleSession;
import me.wakka.mapletools.models.MapleMap;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class MapLocationMatcher {

	@Nullable
	public static MapleMap match(MapleSession session, String rawLocation, Map<String, List<MapleMap>> mapsByStreet) {
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

	private static String findBestStreet(String guess, Set<String> streets) {
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

	private static MapleMap findBestMap(String guess, List<MapleMap> maps) {
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

	private static int scoreMatch(String guess, String official) {
		String a = normalizeForMatch(guess);
		String b = normalizeForMatch(official);

		return similarity(a, b);
	}

	private static String normalizeForMatch(String text) {
		return text.toLowerCase()
			.replace('|', 'l')
			.replace('=', '<')
			.replace('6', 's')
			.replaceAll("\\s+", " ")
			.trim();
	}


	private static int similarity(String a, String b) {
		if (a.isEmpty() || b.isEmpty())
			return 0;

		int distance = levenshtein(a, b);
		int maxLength = Math.max(a.length(), b.length());

		return (int) Math.round((1.0 - ((double) distance / maxLength)) * 100);
	}

	private static int levenshtein(String a, String b) {
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

	private static void debugStreetMatches(MapleSession session, String guess, Set<String> streets) {
		session.getLogger().log("Street Guess: [" + guess + "]");
		session.getLogger().log("Normalized Guess: [" + normalizeForMatch(guess) + "]");

		streets.stream()
			.map(street -> new StreetScore(street, scoreMatch(guess, street)))
			.sorted((a, b) -> Integer.compare(b.score(), a.score()))
			.limit(10)
			.forEach(result ->
				session.getLogger().log(result.score() + " -> " + result.street())
			);
	}

	private record StreetScore(String street, int score) {
	}
}

package me.wakka.mapletools.wz;

import me.wakka.mapletools.wz.models.WzNode;

import java.util.ArrayList;
import java.util.List;

public class WzTreePrinter {

	public static void print(WzNode root) {
		print(root, "", true);
	}

	private static void print(WzNode node, String prefix, boolean isLast) {
		if (node == null)
			return;

		// Current node
		System.out.println(prefix + (isLast ? "-- " : "|- ") + format(node));

		List<WzNode> children = new ArrayList<>(node.getChildren().values());

		for (int i = 0; i < children.size(); i++) {
			boolean last = i == children.size() - 1;

			print(children.get(i), prefix + (isLast ? "    " : "|   "), last);
		}
	}

	private static String format(WzNode node) {
		if (node.getChildren().isEmpty())
			return node.getName() + " = " + node.getValue();

		return node.getName();
	}
}

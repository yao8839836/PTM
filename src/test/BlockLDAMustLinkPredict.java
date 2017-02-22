package test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BlockLDAMustLinkPredict {

	/**
	 * 获得药对集合
	 * 
	 * @param herbs
	 * @return
	 */
	public static int[][] getHerbLinksSet(int[][] herbs) {

		Set<Set<Integer>> pair_set = new HashSet<>();

		for (int p = 0; p < herbs.length; p++) {

			for (int h = 0; h < herbs[p].length; h++) {

				for (int h1 = 0; h1 < herbs[p].length; h1++) {

					if (herbs[p][h] != herbs[p][h1]) {

						Set<Integer> pair = new HashSet<>();

						pair.add(herbs[p][h]);

						pair.add(herbs[p][h1]);

						pair_set.add(pair);

					}

				}

			}
		}

		List<Set<Integer>> pairs_list = new ArrayList<>(pair_set);

		int[][] links = new int[pairs_list.size()][2];

		for (int i = 0; i < links.length; i++) {

			List<Integer> pair = new ArrayList<>(pairs_list.get(i));

			links[i][0] = pair.get(0);

			links[i][1] = pair.get(1);

		}

		return links;

	}

	/**
	 * 获得药对列表
	 * 
	 * @param herbs
	 * @return
	 */

	public static int[][] getHerbLinksList(int[][] herbs) {

		List<String> pairs = new ArrayList<>();

		for (int p = 0; p < herbs.length; p++) {

			for (int h = 0; h < herbs[p].length; h++) {

				for (int h1 = 0; h1 < herbs[p].length; h1++) {

					if (herbs[p][h] != herbs[p][h1]) {

						pairs.add(herbs[p][h] + "\t" + herbs[p][h1]);

					}

				}

			}
		}

		int[][] links = new int[pairs.size()][2];

		for (int i = 0; i < links.length; i++) {

			String[] temp = pairs.get(i).split("\t");

			links[i][0] = Integer.parseInt(temp[0]);

			links[i][1] = Integer.parseInt(temp[1]);

		}

		return links;

	}

}

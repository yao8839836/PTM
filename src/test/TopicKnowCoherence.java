package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TopicKnowCoherence {

	public static void main(String[] args) throws IOException, IOException {

		Map<String, Set<String>> herb_category = getCategory("file//herb_category.txt");

		System.out.println(herb_category);

		Map<String, Set<String>> symptom_category = getCategory("file//symptom_category.txt");

		System.out.println(symptom_category);

		String topic_file = "result//topic_atm.txt";

		File f = new File(topic_file);

		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
		String line = "";

		int line_count = 0;

		double herb_coherence = 0;

		double symptom_coherence = 0;

		while ((line = reader.readLine()) != null) {

			String[] temp = line.split("\t");

			// 单味药

			String[] herbs_str = temp[0].split(" ");

			int herb_count = 0;

			for (int i = 1; i < herbs_str.length; i++) {

				Set<String> categories_i = herb_category.get(herbs_str[i]);

				for (int j = 0; j < i; j++) {

					Set<String> categories_j = herb_category.get(herbs_str[j]);

					if (categories_i != null && categories_j != null) {

						for (String c : categories_i) {

							if (categories_j.contains(c)) {
								herb_count++;
								break;
							}

						}

					}

				}

			}

			herb_coherence += herb_count;

			// 症状

			String[] symptoms_str = temp[1].split(" ");

			int symptom_count = 0;

			for (int i = 1; i < symptoms_str.length; i++) {

				Set<String> categories_i = symptom_category.get(symptoms_str[i]);

				for (int j = 0; j < i; j++) {

					Set<String> categories_j = symptom_category.get(symptoms_str[j]);

					System.out
							.println(symptoms_str[i] + "," + categories_i + "," + symptoms_str[i] + "," + categories_j);

					if (categories_i != null && categories_j != null) {

						for (String c : categories_i) {

							if (categories_j.contains(c)) {
								symptom_count++;
								break;
							}

						}

					}

				}

			}

			symptom_coherence += symptom_count;

			line_count++;

		}

		System.out.println(herb_coherence / line_count);

		System.out.println(symptom_coherence / line_count);

		reader.close();

	}

	/**
	 * 从文件中读取目录归属
	 * 
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public static Map<String, Set<String>> getCategory(String filename) throws IOException {

		Map<String, Set<String>> category = new HashMap<>();

		File f = new File(filename);

		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
		String line = "";

		while ((line = reader.readLine()) != null) {

			String[] temp = line.split("\t");

			Set<String> belongs = new HashSet<>();

			String[] cate_str = temp[1].split(" ");

			for (String s : cate_str) {
				belongs.add(s);
			}

			category.put(temp[0], belongs);

		}

		reader.close();

		return category;

	}
}

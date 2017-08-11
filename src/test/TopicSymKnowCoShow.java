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

import util.ReadWriteFile;

public class TopicSymKnowCoShow {

	public static void main(String[] args) throws IOException {

		Map<String, Set<String>> symptom_category = getCategory("file//symptom_category.txt");

		System.out.println(symptom_category);

		String topic_file = "result//blocklda_topic_25.txt";

		File f = new File(topic_file);

		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
		String line = "";

		StringBuilder sb = new StringBuilder();

		while ((line = reader.readLine()) != null) {

			String[] temp = line.split("\t");

			String[] symptoms_str = temp[1].split(" ");

			for (int i = 0; i < symptoms_str.length; i++) {

				Set<String> categories_i = symptom_category.get(symptoms_str[i]);

				boolean mark = false;

				for (int j = 0; j < symptoms_str.length; j++) {

					if (i == j)
						continue;

					Set<String> categories_j = symptom_category.get(symptoms_str[j]);

					System.out
							.println(symptoms_str[i] + "," + categories_i + "," + symptoms_str[i] + "," + categories_j);

					if (categories_i != null && categories_j != null) {

						for (String c : categories_i) {

							if (categories_j.contains(c)) {
								mark = true;
								break;
							}

						}

					}

				}
				if (mark) {
					sb.append(symptoms_str[i] + " " + 1 + " ");
				} else {
					sb.append(symptoms_str[i] + " ");
				}

			}
			sb.append("\n");
		}

		reader.close();

		ReadWriteFile.writeFile("result//blocklda_topic_25_sym_pre.txt", sb.toString());
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

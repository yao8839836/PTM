package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class TopicPrecisionHerbToSym {

	public static void main(String[] args) throws IOException {

		String topic_file = "result//topic_link_lda.txt";

		Map<String, String> herb_symptom = getHerbSymptomKnowledge("data//herb_symptom_knowledge.txt");

		System.out.println(herb_symptom);

		File f = new File(topic_file);
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
		String line = "";

		int count = 0;

		int line_count = 0;

		while ((line = reader.readLine()) != null) {

			String[] temp = line.split("\t");

			String[] herbs = temp[0].split(" ");

			String[] symptoms = temp[1].split(" ");

			StringBuilder sym_str = new StringBuilder();

			for (int h = 0; h < herbs.length; h++) {

				String sym = herb_symptom.get(herbs[h]);

				sym_str.append(sym + "\t");

			}

			String sym_set = sym_str.toString();

			for (int s = 0; s < symptoms.length; s++) {

				if (sym_set.contains(symptoms[s]))

					count++;

			}

			line_count++;

		}

		System.out.println((double) count / (line_count * 10));

		reader.close();

	}

	/**
	 * 单味药的症状知识
	 * 
	 * @param filename
	 * @return
	 * @throws IOException
	 * 
	 */
	public static Map<String, String> getHerbSymptomKnowledge(String filename) throws IOException {

		File f = new File(filename);
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
		String line = "";

		Map<String, String> herb_symptom = new HashMap<>();

		while ((line = reader.readLine()) != null) {

			System.out.println(line);

			String[] temp = line.split("\t");

			if (temp.length == 2)

				herb_symptom.put(temp[0], temp[1]);

		}

		reader.close();

		return herb_symptom;
	}

}

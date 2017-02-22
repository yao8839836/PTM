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

public class TopicPrecisionSymToHerb {

	public static String main(String[] args) throws IOException {

		String topic_file = args[0];

		Map<String, String> symptom_herb = getSymptomHerbKnowledge("data//symptom_herb_tcm_mesh.txt");

		System.out.println(symptom_herb);

		File f = new File(topic_file);
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
		String line = "";

		int count = 0;

		int line_count = 0;

		while ((line = reader.readLine()) != null) {

			String[] temp = line.split("\t");

			String[] herbs = temp[0].split(" ");

			String[] symptoms = temp[1].split(" ");

			StringBuilder herb_str = new StringBuilder();

			for (int s = 0; s < symptoms.length; s++) {

				String herb = symptom_herb.get(symptoms[s]);

				herb_str.append(herb + "\t");

			}

			String herb_set = herb_str.toString();

			for (int h = 0; h < herbs.length; h++) {

				if (herb_set.contains(herbs[h]))

					count++;

			}

			line_count++;

		}

		double precision = (double) count / (10 * line_count);
		System.out.println(precision);

		reader.close();

		return precision + "";

	}

	/**
	 * 单味药的症状知识
	 * 
	 * @param filename
	 * @return
	 * @throws IOException
	 * 
	 */
	public static Map<String, String> getSymptomHerbKnowledge(String filename) throws IOException {

		File f = new File(filename);
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
		String line = "";

		Map<String, String> symptom_herb = new HashMap<>();

		while ((line = reader.readLine()) != null) {

			System.out.println(line);

			String[] temp = line.split("\t");

			if (temp.length == 2)

				symptom_herb.put(temp[0], temp[1]);

		}

		reader.close();

		return symptom_herb;
	}

	/**
	 * 单味药的症状知识
	 * 
	 * @param filename
	 * @return
	 * @throws IOException
	 * 
	 */
	public static Map<String, Set<String>> getSymptomHerbSetKnowledge(String filename) throws IOException {

		File f = new File(filename);
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
		String line = "";

		Map<String, Set<String>> symptom_herb = new HashMap<>();

		while ((line = reader.readLine()) != null) {

			System.out.println(line);

			String[] temp = line.split("\t");

			if (temp.length == 2) {

				Set<String> herb_set = new HashSet<>();

				String[] herbs = temp[1].split(" ");

				for (String herb : herbs) {

					herb_set.add(herb);
				}
				symptom_herb.put(temp[0], herb_set);
			}

		}

		reader.close();

		return symptom_herb;
	}

}

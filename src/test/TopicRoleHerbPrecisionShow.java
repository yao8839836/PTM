package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import util.ReadWriteFile;

public class TopicRoleHerbPrecisionShow {

	public static void main(String[] args) throws IOException {

		String topic_file = "result//topic_ptm_c_25_role.txt";

		Map<String, String> symptom_herb = getSymptomHerbKnowledge("data//symptom_herb_tcm_mesh.txt");

		System.out.println(symptom_herb);

		File f = new File(topic_file);
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
		String line = "";

		StringBuilder sb = new StringBuilder();

		while ((line = reader.readLine()) != null) {

			String[] temp = line.split("\t");

			String[] symptoms = temp[1].split(" ");

			StringBuilder herb_str = new StringBuilder();

			for (int s = 0; s < symptoms.length; s++) {

				String herb = symptom_herb.get(symptoms[s]);

				herb_str.append(herb + "\t");

			}

			String herb_set = herb_str.toString();

			String[] roles = temp[0].split("  ");

			for (String role : roles) {
				System.out.println(role);

				String herbs = role.substring(6);

				System.out.println(herbs);

				String[] herb = herbs.split(" ");

				for (String h : herb) {

					if (herb_set.contains(h)) {

						sb.append(h + " " + 1 + " ");

						for (String symptom : symptoms) {

							String h1 = symptom_herb.get(symptom);

							if (h1 != null && h1.contains(h)) {
								sb.append("(" + symptom + ")");
							}
						}

					} else {
						sb.append(h + " ");
					}

				}
				sb.append("    ");
			}
			sb.append(temp[1]);
			sb.append("\n");

		}

		reader.close();

		ReadWriteFile.writeFile("file//topic_ptm_c_role_evaluation.txt", sb.toString().trim());

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

}

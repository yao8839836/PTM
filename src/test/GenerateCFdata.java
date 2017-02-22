package test;

import java.io.IOException;
import java.util.List;

import util.Corpus;
import util.ReadWriteFile;

public class GenerateCFdata {

	public static void main(String[] args) throws IOException {

		List<String> herbs_list = Corpus.getVocab("data//herbs_contains.txt");

		List<String> symptoms_list = Corpus.getVocab("data//symptom_contains.txt");

		int[][] herbs_train = Corpus.getDocuments("file//pre_herbs_train.txt");

		int[][] symptoms_train = Corpus.getDocuments("file//pre_symptoms_train.txt");

		double[][] herb_symptom_matrix = new double[herbs_list.size()][symptoms_list.size()];

		double[] herb_count = new double[herbs_list.size()];

		double[] symptom_count = new double[symptoms_list.size()];

		// 共生次数作为评价
		for (int i = 0; i < herbs_train.length; i++) {

			int[] herb_train = herbs_train[i];

			for (int h : herb_train) {
				herb_count[h] += 1.0 / herb_train.length;
			}

			int[] symptom_train = symptoms_train[i];

			for (int s : symptom_train) {
				symptom_count[s] += 1.0 / symptom_train.length;
			}

			for (int h : herb_train) {

				for (int s : symptom_train) {
					herb_symptom_matrix[h][s] += 1.0 / (herb_train.length * symptom_train.length);

				}
			}

		}

		// 单味药作为用户
		StringBuilder sb = new StringBuilder();

		for (int h = 0; h < herb_symptom_matrix.length; h++) {

			for (int s = 0; s < herb_symptom_matrix[0].length; s++) {
				if (herb_symptom_matrix[h][s] != 0)
					sb.append(h + "," + s + "," + herb_symptom_matrix[h][s] / herb_count[h] + "\n");
				if (herb_count[h] == 0) {
					sb.append(h + "," + s + "," + 0 + "\n");
				}
			}

		}

		ReadWriteFile.writeFile("file//cf_herbs_as_users.csv", sb.toString());

		// 症状作为用户

		sb = new StringBuilder();

		for (int s = 0; s < herb_symptom_matrix[0].length; s++) {

			for (int h = 0; h < herb_symptom_matrix.length; h++) {
				if (herb_symptom_matrix[h][s] != 0)
					sb.append(s + "," + h + "," + herb_symptom_matrix[h][s] / symptom_count[s] + "\n");

				if (symptom_count[s] == 0) {
					sb.append(s + "," + h + "," + 0 + "\n");
				}

			}
		}

		ReadWriteFile.writeFile("file//cf_symptoms_as_users.csv", sb.toString());

	}

}

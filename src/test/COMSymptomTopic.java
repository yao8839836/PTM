package test;

import java.io.IOException;
import java.util.List;

import topic.COM;
import util.Common;
import util.Corpus;
import util.ReadWriteFile;

public class COMSymptomTopic {

	public static void main(String[] args) throws IOException {

		StringBuilder sb_str = new StringBuilder();

		int K = 20;

		for (int iter = 0; iter < 10; iter++) {

			List<String> herbs_list = Corpus.getVocab("data//herbs_contains.txt");

			List<String> symptoms_list = Corpus.getVocab("data//symptom_contains.txt");

			int[][] herbs_train = Corpus.getDocuments("file//pre_herbs.txt");

			int[][] symptoms_train = Corpus.getDocuments("file//pre_symptoms.txt");

			int total_items = 0;

			for (int[] symptom_train : symptoms_train) {

				total_items += symptom_train.length;

			}

			int[][] users = new int[total_items][];

			int[] items = new int[total_items];

			System.out.println("有多少个Group : " + total_items);

			int group_index = 0;

			for (int i = 0; i < herbs_train.length; i++) {
				for (int j = 0; j < symptoms_train[i].length; j++) {

					users[group_index] = herbs_train[i];
					items[group_index] = symptoms_train[i][j];
					group_index++;

				}
			}

			COM com = new COM(users, items, herbs_list.size(), symptoms_list.size());

			double alpha = 50.0 / K;
			double beta = 0.01;
			double eta = 0.01;
			double gamma = 0.5;
			double rou = 0.01;

			int iterations = 1000;

			com.markovChain(K, alpha, beta, gamma, rou, eta, iterations);

			double[][] phi_zu = com.estimatePhiZU();

			double[][] phi_zi = com.estimatePhiZI();

			double[][] phi_for_write = Common.makeCopy(phi_zu);

			double[][] phi_bar_for_write = Common.makeCopy(phi_zi);

			StringBuilder sb = new StringBuilder();

			for (int k = 0; k < phi_for_write.length; k++) {

				double[] phi_k = phi_for_write[k];

				StringBuilder herb_str = new StringBuilder();

				for (int i = 0; i < 10; i++) {

					int max_index = Common.maxIndex(phi_k);

					herb_str.append(herbs_list.get(max_index) + " ");

					phi_k[max_index] = 0;

				}

				double[] phi_bar_k = phi_bar_for_write[k];

				StringBuilder symptom_str = new StringBuilder();

				for (int i = 0; i < 10; i++) {

					int max_index = Common.maxIndex(phi_bar_k);

					symptom_str.append(symptoms_list.get(max_index) + " ");

					phi_bar_k[max_index] = 0;

				}

				sb.append(herb_str.toString().trim() + "\t" + symptom_str.toString().trim() + "\n");

			}

			String filename = "result//topic_com_sym_" + K + ".txt";
			ReadWriteFile.writeFile(filename, sb.toString());

			args = new String[1];

			args[0] = filename;

			String precision = TopicPrecisionSymToHerb.main(args);

			String symptom_coherence = TopicKnowCoherence.main(args);

			sb_str.append(precision + "," + symptom_coherence + "\n");

		}

		ReadWriteFile.writeFile("file///com_sym_" + K + "_topic.csv", sb_str.toString());

	}

}

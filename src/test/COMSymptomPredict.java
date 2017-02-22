package test;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import topic.COM;
import util.Common;
import util.Corpus;
import util.ReadWriteFile;

public class COMSymptomPredict {

	public static void main(String[] args) throws IOException {

		StringBuilder sb = new StringBuilder();

		int K = 15;

		int top_k = 5;

		for (int iter = 0; iter < 10; iter++) {

			List<String> herbs_list = Corpus.getVocab("data//herbs_contains.txt");

			List<String> symptoms_list = Corpus.getVocab("data//symptom_contains.txt");

			int[][] herbs_train = Corpus.getDocuments("file//pre_herbs_train.txt");

			int[][] symptoms_train = Corpus.getDocuments("file//pre_symptoms_train.txt");

			int[][] herbs_test = Corpus.getDocuments("file//pre_herbs_test.txt");

			int[][] symptoms_test = Corpus.getDocuments("file//pre_symptoms_test.txt");

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

			double[][] phi_ui = com.estimatePhiUI();

			double[][] phi_zi = com.estimatePhiZI();

			double[] lambda = com.estimateLambada();

			double precision_k = 0;

			for (int i = 0; i < herbs_test.length; i++) {

				int[] test_group = herbs_test[i];

				double[] theta = COMHerbPredict.estimateTheta(test_group, phi_zu, alpha);

				double[] scores = COMHerbPredict.computeRecommendScore(test_group, theta, phi_zu, phi_zi, lambda,
						phi_ui);

				Set<Integer> top_k_predict = new HashSet<>();

				for (int k = 0; k < top_k; k++) {

					int max_index = Common.maxIndex(scores);
					top_k_predict.add(max_index);
					scores[max_index] = 0;

				}

				Set<Integer> real_items = new HashSet<>();

				for (int s : symptoms_test[i]) {

					real_items.add(s);
				}

				int hit_count = 0;
				for (int s : top_k_predict) {

					if (real_items.contains(s)) {
						hit_count++;
					}
				}

				precision_k += (double) hit_count / top_k;

			}

			precision_k /= symptoms_test.length;

			System.out.println("Precision@" + top_k + " : " + precision_k);

			sb.append(precision_k + "\n");

		}

		ReadWriteFile.writeFile("file//com_symptom_" + K + "_" + top_k + ".csv", sb.toString());

	}

}

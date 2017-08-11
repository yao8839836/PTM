package test;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import topic.COM;
import util.Common;
import util.Corpus;
import util.ReadWriteFile;

public class COMHerbPredict {

	public static void main(String[] args) throws IOException {

		StringBuilder sb = new StringBuilder();

		int K = 20;

		int top_k = 5;

		for (int iter = 0; iter < 10; iter++) {

			List<String> herbs_list = Corpus.getVocab("data//herbs_contains.txt");

			List<String> symptoms_list = Corpus.getVocab("data//symptom_contains.txt");

			int[][] herbs_train = Corpus.getDocuments("file//pre_herbs_train.txt");

			int[][] symptoms_train = Corpus.getDocuments("file//pre_symptoms_train.txt");

			int[][] herbs_test = Corpus.getDocuments("file//pre_herbs_test.txt");

			int[][] symptoms_test = Corpus.getDocuments("file//pre_symptoms_test.txt");

			int total_items = 0;

			for (int[] herb_train : herbs_train) {

				total_items += herb_train.length;

			}

			int[][] users = new int[total_items][];

			int[] items = new int[total_items];

			System.out.println("有多少个Group : " + total_items);

			int group_index = 0;

			for (int i = 0; i < symptoms_train.length; i++) {
				for (int j = 0; j < herbs_train[i].length; j++) {

					users[group_index] = symptoms_train[i];
					items[group_index] = herbs_train[i][j];
					group_index++;

				}
			}

			COM com = new COM(users, items, symptoms_list.size(), herbs_list.size());

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

			for (int i = 0; i < symptoms_test.length; i++) {

				int[] test_group = symptoms_test[i];

				double[] theta = estimateTheta(test_group, phi_zu, alpha);

				double[] scores = computeRecommendScore(test_group, theta, phi_zu, phi_zi, lambda, phi_ui);

				// double prob_sum = 0;
				//
				// for (int j = 0; j < scores.length; j++)
				// prob_sum += scores[j];

				// if (prob_sum - 1 > 0.0001 || prob_sum - 1 < -0.0001)
				// System.out.println("单味药预测概率之和不为1 : " + prob_sum);
				Set<Integer> top_k_predict = new HashSet<>();

				for (int k = 0; k < top_k; k++) {

					int max_index = Common.maxIndex(scores);
					top_k_predict.add(max_index);
					scores[max_index] = 0;

				}

				Set<Integer> real_items = new HashSet<>();

				for (int h : herbs_test[i]) {

					real_items.add(h);
				}

				int hit_count = 0;
				for (int h : top_k_predict) {

					if (real_items.contains(h)) {
						hit_count++;
					}
				}

				precision_k += (double) hit_count / top_k;

			}

			precision_k /= symptoms_test.length;

			System.out.println("Precision@" + top_k + " : " + precision_k);

			sb.append(precision_k + "\n");

		}

		ReadWriteFile.writeFile("file//com_herb_" + K + "_" + top_k + ".csv", sb.toString());

	}

	/**
	 * 计算推荐分值，KDD 14论文公式13
	 * 
	 * @param test_group
	 * @param theta
	 * @param phi_zu
	 * @param lambda
	 * @param phi_ui
	 * @return
	 */
	public static double[] computeRecommendScore(int[] test_group, double[] theta, double[][] phi_zu, double[][] phi_zi,
			double[] lambda, double[][] phi_ui) {

		double[] scores = new double[phi_ui[0].length];

		for (int item = 0; item < scores.length; item++) {

			double score = 1;
			for (int i = 0; i < test_group.length; i++) {

				int user = test_group[i];

				double sum = 0;

				for (int k = 0; k < theta.length; k++) {
					sum += theta[k] * phi_zu[k][user]
							* (lambda[user] * phi_zi[k][item] + (1 - lambda[user]) * phi_ui[user][i]);
				}
				score *= sum;

			}

			scores[item] = score;

		}

		return scores;
	}

	/**
	 * 用吉布斯采样估计测试用户组的主题比例,KDD 14论文公式12
	 * 
	 * @param test_group
	 * @param phi_zu
	 * @param alpha
	 * @return
	 */
	public static double[] estimateTheta(int[] test_group, double[][] phi_zu, double alpha) {

		int K = phi_zu.length;
		double[] theta = new double[K];

		int[] gz = new int[K];

		int iterations = 1000;

		int[] z = new int[test_group.length];

		// initialize
		for (int n = 0; n < z.length; n++) {

			int topic = (int) (Math.random() * K);

			z[n] = topic;

			gz[topic]++;

		}

		// inference,sampling

		for (int iter = 0; iter < iterations; iter++) {

			for (int n = 0; n < z.length; n++) {

				int topic = z[n];

				gz[topic]--;

				double[] pr = new double[K];

				for (int k = 0; k < K; k++) {
					pr[k] = phi_zu[k][test_group[n]] * (gz[k] + alpha);
				}

				topic = Common.sample(pr);

				z[n] = topic;

				gz[topic]++;

			}

		}

		// estimate

		for (int k = 0; k < K; k++) {
			theta[k] = (gz[k] + alpha) / (test_group.length + K * alpha);
		}

		return theta;
	}
}

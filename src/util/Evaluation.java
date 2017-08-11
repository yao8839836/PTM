package util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Evaluation {

	/**
	 * LinkLDA的predictive perplexity (including both symptoms and herbs)
	 * 
	 * @param test_herbs
	 * @param test_symptoms
	 * @param herb_topic
	 * @param symptom_topic
	 * @return
	 */
	public static double link_lda_predictive_perplexity(int[][] test_herbs, int[][] test_symptoms,
			double[][] herb_topic, double[][] symptom_topic, double alpha, int iterations) {

		int K = herb_topic.length;

		int[][] nd = new int[test_herbs.length][K];

		double[][] theta_test = new double[test_herbs.length][K];

		int[][] syndrome = new int[test_symptoms.length][];

		int[][] treatment = new int[test_herbs.length][];

		// 随机初始化测试集的隐变量
		for (int p = 0; p < test_herbs.length; p++) {

			treatment[p] = new int[test_herbs[p].length];

			for (int h = 0; h < test_herbs[p].length; h++) {

				int topic = (int) (Math.random() * K);

				treatment[p][h] = topic;

				nd[p][topic]++;

			}

			syndrome[p] = new int[test_symptoms[p].length];

			for (int s = 0; s < test_symptoms[p].length; s++) {

				int topic = (int) (Math.random() * K);

				syndrome[p][s] = topic;

				nd[p][topic]++;

			}

		}

		// 迭代若干次，采样隐变量
		for (int iter = 0; iter < iterations; iter++) {

			for (int p = 0; p < test_herbs.length; p++) {

				for (int h = 0; h < test_herbs[p].length; h++) {

					nd[p][treatment[p][h]]--;

					double[] pr = new double[K];

					for (int k = 0; k < K; k++) {

						pr[k] = (nd[p][k] + alpha) / (test_herbs[p].length + test_symptoms[p].length + K * alpha)
								* herb_topic[k][test_herbs[p][h]];

					}

					int topic = Common.sample(pr);

					treatment[p][h] = topic;

					nd[p][treatment[p][h]]++;

				}

				for (int s = 0; s < test_symptoms[p].length; s++) {

					nd[p][syndrome[p][s]]--;

					double[] pr = new double[K];

					for (int k = 0; k < K; k++) {

						pr[k] = (nd[p][k] + alpha) / (test_herbs[p].length + test_symptoms[p].length + K * alpha)
								* symptom_topic[k][test_symptoms[p][s]];

					}

					int topic = Common.sample(pr);

					syndrome[p][s] = topic;

					nd[p][syndrome[p][s]]++;

				}

			}

		}
		// 估计测试集theta
		for (int p = 0; p < test_herbs.length; p++) {

			for (int k = 0; k < K; k++) {

				theta_test[p][k] = (nd[p][k] + alpha) / (test_herbs[p].length + test_symptoms[p].length + K * alpha);

			}

		}

		// 计算预测困惑度
		double perplexity = 0;

		int denominator = 0;

		for (int p = 0; p < test_herbs.length; p++) {

			denominator += (test_symptoms[p].length + test_herbs[p].length);

		}

		double numerator = 0;

		for (int p = 0; p < test_herbs.length; p++) {

			for (int h = 0; h < test_herbs[p].length; h++) {

				double prob = 0;

				for (int k = 0; k < K; k++) {

					prob += theta_test[p][k] * herb_topic[k][test_herbs[p][h]];

				}

				numerator += Math.log(prob);

			}

			for (int s = 0; s < test_symptoms[p].length; s++) {

				double prob = 0;

				for (int k = 0; k < K; k++) {

					prob += theta_test[p][k] * symptom_topic[k][test_symptoms[p][s]];

				}

				numerator += Math.log(prob);

			}

		}

		perplexity = Math.exp(-numerator / denominator);

		return perplexity;
	}

	/**
	 * 
	 * LinkLDA的training perplexity (including both symptoms and herbs)
	 * 
	 * @param herbs
	 * @param symptoms
	 * @param herb_topic
	 * @param symptom_topic
	 * @param prescription_topic
	 * @return
	 */
	public static double link_lda_training_perplexity(int[][] herbs, int[][] symptoms, double[][] herb_topic,
			double[][] symptom_topic, double[][] prescription_topic) {

		double perplexity = 0;

		int denominator = 0;

		for (int p = 0; p < herbs.length; p++) {

			denominator += (symptoms[p].length + herbs[p].length);

		}

		double numerator = 0;

		for (int p = 0; p < herbs.length; p++) {

			for (int h = 0; h < herbs[p].length; h++) {

				double prob = 0;

				for (int k = 0; k < herb_topic.length; k++) {

					prob += prescription_topic[p][k] * herb_topic[k][herbs[p][h]];

				}

				numerator += Math.log(prob);

			}

			for (int s = 0; s < symptoms[p].length; s++) {

				double prob = 0;

				for (int k = 0; k < symptom_topic.length; k++) {

					prob += prescription_topic[p][k] * symptom_topic[k][symptoms[p][s]];

				}

				numerator += Math.log(prob);

			}

		}

		perplexity = Math.exp(-numerator / denominator);

		return perplexity;

	}

	/**
	 * LinkLDA症状预测效果
	 * 
	 * @param test_herbs
	 *            测试集的单味药
	 * @param test_symptoms
	 *            测试集的症状
	 * @param herb_topic
	 *            单味药主题
	 * @param symptom_topic
	 *            症状主题
	 * @return
	 */

	public static double link_lda_symptom_predictive_perplexity(int[][] test_herbs, int[][] test_symptoms,
			double[][] herb_topic, double[][] symptom_topic) {

		double perplexity = 0;

		int denominator = 0;

		for (int i = 0; i < test_symptoms.length; i++) {

			denominator += test_symptoms[i].length;

		}
		double numerator = 0;

		for (int i = 0; i < test_herbs.length; i++) {

			double prob = link_lda_symptom_predictive_probability(test_herbs[i], test_symptoms[i], herb_topic,
					symptom_topic);
			// System.out.println(prob);

			numerator += Math.log(prob);

		}

		perplexity = Math.exp(-numerator / denominator);

		return perplexity;
	}

	/**
	 * LinkLDA症状预测Precision@K
	 * 
	 * @param test_herbs
	 *            测试集的单味药
	 * @param test_symptoms
	 *            测试集的症状
	 * @param herb_topic
	 *            单味药主题
	 * @param symptom_topic
	 *            症状主题
	 * @param K
	 *            前K个
	 * @return
	 * @throws IOException
	 */
	public static double link_lda_symptom_precision_k(int[][] test_herbs, int[][] test_symptoms, double[][] herb_topic,
			double[][] symptom_topic, int K) throws IOException {

		double precision_k = 0;

		StringBuilder sb = new StringBuilder();

		double rank_sum = 0;

		int symptom_count = 0;

		for (int i = 0; i < test_symptoms.length; i++) {

			int[] test_herb = test_herbs[i];

			int[] test_symptom = test_symptoms[i];

			double[] prob = new double[symptom_topic[0].length];

			double prob_sum = 0;

			for (int s = 0; s < prob.length; s++) {
				prob[s] = link_lda_symptom_predictive_probability(test_herb, s, herb_topic, symptom_topic);

				prob_sum += prob[s];
			}

			if (prob_sum - 1 > 0.0001 || prob_sum - 1 < -0.0001)
				System.out.println(prob_sum);

			// 输出每个症状预测概率

			for (int s : test_symptom) {

				int rank = 1;
				double prob_s = link_lda_symptom_predictive_probability(test_herb, s, herb_topic, symptom_topic);

				for (int s_i = 0; s_i < prob.length; s_i++) {
					if (prob[s_i] > prob_s)
						rank++;
				}

				rank_sum += rank;

				symptom_count++;

				sb.append(prob_s + "\t" + rank + " ");

			}

			sb.append("\n");

			Set<Integer> top_k_predict = new HashSet<>();

			for (int k = 0; k < K; k++) {

				int max_index = Common.maxIndex(prob);
				top_k_predict.add(max_index);
				prob[max_index] = 0;
			}

			Set<Integer> real_symptoms = new HashSet<>();

			for (int s : test_symptom) {

				real_symptoms.add(s);
			}

			int hit_count = 0;
			for (int sym : top_k_predict) {

				if (real_symptoms.contains(sym)) {
					hit_count++;
				}
			}

			precision_k += (double) hit_count / K;

		}

		ReadWriteFile.writeFile("result//linklda_symptom_predictive_prob.txt", sb.toString());

		System.out.println((double) rank_sum / symptom_count);
		precision_k = precision_k / test_symptoms.length;
		return precision_k;
	}

	/**
	 * LinkLDA症状预测Recall@K
	 * 
	 * @param test_herbs
	 *            测试集的单味药
	 * @param test_symptoms
	 *            测试集的症状
	 * @param herb_topic
	 *            单味药主题
	 * @param symptom_topic
	 *            症状主题
	 * @param K
	 *            前K个
	 * @return
	 */
	public static double link_lda_symptom_recall_k(int[][] test_herbs, int[][] test_symptoms, double[][] herb_topic,
			double[][] symptom_topic, int K) {

		double recall_k = 0;

		for (int i = 0; i < test_symptoms.length; i++) {

			int[] test_herb = test_herbs[i];

			int[] test_symptom = test_symptoms[i];

			double[] prob = new double[symptom_topic[0].length];

			for (int s = 0; s < prob.length; s++) {
				prob[s] = link_lda_symptom_predictive_probability(test_herb, s, herb_topic, symptom_topic);
			}

			Set<Integer> top_k_predict = new HashSet<>();

			for (int k = 0; k < K; k++) {

				int max_index = Common.maxIndex(prob);
				top_k_predict.add(max_index);
				prob[max_index] = 0;
			}

			Set<Integer> real_symptoms = new HashSet<>();

			for (int s : test_symptom) {

				real_symptoms.add(s);
			}

			int hit_count = 0;

			int real_count = real_symptoms.size();

			for (int sym : real_symptoms) {

				if (top_k_predict.contains(sym)) {
					hit_count++;
				}
			}

			recall_k += (double) hit_count / real_count;

		}
		recall_k = recall_k / test_symptoms.length;
		return recall_k;
	}

	/**
	 * LinkLDA症状预测NDCG
	 * 
	 * @param test_herbs
	 *            测试集的单味药
	 * @param test_symptoms
	 *            测试集的症状
	 * @param herb_topic
	 *            单味药主题
	 * @param symptom_topic
	 *            症状主题
	 * @param K
	 *            前K个
	 * @return
	 */
	public static double link_lda_symptom_ndcg(int[][] test_herbs, int[][] test_symptoms, double[][] herb_topic,
			double[][] symptom_topic, int K) {

		double average_ndcg = 0;

		for (int i = 0; i < test_symptoms.length; i++) {

			int[] test_herb = test_herbs[i];

			int[] test_symptom = test_symptoms[i];

			double[] prob = new double[symptom_topic[0].length];

			for (int s = 0; s < prob.length; s++) {
				prob[s] = link_lda_symptom_predictive_probability(test_herb, s, herb_topic, symptom_topic);
			}

			List<Integer> top_k_predict = new ArrayList<>();

			for (int k = 0; k < K; k++) {

				int max_index = Common.maxIndex(prob);
				top_k_predict.add(max_index);
				prob[max_index] = 0;
			}

			Set<Integer> real_symptoms = new HashSet<>();

			for (int s : test_symptom) {

				real_symptoms.add(s);
			}

			double dcg = 0;

			if (real_symptoms.contains(top_k_predict.get(0))) {
				dcg += 1.0;
			}

			for (int k = 1; k < K; k++) {

				int predicted_symptom = top_k_predict.get(k);

				if (real_symptoms.contains(predicted_symptom)) {
					dcg += 1.0 / Common.log2(k + 1);
				}
			}

			double idcg = 1.0;

			if (test_symptom.length > 1) {

				for (int k = 1; k < test_symptom.length; k++) {
					idcg += Common.log2(k + 1);
				}
			}

			double ndcg = dcg / idcg;

			average_ndcg += ndcg;

		}

		return average_ndcg / test_symptoms.length;
	}

	/**
	 * 
	 * LinkLDA一组症状预测概率
	 * 
	 * @param test_herb
	 * @param test_symptom
	 * @param herb_topic
	 * @param symptom_topic
	 * @return
	 */
	public static double link_lda_symptom_predictive_probability(int[] test_herb, int[] test_symptom,
			double[][] herb_topic, double[][] symptom_topic) {

		double prod = 1;

		for (int m = 0; m < test_symptom.length; m++) {

			int symptom = test_symptom[m];

			double sum = link_lda_symptom_predictive_probability(test_herb, symptom, herb_topic, symptom_topic);

			prod *= sum;
		}

		return prod;
	}

	/**
	 * 
	 * LinkLDA一个症状预测概率
	 * 
	 * @param test_herb
	 * @param test_symptom
	 * @param herb_topic
	 * @param symptom_topic
	 * @return
	 */
	public static double link_lda_symptom_predictive_probability(int[] test_herb, int test_symptom,
			double[][] herb_topic, double[][] symptom_topic) {

		double sum = 0;

		for (int n = 0; n < test_herb.length; n++) {

			int herb = test_herb[n];

			for (int k = 0; k < symptom_topic.length; k++) {

				double phi_bar = symptom_topic[k][test_symptom];

				double phi = herb_topic[k][herb];

				double sum_1 = 0;

				for (int k_1 = 0; k_1 < herb_topic.length; k_1++) {

					sum_1 += herb_topic[k_1][herb];

				}

				sum += phi_bar * phi / sum_1;

			}

		}
		sum /= test_herb.length;

		return sum;
	}

	/**
	 * LinkLDA单味药预测效果
	 * 
	 * @param test_herbs
	 *            测试集的单味药
	 * @param test_symptoms
	 *            测试集的症状
	 * @param herb_topic
	 *            单味药主题
	 * @param symptom_topic
	 *            症状主题
	 * @return
	 */

	public static double link_lda_herb_predictive_perplexity(int[][] test_herbs, int[][] test_symptoms,
			double[][] herb_topic, double[][] symptom_topic) {

		double perplexity = 0;

		int denominator = 0;

		for (int i = 0; i < test_herbs.length; i++) {

			denominator += test_herbs[i].length;

		}
		double numerator = 0;

		for (int i = 0; i < test_symptoms.length; i++) {

			numerator += Math.log(
					link_lda_herb_predictive_probability(test_herbs[i], test_symptoms[i], herb_topic, symptom_topic));

		}

		perplexity = Math.exp(-numerator / denominator);

		return perplexity;
	}

	/**
	 * LinkLDA单味药预测Precision@k
	 * 
	 * @param test_herbs
	 *            测试集的单味药
	 * @param test_symptoms
	 *            测试集的症状
	 * @param herb_topic
	 *            单味药主题
	 * @param symptom_topic
	 *            症状主题
	 * @param K
	 *            Top K
	 * @return
	 */
	public static double link_lda_herb_precision_k(int[][] test_herbs, int[][] test_symptoms, double[][] herb_topic,
			double[][] symptom_topic, int K) {

		double precision_k = 0;

		double rank_sum = 0;

		int herb_count = 0;

		for (int i = 0; i < test_symptoms.length; i++) {

			int[] test_herb = test_herbs[i];

			int[] test_symptom = test_symptoms[i];

			double[] prob = new double[herb_topic[0].length];

			double prob_sum = 0;

			for (int h = 0; h < prob.length; h++) {
				prob[h] = link_lda_herb_predictive_probability(h, test_symptom, herb_topic, symptom_topic);

				prob_sum += prob[h];
			}

			if (prob_sum - 1 > 0.0001 || prob_sum - 1 < -0.0001)
				System.out.println("单味药预测概率之和不为1 : " + prob_sum);

			for (int h : test_herb) {

				int rank = 1;

				double h_prob = link_lda_herb_predictive_probability(h, test_symptom, herb_topic, symptom_topic);

				for (int h_1 = 0; h_1 < prob.length; h_1++) {

					if (prob[h_1] > h_prob) {
						rank++;
					}

				}

				rank_sum += rank;

				herb_count++;

			}

			Set<Integer> top_k_predict = new HashSet<>();

			for (int k = 0; k < K; k++) {

				int max_index = Common.maxIndex(prob);
				top_k_predict.add(max_index);
				prob[max_index] = 0;
			}

			Set<Integer> real_herbs = new HashSet<>();

			for (int h : test_herb) {

				real_herbs.add(h);
			}

			int hit_count = 0;

			for (int herb : top_k_predict) {

				if (real_herbs.contains(herb)) {
					hit_count++;
				}
			}

			precision_k += (double) hit_count / K;

		}

		System.out.println((double) rank_sum / herb_count);
		precision_k = precision_k / test_herbs.length;

		return precision_k;
	}

	/**
	 * LinkLDA单味药预测Recall@k
	 * 
	 * @param test_herbs
	 *            测试集的单味药
	 * @param test_symptoms
	 *            测试集的症状
	 * @param herb_topic
	 *            单味药主题
	 * @param symptom_topic
	 *            症状主题
	 * @param K
	 *            Top K
	 * @return
	 */
	public static double link_lda_herb_recall_k(int[][] test_herbs, int[][] test_symptoms, double[][] herb_topic,
			double[][] symptom_topic, int K) {

		double recall_k = 0;

		for (int i = 0; i < test_symptoms.length; i++) {

			int[] test_herb = test_herbs[i];

			int[] test_symptom = test_symptoms[i];

			double[] prob = new double[herb_topic[0].length];

			for (int h = 0; h < prob.length; h++) {
				prob[h] = link_lda_herb_predictive_probability(h, test_symptom, herb_topic, symptom_topic);
			}

			Set<Integer> top_k_predict = new HashSet<>();

			for (int k = 0; k < K; k++) {

				int max_index = Common.maxIndex(prob);
				top_k_predict.add(max_index);
				prob[max_index] = 0;
			}

			Set<Integer> real_herbs = new HashSet<>();

			for (int h : test_herb) {

				real_herbs.add(h);
			}

			int hit_count = 0;

			int real_count = real_herbs.size();

			for (int herb : real_herbs) {

				if (top_k_predict.contains(herb)) {
					hit_count++;
				}
			}

			recall_k += (double) hit_count / real_count;

		}
		recall_k = recall_k / test_herbs.length;

		return recall_k;
	}

	/**
	 * LinkLDA单味药预测NDCG@k
	 * 
	 * @param test_herbs
	 *            测试集的单味药
	 * @param test_symptoms
	 *            测试集的症状
	 * @param herb_topic
	 *            单味药主题
	 * @param symptom_topic
	 *            症状主题
	 * @param K
	 *            Top K
	 * @return
	 */
	public static double link_lda_herb_ndcg(int[][] test_herbs, int[][] test_symptoms, double[][] herb_topic,
			double[][] symptom_topic, int K) {

		double average_ndcg = 0;

		for (int i = 0; i < test_symptoms.length; i++) {

			int[] test_herb = test_herbs[i];

			int[] test_symptom = test_symptoms[i];

			double[] prob = new double[herb_topic[0].length];

			for (int h = 0; h < prob.length; h++) {
				prob[h] = link_lda_herb_predictive_probability(h, test_symptom, herb_topic, symptom_topic);
			}

			List<Integer> top_k_predict = new ArrayList<>();

			for (int k = 0; k < K; k++) {

				int max_index = Common.maxIndex(prob);
				top_k_predict.add(max_index);
				prob[max_index] = 0;
			}

			Set<Integer> real_herbs = new HashSet<>();

			for (int h : test_herb) {

				real_herbs.add(h);
			}

			double dcg = 0;

			if (real_herbs.contains(top_k_predict.get(0))) {
				dcg += 1.0;
			}

			for (int k = 1; k < K; k++) {

				int predicted_herb = top_k_predict.get(k);

				if (real_herbs.contains(predicted_herb)) {
					dcg += 1.0 / Common.log2(k + 1);
				}
			}

			double idcg = 1.0;

			if (test_herb.length > 1) {

				for (int k = 1; k < test_herb.length; k++) {
					idcg += Common.log2(k + 1);
				}
			}

			double ndcg = dcg / idcg;

			average_ndcg += ndcg;

		}

		return average_ndcg / test_herbs.length;
	}

	/**
	 * 
	 * LinkLDA一组单味药预测概率
	 * 
	 * @param test_herb
	 * @param test_symptom
	 * @param herb_topic
	 * @param symptom_topic
	 * @return
	 */
	public static double link_lda_herb_predictive_probability(int[] test_herb, int[] test_symptom,
			double[][] herb_topic, double[][] symptom_topic) {

		double prod = 1;

		for (int n = 0; n < test_herb.length; n++) {

			double sum = 0;

			int herb = test_herb[n];

			for (int m = 0; m < test_symptom.length; m++) {

				int symptom = test_symptom[m];

				for (int k = 0; k < herb_topic.length; k++) {

					double phi_bar = symptom_topic[k][symptom];

					double phi = herb_topic[k][herb];

					double sum_1 = 0;

					for (int k_1 = 0; k_1 < symptom_topic.length; k_1++) {

						sum_1 += symptom_topic[k_1][symptom];

					}

					sum += phi_bar * phi / sum_1;

				}

			}
			sum /= test_symptom.length;

			prod *= sum;
		}

		return prod;
	}

	/**
	 * 
	 * LinkLDA一个单味药预测概率
	 * 
	 * @param test_herb
	 * @param test_symptom
	 * @param herb_topic
	 * @param symptom_topic
	 * @return
	 */
	public static double link_lda_herb_predictive_probability(int test_herb, int[] test_symptom, double[][] herb_topic,
			double[][] symptom_topic) {

		double sum = 0;

		for (int m = 0; m < test_symptom.length; m++) {

			int symptom = test_symptom[m];

			for (int k = 0; k < herb_topic.length; k++) {

				double phi_bar = symptom_topic[k][symptom];

				double phi = herb_topic[k][test_herb];

				double sum_1 = 0;

				for (int k_1 = 0; k_1 < symptom_topic.length; k_1++) {

					sum_1 += symptom_topic[k_1][symptom];

				}

				sum += phi_bar * phi / sum_1;

			}

		}
		sum /= test_symptom.length;

		return sum;
	}

	/**
	 * PTM症状预测效果
	 * 
	 * @param test_herbs
	 *            测试集的单味药
	 * @param test_symptoms
	 *            测试集的症状
	 * @param herb_topic
	 *            单味药主题
	 * @param symptom_topic
	 *            症状主题
	 * @param topic_role
	 *            主题-君臣佐使
	 * @return
	 */

	public static double ptm_symptom_predictive_perplexity(int[][] test_herbs, int[][] test_symptoms,
			double[][][] herb_topic, double[][] symptom_topic, double[][] topic_role) {

		double perplexity = 0;

		int denominator = 0;

		for (int i = 0; i < test_symptoms.length; i++) {

			denominator += test_symptoms[i].length;

		}
		double numerator = 0;

		for (int i = 0; i < test_herbs.length; i++) {

			numerator += Math.log(ptm_symptom_predictive_probability(test_herbs[i], test_symptoms[i], herb_topic,
					symptom_topic, topic_role));

		}

		perplexity = Math.exp(-numerator / denominator);

		return perplexity;
	}

	/**
	 * PTM症状预测precision@K
	 * 
	 * @param test_herbs
	 *            测试集的单味药
	 * @param test_symptoms
	 *            测试集的症状
	 * @param herb_topic
	 *            单味药主题
	 * @param symptom_topic
	 *            症状主题
	 * @param topic_role
	 *            主题-君臣佐使
	 * @param K
	 *            Top K
	 * @return
	 * @throws IOException
	 */
	public static double ptm_symptom_precision_k(int[][] test_herbs, int[][] test_symptoms, double[][][] herb_topic,
			double[][] symptom_topic, double[][] topic_role, int K) throws IOException {

		double precision_k = 0;

		int rank_sum = 0;

		int symptom_count = 0;

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < test_symptoms.length; i++) {

			int[] test_herb = test_herbs[i];

			int[] test_symptom = test_symptoms[i];

			double[] prob = new double[symptom_topic[0].length];

			double prob_sum = 0;

			for (int s = 0; s < prob.length; s++) {
				prob[s] = ptm_symptom_predictive_probability(test_herb, s, herb_topic, symptom_topic, topic_role);

				prob_sum += prob[s];
			}

			if (prob_sum - 1 > 0.0001 || prob_sum - 1 < -0.0001)
				System.out.println(prob_sum);

			// 输出每个症状预测概率

			for (int s : test_symptom) {

				int rank = 1;
				double prob_s = ptm_symptom_predictive_probability(test_herb, s, herb_topic, symptom_topic, topic_role);

				for (int s_i = 0; s_i < prob.length; s_i++) {
					if (prob[s_i] > prob_s)
						rank++;
				}
				rank_sum += rank;

				symptom_count++;

				sb.append(prob_s + "\t" + rank + " ");

			}
			sb.append("\n");

			Set<Integer> top_k_predict = new HashSet<>();

			for (int k = 0; k < K; k++) {

				int max_index = Common.maxIndex(prob);
				top_k_predict.add(max_index);
				prob[max_index] = 0;
			}

			Set<Integer> real_symptoms = new HashSet<>();

			for (int s : test_symptom) {

				real_symptoms.add(s);
			}

			int hit_count = 0;

			for (int sym : top_k_predict) {

				if (real_symptoms.contains(sym)) {
					hit_count++;
				}
			}
			precision_k += (double) hit_count / K;
		}
		System.out.println((double) rank_sum / symptom_count);
		ReadWriteFile.writeFile("result//ptm_symptom_predictive_prob.txt", sb.toString());
		precision_k = precision_k / test_symptoms.length;

		return precision_k;
	}

	/**
	 * PTM症状预测recall@K
	 * 
	 * @param test_herbs
	 *            测试集的单味药
	 * @param test_symptoms
	 *            测试集的症状
	 * @param herb_topic
	 *            单味药主题
	 * @param symptom_topic
	 *            症状主题
	 * @param topic_role
	 *            主题-君臣佐使
	 * @param K
	 *            Top K
	 * @return
	 */
	public static double ptm_symptom_recall_k(int[][] test_herbs, int[][] test_symptoms, double[][][] herb_topic,
			double[][] symptom_topic, double[][] topic_role, int K) {

		double recall_k = 0;

		for (int i = 0; i < test_symptoms.length; i++) {

			int[] test_herb = test_herbs[i];

			int[] test_symptom = test_symptoms[i];

			double[] prob = new double[symptom_topic[0].length];

			for (int s = 0; s < prob.length; s++) {
				prob[s] = ptm_symptom_predictive_probability(test_herb, s, herb_topic, symptom_topic, topic_role);
			}

			Set<Integer> top_k_predict = new HashSet<>();

			for (int k = 0; k < K; k++) {

				int max_index = Common.maxIndex(prob);
				top_k_predict.add(max_index);
				prob[max_index] = 0;
			}

			Set<Integer> real_symptoms = new HashSet<>();

			for (int s : test_symptom) {

				real_symptoms.add(s);
			}

			int hit_count = 0;

			int real_count = real_symptoms.size();

			for (int sym : real_symptoms) {

				if (top_k_predict.contains(sym)) {
					hit_count++;
				}
			}
			recall_k += (double) hit_count / real_count;
		}
		recall_k = recall_k / test_symptoms.length;

		return recall_k;
	}

	/**
	 * PTM症状预测NDCG
	 * 
	 * @param test_herbs
	 *            测试集的单味药
	 * @param test_symptoms
	 *            测试集的症状
	 * @param herb_topic
	 *            单味药主题
	 * @param symptom_topic
	 *            症状主题
	 * @param topic_role
	 *            主题-君臣佐使
	 * @param K
	 *            Top K
	 * @return
	 */
	public static double ptm_symptom_ndcg(int[][] test_herbs, int[][] test_symptoms, double[][][] herb_topic,
			double[][] symptom_topic, double[][] topic_role, int K) {

		double average_ndcg = 0;

		for (int i = 0; i < test_symptoms.length; i++) {

			int[] test_herb = test_herbs[i];

			int[] test_symptom = test_symptoms[i];

			double[] prob = new double[symptom_topic[0].length];

			for (int s = 0; s < prob.length; s++) {
				prob[s] = ptm_symptom_predictive_probability(test_herb, s, herb_topic, symptom_topic, topic_role);
			}

			List<Integer> top_k_predict = new ArrayList<>();

			for (int k = 0; k < K; k++) {

				int max_index = Common.maxIndex(prob);
				top_k_predict.add(max_index);
				prob[max_index] = 0;
			}

			Set<Integer> real_symptoms = new HashSet<>();

			for (int s : test_symptom) {

				real_symptoms.add(s);
			}

			double dcg = 0;

			if (real_symptoms.contains(top_k_predict.get(0))) {
				dcg += 1.0;
			}

			for (int k = 1; k < K; k++) {

				int predicted_symptom = top_k_predict.get(k);

				if (real_symptoms.contains(predicted_symptom)) {
					dcg += 1.0 / Common.log2(k + 1);
				}
			}

			double idcg = 1.0;

			if (test_symptom.length > 1) {

				for (int k = 1; k < test_symptom.length; k++) {
					idcg += Common.log2(k + 1);
				}
			}

			double ndcg = dcg / idcg;

			average_ndcg += ndcg;

		}

		return average_ndcg / test_symptoms.length;
	}

	/**
	 * 
	 * PTM一组症状预测概率
	 * 
	 * @param test_herb
	 * @param test_symptom
	 * @param herb_topic
	 * @param symptom_topic
	 * @param topic_role
	 * @return
	 */
	public static double ptm_symptom_predictive_probability(int[] test_herb, int[] test_symptom,
			double[][][] herb_topic, double[][] symptom_topic, double[][] topic_role) {

		double prod = 1;

		for (int m = 0; m < test_symptom.length; m++) {

			double sum = 0;

			int symptom = test_symptom[m];

			for (int n = 0; n < test_herb.length; n++) {

				int herb = test_herb[n];

				for (int k = 0; k < symptom_topic.length; k++) {

					double phi_bar = symptom_topic[k][symptom];

					double sum_1 = 0;

					for (int x = 0; x < herb_topic[k].length; x++) {

						double phi_psi = herb_topic[k][x][herb];

						sum_1 += phi_psi;
					}

					double sum_2 = 0;

					for (int k_1 = 0; k_1 < herb_topic.length; k_1++) {

						for (int x = 0; x < herb_topic[k_1].length; x++) {

							sum_2 += herb_topic[k_1][x][herb];

						}

					}

					sum += phi_bar * sum_1 / sum_2;

				}

			}
			sum /= test_herb.length;

			prod *= sum;
		}

		return prod;
	}

	/**
	 * 
	 * PTM一个症状预测概率
	 * 
	 * @param test_herb
	 * @param test_symptom
	 * @param herb_topic
	 * @param symptom_topic
	 * @param symptom_topic
	 * @return
	 */
	public static double ptm_symptom_predictive_probability(int[] test_herb, int test_symptom, double[][][] herb_topic,
			double[][] symptom_topic, double[][] topic_role) {

		double sum = 0;

		for (int n = 0; n < test_herb.length; n++) {

			int herb = test_herb[n];

			for (int k = 0; k < symptom_topic.length; k++) {

				double phi_bar = symptom_topic[k][test_symptom];

				double sum_1 = 0;

				for (int x = 0; x < herb_topic[k].length; x++) {

					double phi = herb_topic[k][x][herb];

					sum_1 += phi;
				}

				double sum_2 = 0;

				for (int k_1 = 0; k_1 < herb_topic.length; k_1++) {

					for (int x = 0; x < herb_topic[k_1].length; x++) {

						sum_2 += herb_topic[k_1][x][herb];

					}

				}

				sum += phi_bar * sum_1 / sum_2;

			}

		}
		sum /= test_herb.length;

		return sum;
	}

	/**
	 * PTM单味药预测效果
	 * 
	 * @param test_herbs
	 *            测试集的单味药
	 * @param test_symptoms
	 *            测试集的症状
	 * @param herb_topic
	 *            单味药主题
	 * @param symptom_topic
	 *            症状主题
	 * @param topic_role
	 *            主题-君臣佐使
	 * @return
	 */

	public static double ptm_herb_predictive_perplexity(int[][] test_herbs, int[][] test_symptoms,
			double[][][] herb_topic, double[][] symptom_topic, double[][] topic_role) {

		double perplexity = 0;

		int denominator = 0;

		for (int i = 0; i < test_herbs.length; i++) {

			denominator += test_herbs[i].length;

		}
		double numerator = 0;

		for (int i = 0; i < test_herbs.length; i++) {

			numerator += Math.log(ptm_herb_predictive_probability(test_herbs[i], test_symptoms[i], herb_topic,
					symptom_topic, topic_role));

		}

		perplexity = Math.exp(-numerator / denominator);

		return perplexity;
	}

	/**
	 * PTM单味药预测Precision@K
	 * 
	 * @param test_herbs
	 * @param test_symptoms
	 * @param herb_topic
	 * @param symptom_topic
	 * @param topic_role
	 * @param K
	 * @return
	 */
	public static double ptm_herb_precision_k(int[][] test_herbs, int[][] test_symptoms, double[][][] herb_topic,
			double[][] symptom_topic, double[][] topic_role, int K) {

		double precision_k = 0;

		int rank_sum = 0;

		int herb_count = 0;

		for (int i = 0; i < test_symptoms.length; i++) {

			int[] test_herb = test_herbs[i];

			int[] test_symptom = test_symptoms[i];

			double[] prob = new double[herb_topic[0][0].length];

			double prob_sum = 0;

			for (int h = 0; h < prob.length; h++) {
				prob[h] = ptm_herb_predictive_probability(h, test_symptom, herb_topic, symptom_topic, topic_role);

				prob_sum += prob[h];
			}
			if (prob_sum - 1 > 0.0001 || prob_sum - 1 < -0.0001)
				System.out.println("单味药概率之和不为1 : " + prob_sum);

			for (int h : test_herb) {

				int rank = 1;

				double h_prob = ptm_herb_predictive_probability(h, test_symptom, herb_topic, symptom_topic, topic_role);

				for (int h_1 = 0; h_1 < prob.length; h_1++) {

					if (prob[h_1] > h_prob) {
						rank++;
					}

				}

				rank_sum += rank;

				herb_count++;

			}

			Set<Integer> top_k_predict = new HashSet<>();

			for (int k = 0; k < K; k++) {

				int max_index = Common.maxIndex(prob);

				top_k_predict.add(max_index);

				prob[max_index] = 0;
			}

			Set<Integer> real_herbs = new HashSet<>();

			for (int h : test_herb) {

				real_herbs.add(h);
			}

			int hit_count = 0;

			for (int herb : top_k_predict) {

				if (real_herbs.contains(herb)) {

					hit_count++;
				}
			}
			precision_k += (double) hit_count / K;
		}

		System.out.println((double) rank_sum / herb_count);
		precision_k = precision_k / test_herbs.length;

		return precision_k;

	}

	/**
	 * PTM单味药预测Recall@K
	 * 
	 * @param test_herbs
	 * @param test_symptoms
	 * @param herb_topic
	 * @param symptom_topic
	 * @param topic_role
	 * @param K
	 * @return
	 */
	public static double ptm_herb_recall_k(int[][] test_herbs, int[][] test_symptoms, double[][][] herb_topic,
			double[][] symptom_topic, double[][] topic_role, int K) {

		double recall_k = 0;

		for (int i = 0; i < test_symptoms.length; i++) {

			int[] test_herb = test_herbs[i];

			int[] test_symptom = test_symptoms[i];

			double[] prob = new double[herb_topic[0][0].length];

			for (int h = 0; h < prob.length; h++) {
				prob[h] = ptm_herb_predictive_probability(h, test_symptom, herb_topic, symptom_topic, topic_role);
			}

			Set<Integer> top_k_predict = new HashSet<>();

			for (int k = 0; k < K; k++) {

				int max_index = Common.maxIndex(prob);

				top_k_predict.add(max_index);

				prob[max_index] = 0;
			}

			Set<Integer> real_herbs = new HashSet<>();

			for (int h : test_herb) {

				real_herbs.add(h);
			}

			int hit_count = 0;

			int real_count = real_herbs.size();

			for (int herb : real_herbs) {

				if (top_k_predict.contains(herb)) {

					hit_count++;
				}
			}
			recall_k += (double) hit_count / real_count;
		}
		recall_k = recall_k / test_herbs.length;

		return recall_k;

	}

	/**
	 * PTM单味药预测NDCG
	 * 
	 * @param test_herbs
	 * @param test_symptoms
	 * @param herb_topic
	 * @param symptom_topic
	 * @param topic_role
	 * @param K
	 * @return
	 */
	public static double ptm_herb_ndcg(int[][] test_herbs, int[][] test_symptoms, double[][][] herb_topic,
			double[][] symptom_topic, double[][] topic_role, int K) {

		double average_ndcg = 0;

		for (int i = 0; i < test_symptoms.length; i++) {

			int[] test_herb = test_herbs[i];

			int[] test_symptom = test_symptoms[i];

			double[] prob = new double[herb_topic[0][0].length];

			for (int h = 0; h < prob.length; h++) {
				prob[h] = ptm_herb_predictive_probability(h, test_symptom, herb_topic, symptom_topic, topic_role);
			}

			List<Integer> top_k_predict = new ArrayList<>();

			for (int k = 0; k < K; k++) {

				int max_index = Common.maxIndex(prob);
				top_k_predict.add(max_index);
				prob[max_index] = 0;
			}

			Set<Integer> real_herbs = new HashSet<>();

			for (int h : test_herb) {

				real_herbs.add(h);
			}

			double dcg = 0;

			if (real_herbs.contains(top_k_predict.get(0))) {
				dcg += 1.0;
			}

			for (int k = 1; k < K; k++) {

				int predicted_herb = top_k_predict.get(k);

				if (real_herbs.contains(predicted_herb)) {
					dcg += 1.0 / Common.log2(k + 1);
				}
			}

			double idcg = 1.0;

			if (test_herb.length > 1) {

				for (int k = 1; k < test_herb.length; k++) {
					idcg += Common.log2(k + 1);
				}
			}

			double ndcg = dcg / idcg;

			average_ndcg += ndcg;
		}

		return average_ndcg / test_herbs.length;

	}

	/**
	 * 
	 * PTM一组单味药预测概率
	 * 
	 * @param test_herb
	 * @param test_symptom
	 * @param herb_topic
	 * @param symptom_topic
	 * @param topic_role
	 * @return
	 */
	public static double ptm_herb_predictive_probability(int[] test_herb, int[] test_symptom, double[][][] herb_topic,
			double[][] symptom_topic, double[][] topic_role) {

		double prod = 1;

		for (int n = 0; n < test_herb.length; n++) {

			double sum = 0;

			int herb = test_herb[n];

			for (int m = 0; m < test_symptom.length; m++) {

				int symptom = test_symptom[m];

				for (int k = 0; k < herb_topic.length; k++) {

					double phi_bar = symptom_topic[k][symptom];

					double sum_0 = 0;

					for (int x = 0; x < herb_topic[k].length; x++) {

						double phi = herb_topic[k][x][herb];

						sum_0 += phi * topic_role[k][x];

					}

					double sum_1 = 0;

					for (int k_1 = 0; k_1 < symptom_topic.length; k_1++) {

						sum_1 += symptom_topic[k_1][symptom];

					}

					sum += phi_bar * sum_0 / sum_1;

				}

			}
			sum /= test_symptom.length;

			prod *= sum;
		}

		return prod;
	}

	/**
	 * 
	 * PTM一个单味药预测概率
	 * 
	 * @param test_herb
	 * @param test_symptom
	 * @param herb_topic
	 * @param symptom_topic
	 * @param topic_role
	 * @return
	 */
	public static double ptm_herb_predictive_probability(int test_herb, int[] test_symptom, double[][][] herb_topic,
			double[][] symptom_topic, double[][] topic_role) {

		double sum = 0;

		for (int m = 0; m < test_symptom.length; m++) {

			int symptom = test_symptom[m];

			for (int k = 0; k < herb_topic.length; k++) {

				double phi_bar = symptom_topic[k][symptom];

				double sum_0 = 0;

				for (int x = 0; x < herb_topic[k].length; x++) {

					double phi = herb_topic[k][x][test_herb];

					sum_0 += phi * topic_role[k][x];

				}

				double sum_1 = 0;

				for (int k_1 = 0; k_1 < symptom_topic.length; k_1++) {

					sum_1 += symptom_topic[k_1][symptom];

				}

				sum += phi_bar * sum_0 / sum_1;

			}

		}
		sum /= test_symptom.length;

		return sum;
	}

	/**
	 * ATM症状预测效果
	 * 
	 * @param test_herbs
	 *            测试集的单味药
	 * @param test_symptoms
	 *            测试集的症状
	 * @param herb_topic
	 *            单味药主题
	 * @param topic_symptom
	 *            主题-症状分布
	 * @return
	 */

	public static double atm_symptom_predictive_perplexity(int[][] test_herbs, int[][] test_symptoms,
			double[][] herb_topic, double[][] topic_symptom) {

		double perplexity = 0;

		int denominator = 0;

		for (int i = 0; i < test_symptoms.length; i++) {

			denominator += test_symptoms[i].length;

		}
		double numerator = 0;

		for (int i = 0; i < test_herbs.length; i++) {

			numerator += Math.log(
					atm_symptom_predictive_probability(test_herbs[i], test_symptoms[i], herb_topic, topic_symptom));

		}

		perplexity = Math.exp(-numerator / denominator);

		return perplexity;
	}

	/**
	 * ATM症状预测Precision@K
	 * 
	 * @param test_herbs
	 * @param test_symptoms
	 * @param herb_topic
	 * @param topic_symptom
	 * @param K
	 * @return
	 * @throws IOException
	 */
	public static double atm_symptom_precision_k(int[][] test_herbs, int[][] test_symptoms, double[][] herb_topic,
			double[][] topic_symptom, int K) throws IOException {

		double precision_k = 0;

		double rank_sum = 0;

		int symptom_count = 0;

		for (int i = 0; i < test_symptoms.length; i++) {

			int[] test_herb = test_herbs[i];

			int[] test_symptom = test_symptoms[i];

			double[] prob = new double[topic_symptom[0].length];

			double prob_sum = 0;

			for (int s = 0; s < prob.length; s++) {
				prob[s] = atm_symptom_predictive_probability(test_herb, s, herb_topic, topic_symptom);

				prob_sum += prob[s];
			}

			if (prob_sum - 1 > 0.0001 || prob_sum - 1 < -0.0001)
				System.out.println("症状预测概率之和不为1 : " + prob_sum);

			// 输出每个症状预测概率

			for (int s : test_symptom) {

				int rank = 1;
				double prob_s = atm_symptom_predictive_probability(test_herb, s, herb_topic, topic_symptom);

				for (int s_i = 0; s_i < prob.length; s_i++) {
					if (prob[s_i] > prob_s)
						rank++;
				}

				rank_sum += rank;

				symptom_count++;

			}

			Set<Integer> top_k_predict = new HashSet<>();

			for (int k = 0; k < K; k++) {

				int max_index = Common.maxIndex(prob);
				top_k_predict.add(max_index);
				prob[max_index] = 0;
			}

			Set<Integer> real_symptoms = new HashSet<>();

			for (int s : test_symptom) {

				real_symptoms.add(s);
			}

			int hit_count = 0;
			for (int sym : top_k_predict) {

				if (real_symptoms.contains(sym)) {
					hit_count++;
				}
			}

			precision_k += (double) hit_count / K;

		}

		System.out.println((double) rank_sum / symptom_count);
		precision_k = precision_k / test_symptoms.length;

		return precision_k;
	}

	/**
	 * 
	 * ATM症状预测Recall@K
	 * 
	 * @param test_herbs
	 * @param test_symptoms
	 * @param herb_topic
	 * @param topic_symptom
	 * @param K
	 * @return
	 */
	public static double atm_symptom_recall_k(int[][] test_herbs, int[][] test_symptoms, double[][] herb_topic,
			double[][] topic_symptom, int K) {

		double recall_k = 0;

		for (int i = 0; i < test_symptoms.length; i++) {

			int[] test_herb = test_herbs[i];

			int[] test_symptom = test_symptoms[i];

			double[] prob = new double[topic_symptom[0].length];

			for (int s = 0; s < prob.length; s++) {
				prob[s] = atm_symptom_predictive_probability(test_herb, s, herb_topic, topic_symptom);
			}

			Set<Integer> top_k_predict = new HashSet<>();

			for (int k = 0; k < K; k++) {

				int max_index = Common.maxIndex(prob);
				top_k_predict.add(max_index);
				prob[max_index] = 0;
			}

			Set<Integer> real_symptoms = new HashSet<>();

			for (int s : test_symptom) {

				real_symptoms.add(s);
			}

			int hit_count = 0;

			int real_count = real_symptoms.size();

			for (int sym : real_symptoms) {

				if (top_k_predict.contains(sym)) {
					hit_count++;
				}
			}

			recall_k += (double) hit_count / real_count;

		}
		recall_k = recall_k / test_symptoms.length;
		return recall_k;
	}

	/**
	 * 
	 * ATM症状预测NDCG
	 * 
	 * @param test_herbs
	 * @param test_symptoms
	 * @param herb_topic
	 * @param topic_symptom
	 * @param K
	 * @return
	 */
	public static double atm_symptom_ndcg(int[][] test_herbs, int[][] test_symptoms, double[][] herb_topic,
			double[][] topic_symptom, int K) {

		double average_ndcg = 0;

		for (int i = 0; i < test_symptoms.length; i++) {

			int[] test_herb = test_herbs[i];

			int[] test_symptom = test_symptoms[i];

			double[] prob = new double[topic_symptom[0].length];

			for (int s = 0; s < prob.length; s++) {
				prob[s] = atm_symptom_predictive_probability(test_herb, s, herb_topic, topic_symptom);
			}

			List<Integer> top_k_predict = new ArrayList<>();

			for (int k = 0; k < K; k++) {

				int max_index = Common.maxIndex(prob);
				top_k_predict.add(max_index);
				prob[max_index] = 0;
			}

			Set<Integer> real_symptoms = new HashSet<>();

			for (int s : test_symptom) {

				real_symptoms.add(s);
			}

			double dcg = 0;

			if (real_symptoms.contains(top_k_predict.get(0))) {
				dcg += 1.0;
			}

			for (int k = 1; k < K; k++) {

				int predicted_symptom = top_k_predict.get(k);

				if (real_symptoms.contains(predicted_symptom)) {
					dcg += 1.0 / Common.log2(k + 1);
				}
			}

			double idcg = 1.0;

			if (test_symptom.length > 1) {

				for (int k = 1; k < test_symptom.length; k++) {
					idcg += Common.log2(k + 1);
				}
			}

			double ndcg = dcg / idcg;

			average_ndcg += ndcg;

		}

		return average_ndcg / test_symptoms.length;
	}

	/**
	 * 
	 * ATM一组症状预测概率
	 * 
	 * @param test_herb
	 * @param test_symptom
	 * @param herb_topic
	 * @param topic_symptom
	 * @return
	 */
	public static double atm_symptom_predictive_probability(int[] test_herb, int[] test_symptom, double[][] herb_topic,
			double[][] topic_symptom) {

		double prod = 1;

		for (int m = 0; m < test_symptom.length; m++) {

			double sum = 0;

			int symptom = test_symptom[m];

			for (int n = 0; n < test_herb.length; n++) {

				int herb = test_herb[n];

				for (int k = 0; k < topic_symptom.length; k++) {
					sum += herb_topic[herb][k] * topic_symptom[k][symptom];
				}

			}

			sum /= test_herb.length;

			prod *= sum;

		}

		return prod;
	}

	/**
	 * 
	 * ATM一个症状预测概率
	 * 
	 * @param test_herb
	 * @param test_symptom
	 * @param herb_topic
	 * @param topic_symptom
	 * @return
	 */
	public static double atm_symptom_predictive_probability(int[] test_herb, int test_symptom, double[][] herb_topic,
			double[][] topic_symptom) {

		double sum = 0;

		for (int n = 0; n < test_herb.length; n++) {

			int herb = test_herb[n];

			for (int k = 0; k < topic_symptom.length; k++) {
				sum += herb_topic[herb][k] * topic_symptom[k][test_symptom];
			}

		}

		sum /= test_herb.length;

		return sum;
	}

	/**
	 * ATM单味药预测效果
	 * 
	 * @param test_herbs
	 *            测试集的单味药
	 * @param test_symptoms
	 *            测试集的症状
	 * @param herb_topic
	 *            单味药主题
	 * @param topic_symptom
	 *            主题-症状分布
	 * @return
	 */

	public static double atm_herb_predictive_perplexity(int[][] test_herbs, int[][] test_symptoms,
			double[][] herb_topic, double[][] topic_symptom) {

		double perplexity = 0;

		int denominator = 0;

		for (int i = 0; i < test_herbs.length; i++) {

			denominator += test_herbs[i].length;

		}
		double numerator = 0;

		for (int i = 0; i < test_herbs.length; i++) {

			numerator += Math
					.log(atm_herb_predictive_probability(test_herbs[i], test_symptoms[i], herb_topic, topic_symptom));

		}

		perplexity = Math.exp(-numerator / denominator);

		return perplexity;
	}

	/**
	 * ATM单味药预测Precision@K
	 * 
	 * @param test_herbs
	 * @param test_symptoms
	 * @param herb_topic
	 * @param topic_symptom
	 * @param K
	 * @return
	 */
	public static double atm_herb_precision_k(int[][] test_herbs, int[][] test_symptoms, double[][] herb_topic,
			double[][] topic_symptom, int K) {

		double precision_k = 0;

		double rank_sum = 0;

		int herb_count = 0;

		for (int i = 0; i < test_symptoms.length; i++) {

			int[] test_herb = test_herbs[i];

			int[] test_symptom = test_symptoms[i];

			double[] prob = new double[herb_topic.length];

			double prob_sum = 0;

			for (int h = 0; h < prob.length; h++) {
				prob[h] = atm_herb_predictive_probability(h, test_symptom, herb_topic, topic_symptom);

				prob_sum += prob[h];
			}

			if (prob_sum - 1 > 0.0001 || prob_sum - 1 < -0.0001)
				System.out.println("单味药预测概率之和不为1 : " + prob_sum);

			for (int h : test_herb) {

				int rank = 1;

				double h_prob = atm_herb_predictive_probability(h, test_symptom, herb_topic, topic_symptom);

				for (int h_1 = 0; h_1 < prob.length; h_1++) {

					if (prob[h_1] > h_prob) {
						rank++;
					}

				}

				rank_sum += rank;

				herb_count++;

			}

			Set<Integer> top_k_predict = new HashSet<>();

			for (int k = 0; k < K; k++) {

				int max_index = Common.maxIndex(prob);
				top_k_predict.add(max_index);
				prob[max_index] = 0;
			}

			Set<Integer> real_herbs = new HashSet<>();

			for (int h : test_herb) {

				real_herbs.add(h);
			}

			int hit_count = 0;

			for (int herb : top_k_predict) {

				if (real_herbs.contains(herb)) {
					hit_count++;
				}
			}

			precision_k += (double) hit_count / K;

		}

		System.out.println((double) rank_sum / herb_count);
		precision_k = precision_k / test_herbs.length;

		return precision_k;
	}

	/**
	 * ATM单味药预测Recall@k
	 * 
	 * @param test_herbs
	 *            测试集的单味药
	 * @param test_symptoms
	 *            测试集的症状
	 * @param herb_topic
	 *            单味药主题
	 * @param topic_symptom
	 *            症状主题
	 * @param K
	 *            Top K
	 * @return
	 */
	public static double atm_herb_recall_k(int[][] test_herbs, int[][] test_symptoms, double[][] herb_topic,
			double[][] topic_symptom, int K) {

		double recall_k = 0;

		for (int i = 0; i < test_symptoms.length; i++) {

			int[] test_herb = test_herbs[i];

			int[] test_symptom = test_symptoms[i];

			double[] prob = new double[herb_topic.length];

			for (int h = 0; h < prob.length; h++) {
				prob[h] = atm_herb_predictive_probability(h, test_symptom, herb_topic, topic_symptom);
			}

			Set<Integer> top_k_predict = new HashSet<>();

			for (int k = 0; k < K; k++) {

				int max_index = Common.maxIndex(prob);
				top_k_predict.add(max_index);
				prob[max_index] = 0;
			}

			Set<Integer> real_herbs = new HashSet<>();

			for (int h : test_herb) {

				real_herbs.add(h);
			}

			int hit_count = 0;

			int real_count = real_herbs.size();

			for (int herb : real_herbs) {

				if (top_k_predict.contains(herb)) {
					hit_count++;
				}
			}

			recall_k += (double) hit_count / real_count;

		}
		recall_k = recall_k / test_herbs.length;

		return recall_k;
	}

	/**
	 * ATM单味药预测NDCG@k
	 * 
	 * @param test_herbs
	 *            测试集的单味药
	 * @param test_symptoms
	 *            测试集的症状
	 * @param herb_topic
	 *            单味药主题
	 * @param topic_symptom
	 *            症状主题
	 * @param K
	 *            Top K
	 * @return
	 */
	public static double atm_herb_ndcg(int[][] test_herbs, int[][] test_symptoms, double[][] herb_topic,
			double[][] topic_symptom, int K) {

		double average_ndcg = 0;

		for (int i = 0; i < test_symptoms.length; i++) {

			int[] test_herb = test_herbs[i];

			int[] test_symptom = test_symptoms[i];

			double[] prob = new double[herb_topic.length];

			for (int h = 0; h < prob.length; h++) {
				prob[h] = atm_herb_predictive_probability(h, test_symptom, herb_topic, topic_symptom);
			}

			List<Integer> top_k_predict = new ArrayList<>();

			for (int k = 0; k < K; k++) {

				int max_index = Common.maxIndex(prob);
				top_k_predict.add(max_index);
				prob[max_index] = 0;
			}

			Set<Integer> real_herbs = new HashSet<>();

			for (int h : test_herb) {

				real_herbs.add(h);
			}

			double dcg = 0;

			if (real_herbs.contains(top_k_predict.get(0))) {
				dcg += 1.0;
			}

			for (int k = 1; k < K; k++) {

				int predicted_herb = top_k_predict.get(k);

				if (real_herbs.contains(predicted_herb)) {
					dcg += 1.0 / Common.log2(k + 1);
				}
			}

			double idcg = 1.0;

			if (test_herb.length > 1) {

				for (int k = 1; k < test_herb.length; k++) {
					idcg += Common.log2(k + 1);
				}
			}

			double ndcg = dcg / idcg;

			average_ndcg += ndcg;

		}

		return average_ndcg / test_herbs.length;
	}

	/**
	 * 
	 * ATM一组单味药预测概率
	 * 
	 * @param test_herb
	 * @param test_symptom
	 * @param herb_topic
	 * @param topic_symptom
	 * @return
	 */
	public static double atm_herb_predictive_probability(int[] test_herb, int[] test_symptom, double[][] herb_topic,
			double[][] topic_symptom) {

		double prod = 1;

		for (int n = 0; n < test_herb.length; n++) {

			double sum = 0;

			int herb = test_herb[n];

			for (int m = 0; m < test_symptom.length; m++) {

				int symptom = test_symptom[m];

				for (int k = 0; k < topic_symptom.length; k++) {

					double theta = herb_topic[herb][k];

					double sum_1 = 0;

					for (int h = 0; h < herb_topic.length; h++) {

						sum_1 += herb_topic[h][k];

					}

					double phi = topic_symptom[k][symptom];

					double sum_2 = 0;

					for (int k_1 = 0; k_1 < topic_symptom.length; k_1++) {
						sum_2 += topic_symptom[k_1][symptom];
					}

					sum += theta / sum_1 * phi / sum_2;

				}

			}
			sum /= test_symptom.length;

			prod *= sum;
		}

		return prod;
	}

	/**
	 * 
	 * ATM一个单味药预测概率
	 * 
	 * @param test_herb
	 * @param test_symptom
	 * @param herb_topic
	 * @param topic_symptom
	 * @return
	 */
	public static double atm_herb_predictive_probability(int test_herb, int[] test_symptom, double[][] herb_topic,
			double[][] topic_symptom) {

		double sum = 0;

		for (int m = 0; m < test_symptom.length; m++) {

			int symptom = test_symptom[m];

			for (int k = 0; k < topic_symptom.length; k++) {

				double theta = herb_topic[test_herb][k];

				double sum_1 = 0;

				for (int h = 0; h < herb_topic.length; h++) {

					sum_1 += herb_topic[h][k];

				}

				double phi = topic_symptom[k][symptom];

				double sum_2 = 0;

				for (int k_1 = 0; k_1 < topic_symptom.length; k_1++) {
					sum_2 += topic_symptom[k_1][symptom];
				}

				sum += theta / sum_1 * phi / sum_2;

			}

		}
		sum /= test_symptom.length;

		return sum;
	}

}

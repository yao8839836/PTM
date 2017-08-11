package util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EvaluationPTM {

	/**
	 * 
	 * PTM的predictive perplexity (including both symptoms and herbs)
	 * 
	 * @param test_herbs
	 * @param test_symptoms
	 * @param herb_topic
	 * @param symptom_topic
	 * @param alpha
	 * @param eta
	 * @param iterations
	 * @return
	 */
	public static double ptm_predictive_perplexity(int[][] test_herbs, int[][] test_symptoms, double[][][] herb_topic,
			double[][] symptom_topic, double alpha, double eta, int iterations) {

		int K = herb_topic.length;

		int[][] nd = new int[test_herbs.length][K];

		int X = herb_topic[0].length;

		System.out.println(K);

		System.out.println(X);

		int[][][] nx = new int[test_herbs.length][K][X];

		int[][] nxsum = new int[test_herbs.length][K];

		double[][] theta_test = new double[test_herbs.length][K];

		double[][][] pi_test = new double[test_herbs.length][K][X];

		int[][] syndrome = new int[test_symptoms.length][];

		int[][] treatment = new int[test_herbs.length][];

		int[][] x = new int[test_herbs.length][];

		// 随机初始化测试集的隐变量

		for (int p = 0; p < test_herbs.length; p++) {

			treatment[p] = new int[test_herbs[p].length];
			x[p] = new int[test_herbs[p].length];

			for (int h = 0; h < test_herbs[p].length; h++) {

				int topic = (int) (Math.random() * K);

				treatment[p][h] = topic;

				nd[p][topic]++;

				int jun_chen_zuo_shi = (int) (Math.random() * X);
				x[p][h] = jun_chen_zuo_shi;

				nx[p][topic][jun_chen_zuo_shi]++;

				nxsum[p][topic]++;

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

					nx[p][treatment[p][h]][x[p][h]]--;

					nxsum[p][treatment[p][h]]--;

					double[][] pr = new double[K][X];

					for (int k = 0; k < K; k++) {

						for (int r = 0; r < X; r++) {

							pr[k][r] = (nd[p][k] + alpha) / (test_herbs[p].length + test_symptoms[p].length + K * alpha)
									* (nx[p][k][r] + eta) / (nxsum[p][k] + X * eta) * herb_topic[k][r][h];

						}
					}

					double[] pr_sum = new double[K * X];

					for (int k = 0; k < K; k++) {

						for (int r = 0; r < X; r++) {
							pr_sum[k * X + r] = pr[k][r];
						}

					}

					int index = Common.sample(pr_sum);

					treatment[p][h] = index / X;
					x[p][h] = index % X;

					nd[p][treatment[p][h]]++;

					nx[p][treatment[p][h]][x[p][h]]++;

					nxsum[p][treatment[p][h]]++;

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
		// 估计测试集pi

		for (int p = 0; p < test_herbs.length; p++) {

			for (int k = 0; k < K; k++)
				for (int r = 0; r < X; r++) {
					pi_test[p][k][r] = (nx[p][k][r] + eta) / (nxsum[p][k] + X * eta);
				}
		}

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
					for (int r = 0; r < X; r++) {
						prob += theta_test[p][k] * pi_test[p][k][r] * herb_topic[k][r][test_herbs[p][h]];
					}

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
	 * PTM的training perplexity (including both symptoms and herbs)
	 * 
	 * @param herbs
	 * @param symptoms
	 * @param herb_topic
	 * @param symptom_topic
	 * @param prescription_topic
	 * @param prescription_topic_role
	 * @return
	 */
	public static double ptm_training_perplexity(int[][] herbs, int[][] symptoms, double[][][] herb_topic,
			double[][] symptom_topic, double[][] prescription_topic, double[][][] prescription_topic_role) {

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
					for (int r = 0; r < herb_topic[0].length; r++) {
						prob += prescription_topic[p][k] * prescription_topic_role[p][k][r]
								* herb_topic[k][r][herbs[p][h]];
					}

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
	 * @return
	 */

	public static double ptm_symptom_predictive_perplexity(int[][] test_herbs, int[][] test_symptoms,
			double[][][] herb_topic, double[][] symptom_topic) {

		double perplexity = 0;

		int denominator = 0;

		for (int i = 0; i < test_symptoms.length; i++) {

			denominator += test_symptoms[i].length;

		}
		double numerator = 0;

		for (int i = 0; i < test_herbs.length; i++) {

			numerator += Math.log(
					ptm_symptom_predictive_probability(test_herbs[i], test_symptoms[i], herb_topic, symptom_topic));

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
	 * @param K
	 *            Top K
	 * @return
	 */
	public static double ptm_symptom_precision_k(int[][] test_herbs, int[][] test_symptoms, double[][][] herb_topic,
			double[][] symptom_topic, int K) {

		double precision_k = 0;

		int rank_sum = 1;

		int symptom_count = 0;

		for (int i = 0; i < test_symptoms.length; i++) {

			int[] test_herb = test_herbs[i];

			int[] test_symptom = test_symptoms[i];

			double[] prob = new double[symptom_topic[0].length];

			double prob_sum = 0;
			for (int s = 0; s < prob.length; s++) {

				prob[s] = ptm_symptom_predictive_probability(test_herb, s, herb_topic, symptom_topic);
				prob_sum += prob[s];
			}

			if (prob_sum - 1 > 0.0001 || prob_sum - 1 < -0.0001)
				System.out.println("症状概率之和不为1 : " + prob_sum);

			for (int s : test_symptom) {

				int rank = 1;
				double prob_s = ptm_symptom_predictive_probability(test_herb, s, herb_topic, symptom_topic);

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
	 * @param K
	 *            Top K
	 * @return
	 */
	public static double ptm_symptom_recall_k(int[][] test_herbs, int[][] test_symptoms, double[][][] herb_topic,
			double[][] symptom_topic, int K) {

		double recall_k = 0;

		for (int i = 0; i < test_symptoms.length; i++) {

			int[] test_herb = test_herbs[i];

			int[] test_symptom = test_symptoms[i];

			double[] prob = new double[symptom_topic[0].length];

			for (int s = 0; s < prob.length; s++) {
				prob[s] = ptm_symptom_predictive_probability(test_herb, s, herb_topic, symptom_topic);
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
	 * @param K
	 *            Top K
	 * @return
	 */
	public static double ptm_symptom_ndcg(int[][] test_herbs, int[][] test_symptoms, double[][][] herb_topic,
			double[][] symptom_topic, int K) {

		double average_ndcg = 0;

		for (int i = 0; i < test_symptoms.length; i++) {

			int[] test_herb = test_herbs[i];

			int[] test_symptom = test_symptoms[i];

			double[] prob = new double[symptom_topic[0].length];

			for (int s = 0; s < prob.length; s++) {
				prob[s] = ptm_symptom_predictive_probability(test_herb, s, herb_topic, symptom_topic);
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
	 * @return
	 */
	public static double ptm_symptom_predictive_probability(int[] test_herb, int[] test_symptom,
			double[][][] herb_topic, double[][] symptom_topic) {

		double prod = 1;

		for (int m = 0; m < test_symptom.length; m++) {

			double sum = 0;

			int symptom = test_symptom[m];

			for (int n = 0; n < test_herb.length; n++) {

				int herb = test_herb[n];

				for (int k = 0; k < symptom_topic.length; k++) {

					double phi_bar = symptom_topic[k][symptom];

					double sum_1 = 0;

					for (int x = 0; x < herb_topic[0].length; x++) {

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
	 * @return
	 */
	public static double ptm_symptom_predictive_probability(int[] test_herb, int test_symptom, double[][][] herb_topic,
			double[][] symptom_topic) {

		double sum = 0;

		for (int n = 0; n < test_herb.length; n++) {

			int herb = test_herb[n];

			for (int k = 0; k < symptom_topic.length; k++) {

				double phi_bar = symptom_topic[k][test_symptom];

				double sum_1 = 0;

				for (int x = 0; x < herb_topic[0].length; x++) {

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
	 * P(单味药|症状)矩阵
	 */
	public static double[][] ptm_herb_given_symptom_prob;

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
	 * @param prescription_topic_role
	 *            处方-君臣佐使
	 * @param prescription_topic
	 *            处方-主题
	 * @return
	 */

	public static double ptm_herb_predictive_perplexity(int[][] test_herbs, int[][] test_symptoms,
			double[][][] herb_topic, double[][] symptom_topic, double[][][] prescription_topic_role,
			double[][] prescription_topic) {

		boolean[][] herb_symptom_mark = new boolean[herb_topic[0][0].length][symptom_topic[0].length];
		// 只标记需要计算的概率
		for (int i = 0; i < test_symptoms.length; i++) {

			int[] symptoms = test_symptoms[i];

			for (int h = 0; h < herb_topic[0][0].length; h++)
				for (int s : symptoms) {
					herb_symptom_mark[h][s] = true;
				}

		}

		// P(单味药|症状)矩阵
		ptm_herb_predictive_probability(herb_topic, symptom_topic, prescription_topic_role, prescription_topic,
				herb_symptom_mark);

		double perplexity = 0;

		int denominator = 0;

		for (int i = 0; i < test_herbs.length; i++) {

			denominator += test_herbs[i].length;

		}
		double numerator = 0;

		for (int i = 0; i < test_herbs.length; i++) {

			numerator += Math.log(ptm_herb_predictive_probability(test_herbs[i], test_symptoms[i], herb_topic,
					symptom_topic, prescription_topic_role, prescription_topic));

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
	 * @param prescription_topic_role
	 * @param prescription_topic
	 *            处方-主题
	 * 
	 * @param K
	 * @return
	 */
	public static double ptm_herb_precision_k(int[][] test_herbs, int[][] test_symptoms, double[][][] herb_topic,
			double[][] symptom_topic, double[][][] prescription_topic_role, double[][] prescription_topic, int K) {

		double[][] herb_symptom_prob = ptm_herb_given_symptom_prob;

		double precision_k = 0;

		int rank_sum = 0;

		int herb_count = 0;

		for (int i = 0; i < test_symptoms.length; i++) {

			int[] test_herb = test_herbs[i];

			int[] test_symptom = test_symptoms[i];

			double[] prob = new double[herb_topic[0][0].length];

			double prob_sum = 0;

			for (int h = 0; h < prob.length; h++) {

				prob[h] = ptm_herb_predictive_probability(h, test_symptom, herb_symptom_prob);

				prob_sum += prob[h];

			}

			if (prob_sum - 1 > 0.0001 || prob_sum - 1 < -0.0001)
				System.out.println("单味药概率之和不为1" + prob_sum);

			for (int h : test_herb) {

				int rank = 1;

				double h_prob = ptm_herb_predictive_probability(h, test_symptom, herb_symptom_prob);

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
	 * @param prescription_topic_role
	 * @param prescription_topic
	 *            处方-主题
	 * 
	 * @param K
	 * @return
	 */
	public static double ptm_herb_recall_k(int[][] test_herbs, int[][] test_symptoms, double[][][] herb_topic,
			double[][] symptom_topic, double[][][] prescription_topic_role, double[][] prescription_topic, int K) {

		double[][] herb_symptom_prob = ptm_herb_given_symptom_prob;

		double recall_k = 0;

		for (int i = 0; i < test_symptoms.length; i++) {

			int[] test_herb = test_herbs[i];

			int[] test_symptom = test_symptoms[i];

			double[] prob = new double[herb_topic[0][0].length];

			for (int h = 0; h < prob.length; h++) {
				prob[h] = ptm_herb_predictive_probability(h, test_symptom, herb_symptom_prob);
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
	 * @param prescription_topic_role
	 * @param prescription_topic
	 *            处方-主题
	 * @param K
	 * @return
	 */
	public static double ptm_herb_ndcg(int[][] test_herbs, int[][] test_symptoms, double[][][] herb_topic,
			double[][] symptom_topic, double[][][] prescription_topic_role, double[][] prescription_topic, int K) {

		double[][] herb_symptom_prob = ptm_herb_given_symptom_prob;

		double average_ndcg = 0;

		for (int i = 0; i < test_symptoms.length; i++) {

			int[] test_herb = test_herbs[i];

			int[] test_symptom = test_symptoms[i];

			double[] prob = new double[herb_topic[0][0].length];

			for (int h = 0; h < prob.length; h++) {
				prob[h] = ptm_herb_predictive_probability(h, test_symptom, herb_symptom_prob);
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
	 * @param prescription_role
	 * @param prescription_topic
	 * 
	 * @return
	 */
	public static double ptm_herb_predictive_probability(int[] test_herb, int[] test_symptom, double[][][] herb_topic,
			double[][] symptom_topic, double[][][] prescription_topic_role, double[][] prescription_topic) {

		double[][] herb_symptom_prob = ptm_herb_given_symptom_prob;

		double prod = 1;

		for (int n = 0; n < test_herb.length; n++) {

			double sum = 0;

			int herb = test_herb[n];

			for (int m = 0; m < test_symptom.length; m++) {

				int symptom = test_symptom[m];

				sum += herb_symptom_prob[herb][symptom];

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
	 * 
	 * @param herb_symptom_prob
	 * @return
	 */
	public static double ptm_herb_predictive_probability(int test_herb, int[] test_symptom,
			double[][] herb_symptom_prob) {

		double sum = 0;

		for (int m = 0; m < test_symptom.length; m++) {

			int symptom = test_symptom[m];

			sum += herb_symptom_prob[test_herb][symptom];

		}
		sum /= test_symptom.length;

		return sum;
	}

	/**
	 * PTM给定一个症状一个单味药预测概率(全部算出来)
	 * 
	 * @param test_herb
	 * @param test_symptom
	 * @param herb_topic
	 * @param symptom_topic
	 * @param prescription_topic_role
	 * @param prescription_topic
	 * @param herb_symptom_mark
	 *            是否需要计算标记
	 * @return
	 */
	public static void ptm_herb_predictive_probability(double[][][] herb_topic, double[][] symptom_topic,
			double[][][] prescription_topic_role, double[][] prescription_topic, boolean[][] herb_symptom_mark) {

		double[] sum_1 = new double[symptom_topic[0].length];

		for (int k_1 = 0; k_1 < herb_topic.length; k_1++) {
			for (int s = 0; s < sum_1.length; s++)
				sum_1[s] += symptom_topic[k_1][s];
		}

		double[] sum_2 = new double[symptom_topic.length];

		for (int p_1 = 0; p_1 < prescription_topic.length; p_1++) {

			for (int k = 0; k < herb_topic.length; k++)
				sum_2[k] += prescription_topic[p_1][k];
		}

		double[][] prob = new double[herb_topic[0][0].length][symptom_topic[0].length];

		for (int h = 0; h < prob.length; h++) {
			for (int s = 0; s < prob[0].length; s++) {

				if (herb_symptom_mark[h][s]) {

					for (int k = 0; k < herb_topic.length; k++) {

						double phi_bar = symptom_topic[k][s];

						for (int p = 0; p < prescription_topic_role.length; p++) {

							double sum_0 = 0;

							for (int x = 0; x < prescription_topic_role[0][0].length; x++) {

								double pi = prescription_topic_role[p][k][x];

								double phi = herb_topic[k][x][h];

								sum_0 += phi * pi;

							}

							double theta = prescription_topic[p][k];

							prob[h][s] += sum_0 * theta / sum_2[k] * phi_bar / sum_1[s];

						}

					}

				}

			}
		}

		ptm_herb_given_symptom_prob = prob;
	}

}

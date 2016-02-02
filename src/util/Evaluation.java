package util;

public class Evaluation {

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

			numerator += Math.log(link_lda_symptom_predictive_probability(test_herbs[i], test_symptoms[i], herb_topic,
					symptom_topic));

		}

		perplexity = Math.exp(-numerator / denominator);

		return perplexity;
	}

	/**
	 * 
	 * LinkLDA症状预测概率
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

			double sum = 0;

			int symptom = test_symptom[m];

			for (int n = 0; n < test_herb.length; n++) {

				int herb = test_herb[n];

				for (int k = 0; k < symptom_topic.length; k++) {

					double phi_bar = symptom_topic[k][symptom];

					double phi = herb_topic[k][herb];

					double sum_1 = 0;

					for (int k_1 = 0; k_1 < herb_topic.length; k_1++) {

						sum_1 += herb_topic[k_1][herb];

					}

					sum += phi_bar * phi / sum_1;

				}

			}
			sum /= test_herb.length;

			prod *= sum;
		}

		return prod;
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
	 * 
	 * LinkLDA单味药预测概率
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
	 * 
	 * PTM症状预测概率
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

			prod *= sum;
		}

		return prod;
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
	 * 
	 * PTM单味药预测概率
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
	 * 
	 * ATM症状预测概率
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
	 * 
	 * ATM单味药预测概率
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
	 * PTM症状预测效果(治法-君臣佐使-单味药一条线)
	 * 
	 * @param test_herbs
	 *            测试集的单味药
	 * @param test_symptoms
	 *            测试集的症状
	 * @param topic_role
	 *            治法-君臣佐使
	 * @param role_herb
	 *            君臣佐使-单味药
	 * @param symptom_topic
	 *            症状主题
	 * @return
	 */
	public static double ptm_symptom_predictive_perplexity(int[][] test_herbs, int[][] test_symptoms,
			double[][] topic_role, double[][] role_herb, double[][] symptom_topic) {

		double perplexity = 0;

		int denominator = 0;

		for (int i = 0; i < test_symptoms.length; i++) {

			denominator += test_symptoms[i].length;

		}
		double numerator = 0;

		for (int i = 0; i < test_herbs.length; i++) {

			numerator += Math.log(ptm_symptom_predictive_probability(test_herbs[i], test_symptoms[i], topic_role,
					role_herb, symptom_topic));

		}

		perplexity = Math.exp(-numerator / denominator);

		return perplexity;
	}

	/**
	 * PTM症状预测概率(治法-君臣佐使-单味药一条线)
	 * 
	 * @param test_herb
	 * @param test_symptom
	 * @param topic_role
	 * @param role_herb
	 * @param symptom_topic
	 * @return
	 */
	public static double ptm_symptom_predictive_probability(int[] test_herb, int[] test_symptom, double[][] topic_role,
			double[][] role_herb, double[][] symptom_topic) {

		double prod = 1;

		for (int m = 0; m < test_symptom.length; m++) {

			double sum = 0;

			int symptom = test_symptom[m];

			for (int n = 0; n < test_herb.length; n++) {

				int herb = test_herb[n];

				for (int k = 0; k < symptom_topic.length; k++) {

					double phi_bar = symptom_topic[k][symptom];

					for (int x = 0; x < role_herb.length; x++) {

						double phi = role_herb[x][herb];

						double sum_1 = 0;

						for (int x1 = 0; x1 < role_herb.length; x1++) {

							sum_1 += role_herb[x1][herb];

						}

						double sum_2 = 0;

						double psi = topic_role[k][x];

						for (int k1 = 0; k1 < topic_role.length; k1++) {

							sum_2 += topic_role[k1][x];

						}

						sum += phi / sum_1 * psi / sum_2 * phi_bar;

					}

				}

			}
			sum /= test_herb.length;

			prod *= sum;
		}

		return prod;
	}

	/**
	 * PTM单味药预测效果(治法-君臣佐使-单味药一条线)
	 * 
	 * @param test_herbs
	 *            测试集的单味药
	 * @param test_symptoms
	 *            测试集的症状
	 * @param topic_role
	 *            治法-君臣佐使
	 * @param role_herb
	 *            君臣佐使-单味药
	 * @param symptom_topic
	 *            症状主题
	 * @return
	 */

	public static double ptm_herb_predictive_perplexity(int[][] test_herbs, int[][] test_symptoms,
			double[][] topic_role, double[][] role_herb, double[][] symptom_topic) {

		double perplexity = 0;

		int denominator = 0;

		for (int i = 0; i < test_herbs.length; i++) {

			denominator += test_herbs[i].length;

		}
		double numerator = 0;

		for (int i = 0; i < test_herbs.length; i++) {

			numerator += Math.log(ptm_herb_predictive_probability(test_herbs[i], test_symptoms[i], topic_role,
					role_herb, symptom_topic));

		}

		perplexity = Math.exp(-numerator / denominator);

		return perplexity;
	}

	/**
	 * 
	 * PTM单味药预测概率(治法-君臣佐使-单味药一条线)
	 * 
	 * @param test_herb
	 * @param test_symptom
	 * @param topic_role
	 * @param role_herb
	 * @param symptom_topic
	 * @return
	 */
	public static double ptm_herb_predictive_probability(int[] test_herb, int[] test_symptom, double[][] topic_role,
			double[][] role_herb, double[][] symptom_topic) {

		double prod = 1;

		for (int n = 0; n < test_herb.length; n++) {

			double sum = 0;

			int herb = test_herb[n];

			for (int m = 0; m < test_symptom.length; m++) {

				for (int k = 0; k < symptom_topic.length; k++) {

					for (int x = 0; x < role_herb.length; x++) {

						double phi = role_herb[x][herb];

						double psi = topic_role[k][x];

						double phi_bar = symptom_topic[k][test_symptom[m]];

						double sum_1 = 0;

						for (int k1 = 0; k1 < symptom_topic.length; k1++) {

							sum_1 += symptom_topic[k1][test_symptom[m]];

						}

						sum += phi * psi * phi_bar / sum_1;

					}

				}

			}
			sum /= test_symptom.length;

			prod *= sum;
		}

		return prod;
	}

}

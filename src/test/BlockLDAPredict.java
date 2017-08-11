package test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import topic.BlockLDA;
import util.Corpus;
import util.Evaluation;
import util.ReadWriteFile;

public class BlockLDAPredict {

	public static void main(String[] args) throws IOException {

		StringBuilder sb = new StringBuilder();

		int K = 20;

		int N = 5;

		for (int i = 0; i < 10; i++) {

			List<String> herbs_list = Corpus.getVocab("data//herbs_contains.txt");

			List<String> symptoms_list = Corpus.getVocab("data//symptom_contains.txt");

			int[][] herbs_train = Corpus.getDocuments("file//pre_herbs_train.txt");

			int[][] symptoms_train = Corpus.getDocuments("file//pre_symptoms_train.txt");

			int[][] herbs_test = Corpus.getDocuments("file//pre_herbs_test.txt");

			int[][] symptoms_test = Corpus.getDocuments("file//pre_symptoms_test.txt");

			int[][] links = getHerbLinksSet(herbs_train);

			System.out.println(links.length);

			BlockLDA blda = new BlockLDA(herbs_train, symptoms_train, herbs_list.size(), symptoms_list.size(), links);

			double alpha = 1;
			double alpha_l = 1;
			double beta = 0.1;
			double beta_bar = 0.1;
			int iterations = 1000;

			blda.markovChain(K, alpha, beta, beta_bar, alpha_l, iterations);

			double[][] herb_topic = blda.estimatePhi();

			for (int k = 0; k < herb_topic.length; k++) {

				double sum = 0;
				for (int s = 0; s < herb_topic[0].length; s++) {

					sum += herb_topic[k][s];

					if (herb_topic[k][s] < 0)
						System.out.println("主题-药物概率为负");
				}
				System.out.println(sum);
			}

			double[][] symptom_topic = blda.estimatePhiBar();

			for (int k = 0; k < symptom_topic.length; k++) {

				double sum = 0;
				for (int s = 0; s < symptom_topic[0].length; s++) {

					sum += symptom_topic[k][s];

					if (symptom_topic[k][s] < 0)
						System.out.println("主题-症状概率为负");
				}
				System.out.println(sum);
			}

			double symptom_perplexity = Evaluation.link_lda_symptom_predictive_perplexity(herbs_test, symptoms_test,
					herb_topic, symptom_topic);
			System.out.println("BlockLDA symptom predictive perplexity : " + symptom_perplexity);

			double symptom_precision_k = Evaluation.link_lda_symptom_precision_k(herbs_test, symptoms_test, herb_topic,
					symptom_topic, N);
			System.out.println("BlockLDA symptom precision@" + N + ": " + symptom_precision_k);

			double symptom_recall_k = Evaluation.link_lda_symptom_recall_k(herbs_test, symptoms_test, herb_topic,
					symptom_topic, N);
			System.out.println("BlockLDA symptom recall@" + N + ": " + symptom_recall_k);

			double symptom_ndcg_k = Evaluation.link_lda_symptom_ndcg(herbs_test, symptoms_test, herb_topic,
					symptom_topic, N);
			System.out.println("BlockLDA symptom NDCG@" + N + ": " + symptom_ndcg_k);

			double herb_perplexity = Evaluation.link_lda_herb_predictive_perplexity(herbs_test, symptoms_test,
					herb_topic, symptom_topic);
			System.out.println("BlockLDA herb predictive perplexity : " + herb_perplexity);

			double herb_precision_k = Evaluation.link_lda_herb_precision_k(herbs_test, symptoms_test, herb_topic,
					symptom_topic, N);
			System.out.println("BlockLDA herb precision@" + N + ": " + herb_precision_k);

			double herb_recall_k = Evaluation.link_lda_herb_recall_k(herbs_test, symptoms_test, herb_topic,
					symptom_topic, N);
			System.out.println("BlockLDA herb recall@" + N + ": " + herb_recall_k);

			double herb_ndcg_k = Evaluation.link_lda_herb_ndcg(herbs_test, symptoms_test, herb_topic, symptom_topic, N);
			System.out.println("BlockLDA herb NDCG@" + N + ": " + herb_ndcg_k);

			sb.append(herb_perplexity + "," + herb_precision_k + "," + symptom_perplexity + "," + symptom_precision_k
					+ "\n");
		}

		ReadWriteFile.writeFile("file//blocklda_" + K + "_" + N + ".csv", sb.toString());

	}

	/**
	 * 获得药对集合
	 * 
	 * @param herbs
	 * @return
	 */
	public static int[][] getHerbLinksSet(int[][] herbs) {

		Set<Set<Integer>> pair_set = new HashSet<>();

		for (int p = 0; p < herbs.length; p++) {

			for (int h = 0; h < herbs[p].length; h++) {

				for (int h1 = 0; h1 < herbs[p].length; h1++) {

					if (herbs[p][h] != herbs[p][h1]) {

						Set<Integer> pair = new HashSet<>();

						pair.add(herbs[p][h]);

						pair.add(herbs[p][h1]);

						pair_set.add(pair);

					}

				}

			}
		}

		List<Set<Integer>> pairs_list = new ArrayList<>(pair_set);

		int[][] links = new int[pairs_list.size()][2];

		for (int i = 0; i < links.length; i++) {

			List<Integer> pair = new ArrayList<>(pairs_list.get(i));

			links[i][0] = pair.get(0);

			links[i][1] = pair.get(1);

		}

		return links;

	}

	/**
	 * 获得药对列表
	 * 
	 * @param herbs
	 * @return
	 */

	public static int[][] getHerbLinksList(int[][] herbs) {

		List<String> pairs = new ArrayList<>();

		for (int p = 0; p < herbs.length; p++) {

			for (int h = 0; h < herbs[p].length; h++) {

				for (int h1 = 0; h1 < herbs[p].length; h1++) {

					if (herbs[p][h] != herbs[p][h1]) {

						pairs.add(herbs[p][h] + "\t" + herbs[p][h1]);

					}

				}

			}
		}

		int[][] links = new int[pairs.size()][2];

		for (int i = 0; i < links.length; i++) {

			String[] temp = pairs.get(i).split("\t");

			links[i][0] = Integer.parseInt(temp[0]);

			links[i][1] = Integer.parseInt(temp[1]);

		}

		return links;

	}

}

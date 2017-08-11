package test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import topic.BiBTM;
import util.Corpus;
import util.Evaluation;
import util.ReadWriteFile;

public class BiBTMPredict {

	public static void main(String[] args) throws IOException {

		StringBuilder sb = new StringBuilder();

		int K = 20;

		int N = 5;

		for (int i = 0; i < 10; i++) {

			List<String> herbs_list = Corpus.getVocab("data//herbs_contains.txt");

			List<String> symptoms_list = Corpus.getVocab("data//symptom_contains.txt");

			int[][] herbs_train = Corpus.getDocuments("file//pre_herbs_train.txt");

			int[][] symptoms_train = Corpus.getDocuments("file//pre_symptoms_train.txt");

			int[][] herb_pairs = getHerbLinksSet(herbs_train);

			System.out.println("药对共计" + herb_pairs.length + "对");

			int[][] symptom_pairs = getHerbLinksSet(symptoms_train);

			System.out.println("症状对共计" + symptom_pairs.length + "对");

			int[][] pairs = getHerbSymptomLinksSet(herbs_train, symptoms_train);

			System.out.println("药物-症状对共计" + pairs.length + "对");

			int H = herbs_list.size();
			int S = symptoms_list.size();

			BiBTM bibtm = new BiBTM(herb_pairs, symptom_pairs, pairs, H, S);

			double alpha = 1;
			double beta = 0.1;

			int iterations = 1000;

			bibtm.markovChain(K, alpha, beta, iterations);

			double[][] herb_topic = bibtm.estimatePhi();

			double[][] symptom_topic = bibtm.estimatePhiBar();

			int[][] herbs_test = Corpus.getDocuments("file//pre_herbs_test.txt");

			int[][] symptoms_test = Corpus.getDocuments("file//pre_symptoms_test.txt");

			double symptom_perplexity = Evaluation.link_lda_symptom_predictive_perplexity(herbs_test, symptoms_test,
					herb_topic, symptom_topic);

			System.out.println("BiBTM symptom predictive perplexity : " + symptom_perplexity);

			double symptom_precision_k = Evaluation.link_lda_symptom_precision_k(herbs_test, symptoms_test, herb_topic,
					symptom_topic, N);
			System.out.println("BiBTM symptom precision@" + N + ": " + symptom_precision_k);

			double symptom_recall_k = Evaluation.link_lda_symptom_recall_k(herbs_test, symptoms_test, herb_topic,
					symptom_topic, N);
			System.out.println("BiBTM symptom recall@" + N + ": " + symptom_recall_k);

			double symptom_ndcg_k = Evaluation.link_lda_symptom_ndcg(herbs_test, symptoms_test, herb_topic,
					symptom_topic, N);
			System.out.println("BiBTM symptom NDCG@" + N + ": " + symptom_ndcg_k);

			double herb_perplexity = Evaluation.link_lda_herb_predictive_perplexity(herbs_test, symptoms_test,
					herb_topic, symptom_topic);
			System.out.println("BiBTM herb predictive perplexity : " + herb_perplexity);

			double herb_precision_k = Evaluation.link_lda_herb_precision_k(herbs_test, symptoms_test, herb_topic,
					symptom_topic, N);
			System.out.println("BiBTM herb precision@" + N + ": " + herb_precision_k);

			double herb_recall_k = Evaluation.link_lda_herb_recall_k(herbs_test, symptoms_test, herb_topic,
					symptom_topic, N);
			System.out.println("BiBTM herb recall@" + N + ": " + herb_recall_k);

			double herb_ndcg_k = Evaluation.link_lda_herb_ndcg(herbs_test, symptoms_test, herb_topic, symptom_topic, N);
			System.out.println("BiBTM herb NDCG@" + N + ": " + herb_ndcg_k);

			sb.append(herb_perplexity + "," + herb_precision_k + "," + symptom_perplexity + "," + symptom_precision_k
					+ "\n");

		}

		ReadWriteFile.writeFile("file//bibtm_" + K + "_" + N + ".csv", sb.toString());

	}

	/**
	 * 获取药对列表
	 * 
	 * @param herbs
	 * @return
	 */
	public static int[][] getHerbLinksSet(int[][] herbs) {

		List<int[]> herb_pair_list = new ArrayList<>();

		for (int p = 0; p < herbs.length; p++) {

			int[] herb_list = herbs[p];

			for (int i = 1; i < herb_list.length; i++) {

				for (int j = 0; j < i; j++) {

					int[] herb_pair = new int[2];
					herb_pair[0] = herb_list[i];
					herb_pair[1] = herb_list[j];

					herb_pair_list.add(herb_pair);

				}

			}

			if (herb_list.length == 1) {

				int[] herb_pair = new int[2];
				herb_pair[0] = herb_list[0];
				herb_pair[1] = herb_list[0];

				herb_pair_list.add(herb_pair);

			}

		}

		int[][] herb_pairs = new int[herb_pair_list.size()][2];

		for (int i = 0; i < herb_pairs.length; i++) {

			herb_pairs[i] = herb_pair_list.get(i);

		}

		return herb_pairs;
	}

	/**
	 * 获取症状对列表
	 * 
	 * @param symptoms
	 * @return
	 */
	public static int[][] getSymptomLinksSet(int[][] symptoms) {

		List<int[]> symptom_pair_list = new ArrayList<>();

		for (int p = 0; p < symptoms.length; p++) {

			int[] symptom_list = symptoms[p];

			for (int i = 1; i < symptom_list.length; i++) {

				for (int j = 0; j < i; j++) {

					int[] symptom_pair = new int[2];
					symptom_pair[0] = symptom_list[i];
					symptom_pair[1] = symptom_list[j];

					symptom_pair_list.add(symptom_pair);

				}

			}

			if (symptom_list.length == 1) {

				int[] symptom_pair = new int[2];
				symptom_pair[0] = symptom_pair[0];
				symptom_pair[1] = symptom_pair[0];

				symptom_pair_list.add(symptom_pair);

			}

		}

		int[][] symptom_pairs = new int[symptom_pair_list.size()][2];

		for (int i = 0; i < symptom_pairs.length; i++) {

			symptom_pairs[i] = symptom_pair_list.get(i);

		}

		return symptom_pairs;
	}

	/**
	 * 获取药物-症状对列表
	 * 
	 * @param herbs
	 * @param symptoms
	 * @return
	 */
	public static int[][] getHerbSymptomLinksSet(int[][] herbs, int[][] symptoms) {

		List<int[]> pair_list = new ArrayList<>();

		for (int p = 0; p < herbs.length; p++) {

			int[] herb_list = herbs[p];

			int[] symptom_list = symptoms[p];

			for (int h : herb_list) {

				for (int s : symptom_list) {

					int[] pair = new int[2];

					pair[0] = h;
					pair[1] = s;

					pair_list.add(pair);

				}

			}

		}

		int[][] pairs = new int[pair_list.size()][2];

		for (int i = 0; i < pairs.length; i++) {

			pairs[i] = pair_list.get(i);

		}

		return pairs;

	}

}

package test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import topic.LinkPLSALDA;
import util.Corpus;
import util.Evaluation;
import util.ReadWriteFile;

public class LinkPLSALDAPredict {

	public static void main(String[] args) throws IOException {

		StringBuilder sb = new StringBuilder();

		int K = 15;

		int N = 5;

		for (int i = 0; i < 10; i++) {

			List<String> herbs_list = Corpus.getVocab("data//herbs_contains.txt");

			List<String> symptoms_list = Corpus.getVocab("data//symptom_contains.txt");

			int[][] herbs_train = Corpus.getDocuments("file//pre_herbs_train.txt");

			int[][] symptoms_train = Corpus.getDocuments("file//pre_symptoms_train.txt");

			int[][] herbs_test = Corpus.getDocuments("file//pre_herbs_test.txt");

			int[][] symptoms_test = Corpus.getDocuments("file//pre_symptoms_test.txt");

			Map<String, String> symptom_herb = TopicPrecisionSymToHerb
					.getSymptomHerbKnowledge("data//symptom_herb_tcm_mesh.txt");

			int[][] herb_symptom_links = getHerbSymptomLinksSet(symptom_herb, herbs_list, symptoms_list, herbs_train,
					symptoms_train);

			System.out.println(herb_symptom_links.length);

			LinkPLSALDA linklda = new LinkPLSALDA(herbs_train, symptoms_train, herbs_list.size(), symptoms_list.size(),
					herb_symptom_links);

			double alpha = 1;
			double beta = 0.1;
			double beta_bar = 0.1;

			double alpha_t = 1;
			int iterations = 1000;

			linklda.markovChain(K, alpha, beta, beta_bar, alpha_t, iterations);

			double[][] herb_topic = linklda.estimatePhi();

			double[][] symptom_topic = linklda.estimatePhiBar();

			double symptom_perplexity = Evaluation.link_lda_symptom_predictive_perplexity(herbs_test, symptoms_test,
					herb_topic, symptom_topic);

			System.out.println("LinkPLSALDA symptom predictive perplexity : " + symptom_perplexity);

			double symptom_precision_k = Evaluation.link_lda_symptom_precision_k(herbs_test, symptoms_test, herb_topic,
					symptom_topic, N);
			System.out.println("LinkPLSALDA symptom precision@" + N + ": " + symptom_precision_k);

			double symptom_recall_k = Evaluation.link_lda_symptom_recall_k(herbs_test, symptoms_test, herb_topic,
					symptom_topic, N);
			System.out.println("LinkPLSALDA symptom recall@" + N + ": " + symptom_recall_k);

			double symptom_ndcg_k = Evaluation.link_lda_symptom_ndcg(herbs_test, symptoms_test, herb_topic,
					symptom_topic, N);
			System.out.println("LinkPLSALDA symptom NDCG@" + N + ": " + symptom_ndcg_k);

			double herb_perplexity = Evaluation.link_lda_herb_predictive_perplexity(herbs_test, symptoms_test,
					herb_topic, symptom_topic);
			System.out.println("LinkPLSALDA herb predictive perplexity : " + herb_perplexity);

			double herb_precision_k = Evaluation.link_lda_herb_precision_k(herbs_test, symptoms_test, herb_topic,
					symptom_topic, N);
			System.out.println("LinkPLSALDA herb precision@" + N + ": " + herb_precision_k);

			double herb_recall_k = Evaluation.link_lda_herb_recall_k(herbs_test, symptoms_test, herb_topic,
					symptom_topic, N);
			System.out.println("LinkPLSALDA herb recall@" + N + ": " + herb_recall_k);

			double herb_ndcg_k = Evaluation.link_lda_herb_ndcg(herbs_test, symptoms_test, herb_topic, symptom_topic, N);
			System.out.println("LinkPLSALDA herb NDCG@" + N + ": " + herb_ndcg_k);

			sb.append(herb_perplexity + "," + herb_precision_k + "," + symptom_perplexity + "," + symptom_precision_k
					+ "\n");

		}

		ReadWriteFile.writeFile("file//linkplsalda_" + K + "_" + N + ".csv", sb.toString());

	}

	/**
	 * 从训练集和领域知识中获取治疗关系集合
	 * 
	 * @param symptom_herb
	 * @param herbs_list
	 * @param symptoms_list
	 * @param herbs
	 * @param symptoms
	 * @return
	 */
	public static int[][] getHerbSymptomLinksSet(Map<String, String> symptom_herb, List<String> herbs_list,
			List<String> symptoms_list, int[][] herbs, int[][] symptoms) {

		Set<List<String>> link_set = new HashSet<>();

		for (int i = 0; i < symptoms.length; i++) {

			for (int symptom : symptoms[i]) {

				String herb_str = symptom_herb.get(symptoms_list.get(symptom));

				if (herb_str == null)
					continue;

				for (int herb : herbs[i]) {

					if (herb_str.contains(herbs_list.get(herb))) {

						List<String> pair = new ArrayList<>();

						pair.add(herbs_list.get(herb));

						pair.add(symptoms_list.get(symptom));

						link_set.add(pair);

						// System.out.println(link_set);

					}

				}

			}

		}

		List<List<String>> link_list = new ArrayList<>(link_set);

		int[][] herb_symptom_links = new int[link_list.size()][2];

		for (int i = 0; i < herb_symptom_links.length; i++) {

			herb_symptom_links[i][0] = herbs_list.indexOf(link_list.get(i).get(0));

			herb_symptom_links[i][1] = symptoms_list.indexOf(link_list.get(i).get(1));

		}

		return herb_symptom_links;

	}

	/**
	 * 从训练集和领域知识中获取治疗关系列表
	 * 
	 * @param symptom_herb
	 * @param herbs_list
	 * @param symptoms_list
	 * @param herbs
	 * @param symptoms
	 * @return
	 */
	public static int[][] getHerbSymptomLinksList(Map<String, String> symptom_herb, List<String> herbs_list,
			List<String> symptoms_list, int[][] herbs, int[][] symptoms) {

		List<List<String>> link_set = new ArrayList<>();

		for (int i = 0; i < symptoms.length; i++) {

			for (int symptom : symptoms[i]) {

				String herb_str = symptom_herb.get(symptoms_list.get(symptom));

				if (herb_str == null)
					continue;

				for (int herb : herbs[i]) {

					if (herb_str.contains(herbs_list.get(herb))) {

						List<String> pair = new ArrayList<>();

						pair.add(herbs_list.get(herb));

						pair.add(symptoms_list.get(symptom));

						link_set.add(pair);

						// System.out.println(link_set);

					}

				}

			}

		}

		List<List<String>> link_list = new ArrayList<>(link_set);

		int[][] herb_symptom_links = new int[link_list.size()][2];

		for (int i = 0; i < herb_symptom_links.length; i++) {

			herb_symptom_links[i][0] = herbs_list.indexOf(link_list.get(i).get(0));

			herb_symptom_links[i][1] = symptoms_list.indexOf(link_list.get(i).get(1));

		}

		return herb_symptom_links;

	}

}

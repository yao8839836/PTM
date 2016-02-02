package test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import topic.BlockLDAMustLink;
import util.Corpus;
import util.Evaluation;

public class BlockLDAMustLinkPredict {

	public static void main(String[] args) throws IOException {

		List<String> herbs_list = Corpus.getVocab("data//herbs_contains.txt");

		List<String> symptoms_list = Corpus.getVocab("data//symptom_contains.txt");

		int[][] herbs_train = Corpus.getDocuments("file//pre_herbs_train.txt");

		int[][] symptoms_train = Corpus.getDocuments("file//pre_symptoms_train.txt");

		int[][] herbs_test = Corpus.getDocuments("file//pre_herbs_test.txt");

		int[][] symptoms_test = Corpus.getDocuments("file//pre_symptoms_test.txt");

		int[][] links = getHerbLinksSet(herbs_train);

		System.out.println(links.length);

		BlockLDAMustLink blda = new BlockLDAMustLink(herbs_train, symptoms_train, herbs_list.size(),
				symptoms_list.size(), links);

		int K = 15;
		double alpha = 1;
		double alpha_l = 1;
		double beta = 0.1;
		double beta_bar = 0.1;
		int iterations = 1000;

		blda.markovChain(K, alpha, beta, beta_bar, alpha_l, iterations);

		double[][] herb_topic = blda.estimatePhi();

		double[][] symptom_topic = blda.estimatePhiBar();

		double symptom_perplexity = Evaluation.link_lda_symptom_predictive_perplexity(herbs_test, symptoms_test,
				herb_topic, symptom_topic);

		System.out.println("LinkLDA symptom predictive perplexity : " + symptom_perplexity);

		double herb_perplexity = Evaluation.link_lda_herb_predictive_perplexity(herbs_test, symptoms_test, herb_topic,
				symptom_topic);

		System.out.println("LinkLDA herb predictive perplexity : " + herb_perplexity);
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

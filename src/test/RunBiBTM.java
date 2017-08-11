package test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import topic.BiBTM;
import util.Common;
import util.Corpus;
import util.ReadWriteFile;

public class RunBiBTM {

	public static void main(String[] args) throws IOException {

		StringBuilder sb_str = new StringBuilder();

		int K = 25;

		for (int iter = 0; iter < 10; iter++) {

			List<String> herbs_list = Corpus.getVocab("data//herbs_contains.txt");

			List<String> symptoms_list = Corpus.getVocab("data//symptom_contains.txt");

			int[][] herbs = Corpus.getDocuments("file//pre_herbs.txt");

			int[][] symptoms = Corpus.getDocuments("file//pre_symptoms.txt");

			int[][] herb_pairs = getHerbLinksSet(herbs);

			System.out.println("药对共计" + herb_pairs.length + "对");

			int[][] symptom_pairs = getHerbLinksSet(symptoms);

			System.out.println("症状对共计" + symptom_pairs.length + "对");

			int[][] pairs = getHerbSymptomLinksSet(herbs, symptoms);

			System.out.println("药物-症状对共计" + pairs.length + "对");

			int H = herbs_list.size();
			int S = symptoms_list.size();

			BiBTM bibtm = new BiBTM(herb_pairs, symptom_pairs, pairs, H, S);

			double alpha = 1;
			double beta = 0.1;

			int iterations = 1000;

			bibtm.markovChain(K, alpha, beta, iterations);

			double[][] phi = bibtm.estimatePhi();

			double[][] phi_bar = bibtm.estimatePhiBar();

			double[][] phi_for_write = Common.makeCopy(phi);

			double[][] phi_bar_for_write = Common.makeCopy(phi_bar);

			StringBuilder sb = new StringBuilder();

			for (int k = 0; k < phi.length; k++) {

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

			String filename = "result//topic_bibtm_" + K + ".txt";
			ReadWriteFile.writeFile(filename, sb.toString());

			args = new String[1];

			args[0] = filename;

			String precision = TopicPrecisionSymToHerb.main(args);

			String symptom_coherence = TopicKnowCoherence.main(args);

			sb_str.append(precision + "," + symptom_coherence + "\n");

		}

		ReadWriteFile.writeFile("file///bibtm_" + K + "_topic.csv", sb_str.toString());

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

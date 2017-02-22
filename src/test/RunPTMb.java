package test;

import java.io.IOException;
import java.util.List;

import topic.PTMb;
import util.Common;
import util.Corpus;
import util.ReadWriteFile;

public class RunPTMb {

	public static void main(String[] args) throws IOException {

		StringBuilder sb_str = new StringBuilder();

		int K = 15;

		for (int iter = 0; iter < 10; iter++) {

			List<String> herbs_list = Corpus.getVocab("data//herbs_contains.txt");

			List<String> symptoms_list = Corpus.getVocab("data//symptom_contains.txt");

			int[][] herbs = Corpus.getDocuments("file//pre_herbs.txt");

			int[][] symptoms = Corpus.getDocuments("file//pre_symptoms.txt");

			PTMb ptm = new PTMb(herbs, symptoms, herbs_list.size(), symptoms_list.size());

			double alpha = 1;
			double beta = 0.1;
			double beta_bar = 0.1;
			double eta = 1;
			int iterations = 1000;

			ptm.markovChain(K, alpha, beta, beta_bar, eta, iterations);

			double[][][] herb_topic = ptm.estimatePhi();

			double[][] symptom_topic = ptm.estimatePhiBar();

			double[][][] prescription_topic_role = ptm.estimatePi();

			double[][] prescription_topic = ptm.estimateTheta();

			double[][] phi_bar_for_write = Common.makeCopy(symptom_topic);

			StringBuilder sb = new StringBuilder();

			double[] sum_2 = new double[symptom_topic.length];

			for (int p_1 = 0; p_1 < prescription_topic.length; p_1++) {

				for (int k = 0; k < herb_topic.length; k++)
					sum_2[k] += prescription_topic[p_1][k];
			}

			for (int k = 0; k < K; k++) {

				double[] topic_herb_prob = new double[herb_topic[0][0].length];

				for (int p = 0; p < herbs.length; p++) {

					for (int x = 0; x < herb_topic[0].length; x++) {

						for (int h = 0; h < herb_topic[0][0].length; h++) {
							topic_herb_prob[h] += prescription_topic[p][k] / sum_2[k] * prescription_topic_role[p][k][x]
									* herb_topic[k][x][h];
						}

					}

				}

				StringBuilder herb_str = new StringBuilder();

				for (int i = 0; i < 10; i++) {

					int max_index = Common.maxIndex(topic_herb_prob);

					herb_str.append(herbs_list.get(max_index) + " ");

					topic_herb_prob[max_index] = 0;

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

			String filename = "result//topic_ptm_b_" + K + ".txt";
			ReadWriteFile.writeFile(filename, sb.toString());

			args = new String[1];

			args[0] = filename;

			String precision = TopicPrecisionSymToHerb.main(args);

			String symptom_coherence = TopicKnowCoherence.main(args);

			sb_str.append(precision + "," + symptom_coherence + "\n");

			/*
			 * 显示治法、君臣佐使下药的情况
			 */

			sb = new StringBuilder();

			phi_bar_for_write = Common.makeCopy(symptom_topic);

			for (int k = 0; k < K; k++) {

				StringBuilder role_str = new StringBuilder();

				for (int x = 0; x < herb_topic[0].length; x++) {

					role_str.append("角色" + x + " ： ");

					double[] topic_role_herb = new double[herb_topic[0][0].length];

					for (int h = 0; h < topic_role_herb.length; h++) {
						topic_role_herb[h] = herb_topic[k][x][h];
					}

					StringBuilder herb_str = new StringBuilder();

					for (int i = 0; i < 10; i++) {

						int max_index = Common.maxIndex(topic_role_herb);

						herb_str.append(herbs_list.get(max_index) + " ");

						topic_role_herb[max_index] = 0;

					}
					role_str.append(herb_str.toString().trim() + "  ");

				}

				double[] phi_bar_k = phi_bar_for_write[k];

				StringBuilder symptom_str = new StringBuilder();

				for (int i = 0; i < 10; i++) {

					int max_index = Common.maxIndex(phi_bar_k);

					symptom_str.append(symptoms_list.get(max_index) + " ");

					phi_bar_k[max_index] = 0;

				}

				sb.append(role_str.toString().trim() + "\t" + symptom_str.toString().trim() + "\n");

			}

			filename = "result//topic_ptm_b_" + K + "_role.txt";
			ReadWriteFile.writeFile(filename, sb.toString());

		}

		ReadWriteFile.writeFile("file///ptm_b_" + K + "_topic.csv", sb_str.toString());

	}

}

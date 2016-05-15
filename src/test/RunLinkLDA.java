package test;

import java.io.IOException;
import java.util.List;

import topic.LinkLDA;
import util.Common;
import util.Corpus;
import util.ReadWriteFile;

public class RunLinkLDA {

	public static void main(String[] args) throws IOException {

		List<String> herbs_list = Corpus.getVocab("data//herbs_contains.txt");

		List<String> symptoms_list = Corpus.getVocab("data//symptom_contains.txt");

		int[][] herbs = Corpus.getDocuments("file//pre_herbs.txt");

		int[][] symptoms = Corpus.getDocuments("file//pre_symptoms.txt");

		LinkLDA linklda = new LinkLDA(herbs, symptoms, herbs_list.size(), symptoms_list.size());

		int K = 25;
		double alpha = 1;
		double beta = 0.1;
		double beta_bar = 0.1;
		int iterations = 1000;

		linklda.markovChain(K, alpha, beta, beta_bar, iterations);

		double[][] phi = linklda.estimatePhi();

		double[][] phi_bar = linklda.estimatePhiBar();

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

		String filename = "result//topic_link_lda_25.txt";
		ReadWriteFile.writeFile(filename, sb.toString());
	}

}

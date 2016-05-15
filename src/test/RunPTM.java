package test;

import java.io.IOException;
import java.util.List;

import topic.PTM;
import util.Common;
import util.Corpus;
import util.ReadWriteFile;

public class RunPTM {

	public static void main(String[] args) throws IOException {

		List<String> herbs_list = Corpus.getVocab("data//herbs_contains.txt");

		List<String> symptoms_list = Corpus.getVocab("data//symptom_contains.txt");

		int[][] herbs = Corpus.getDocuments("file//pre_herbs.txt");

		int[][] symptoms = Corpus.getDocuments("file//pre_symptoms.txt");

		PTM linklda = new PTM(herbs, symptoms, herbs_list.size(), symptoms_list.size());

		int K = 25;
		double alpha = 1;
		double beta = 0.1;
		double beta_bar = 0.1;

		double eta = 0.1;
		int iterations = 1000;

		linklda.markovChain(K, alpha, beta, beta_bar, eta, iterations);

		// int[][] role = linklda.x;
		// for (int[] x_p : role) {
		// for (int x_pn : x_p) {
		// System.out.print(x_pn + " ");
		// }
		// System.out.println();
		// }
		// int[][] treat = linklda.treatment;
		//
		// for (int[] z_p : treat) {
		// for (int x_pn : z_p) {
		// System.out.print(x_pn + " ");
		// }
		// System.out.println();
		// }

		int[][] syndrome = linklda.syndrome;

		for (int[] z_p : syndrome) {
			for (int x_pn : z_p) {
				System.out.print(x_pn + " ");
			}
			System.out.println();
		}

		double[][] phi_bar = linklda.estimatePhiBar();

		double[][] phi_bar_for_write = Common.makeCopy(phi_bar);

		double[][] psi = linklda.estimatePsi();

		double[][][] phi = linklda.estimatePhi();

		StringBuilder sb = new StringBuilder();

		for (int k = 0; k < phi_bar.length; k++) {

			double[] topic_herb = new double[phi[0][0].length];

			for (int x = 0; x < phi[0].length; x++) {
				for (int h = 0; h < topic_herb.length; h++) {
					topic_herb[h] += phi[k][x][h] * psi[k][x];
				}

			}
			StringBuilder herb_str = new StringBuilder();

			for (int i = 0; i < 10; i++) {

				int max_index = Common.maxIndex(topic_herb);

				herb_str.append(herbs_list.get(max_index) + " ");

				topic_herb[max_index] = 0;

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

		String filename = "result//topic_ptm_3d(a)_25.txt";
		ReadWriteFile.writeFile(filename, sb.toString());
	}

}

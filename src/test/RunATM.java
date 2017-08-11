package test;

import java.io.IOException;
import java.util.List;

import topic.ATM;
import util.Common;
import util.Corpus;
import util.ReadWriteFile;

public class RunATM {

	public static void main(String[] args) throws IOException {

		List<String> herbs_list = Corpus.getVocab("data//herbs_contains.txt");

		List<String> symptoms_list = Corpus.getVocab("data//symptom_contains.txt");

		int[][] herbs = Corpus.getDocuments("file//pre_herbs.txt");

		int[][] symptoms = Corpus.getDocuments("file//pre_symptoms.txt");

		ATM atm = new ATM(symptoms, herbs, symptoms_list.size(), herbs_list.size());

		int K = 25;
		double alpha = (double) 50 / K;
		double beta = 0.01;

		int iterations = 1000;

		atm.markovChain(K, alpha, beta, iterations);

		double[][] phi = atm.estimatePhi();

		double[][] theta = atm.estimateTheta();

		double[][] phi_for_write = Common.makeCopy(phi);

		StringBuilder sb = new StringBuilder();

		for (int k = 0; k < phi.length; k++) {

			StringBuilder symptom_str = new StringBuilder();

			double[] phi_k = phi_for_write[k];

			for (int i = 0; i < 10; i++) {

				int max_index = Common.maxIndex(phi_k);

				symptom_str.append(symptoms_list.get(max_index) + " ");

				phi_k[max_index] = 0;

			}

			double[] topic_herb = new double[theta.length];

			System.out.println(topic_herb.length);

			for (int h = 0; h < topic_herb.length; h++) {
				topic_herb[h] = theta[h][k];
			}

			StringBuilder herb_str = new StringBuilder();

			for (int i = 0; i < 10; i++) {

				int max_index = Common.maxIndex(topic_herb);

				herb_str.append(herbs_list.get(max_index) + " ");

				topic_herb[max_index] = 0;

			}
			sb.append(herb_str.toString().trim() + "\t" + symptom_str.toString().trim() + "\n");

		}

		String filename = "result//topic_atm_25.txt";
		ReadWriteFile.writeFile(filename, sb.toString());

	}
}

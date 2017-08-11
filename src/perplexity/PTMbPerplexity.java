package perplexity;

import java.io.IOException;
import java.util.List;

import topic.PTMb;
import util.Common;
import util.Corpus;
import util.EvaluationPTM;
import util.ReadWriteFile;

public class PTMbPerplexity {

	public static void main(String[] args) throws IOException, ClassNotFoundException {

		StringBuilder sb = new StringBuilder();

		int K = 20;

		for (int i = 0; i < 10; i++) {

			List<String> herbs_list = Corpus.getVocab("data//herbs_contains.txt");

			List<String> symptoms_list = Corpus.getVocab("data//symptom_contains.txt");

			int[][] herbs_train_new = (int[][]) Common.readObject("data//herbs_train_new.obj");

			int[][] symptoms_train_new = (int[][]) Common.readObject("data//symptoms_train_new.obj");

			int[][] herbs_held_out = (int[][]) Common.readObject("data//herbs_held_out.obj");

			int[][] symptoms_held_out = (int[][]) Common.readObject("data//symptoms_held_out.obj");

			PTMb ptm = new PTMb(herbs_train_new, symptoms_train_new, herbs_list.size(), symptoms_list.size());

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

			double[][] held_out_prescription_topic = new double[symptoms_held_out.length][];

			for (int t = 0; t < held_out_prescription_topic.length; t++) {

				int begin_index = prescription_topic.length - symptoms_held_out.length;

				held_out_prescription_topic[t] = prescription_topic[begin_index + t];

			}

			double[][][] held_out_prescription_topic_role = new double[symptoms_held_out.length][][];

			for (int t = 0; t < held_out_prescription_topic_role.length; t++) {

				int begin_index = prescription_topic_role.length - symptoms_held_out.length;

				held_out_prescription_topic_role[t] = prescription_topic_role[begin_index + t];

			}

			double perplexity = EvaluationPTM.ptm_training_perplexity(herbs_held_out, symptoms_held_out, herb_topic,
					symptom_topic, held_out_prescription_topic, held_out_prescription_topic_role);

			System.out.println("PTM(b) perplexity : " + perplexity);

			System.out.println(perplexity);

			sb.append(perplexity + "\n");

		}

		ReadWriteFile.writeFile("file//PTMb_Perplexity_" + K + ".csv", sb.toString());

	}

}

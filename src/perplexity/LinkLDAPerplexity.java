package perplexity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import topic.LinkLDA;
import util.Common;
import util.Corpus;
import util.Evaluation;
import util.ReadWriteFile;

public class LinkLDAPerplexity {

	/**
	 * LinkLDA perplexity document completion
	 * 
	 * @param args
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException {

		StringBuilder sb = new StringBuilder();

		int K = 20;

		for (int i = 0; i < 10; i++) {

			List<String> herbs_list = Corpus.getVocab("data//herbs_contains.txt");

			List<String> symptoms_list = Corpus.getVocab("data//symptom_contains.txt");

			int[][] herbs_train = Corpus.getDocuments("file//pre_herbs_train.txt");

			int[][] symptoms_train = Corpus.getDocuments("file//pre_symptoms_train.txt");

			int[][] herbs_test = Corpus.getDocuments("file//pre_herbs_test.txt");

			int[][] symptoms_test = Corpus.getDocuments("file//pre_symptoms_test.txt");

			int[][] herbs_train_new = new int[herbs_train.length + herbs_test.length][];

			int[][] symptoms_train_new = new int[symptoms_train.length + symptoms_test.length][];

			int[][] herbs_held_out = new int[herbs_test.length][];

			int[][] symptoms_held_out = new int[symptoms_test.length][];

			for (int p = 0; p < herbs_train_new.length; p++) {

				if (p < herbs_train.length) {

					herbs_train_new[p] = herbs_train[p];

					symptoms_train_new[p] = symptoms_train[p];

				} else {

					// 药物拿出一般训练，一半测试
					List<Integer> herb_first_half = new ArrayList<>();

					List<Integer> herb_second_half = new ArrayList<>();

					for (int h = 0; h < herbs_test[p - herbs_train.length].length; h++) {

						double random = Math.random();

						if (random < 0.5) {

							herb_first_half.add(herbs_test[p - herbs_train.length][h]);

						} else {

							herb_second_half.add(herbs_test[p - herbs_train.length][h]);
						}

					}

					herbs_train_new[p] = new int[herb_first_half.size()];

					for (int h = 0; h < herbs_train_new[p].length; h++) {

						herbs_train_new[p][h] = herb_first_half.get(h);

					}

					herbs_held_out[p - herbs_train.length] = new int[herb_second_half.size()];

					for (int h = 0; h < herbs_held_out[p - herbs_train.length].length; h++) {

						herbs_held_out[p - herbs_train.length][h] = herb_second_half.get(h);

					}

					// 症状拿出一般训练，一半测试

					List<Integer> symptom_first_half = new ArrayList<>();

					List<Integer> symptom_second_half = new ArrayList<>();

					for (int s = 0; s < symptoms_test[p - symptoms_train.length].length; s++) {

						double random = Math.random();

						if (random < 0.5) {

							symptom_first_half.add(symptoms_test[p - symptoms_train.length][s]);

						} else {

							symptom_second_half.add(symptoms_test[p - symptoms_train.length][s]);
						}

					}

					symptoms_train_new[p] = new int[symptom_first_half.size()];

					for (int s = 0; s < symptoms_train_new[p].length; s++) {

						symptoms_train_new[p][s] = symptom_first_half.get(s);

					}

					symptoms_held_out[p - symptoms_train.length] = new int[symptom_second_half.size()];

					for (int s = 0; s < symptoms_held_out[p - symptoms_train.length].length; s++) {

						symptoms_held_out[p - symptoms_train.length][s] = symptom_second_half.get(s);

					}

				}

			}

			// Common.writeObject(herbs_train_new, "data//herbs_train_new.obj");
			// Common.writeObject(symptoms_train_new,
			// "data//symptoms_train_new.obj");
			// Common.writeObject(herbs_held_out, "data//herbs_held_out.obj");
			// Common.writeObject(symptoms_held_out,
			// "data//symptoms_held_out.obj");

			herbs_train_new = (int[][]) Common.readObject("data//herbs_train_new.obj");

			symptoms_train_new = (int[][]) Common.readObject("data//symptoms_train_new.obj");

			herbs_held_out = (int[][]) Common.readObject("data//herbs_held_out.obj");

			symptoms_held_out = (int[][]) Common.readObject("data//symptoms_held_out.obj");

			LinkLDA linklda = new LinkLDA(herbs_train_new, symptoms_train_new, herbs_list.size(), symptoms_list.size());

			double alpha = 1;
			double beta = 0.1;
			double beta_bar = 0.1;
			int iterations = 1000;

			linklda.markovChain(K, alpha, beta, beta_bar, iterations);

			double[][] herb_topic = linklda.estimatePhi();

			double[][] symptom_topic = linklda.estimatePhiBar();

			double[][] prescription_topic = linklda.estimateTheta();

			double[][] held_out_prescription_topic = new double[symptoms_held_out.length][];

			for (int t = 0; t < held_out_prescription_topic.length; t++) {

				held_out_prescription_topic[t] = prescription_topic[herbs_train.length + t];

			}

			double perplexity = Evaluation.link_lda_training_perplexity(herbs_held_out, symptoms_held_out, herb_topic,
					symptom_topic, held_out_prescription_topic);

			System.out.println(perplexity);

			sb.append(perplexity + "\n");

		}

		ReadWriteFile.writeFile("file//LinkLDA_Perplexity_" + K + ".csv", sb.toString());

	}

}

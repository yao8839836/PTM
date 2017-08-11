package perplexity;

import java.io.IOException;
import java.util.List;

import test.BlockLDAPredict;
import topic.BlockLDA;
import util.Common;
import util.Corpus;
import util.Evaluation;
import util.ReadWriteFile;

public class BlockLDAPerplexity {

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

			int[][] links = BlockLDAPredict.getHerbLinksSet(herbs_train_new);

			System.out.println(links.length);

			BlockLDA blda = new BlockLDA(herbs_train_new, symptoms_train_new, herbs_list.size(), symptoms_list.size(),
					links);

			double alpha = 1;
			double alpha_l = 1;
			double beta = 0.1;
			double beta_bar = 0.1;
			int iterations = 1000;

			blda.markovChain(K, alpha, beta, beta_bar, alpha_l, iterations);

			double[][] herb_topic = blda.estimatePhi();

			double[][] symptom_topic = blda.estimatePhiBar();

			double[][] prescription_topic = blda.estimateTheta();

			double[][] held_out_prescription_topic = new double[symptoms_held_out.length][];

			for (int t = 0; t < held_out_prescription_topic.length; t++) {

				int begin_index = prescription_topic.length - symptoms_held_out.length;

				held_out_prescription_topic[t] = prescription_topic[begin_index + t];

			}

			double perplexity = Evaluation.link_lda_training_perplexity(herbs_held_out, symptoms_held_out, herb_topic,
					symptom_topic, held_out_prescription_topic);

			System.out.println(perplexity);

			sb.append(perplexity + "\n");

		}

		ReadWriteFile.writeFile("file//BlockLDA_Perplexity_" + K + ".csv", sb.toString());

	}

}

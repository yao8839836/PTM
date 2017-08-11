package perplexity;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import test.LinkPLSALDAPredict;
import test.TopicPrecisionSymToHerb;
import topic.LinkPLSALDA;
import util.Common;
import util.Corpus;
import util.Evaluation;
import util.ReadWriteFile;

public class LinkPLSALDAPerplexity {

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

			Map<String, String> symptom_herb = TopicPrecisionSymToHerb
					.getSymptomHerbKnowledge("data//symptom_herb_tcm_mesh.txt");

			int[][] herb_symptom_links = LinkPLSALDAPredict.getHerbSymptomLinksSet(symptom_herb, herbs_list,
					symptoms_list, herbs_train_new, symptoms_train_new);

			System.out.println(herb_symptom_links.length);

			LinkPLSALDA linklda = new LinkPLSALDA(herbs_train_new, symptoms_train_new, herbs_list.size(),
					symptoms_list.size(), herb_symptom_links);

			double alpha = 1;
			double beta = 0.1;
			double beta_bar = 0.1;

			double alpha_t = 1;
			int iterations = 1000;

			linklda.markovChain(K, alpha, beta, beta_bar, alpha_t, iterations);

			linklda.markovChain(K, alpha, beta, beta_bar, alpha_t, iterations);

			double[][] herb_topic = linklda.estimatePhi();

			double[][] symptom_topic = linklda.estimatePhiBar();

			double[][] prescription_topic = linklda.estimateTheta();

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

		ReadWriteFile.writeFile("file//LinkPLSALDA_Perplexity_" + K + ".csv", sb.toString());

	}

}

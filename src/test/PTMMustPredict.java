package test;

import java.io.IOException;
import java.util.List;

import topic.PTMMustLink;
import util.Corpus;
import util.Evaluation;

public class PTMMustPredict {

	public static void main(String[] args) throws IOException {

		List<String> herbs_list = Corpus.getVocab("data//herbs_contains.txt");

		List<String> symptoms_list = Corpus.getVocab("data//symptom_contains.txt");

		int[][] herbs_train = Corpus.getDocuments("file//pre_herbs_train.txt");

		int[][] symptoms_train = Corpus.getDocuments("file//pre_symptoms_train.txt");

		int[][] herbs_test = Corpus.getDocuments("file//pre_herbs_test.txt");

		int[][] symptoms_test = Corpus.getDocuments("file//pre_symptoms_test.txt");

		int[][] links = BlockLDAMustLinkPredict.getHerbLinksSet(herbs_train);

		System.out.println(links.length);

		PTMMustLink blda = new PTMMustLink(herbs_train, symptoms_train, herbs_list.size(), symptoms_list.size(), links);

		int K = 15;
		double alpha = 1;
		double alpha_l = 1;
		double beta = 0.1;
		double eta = 0.1;
		double beta_bar = 0.1;
		int iterations = 1000;

		blda.markovChain(K, alpha, beta, beta_bar, eta, alpha_l, iterations);

		double[][][] herb_topic = blda.estimatePhi();

		double[][] symptom_topic = blda.estimatePhiBar();

		double[][] topic_role = blda.estimatePsi();

		double symptom_perplexity = Evaluation.ptm_symptom_predictive_perplexity(herbs_test, symptoms_test, herb_topic,
				symptom_topic);

		System.out.println("PTM(b) symptom predictive perplexity : " + symptom_perplexity);

		double herb_perplexity = Evaluation.ptm_herb_predictive_perplexity(herbs_test, symptoms_test, herb_topic,
				symptom_topic, topic_role);

		System.out.println("PTM(b) herb predictive perplexity : " + herb_perplexity);
	}

}

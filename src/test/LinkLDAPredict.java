package test;

import java.io.IOException;
import java.util.List;

import topic.LinkLDA;
import util.Corpus;
import util.Evaluation;

public class LinkLDAPredict {

	public static void main(String[] args) throws IOException {

		List<String> herbs_list = Corpus.getVocab("data//herbs_contains.txt");

		List<String> symptoms_list = Corpus.getVocab("data//symptom_contains.txt");

		int[][] herbs_train = Corpus.getDocuments("file//pre_herbs_train.txt");

		int[][] symptoms_train = Corpus.getDocuments("file//pre_symptoms_train.txt");

		int[][] herbs_test = Corpus.getDocuments("file//pre_herbs_test.txt");

		int[][] symptoms_test = Corpus.getDocuments("file//pre_symptoms_test.txt");

		LinkLDA linklda = new LinkLDA(herbs_train, symptoms_train, herbs_list.size(), symptoms_list.size());

		int K = 5;
		double alpha = 1;
		double beta = 0.1;
		double beta_bar = 0.1;
		int iterations = 1000;

		linklda.markovChain(K, alpha, beta, beta_bar, iterations);

		double[][] herb_topic = linklda.estimatePhi();

		double[][] symptom_topic = linklda.estimatePhiBar();

		double symptom_perplexity = Evaluation.link_lda_symptom_predictive_perplexity(herbs_test, symptoms_test,
				herb_topic, symptom_topic);

		System.out.println("LinkLDA symptom predictive perplexity : " + symptom_perplexity);

		double herb_perplexity = Evaluation.link_lda_herb_predictive_perplexity(herbs_test, symptoms_test, herb_topic,
				symptom_topic);

		System.out.println("LinkLDA herb predictive perplexity : " + herb_perplexity);
	}

}

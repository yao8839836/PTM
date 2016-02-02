package test;

import java.io.IOException;
import java.util.List;

import topic.PTM;
import util.Corpus;
import util.Evaluation;

public class PTMPredict {

	public static void main(String[] args) throws IOException {

		List<String> herbs_list = Corpus.getVocab("data//herbs_contains.txt");

		List<String> symptoms_list = Corpus.getVocab("data//symptom_contains.txt");

		int[][] herbs_train = Corpus.getDocuments("file//pre_herbs_train.txt");

		int[][] symptoms_train = Corpus.getDocuments("file//pre_symptoms_train.txt");

		int[][] herbs_test = Corpus.getDocuments("file//pre_herbs_test.txt");

		int[][] symptoms_test = Corpus.getDocuments("file//pre_symptoms_test.txt");

		PTM ptm = new PTM(herbs_train, symptoms_train, herbs_list.size(), symptoms_list.size());

		int K = 5;
		double alpha = 1;
		double beta = 0.1;
		double beta_bar = 0.1;
		double eta = 0.1;
		int iterations = 1000;

		ptm.markovChain(K, alpha, beta, beta_bar, eta, iterations);

		double[][][] herb_topic = ptm.estimatePhi();

		double[][] symptom_topic = ptm.estimatePhiBar();

		double[][] topic_role = ptm.estimatePsi();

		double symptom_perplexity = Evaluation.ptm_symptom_predictive_perplexity(herbs_test, symptoms_test, herb_topic,
				symptom_topic);

		System.out.println("PTM(a) symptom predictive perplexity : " + symptom_perplexity);

		double herb_perplexity = Evaluation.ptm_herb_predictive_perplexity(herbs_test, symptoms_test, herb_topic,
				symptom_topic, topic_role);

		System.out.println("PTM(a) herb predictive perplexity : " + herb_perplexity);

	}

}

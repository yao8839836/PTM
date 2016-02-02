package test;

import java.io.IOException;
import java.util.List;

import topic.ATM;
import util.Corpus;
import util.Evaluation;

public class ATMPredict {

	public static void main(String[] args) throws IOException {

		List<String> herbs_list = Corpus.getVocab("data//herbs_contains.txt");

		List<String> symptoms_list = Corpus.getVocab("data//symptom_contains.txt");

		int[][] herbs_train = Corpus.getDocuments("file//pre_herbs_train.txt");

		int[][] symptoms_train = Corpus.getDocuments("file//pre_symptoms_train.txt");

		int[][] herbs_test = Corpus.getDocuments("file//pre_herbs_test.txt");

		int[][] symptoms_test = Corpus.getDocuments("file//pre_symptoms_test.txt");

		ATM atm = new ATM(symptoms_train, herbs_train, symptoms_list.size(), herbs_list.size());

		int K = 15;
		double alpha = (double) 50 / K;
		double beta = 0.01;

		int iterations = 1000;

		atm.markovChain(K, alpha, beta, iterations);

		double[][] phi = atm.estimatePhi();

		double[][] theta = atm.estimateTheta();

		double symptom_perplexity = Evaluation.atm_symptom_predictive_perplexity(herbs_test, symptoms_test, theta, phi);

		System.out.println("ATM symptom predictive perplexity : " + symptom_perplexity);

		double herb_perplexity = Evaluation.atm_herb_predictive_perplexity(herbs_test, symptoms_test, theta, phi);

		System.out.println("ATM herb predictive perplexity : " + herb_perplexity);

	}

}
